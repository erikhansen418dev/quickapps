package com.yoavst.quickapps;

import android.app.Activity;
import android.view.View;

import com.lge.qcircle.template.QCircleTemplate;

/**
 * Created by Yoav.
 */
public class QCircleActivity extends Activity {
	protected QCircleTemplate template;

	@Override
	protected void onStart() {
		super.onStart();
		View circle = findViewById(R.id.circle);
		if (circle != null && new Preferences_(this).g2Mode().getOr(false)) circle.setVisibility(View.GONE);
		try {
			template.registerIntentReceiver();
		} catch (Exception ignored) {}
	}

	@Override
	protected void onPause() {
		try {
			super.onPause();
			template.unregisterReceiver();
		} catch (Exception ignored){}
	}
}
