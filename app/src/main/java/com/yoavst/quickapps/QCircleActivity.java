package com.yoavst.quickapps;

import android.app.Activity;

import com.lge.qcircle.template.QCircleTemplate;

/**
 * Created by Yoav.
 */
public class QCircleActivity extends Activity {
	protected QCircleTemplate template;

	@Override
	protected void onPause() {
		try {
			super.onPause();
			template.unregisterReceiver();
		} catch (Exception ignored){}
	}
}
