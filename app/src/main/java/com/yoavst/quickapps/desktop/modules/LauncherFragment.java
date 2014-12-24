package com.yoavst.quickapps.desktop.modules;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yoavst.quickapps.R;
import com.yoavst.quickapps.launcher.CLauncherActivity;

import java.util.ArrayList;
import java.util.Collections;

import mobeta.android.dslv.DragSortListView;

/**
 * Created by Yoav.
 */
public class LauncherFragment extends BaseModuleFragment {
	ArrayList<CLauncherActivity.ListItem> mItems;
	BroadcastReceiver installReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CLauncherActivity.defaultItems = null;
			mItems = CLauncherActivity.getIconsFromPrefs(getActivity());
			sortItems();
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
		getActivity().registerReceiver(installReceiver, filter);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(installReceiver);
	}

	@Override
	int getLayoutId() {
		return R.layout.desktop_module_launcher;
	}

	@Override
	int[] getIdsForCheckboxes() {
		return new int[]{R.id.auto_load_checkbox,R.id.load_externalg_checkbox,R.id.remove_lg_checkbox,R.id.orientation_switch};
	}

	@Override
	int[] getIdsForRows() {
		return new int[]{R.id.modules_auto_load_row, R.id.modules_load_external_row, R.id.modules_remove_lg_row, R.id.modules_orientation_row};
	}

	@Override
	boolean shouldCheck(int id) {
		switch (id) {
			case R.id.load_externalg_checkbox:
				return prefs.launcherLoadExternalModules().get();
			case R.id.auto_load_checkbox:
				return prefs.launcherAutoAddModules().get();
			case R.id.remove_lg_checkbox:
				return prefs.showAppsThatInLg().get();
			default:
			case R.id.orientation_switch:
				return prefs.launcherIsVertical().get();
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (prefs.launcherItems().exists())
			mItems = CLauncherActivity.getIconsFromPrefs(getActivity());
		else
			mItems = CLauncherActivity.initDefaultIcons(getActivity());
		sortItems();
		view.findViewById(R.id.modules_order_row).setOnClickListener(v -> onOpenSettingsClicked());
	}

	void sortItems() {
		if (mItems != null && mItems.size() != 0) {
			// We got to keep the same sort for checked, and put the unchecked at the end
			ArrayList<CLauncherActivity.ListItem> checked = new ArrayList<>();
			ArrayList<CLauncherActivity.ListItem> unchecked = new ArrayList<>();
			for (CLauncherActivity.ListItem mItem : mItems) {
				if (!mItem.enabled) unchecked.add(mItem);
				else checked.add(mItem);
			}
			Collections.sort(unchecked, (lhs, rhs) -> lhs.name.compareTo(rhs.name));
			Collections.addAll(checked, unchecked.toArray(new CLauncherActivity.ListItem[unchecked.size()]));
			mItems = checked;
		}
	}

	void onOpenSettingsClicked() {
		final DragSortListView listview = (DragSortListView) LayoutInflater.from(getActivity()).inflate(R.layout.desktop_module_drag_list, null);
		final Adapter adapter = new Adapter();
		listview.setAdapter(adapter);
		listview.setRemoveListener(which -> adapter.remove(adapter.getItem(which)));
		listview.setDropListener((from, to) -> {
			CLauncherActivity.ListItem item = adapter.getItem(from);
			adapter.remove(item);
			adapter.insert(item, to);
		});
		new AlertDialog.Builder(getActivity()).setPositiveButton(android.R.string.yes, (dialog, which) ->
				prefs.launcherItems().put(CLauncherActivity.gson.toJson(mItems, CLauncherActivity.listType)))
				.setNegativeButton(android.R.string.no, (dialog, which) -> init())
				.setView(listview).show();
	}

	private class Adapter extends ArrayAdapter<CLauncherActivity.ListItem> {

		public Adapter() {
			super(getActivity(), R.layout.desktop_module_launcher_item, mItems);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.desktop_module_launcher_item, parent, false);
				holder = new ViewHolder(convertView);
				holder.enabled.setTag(position);
				holder.enabled.setOnClickListener(view -> {
					CheckBox check = (CheckBox) view;
					getItem((Integer) check.getTag()).enabled = check.isChecked();
				});
				convertView.setTag(holder);
			} else holder = (ViewHolder) convertView.getTag();
			holder.name.setText(getItem(position).name);
			holder.icon.setImageDrawable(getItem(position).icon);
			holder.enabled.setChecked(getItem(position).enabled);
			holder.enabled.setTag(position);
			return convertView;
		}

		private class ViewHolder {
			TextView name;
			ImageView icon;
			CheckBox enabled;

			public ViewHolder(View view) {
				name = (TextView) view.findViewById(R.id.name);
				icon = (ImageView) view.findViewById(R.id.icon);
				enabled = (CheckBox) view.findViewById(R.id.enabled);
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.load_externalg_checkbox:
				prefs.launcherLoadExternalModules().put(isChecked);
				CLauncherActivity.defaultItems = null;
				mItems = CLauncherActivity.getIconsFromPrefs(getActivity());
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
			case R.id.auto_load_checkbox:
				prefs.launcherAutoAddModules().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
			case R.id.remove_lg_checkbox:
				prefs.showAppsThatInLg().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
			case R.id.orientation_switch:
				prefs.launcherIsVertical().put(isChecked);
				Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
				break;
		}
	}

}
