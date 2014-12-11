package com.yoavst.quickapps.desktop.modules;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.yoavst.quickapps.Preferences_;

/**
 * Created by Yoav.
 */
public abstract class BaseModuleFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
	int[] CompoundButtonIds;
	int[] rowsIds;
	Preferences_ prefs;

	abstract int getLayoutId();

	abstract int[] getIdsForCheckboxes();

	abstract int[] getIdsForRows();

	abstract boolean shouldCheck(int id);

	void init() {

	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(getLayoutId(), container, false);
		prefs = new Preferences_(getActivity());
		init();
		CompoundButtonIds = getIdsForCheckboxes();
		rowsIds = getIdsForRows();
		for (int i = 0; i < CompoundButtonIds.length; i++) {
			CompoundButton CompoundButton = ((CompoundButton) view.findViewById(CompoundButtonIds[i]));
			CompoundButton.setChecked(shouldCheck(CompoundButtonIds[i]));
			CompoundButton.setOnCheckedChangeListener(this);
			View v = view.findViewById(rowsIds[i]);
			v.setTag(CompoundButtonIds[i]);
			v.setOnClickListener(v1 -> {
				((CompoundButton) getActivity().findViewById((Integer) v1.getTag())).toggle();
			});
		}
		return view;
	}
}
