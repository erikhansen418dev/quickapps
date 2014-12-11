package com.yoavst.quickapps.toggles.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.provider.Settings;

import com.yoavst.quickapps.R;
import com.yoavst.quickapps.toggles.ToggleFragment;
import com.yoavst.quickapps.toggles.CTogglesActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.res.StringRes;

/**
 * Created by Yoav.
 */
@EFragment
public class BluetoothFragment extends ToggleFragment {
	@StringRes(R.string.bluetooth)
	String BLUETOOTH;
	@StringRes(R.string.bluetooth_off)
	String BLUETOOTH_OFF;
	@StringRes(R.string.bluetooth_on)
	String BLUETOOTH_ON;
	@StringRes(R.string.bluetooth_turning_off)
	String BLUETOOTH_TURN_OFF;
	@StringRes(R.string.bluetooth_turning_on)
	String BLUETOOTH_TURN_ON;
	Resources mSystemUiResources;
	BroadcastReceiver mBluetoothReceiver;
	// resources id of system ui stuff
	static int mBluetoothOffIcon = -1;
	static int mBluetoothOnIcon = -1;

	@AfterViews
	void init() {
		mToggleTitle.setText(BLUETOOTH);
		mSystemUiResources = ((CTogglesActivity)getActivity()).getSystemUiResource();
		if (mBluetoothOnIcon == -1 || mBluetoothOffIcon == -1) {
			mBluetoothOffIcon = mSystemUiResources.getIdentifier("indi_noti_bluetooth_off", "drawable", "com.android.systemui");
			mBluetoothOnIcon = mSystemUiResources.getIdentifier("indi_noti_bluetooth_on", "drawable", "com.android.systemui");
		}
		if (mBluetoothReceiver == null) {
			mBluetoothReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					setToggleData();
				}
			};
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		getActivity().registerReceiver(mBluetoothReceiver, intentFilter);
		setToggleData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(mBluetoothReceiver);
		} catch (Exception ignored) {
			// Do nothing - receiver not registered
		}
	}

	void setToggleData() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter.isEnabled()) {
			setToggleConnected();
		} else {
			setToggleDisconnected();
		}
	}

	void setToggleConnected() {
		mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(mBluetoothOnIcon));
		mToggleText.setText(BLUETOOTH_ON);
	}

	void setToggleDisconnected() {
		mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(mBluetoothOffIcon));
		mToggleText.setText(BLUETOOTH_OFF);

	}

	@Override
	public void onToggleButtonClicked() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.disable();
			mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(mBluetoothOffIcon));
			mToggleText.setText(BLUETOOTH_TURN_OFF);


		}
		else {
			bluetoothAdapter.enable();
			mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(mBluetoothOnIcon));
			mToggleText.setText(BLUETOOTH_TURN_ON);
		}
	}

	@Override
	public Intent getIntentForLaunch() {
		return new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
	}
}
