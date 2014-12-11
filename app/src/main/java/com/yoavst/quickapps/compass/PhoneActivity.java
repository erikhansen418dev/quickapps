package com.yoavst.quickapps.compass;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lge.app.floating.FloatableActivity;
import com.lge.app.floating.FloatingWindow;
import com.yoavst.quickapps.R;

public class PhoneActivity extends FloatableActivity {
	RelativeLayout windowBackground;
	ImageView needle;
	Compass compass;

	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.compass_qslide_layout);
		windowBackground = (RelativeLayout) findViewById(R.id.compass_background);
		needle = (ImageView) findViewById(R.id.needle);
		compass = new Compass(this, needle);

	}

	@Override
	public void onAttachedToFloatingWindow(FloatingWindow w) {
		super.onAttachedToFloatingWindow(w);
		FloatingWindow window = getFloatingWindow();
		FloatingWindow.LayoutParams layoutParams = window.getLayoutParams();
		layoutParams.width = 720;
		layoutParams.height = 720;
		layoutParams.resizeOption = FloatingWindow.ResizeOption.PROPORTIONAL;
		window.updateLayoutParams(layoutParams);
		TextView titleText = (TextView) w.findViewWithTag
				(FloatingWindow.Tag.TITLE_TEXT);
		if (titleText != null) {
			titleText.setText(getString(R.string.compass_module_name));
		}
		View titleBackground = window.findViewWithTag
				(FloatingWindow.Tag.TITLE_BACKGROUND);
		if (titleBackground != null) {
			windowBackground.setBackground(titleBackground.getBackground().getConstantState().newDrawable());
		}
		ImageButton fullscreenButton = (ImageButton) w.findViewWithTag
				(FloatingWindow.Tag.FULLSCREEN_BUTTON);
		if (fullscreenButton != null) {
			((ViewGroup) fullscreenButton.getParent()).removeView(fullscreenButton);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!isSwitchingToFloatingMode())
			compass.unregisterService();
	}

	@Override
	public void onResume() {
		super.onResume();
		compass.registerService();
	}
}
