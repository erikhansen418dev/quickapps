package com.yoavst.quickapps.toggles;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.R;
import com.yoavst.quickapps.toggles.fragments.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Yoav.
 */
public class TogglesAdapter extends FragmentPagerAdapter {
	public static Type listType = new TypeToken<ArrayList<ToggleItem>>() {
	}.getType();
	ArrayList<ToggleItem> items;
	boolean showBattery = true;

	public TogglesAdapter(FragmentManager fm, Context context) {
		super(fm);
		Preferences_ prefs = new Preferences_(context);
		showBattery = prefs.showBatteryToggle().get();
		if (prefs.togglesItems().exists())
			items = new Gson().fromJson(prefs.togglesItems().get(), listType);
		else items = initDefaultToggles(context);
	}

	@Override
	public Fragment getItem(int i) {
		if (showBattery & i == 0) return BatteryFragment_.builder().build();
		else return getOriginalItem(items.get(showBattery ? i - 1 : i).id + 1);
	}

	public Fragment getOriginalItem(int i) {
		switch (i) {
			default:
			case 1:
				return WifiFragment_.builder().build();
			case 2:
				return DataFragment_.builder().build();
			case 3:
				return BrightnessFragment_.builder().build();
			case 4:
				return SoundFragment_.builder().build();
			case 5:
				return BluetoothFragment_.builder().build();
			case 6:
				return HotSpotFragment_.builder().build();
		}
	}

	@Override
	public int getCount() {
		return showBattery ? items.size() + 1 : items.size();
	}

	public static ArrayList<ToggleItem> initDefaultToggles(Context context) {
		final String[] toggles = context.getResources().getStringArray(R.array.toggles);
		ArrayList<ToggleItem> items = new ArrayList<>(toggles.length);
		for (int i = 0; i < toggles.length; i++) {
			items.add(new ToggleItem(toggles[i], i));
		}
		new Preferences_(context).togglesItems().put(new Gson().toJson(items, listType));
		return items;
	}

	public static ArrayList<ToggleItem> addNewToggles(Context context, ToggleItem... items) {
		Preferences_ prefs = new Preferences_(context);
		ArrayList<ToggleItem> listItems;
		if (!prefs.togglesItems().exists()) listItems = initDefaultToggles(context);
		else listItems = new Gson().fromJson(prefs.togglesItems().get(), listType);
		if (items.length != 0) {
			Collections.addAll(listItems, items);
			prefs.togglesItems().put(new Gson().toJson(items, listType));
		}
		return listItems;
	}

	public static class ToggleItem {
		public String name;
		public int id;

		public ToggleItem(String name, int id) {
			this.name = name;
			this.id = id;
		}
	}
}
