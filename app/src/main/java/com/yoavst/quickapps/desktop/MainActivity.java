package com.yoavst.quickapps.desktop;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.yoavst.quickapps.R;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.MaterialSection;

/**
 * Created by Yoav.
 */
public class MainActivity extends MaterialNavigationDrawer {
	int primaryColor;
	int primaryColorDark;

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
	}
}
