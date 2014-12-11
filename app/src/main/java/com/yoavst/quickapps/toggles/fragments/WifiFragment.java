package com.yoavst.quickapps.toggles.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;

import com.yoavst.quickapps.R;
import com.yoavst.quickapps.toggles.CTogglesActivity;
import com.yoavst.quickapps.toggles.Connectivity;
import com.yoavst.quickapps.toggles.ToggleFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;

/**
 * Created by Yoav.
 */
@EFragment
public class WifiFragment extends ToggleFragment {
	@StringRes(R.string.wifi)
	String WIFI;
	@StringRes(R.string.wifi_off)
	String WIFI_OFF;
	@StringRes(R.string.wifi_on)
	String WIFI_ON;
	@StringRes(R.string.wifi_network_available)
	String WIFI_NETWORK_AVAILABLE;
	@StringRes(R.string.wifi_connected)
	String WIFI_CONNECTED;
	@StringRes(R.string.wifi_turning_off)
	String WIFI_TURN_OFF;
	@StringRes(R.string.wifi_turning_on)
	String WIFI_TURN_ON;
	@SystemService
	WifiManager mWifiManager;
	Resources mSystemUiResources;
	BroadcastReceiver mWifiReceiver;
	// resources id of system ui stuff
	static int wifiOffIcon = -1;
	static int wifiOnIcon = -1;

	@AfterViews
	void init() {
		mToggleTitle.setText(WIFI);
		mSystemUiResources = ((CTogglesActivity) getActivity()).getSystemUiResource();
		if (wifiOnIcon == -1 || wifiOffIcon == -1) {
			wifiOffIcon = mSystemUiResources.getIdentifier("indi_noti_wifi_off", "drawable", "com.android.systemui");
			wifiOnIcon = mSystemUiResources.getIdentifier("indi_noti_wifi_on", "drawable", "com.android.systemui");
		}
		if (mWifiReceiver == null) {
			mWifiReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					setToggleData();
				}
			};
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		getActivity().registerReceiver(mWifiReceiver, intentFilter);
		setToggleData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(mWifiReceiver);
		} catch (Exception ignored) {
			// Do nothing - receiver not registered
		}
	}

	void setToggleData() {
		if (mWifiManager.isWifiEnabled()) {
			setToggleConnected();
		} else {
			setToggleDisconnected();
		}
	}

	void setToggleConnected() {
		mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(wifiOnIcon));
		String networkName = getWifiNetworkName();
		// Mean that it is not connected to a network, but wifi is on
		if (networkName == null || networkName.equals("<unknown ssid>")) {
			List<ScanResult> results = mWifiManager.getScanResults();
			ScanResult bestSignalAndPublic = null;
			for (ScanResult result : results) {
				if (result.capabilities.equals("[WPS][ESS]") || result.capabilities.equals("[ESS]") &&
						(bestSignalAndPublic == null || WifiManager.compareSignalLevel(bestSignalAndPublic.level, result.level) < 0))
					bestSignalAndPublic = result;
			}
			if (bestSignalAndPublic == null) {
				mToggleText.setText(WIFI_ON);
			} else mToggleText.setText(WIFI_NETWORK_AVAILABLE);
		} else {
			mToggleText.setText(String.format(WIFI_CONNECTED, networkName));
		}
	}

	void setToggleDisconnected() {
		mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(wifiOffIcon));
		mToggleText.setText(WIFI_OFF);

	}

	@Override
	public void onToggleButtonClicked() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
			mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(wifiOffIcon));
			mToggleText.setText(WIFI_TURN_OFF);
		} else {
			if (Connectivity.isApOn(getActivity())) {
				Connectivity.configApState(getActivity());
				new Handler().postDelayed(this::toggleOn, 400);
			} else toggleOn();
			mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(wifiOnIcon));
			mToggleText.setText(WIFI_TURN_ON);
		}
	}

	private void toggleOn() {
		mWifiManager.setWifiEnabled(true);

	}

	@Override
	public Intent getIntentForLaunch() {
		return new Intent(Settings.ACTION_WIFI_SETTINGS);
	}

	public String getWifiNetworkName() {
		if (mWifiManager.isWifiEnabled()) {
			WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			if (wifiInfo != null) {
				NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
				if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
					return wifiInfo.getSSID();
				}
			}
		}
		return null;
	}
}
