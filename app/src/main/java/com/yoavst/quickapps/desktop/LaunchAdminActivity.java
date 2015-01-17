package com.yoavst.quickapps.desktop;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.yoavst.quickapps.AdminListener;
import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class LaunchAdminActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intentToAddThisAppAsDeviceAdmin =
				new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intentToAddThisAppAsDeviceAdmin.putExtra(
				DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(this, AdminListener.class));
		intentToAddThisAppAsDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				getString(R.string.add_admin_extra_app_text));
		startActivity(intentToAddThisAppAsDeviceAdmin);
		finish();
	}
}
