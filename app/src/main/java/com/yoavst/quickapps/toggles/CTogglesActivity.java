package com.yoavst.quickapps.toggles;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class CTogglesActivity extends QCircleActivity {
	Resources systemUiResources;
	ViewPager pager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final PackageManager pm = getPackageManager();
		final ApplicationInfo applicationInfo;
		try {
			applicationInfo = pm.getApplicationInfo("com.android.systemui", PackageManager.GET_META_DATA);
			systemUiResources = pm.getResourcesForApplication(applicationInfo);
		} catch (PackageManager.NameNotFoundException e) {
			// Congratulations user, you are so dumb that there is no system ui...
			e.printStackTrace();
		}
		template = new QCircleTemplate(this);
		template.setBackButton();
		template.setTitle(getString(R.string.toggles_module_name), Color.WHITE, getResources().getColor(R.color.md_indigo_700));
		template.setTitleTextSize(17);
		template.setFullscreenIntent(() -> ((ToggleFragment)
				(getFragmentManager().findFragmentByTag("android:switcher:" + R.id.toggles_pager + ":" + pager.getCurrentItem()))).getIntentForLaunch().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		pager = new ViewPager(this);
		pager.setId(R.id.toggles_pager);
		pager.setAdapter(new TogglesAdapter(getFragmentManager(), this));
		template.getLayoutById(TemplateTag.CONTENT_MAIN).addView(pager);
		setContentView(template.getView());
	}

	public Resources getSystemUiResource() {
		return systemUiResources;
	}

}
