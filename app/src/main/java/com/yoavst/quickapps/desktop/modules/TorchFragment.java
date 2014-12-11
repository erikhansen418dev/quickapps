package com.yoavst.quickapps.desktop.modules;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class TorchFragment extends BaseModuleFragment {
	private static final String LAUNCHER_CLASS_NAME = "com.yoavst.quickapps.torch.PhoneActivityLauncher";
	private static final String QSLIDE_CLASS_NAME = "com.yoavst.quickapps.torch.PhoneActivity";

	@Override
	int getLayoutId() {
		return R.layout.desktop_module_torch;
	}

	@Override
	int[] getIdsForCheckboxes() {
		return new int[]{R.id.launcher_checkbox, R.id.qslide_checkbox, R.id.floating_checkbox};
	}

	@Override
	int[] getIdsForRows() {
		return new int[]{R.id.launcher_row, R.id.qslide_row, R.id.floating_row};
	}

	@Override
	boolean shouldCheck(int id) {
		switch (id) {
			case R.id.launcher_checkbox:
				return isActivityEnabled(getActivity(), LAUNCHER_CLASS_NAME);
			case R.id.qslide_checkbox:
				return isActivityEnabled(getActivity(), QSLIDE_CLASS_NAME);
			default:
			case R.id.floating_checkbox:
				return prefs.torchForceFloating().get();

		}
	}
	public static void setActivityEnabled(Context context, String activityName, final boolean enable) {
		final PackageManager pm = context.getPackageManager();
		final int enableFlag = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(new ComponentName(context, activityName), enableFlag, PackageManager.DONT_KILL_APP);
	}

	public static boolean isActivityEnabled(Context context, String activityName) {
		final PackageManager pm = context.getPackageManager();
		int flags = pm.getComponentEnabledSetting(new ComponentName(context.getPackageName(), activityName));
		return (flags & PackageManager.COMPONENT_ENABLED_STATE_DISABLED) == 0;
	}

		@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.launcher_checkbox:
				setActivityEnabled(getActivity(), LAUNCHER_CLASS_NAME, isChecked);
				Toast.makeText(getActivity(), R.string.restart_launcher_for_update, Toast.LENGTH_SHORT).show();
				break;
			case R.id.qslide_checkbox:
				setActivityEnabled(getActivity(), QSLIDE_CLASS_NAME, isChecked);
				Toast.makeText(getActivity(), R.string.reboot_for_update, Toast.LENGTH_SHORT).show();
				break;
			case R.id.floating_checkbox:
				prefs.torchForceFloating().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
