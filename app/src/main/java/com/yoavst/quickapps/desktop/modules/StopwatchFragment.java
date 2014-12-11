package com.yoavst.quickapps.desktop.modules;

import android.widget.CompoundButton;
import android.widget.Toast;

import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class StopwatchFragment extends BaseModuleFragment {

	@Override
	int getLayoutId() {
		return R.layout.desktop_module_stopwatch;
	}

	@Override
	int[] getIdsForCheckboxes() {
		return new int[]{R.id.millis_checkbox};
	}

	@Override
	int[] getIdsForRows() {
		return new int[]{R.id.millis_row};
	}

	@Override
	boolean shouldCheck(int id) {
		return prefs.stopwatchShowMillis().get();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.millis_checkbox:
				prefs.stopwatchShowMillis().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
