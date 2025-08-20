package com.thesunnahrevival.sunnahassistant.data.model

enum class TrainingSection(val key: String) {
    QURAN_RESOURCE_SECTION("quran_section"),
    ADHKAAR_RESOURCE_SECTION("adhkaar_section")
}

enum class TrainingStep(val section: TrainingSection, val index: Int, val messageResId: Int) {
    QURAN_TAP_TO_READ(TrainingSection.QURAN_RESOURCE_SECTION, 0, com.thesunnahrevival.sunnahassistant.R.string.training_tap_to_read_quran),
    QURAN_DOUBLE_TAP_TO_PIN(TrainingSection.QURAN_RESOURCE_SECTION, 1, com.thesunnahrevival.sunnahassistant.R.string.training_double_tap_to_pin_surah),
    ADHKAAR_TAP_TO_READ(TrainingSection.ADHKAAR_RESOURCE_SECTION, 0, com.thesunnahrevival.sunnahassistant.R.string.training_tap_to_read_adhkaar),
    ADHKAAR_DOUBLE_TAP_TO_PIN(TrainingSection.ADHKAAR_RESOURCE_SECTION, 1, com.thesunnahrevival.sunnahassistant.R.string.training_double_tap_to_pin_adhkaar)
}