package com.yoavst.quickapps.desktop.modules;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yoavst.quickapps.R;
import com.yoavst.quickapps.toggles.TogglesAdapter;

import java.util.ArrayList;

import mobeta.android.dslv.DragSortListView;

/**
 * Created by Yoav.
 */
public class TogglesFragment extends BaseModuleFragment {

	ArrayList<TogglesAdapter.ToggleItem> items;

	@Override
	int getLayoutId() {
		return R.layout.desktop_module_toggles;
	}

	@Override
	int[] getIdsForCheckboxes() {
		return new int[]{R.id.battery_checkbox};
	}

	@Override
	int[] getIdsForRows() {
		return new int[]{R.id.battery_row};
	}

	@Override
	boolean shouldCheck(int id) {
		return prefs.showBatteryToggle().get();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initItems();
		view.findViewById(R.id.toggles_order_row).setOnClickListener(this::clickRow);
	}

	private void initItems() {
		if (prefs.togglesItems().exists())
			items = new Gson().fromJson(prefs.togglesItems().get(), TogglesAdapter.listType);
		else items = TogglesAdapter.initDefaultToggles(getActivity());
	}

	void clickRow(View view) {
		final DragSortListView listview = (DragSortListView) LayoutInflater.from(getActivity()).inflate(R.layout.desktop_module_drag_list, null);
		final ArrayAdapter<TogglesAdapter.ToggleItem> adapter = new Adapter();
		listview.setAdapter(adapter);
		listview.setRemoveListener(which -> adapter.remove(adapter.getItem(which)));
		listview.setDropListener((from, to) -> {
			TogglesAdapter.ToggleItem item = adapter.getItem(from);
			adapter.remove(item);
			adapter.insert(item, to);
		});
		new AlertDialog.Builder(getActivity()).setPositiveButton(android.R.string.yes, (dialog, which) ->
				prefs.togglesItems().put(new Gson().toJson(items, TogglesAdapter.listType))).setNegativeButton(android.R.string.no, (dialog, which) ->
				initItems()).setView(listview).show();

	}

	private class Adapter extends ArrayAdapter<TogglesAdapter.ToggleItem> {

		public Adapter() {
			super(getActivity(), android.R.layout.simple_list_item_1, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, parent, false);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			} else holder = (ViewHolder) convertView.getTag();
			holder.name.setText(getItem(position).name);
			return convertView;
		}

		private class ViewHolder {
			TextView name;

			public ViewHolder(View view) {
				name = (TextView) view.findViewById(android.R.id.text1);
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.battery_checkbox:
				prefs.showBatteryToggle().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
