package com.yoavst.quickapps.desktop.modules;

import android.widget.CompoundButton;
import android.widget.Toast;

import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class CalculatorFragment extends BaseModuleFragment implements CompoundButton.OnCheckedChangeListener {
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.qslide_checkbox:
				prefs.calculatorForceFloating().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
		}
	}

	@Override
	int getLayoutId() {
		return R.layout.desktop_module_calculator;
	}

	@Override
	int[] getIdsForCheckboxes() {
		return new int[]{R.id.qslide_checkbox};
	}

	@Override
	int[] getIdsForRows() {
		return new int[]{R.id.qslide_row};
	}

	@Override
	boolean shouldCheck(int id) {
		return prefs.calculatorForceFloating().get();
	}
}
