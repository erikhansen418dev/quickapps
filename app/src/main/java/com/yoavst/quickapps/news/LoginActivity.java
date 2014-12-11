/*
 * Copyright 2014 Bademus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Contributors:
 *                Bademus
 */

package com.yoavst.quickapps.news;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.lge.qcircle.template.QCircleTemplate;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;
import com.yoavst.quickapps.URLEncodedUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.http.NameValuePair;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@EActivity
public class LoginActivity extends QCircleActivity {
	@Pref
	Prefs_ mPrefs;
	@Bean
	DownloadManager mManager;
	private static final Token EMPTY_TOKEN = null;

	@AfterInject
	void init() {
		String url = mManager.getService().getAuthorizationUrl(EMPTY_TOKEN);
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.loading));
		WebView webView = createWebView(this);
		setContentView(webView);
		webView.loadUrl(url);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private WebView createWebView(final Context context) {
		WebView webView = new WebView(context);
		webView.setWebViewClient(createWebViewClient());
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setVisibility(View.VISIBLE);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(false);
		return webView;
	}

	private WebViewClient createWebViewClient() {
		return new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap fav) {
				mProgressDialog.show();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				mProgressDialog.dismiss();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith(REDIRECT_URI_LOCAL) || url.startsWith(REDIRECT_URN)) {
					mProgressDialog.show();
					handleUrl(url);
					return true;
				}
				mProgressDialog.dismiss();
				return false;
			}
		};
	}

	void handleUrl(String url) {
		try {
			URI uri = new URI(url);
			List<NameValuePair> parameters = URLEncodedUtils.parse(uri, "UTF-8");
			for (NameValuePair pair : parameters) {
				if (pair.getName().equals("error")) {
					handleLoginError("Error: " + pair.getValue());
					return;
				} else if (pair.getName().equals("code")) {
					handleCode(pair.getValue());
					return;
				}
			}
			handleLoginError("Error! please try again");
		} catch (URISyntaxException e) {
			// Impossible, I think
			e.printStackTrace();
		}
	}

	@Background
	void handleCode(String code) {
		try {
			Verifier verifier = new Verifier(code);
			Token accessToken = mManager.getService().getAccessToken(EMPTY_TOKEN, verifier);
			mManager.saveToken(accessToken);
			success();
		} catch (OAuthException exception) {
			handleLoginError("Error! please try again");
		}
	}

	@UiThread
	void handleLoginError(String textToShow) {
		mProgressDialog.hide();
		Toast.makeText(this, textToShow, Toast.LENGTH_SHORT).show();
		recreate();
	}

	@UiThread
	void success() {
		mProgressDialog.hide();
		Toast.makeText(this, R.string.login_to_feedly, Toast.LENGTH_SHORT).show();
		finish();
	}

	private ProgressDialog mProgressDialog;

	public static final String REDIRECT_URI_LOCAL = "http://localhost";

	public static final String REDIRECT_URN = "urn:ietf:wg:oauth:2.0:oob";
}
