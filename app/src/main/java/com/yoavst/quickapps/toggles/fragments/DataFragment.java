package com.yoavst.quickapps.toggles.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.provider.Settings;

import com.yoavst.quickapps.R;
import com.yoavst.quickapps.toggles.Connectivity;
import com.yoavst.quickapps.toggles.ToggleFragment;
import com.yoavst.quickapps.toggles.CTogglesActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.StringRes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Yoav.
 */
@EFragment
public class DataFragment extends ToggleFragment {
	@StringRes(R.string.data_data)
	String DATA;
	@StringRes(R.string.data_off)
	String DATA_OFF;
	@StringRes(R.string.data_off_airplane)
	String DATA_OFF_AIRPLANE;
	@StringRes(R.string.data_on)
	String DATA_ON;
	Resources mSystemUiResources;
	// resources id of system ui stuff
	static int dataOnIcon = -1;
	static int dataOffIcon = -1;
	static int dataForcedOffIcon = -1;
	@SystemService
	ConnectivityManager mConnectivityManager;
	BroadcastReceiver mDataReceiver;

	@AfterViews
	void init() {
		mToggleTitle.setText(DATA);
		mSystemUiResources = ((CTogglesActivity) getActivity()).getSystemUiResource();
		if (dataOnIcon == -1 || dataOffIcon == -1 || dataForcedOffIcon == -1) {
			dataOnIcon = mSystemUiResources.getIdentifier("indi_noti_data_on", "drawable", "com.android.systemui");
			dataOffIcon = mSystemUiResources.getIdentifier("indi_noti_data_off", "drawable", "com.android.systemui");
			dataForcedOffIcon = mSystemUiResources.getIdentifier("indi_noti_data_disable_normal_vzw", "drawable", "com.android.systemui");
		}
		if (mDataReceiver == null) {
			mDataReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					setToggleData();
				}
			};
		}
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(mDataReceiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(mDataReceiver);
		} catch (Exception ignored) {
			// Do nothing - receiver not registered
		}
	}

	void setToggleData() {
		if (isMobileDataEnabled()) {
			mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(dataOnIcon));
			mToggleText.setText(DATA_ON);
		} else {
			if (Connectivity.isAirplaneMode(getActivity())) {
				mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(dataForcedOffIcon));
				mToggleText.setText(DATA_OFF_AIRPLANE);
			} else {
				mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(dataOffIcon));
				mToggleText.setText(DATA_OFF);
			}
		}
	}

	@Override
	public void onToggleButtonClicked() {
		if (!Connectivity.isAirplaneMode(getActivity())) {
			boolean newState = !isMobileDataEnabled();
			setMobileDataEnabled(newState);
			mToggleText.setText(newState ? DATA_ON : DATA_OFF);
			mToggleIcon.setImageDrawable(newState ? mSystemUiResources.getDrawable(dataOnIcon) : mSystemUiResources.getDrawable(dataOffIcon));
		}
	}

	@Override
	public Intent getIntentForLaunch() {
		return new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
	}

	private void setMobileDataEnabled(boolean enabled) {
		try {
			final Method method = ConnectivityManager.class.getDeclaredMethod(
					"setMobileDataEnabled", boolean.class);
			method.setAccessible(true);
			method.invoke(mConnectivityManager, enabled);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private boolean isMobileDataEnabled() {
		if (Connectivity.isAirplaneMode(getActivity())) return false;
		boolean mobileDataEnabled = false; // Assume disabled
		try {
			Class<?> cmClass = Class.forName(mConnectivityManager.getClass().getName());
			Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
			method.setAccessible(true); // Make the method callable
			// get the setting for "mobile data"
			mobileDataEnabled = (Boolean) method.invoke(mConnectivityManager);
		} catch (Exception e) {
			// Some problem accessible private API
			// TODO do whatever error handling you want here
		}
		return mobileDataEnabled;
	}

}
