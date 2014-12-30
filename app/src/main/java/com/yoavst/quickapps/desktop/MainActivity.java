package com.yoavst.quickapps.desktop;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.R;
import com.yoavst.quickapps.launcher.CLauncherActivity;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.MaterialSection;

/**
 * Created by Yoav.
 */
public class MainActivity extends MaterialNavigationDrawer {
	int primaryColor;
	int primaryColorDark;
	TextView modeText;
	Pair<String, String> modes;
	Preferences_ prefs;
	boolean isSettingsEnabled;

	@Override
	public int defaultItem() {
		return 1;
	}

	@Override
	public void onClick(MaterialSection section) {
		super.onClick(section);
		findViewById(R.id.circleLayout).setVisibility(section.getPosition() == 0 ? View.VISIBLE : View.GONE);
		getToolbar().setBackgroundColor(primaryColor);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getWindow().setStatusBarColor(primaryColorDark);
	}

	@Override
	public void init(Bundle bundle) {
		primaryColor = getResources().getColor(R.color.primary_color);
		primaryColorDark = getResources().getColor(R.color.primary_color_dark);
		Button settingsHandler = (Button) findViewById(R.id.settings_remover);
		isSettingsEnabled = CLauncherActivity.hasSettings(this);
		settingsHandler.setText(isSettingsEnabled ? R.string.hide_settings_from_quick_circle : R.string.show_settings_from_quick_circle);
		settingsHandler.setOnClickListener(v -> {
			try {
				if (isSettingsEnabled) {
					if (CLauncherActivity.removeSettings(this)) {
						Toast.makeText(this, "Removed successfully, reboot in order to update", Toast.LENGTH_SHORT).show();
						isSettingsEnabled = false;
						settingsHandler.setText(R.string.show_settings_from_quick_circle);

					}
				} else {
					if (CLauncherActivity.addSettings(this)) {
						Toast.makeText(this, "Added successfully, reboot in order to update", Toast.LENGTH_SHORT).show();
						isSettingsEnabled = true;
						settingsHandler.setText(R.string.hide_settings_from_quick_circle);
					} else {
						Toast.makeText(this, "Fail to add settings", Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		AdView adView = (AdView) findViewById(R.id.adView);
		adView.setAdListener(new AdListener() {
			@Override
			public void onAdFailedToLoad(int errorCode) {
				super.onAdFailedToLoad(errorCode);
				adView.setVisibility(View.GONE);
			}

			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				adView.setVisibility(View.VISIBLE);
			}
		});
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addKeyword("Quick Circle, LG G3")
				.build();
		adView.loadAd(adRequest);
		addSection(newSection(getString(R.string.modules), new IconDrawable(this, Iconify.IconValue.md_settings), new ModulesFragment()));
		addSection(newSection(getString(R.string.how_to_add), new IconDrawable(this, Iconify.IconValue.md_help), HowToFragment_.builder().build()));
		addDivisor();
		addSection(newSection(getString(R.string.source), new IconDrawable(this, Iconify.IconValue.md_adb), new SourceFragment()));
		addSection(newSection(getString(R.string.about_title), new IconDrawable(this, Iconify.IconValue.md_account_circle), AboutFragment_.builder().build()));
		SwitchCompat g2Mode = (SwitchCompat) findViewById(R.id.g2_mode_switch);
		modeText = (TextView) findViewById(R.id.mode_text);
		prefs = new Preferences_(this);
		boolean isChecked = !prefs.g2Mode().get();
		modes = Pair.create(getString(R.string.g2_mode), getString(R.string.g3_mode));
		g2Mode.setChecked(isChecked);
		modeText.setText(!isChecked ? modes.first : modes.second);
		g2Mode.setOnCheckedChangeListener((buttonView, isChecked1) -> {
			prefs.g2Mode().put(!isChecked1);
			modeText.setText(!isChecked1 ? modes.first : modes.second);
		});
	}
}
