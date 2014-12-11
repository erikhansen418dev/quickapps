package com.yoavst.quickapps.desktop.modules;

import android.widget.CompoundButton;
import android.widget.Toast;

import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class CalendarFragment extends BaseModuleFragment {

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.repeating_checkbox:
				prefs.showRepeatingEvents().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
			case R.id.location_checkbox:
				prefs.showLocation().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
			case R.id.am_pm_checkbox:
				prefs.amPmInCalendar().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
		}
	}

	@Override
	int getLayoutId() {
		return R.layout.desktop_module_calendar;
	}

	@Override
	int[] getIdsForCheckboxes() {
		return new int[]{R.id.repeating_checkbox, R.id.location_checkbox, R.id.am_pm_checkbox};
	}

	@Override
	int[] getIdsForRows() {
		return new int[]{R.id.repeating_row, R.id.location_row, R.id.am_pm_row};
	}

	@Override
	boolean shouldCheck(int id) {
		switch (id) {
			case R.id.repeating_checkbox:
				return prefs.showRepeatingEvents().get();
			case R.id.location_checkbox:
				return prefs.showLocation().get();
			case R.id.am_pm_checkbox:
				return prefs.amPmInCalendar().get();
			default:
				return false;
		}
	}
}
