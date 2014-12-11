package com.yoavst.quickapps.toggles.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;

import com.yoavst.quickapps.R;
import com.yoavst.quickapps.toggles.ToggleFragment;
import com.yoavst.quickapps.toggles.CTogglesActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.res.StringRes;

/**
 * Created by Yoav.
 */
@EFragment
public class BrightnessFragment extends ToggleFragment {
	@StringRes(R.string.brightness)
	String BRIGHTNESS;
	@StringRes(R.string.brightness_med)
	String MED;
	@StringRes(R.string.brightness_auto)
	String AUTO;
	@StringRes(R.string.brightness_max)
	String MAX;
	@StringRes(R.string.brightness_low)
	String LOW;
	Resources mSystemUiResources;
	// resources id of system ui stuff
	static int maxBrightnessIcon = -1;
	static int medBrightnessIcon = -1;
	static int autoBrightnessIcon = -1;
	/**
	 * 0 is Auto
	 * 1 is Low
	 * 2 is Medium
	 * 3 is Max
	 */
	int selectedMode = -1;

	@AfterViews
	void init() {
		mToggleTitle.setText(BRIGHTNESS);
		mSystemUiResources = ((CTogglesActivity) getActivity()).getSystemUiResource();
		if (maxBrightnessIcon == -1 || medBrightnessIcon == -1 || autoBrightnessIcon == -1) {
			maxBrightnessIcon = mSystemUiResources.getIdentifier("indi_noti_brightness_max_on", "drawable", "com.android.systemui");
			autoBrightnessIcon = mSystemUiResources.getIdentifier("indi_noti_brightness_auto_on", "drawable", "com.android.systemui");
			medBrightnessIcon = mSystemUiResources.getIdentifier("indi_noti_brightness_mid_on", "drawable", "com.android.systemui");
		}
		int brightness = getBrightness();
		if (brightness == -1) selectedMode = 0;
		else if (brightness < 115) selectedMode = 1;
		else if (brightness < 180) selectedMode = 2;
		else selectedMode = 3;
		showBrightness();
	}

	@Override
	public void onToggleButtonClicked() {
		selectedMode++;
		if (selectedMode == 4) selectedMode = 0;
		if (selectedMode == 0) setBrightnessMode(true);
		else if (selectedMode == 1) setBrightness(110); // 30$
		else if (selectedMode == 2) setBrightness(175); // 60%
		else setBrightness(229); // 90 %
		showBrightness();
	}

	@Override
	public Intent getIntentForLaunch() {
		return new Intent(Settings.ACTION_DISPLAY_SETTINGS);
	}

	private void showBrightness() {
		if (selectedMode == 0) {
			mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(autoBrightnessIcon));
			mToggleText.setText(AUTO);
		} else if (selectedMode == 1) {
			mToggleIcon.setImageResource(R.drawable.indi_noti_brightness_low);
			mToggleText.setText(LOW);
		} else if (selectedMode == 2) {
			mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(medBrightnessIcon));
			mToggleText.setText(MED);
		} else {
			mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(maxBrightnessIcon));
			mToggleText.setText(MAX);
		}
	}

	private void setBrightnessMode(boolean auto) {
		Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, auto ? 1 : 0);
	}

	private void setBrightness(int level) {
		setBrightnessMode(false);
		Settings.System.putInt(getActivity().getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, level);
	}

	private int getBrightness() {
		try {
			if (Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == 1)
				return -1;
			return Settings.System.getInt(getActivity().getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (Settings.SettingNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
