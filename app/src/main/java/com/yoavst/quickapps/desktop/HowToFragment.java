package com.yoavst.quickapps.desktop;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import at.markushi.ui.CircleButton;

/**
 * Created by Yoav.
 */
@EFragment(R.layout.desktop_fragment_howto)
public class HowToFragment extends Fragment {

	@AfterViews
	void init() {
		CircleButton button = (CircleButton) getActivity().findViewById(R.id.settings_btn);
		button.setImageDrawable(new IconDrawable(getActivity(), Iconify.IconValue.md_settings).sizeDp(32).color(Color.WHITE));
	}

	@Click(R.id.settings_btn)
	void openQuickSettings() {
		ComponentName settingComp = new ComponentName("com.android.settings", "com.android.settings.lge.QuickWindowCase");
		Intent settingIntent = new Intent("android.intent.action.MAIN");
		settingIntent.setComponent(settingComp);
		startActivity(settingIntent);
	}
}
