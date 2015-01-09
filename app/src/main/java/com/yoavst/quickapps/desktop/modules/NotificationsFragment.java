package com.yoavst.quickapps.desktop.modules;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by Yoav.
 */
public class NotificationsFragment extends BaseModuleFragment implements CompoundButton.OnCheckedChangeListener {
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.listener_row).setOnClickListener(v -> getActivity().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
	}

	@Override
	int getLayoutId() {
		return R.layout.desktop_module_notifications;
	}

	@Override
	int[] getIdsForCheckboxes() {
		return new int[]{R.id.privacy_checkbox,R.id.am_pm_checkbox, R.id.auto_launch_checkbox};
	}

	@Override
	int[] getIdsForRows() {
		return new int[]{R.id.privacy_row, R.id.am_pm_row, R.id.auto_launch_row};
	}

	@Override
	boolean shouldCheck(int id) {
		if (id == R.id.privacy_checkbox) return prefs.notificationShowContent().get();
		else if (id == R.id.auto_launch_checkbox) return prefs.startActivityOnNotification().get();
		else return prefs.amPmInNotifications().get();
	}



	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.privacy_checkbox:
				prefs.notificationShowContent().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully,Toast.LENGTH_SHORT).show();
				break;
			case R.id.am_pm_checkbox:
				prefs.amPmInNotifications().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully,Toast.LENGTH_SHORT).show();
				break;
			case R.id.auto_launch_checkbox:
				prefs.startActivityOnNotification().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully,Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
