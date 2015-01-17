package com.yoavst.quickapps.compass;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.lge.qcircle.template.TemplateType;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class CCompassActivity extends QCircleActivity {
	private Compass compass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this, TemplateType.CIRCLE_EMPTY);
		template.setBackButton();
		template.setBackButtonTheme(true);
		template.setBackgroundDrawable(getResources().getDrawable(R.drawable.compass_back), true);
		RelativeLayout mainLayout = (RelativeLayout) template.getLayoutById(TemplateTag.CONTENT).getParent();
		ImageView needle = (ImageView) LayoutInflater.from(this).inflate(R.layout.compass_circle_layout, mainLayout, false);
		compass = new Compass(this, needle);
		mainLayout.addView(needle);
		setContentView(template.getView());
	}

	@Override
	protected void onResume() {
		super.onResume();
		compass.registerService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		compass.unregisterService();
	}

	@Override
	protected Intent getIntentToShow() {
		return new Intent(this,PhoneActivity.class).putExtra("com.lge.app.floating.launchAsFloating", true);
	}
}
