package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentFontSettingsBinding
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment

class FontSettingsFragment : SunnahAssistantFragment() {

    private lateinit var binding: FragmentFontSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_font_settings, container, false
        )

        setupSeekBars()
        setupResetButton()
        observeSettings()

        return binding.root
    }

    private fun setupSeekBars() {
        // Arabic text font size SeekBar (10-40sp range)
        binding.arabicTextSizeSeekBar.max = 30 // 10-40sp range
        binding.arabicTextSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontSize = progress + 10
                binding.arabicTextSizeValue.text = fontSize.toString()
                binding.previewArabicText.textSize = fontSize.toFloat()
                if (fromUser) {
                    mainActivityViewModel.settingsValue?.arabicTextFontSize = fontSize
                    mainActivityViewModel.settingsValue?.let { mainActivityViewModel.updateSettings(it) }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Translation text font size SeekBar (10-30sp range)
        binding.translationTextSizeSeekBar.max = 20 // 10-30sp range
        binding.translationTextSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontSize = progress + 10
                binding.translationTextSizeValue.text = fontSize.toString()
                binding.previewTranslationText.textSize = fontSize.toFloat()
                if (fromUser) {
                    mainActivityViewModel.settingsValue?.translationTextFontSize = fontSize
                    mainActivityViewModel.settingsValue?.let { mainActivityViewModel.updateSettings(it) }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Footnote text font size SeekBar (8-24sp range)
        binding.footnoteTextSizeSeekBar.max = 16 // 8-24sp range
        binding.footnoteTextSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontSize = progress + 8
                binding.footnoteTextSizeValue.text = fontSize.toString()
                binding.previewFootnoteText.textSize = fontSize.toFloat()
                if (fromUser) {
                    mainActivityViewModel.settingsValue?.footnoteTextFontSize = fontSize
                    mainActivityViewModel.settingsValue?.let { mainActivityViewModel.updateSettings(it) }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupResetButton() {
        binding.resetToDefaultsButton.setOnClickListener {
            resetToDefaults()
        }
    }

    private fun resetToDefaults() {
        val defaultArabicSize = 18
        val defaultTranslationSize = 16
        val defaultFootnoteSize = 12

        mainActivityViewModel.settingsValue?.let { settings ->
            settings.arabicTextFontSize = defaultArabicSize
            settings.translationTextFontSize = defaultTranslationSize
            settings.footnoteTextFontSize = defaultFootnoteSize
            mainActivityViewModel.updateSettings(settings)
        }

        binding.arabicTextSizeSeekBar.progress = defaultArabicSize - 10
        binding.arabicTextSizeValue.text = defaultArabicSize.toString()
        binding.previewArabicText.textSize = defaultArabicSize.toFloat()

        binding.translationTextSizeSeekBar.progress = defaultTranslationSize - 10
        binding.translationTextSizeValue.text = defaultTranslationSize.toString()
        binding.previewTranslationText.textSize = defaultTranslationSize.toFloat()

        binding.footnoteTextSizeSeekBar.progress = defaultFootnoteSize - 8
        binding.footnoteTextSizeValue.text = defaultFootnoteSize.toString()
        binding.previewFootnoteText.textSize = defaultFootnoteSize.toFloat()
    }

    private fun observeSettings() {
        mainActivityViewModel.getSettings().observe(viewLifecycleOwner) { settings ->
            if(settings == null) {
                return@observe
            }
            mainActivityViewModel.settingsValue = settings
            
            // Update SeekBars and display values based on current settings
            binding.arabicTextSizeSeekBar.progress = settings.arabicTextFontSize - 10
            binding.arabicTextSizeValue.text = settings.arabicTextFontSize.toString()
            binding.previewArabicText.textSize = settings.arabicTextFontSize.toFloat()
            
            binding.translationTextSizeSeekBar.progress = settings.translationTextFontSize - 10
            binding.translationTextSizeValue.text = settings.translationTextFontSize.toString()
            binding.previewTranslationText.textSize = settings.translationTextFontSize.toFloat()
            
            binding.footnoteTextSizeSeekBar.progress = settings.footnoteTextFontSize - 8
            binding.footnoteTextSizeValue.text = settings.footnoteTextFontSize.toString()
            binding.previewFootnoteText.textSize = settings.footnoteTextFontSize.toFloat()
        }
    }
}