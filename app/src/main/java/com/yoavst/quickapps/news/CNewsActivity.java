package com.yoavst.quickapps.news;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;
import com.yoavst.quickapps.news.types.Entry;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.ViewById;
import org.scribe.model.Token;

import java.util.ArrayList;

import at.markushi.ui.CircleButton;

/**
 * Created by Yoav.
 */
public class CNewsActivity extends QCircleActivity implements DownloadManager.DownloadingCallback {
	ViewPager pager;
	TextView titleError;
	TextView extraError;
	RelativeLayout errorLayout;
	ProgressBar loading;
	DownloadManager manager;
	boolean shouldOpenLogin = false;
	ArrayList<Entry> entries;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this);
		template.setBackButton();
		template.setTitle(getString(R.string.news_module_name), Color.WHITE, getResources().getColor(R.color.md_teal_900));
		template.setTitleTextSize(17);
		template.setFullscreenIntent(this::getIntentForOpenCase);
		template.getLayoutById(TemplateTag.CONTENT_MAIN).addView(LayoutInflater.from(this).
				inflate(R.layout.news_circle_container_layout, template.getLayoutById(TemplateTag.CONTENT_MAIN), false));
		setContentView(template.getView());
		init();
	}

	void init() {
		pager = (ViewPager) findViewById(R.id.pager);
		titleError = (TextView) findViewById(R.id.title_error);
		extraError = (TextView) findViewById(R.id.extra_error);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
		loading = (ProgressBar) findViewById(R.id.loading);
		manager = DownloadManager_.getInstance_(this);
		CircleButton refresh = (CircleButton) findViewById(R.id.refresh);
		refresh.setImageDrawable(new IconDrawable(this, Iconify.IconValue.md_refresh).sizeDp(24).color(Color.WHITE));
		refresh.setOnClickListener(v -> downloadEntries());
		Token token = manager.getTokenFromPrefs();
		if (token == null) {
			// User not login in
			showError(Error.Login);
		} else {
			entries = manager.getFeedFromPrefs();
			if (entries != null) showEntries();
			downloadEntries();
		}
	}

	@Override
	public void onFail(DownloadManager.DownloadError error) {
		switch (error) {
			case Login:
				showError(Error.Login);
				break;
			case Internet:
			case Other:
				if (entries == null || entries.size() == 0)
					showError(Error.Internet);
				// Else show toast
				noConnectionToast();
				break;
		}
	}

	void noConnectionToast() {
		runOnUiThread(() -> {
			Toast toast = Toast.makeText(CNewsActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		});
	}

	@Override
	public void onSuccess(ArrayList<Entry> entries) {
		this.entries = entries;
		showEntries();
	}

	enum Error {Login, Internet, Empty}

	void showEntries() {
		runOnUiThread(() -> {
			loading.setVisibility(View.GONE);
			errorLayout.setVisibility(View.GONE);
			if (entries == null || entries.size() == 0) showError(Error.Empty);
			else {
				pager.setAdapter(new NewsAdapter(getFragmentManager(), entries));
			}
		});
	}

	void downloadEntries() {
		runOnUiThread(() -> {
			errorLayout.setVisibility(View.GONE);
			if (manager.isNetworkAvailable()) {
				if (entries == null || entries.size() == 0) {
					// Show loading
					loading.setVisibility(View.VISIBLE);
				}
				// Else inform the user we start Downloading but still show content
				else {
					Toast toast = Toast.makeText(CNewsActivity.this, getString(R.string.start_downloading), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
				}
				manager.download(CNewsActivity.this);
			} else {
				if (entries == null || entries.size() == 0) {
					// Show internet error
					showError(Error.Internet);
				}
				// Else inform the user that he has no connection
				else {
					Toast toast = Toast.makeText(CNewsActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
				}
			}
		});
	}

	void showError(Error error) {
		runOnUiThread(() -> {
			errorLayout.setVisibility(View.VISIBLE);
			switch (error) {
				case Login:
					titleError.setText(R.string.news_should_login);
					extraError.setText(R.string.news_should_login_subtext);
					loading.setVisibility(View.GONE);
					shouldOpenLogin = true;
					break;
				case Internet:
					titleError.setText(R.string.news_network_error);
					extraError.setText(R.string.news_network_error_subtext);
					break;
				case Empty:
					titleError.setText(R.string.news_no_content);
					titleError.setText(R.string.news_no_content_subtext);
					break;
			}
		});
	}

	protected Intent getIntentForOpenCase() {
		if (shouldOpenLogin)
			return LoginActivity_.intent(this).get();
		else if (entries == null || entries.size() == 0)
			return null;
		else {
			int id = ((NewsFragment_) (getFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + pager.getCurrentItem()))).entryNumber;
			return new Intent(Intent.ACTION_VIEW, Uri.parse(NewsAdapter.getEntry(id).getAlternate().get(0).getHref()));
		}
	}
}
