package com.thesunnahrevival.sunnahassistant.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thesunnahrevival.sunnahassistant.BuildConfig;
import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;

public class AboutAppFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about_app, container, false);
        ((TextView) view.findViewById(R.id.version)).setText(String.format(getString(R.string.version), BuildConfig.VERSION_NAME));
        TextView apiCredit = view.findViewById(R.id.api_credit);
        apiCredit.setText(Html.fromHtml(getString(R.string.api_credit)));
        apiCredit.setMovementMethod(LinkMovementMethod.getInstance());
        TextView appIconCredit = view.findViewById(R.id.app_icon_credit);
        appIconCredit.setText(Html.fromHtml(getString(R.string.app_icon_credit)));
        appIconCredit.setMovementMethod(LinkMovementMethod.getInstance());
        TextView otherIconCredit = view.findViewById(R.id.other_icon_credit);
        otherIconCredit.setText(Html.fromHtml(getString(R.string.other_icon_credit)));
        otherIconCredit.setMovementMethod(LinkMovementMethod.getInstance());
        view.findViewById(R.id.twitter).setOnClickListener(this);
        view.findViewById(R.id.instagram).setOnClickListener(this);
        view.findViewById(R.id.telegram).setOnClickListener(this);
        view.findViewById(R.id.facebook).setOnClickListener(this);
        view.findViewById(R.id.contact_us).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.twitter:
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.twitter.com/thesunahrevival"));
                break;
            case R.id.facebook:
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.facebook.com/thesunnahrevival"));
                break;
            case R.id.instagram:
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.instagram.com/thesunnahrevival"));
                break;
            case R.id.telegram:
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://t.me/thesunnahrevival"));
                break;
            case R.id.contact_us:
                intent = SunnahAssistantUtil.generateEmailIntent();
        }
        if (intent != null)
            startActivity(intent);
    }
}
