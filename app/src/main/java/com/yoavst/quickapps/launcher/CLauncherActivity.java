package com.yoavst.quickapps.launcher;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import com.google.gson.reflect.TypeToken;
import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.makeramen.RoundedImageView;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Yoav.
 */
public class CLauncherActivity extends QCircleActivity implements View.OnClickListener {
	public static Type listType = new TypeToken<ArrayList<ListItem>>() {
	}.getType();
	public static final Gson gson;
	public static ArrayList<ListItem> defaultItems;
	private AppInstalledReceiver appInstalledReceiver = new AppInstalledReceiver(this);
	private AppRemovedReceiver appRemovedReceiver = new AppRemovedReceiver(this);
	private static ComponentWrapper[] components;

	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(ListItem.class, new LauncherDeSerializer());
		gson = gsonBuilder.create();
	}

	Preferences_ prefs;

	ArrayList<ListItem> items;
	ArrayList<View> views;
	int marginSize;
	int iconSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this);
		template.setBackButton();
		template.setBackgroundColor(Color.BLACK, false);
		template.setBackButtonTheme(true);
		setContentView(template.getView());
		init();
	}

	void init() {
		prefs = new Preferences_(this);
		if (!prefs.showAppsThatInLg().get())
			updateComponents(this);
		ArrayList<ListItem> allItems = getIconsFromPrefs(this);
		items = new ArrayList<>(allItems.size());
		for (ListItem item : allItems)
			if (item.enabled && canBeShown(item))
				items.add(item);
		views = new ArrayList<>(items.size());
		iconSize = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				64,
				getResources().getDisplayMetrics()
		);
		marginSize = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				10,
				getResources().getDisplayMetrics()
		);
		boolean isVertical = prefs.launcherIsVertical().get();
		if (items.size() < 5) {
			isVertical = true;
		}
		getFragmentManager().beginTransaction().replace(TemplateTag.CONTENT_MAIN, isVertical ? new VerticalFragment() : new HorizontalFragment()).commit();
	}

	private static void updateComponents(Context context) {
		Uri uri = Uri.parse("content://com.lge.lockscreensettings/quickwindow");
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			components = new ComponentWrapper[cursor.getCount()];
			int i = 0;
			do {
				components[i] = new ComponentWrapper(new ComponentName(cursor.getString(cursor.getColumnIndex("package")), cursor.getString(cursor.getColumnIndex("class"))),
						cursor.getInt(cursor.getColumnIndex("_id")));
				i++;
			} while (cursor.moveToNext());
		}
	}

	public static boolean removeSettings(Context context) {
		updateComponents(context);
		Uri uri = Uri.parse("content://com.lge.lockscreensettings/quickwindow");
		int settings = -1;
		for (ComponentWrapper wrapper : components) {
			if (wrapper.getComponent().getPackageName().equals("com.lge.clock")) {
				settings = wrapper.getId();
			}
		}
		if (settings != -1) {
			int rows = context.getContentResolver().delete(ContentUris.withAppendedId(uri, settings), null, null);
			return rows > 0;
		} else return false;
	}

	boolean canBeShown(ListItem item) {
		if (components == null) return true;
		for (ComponentWrapper name : components) {
			if (name.getComponent().flattenToString().equals(item.activity)) return false;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent("com.lge.quickcover");
		String componentName = v.getTag().toString();
		if (componentName.equals("com.lge.camera/com.lge.camera.app.QuickWindowCameraActivity")) {
			intent.setAction("com.lge.android.intent.action.STILL_IMAGE_CAMERA_COVER");
		}
		intent.setComponent(ComponentName.unflattenFromString(componentName));
		startActivity(intent);
		for (View view : views) {
			view.setEnabled(false);
		}
		finish();
	}

	@SuppressLint("ValidFragment")
	public class VerticalFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.launcher_circle_vertical, container, false);
			TableLayout layout = (TableLayout) view.findViewById(R.id.table_layout);
			TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(iconSize, iconSize);
			tableParams.setMargins(0, marginSize, 0, 0);
			rowParams.setMargins(0, 0, marginSize, 0);
			TableRow lastRow = null;
			for (int i = 0; i < items.size(); i++) {
				if (i % 2 == 0) {
					lastRow = new TableRow(getActivity());
					lastRow.setLayoutParams(tableParams);
					layout.addView(lastRow);
				}
				if (lastRow != null) {
					View icon = setOnClick(createLauncherIcon(items.get(i), rowParams));
					views.add(icon);
					lastRow.addView(icon);
				}
			}
			return view;
		}

	}

	@SuppressLint("ValidFragment")
	public class HorizontalFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.launcher_circle_horizontal, container, false);
			TableLayout layout = (TableLayout) view.findViewById(R.id.table_layout);
			final int maxItemsPerLine = items.size() % 2 == 0 ? items.size() / 2 : (items.size() / 2 + 1);
			TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(iconSize, iconSize);
			tableParams.setMargins(0, marginSize, 0, 0);
			rowParams.setMargins(0, 0, marginSize, 0);
			TableRow lastRow = null;
			for (int i = 0; i < items.size(); i++) {
				if (i % maxItemsPerLine == 0) {
					lastRow = new TableRow(getActivity());
					lastRow.setLayoutParams(tableParams);
					layout.addView(lastRow);
				}
				int index = i < maxItemsPerLine ? i * 2 : (i - maxItemsPerLine) * 2 + 1;
				if (lastRow != null) {
					View icon = setOnClick(createLauncherIcon(items.get(index), rowParams));
					views.add(icon);
					lastRow.addView(icon);
				}

			}
			return view;
		}
	}

	private View setOnClick(View view) {
		view.setOnClickListener(this);
		return view;
	}

	private ImageView createLauncherIcon(ListItem item, ViewGroup.LayoutParams params) {
		RoundedImageView imageView = new RoundedImageView(this);
		imageView.setLayoutParams(params);
		imageView.setBackground(null);
		imageView.setImageDrawable(item.icon);
		imageView.setTag(item.activity);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imageView.setAdjustViewBounds(true);
		imageView.setCornerRadius(iconSize / 2f);
		imageView.setOval(false);
		imageView.setBorderColor(Color.BLACK);
		imageView.setBorderWidth(20f);
		return imageView;
	}

	public static ArrayList<ListItem> initDefaultIcons(Context context) {
		Preferences_ prefs = new Preferences_(context);
		boolean loadExternal = prefs.launcherLoadExternalModules().get();
		if (defaultItems == null) {
			final Intent myIntent = new Intent("com.lge.quickcover");
			List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(myIntent, 0);
			defaultItems = new ArrayList<>(resInfoList.size());
			List<String> blacklist = Arrays.asList(context.getResources().getStringArray(R.array.launcher_blacklist));
			for (ResolveInfo info : resInfoList) {
				String name = info.loadLabel(context.getPackageManager()).toString();
				Drawable icon = info.loadIcon(context.getPackageManager());
				String packageName = info.activityInfo.applicationInfo.packageName;
				if ((!packageName.equals(context.getPackageName()) && !loadExternal) ||
						blacklist.contains(info.activityInfo.name))
					continue;
				String activity = new ComponentName(packageName, info.activityInfo.name).flattenToString();
				CLauncherActivity.ListItem item = new CLauncherActivity.ListItem(name, icon, activity, true);
				defaultItems.add(item);
			}
		}
		prefs.launcherItems().put(gson.toJson(defaultItems, listType));
		return defaultItems;
	}

	public static ArrayList<ListItem> getIconsFromPrefs(Context context) {
		Preferences_ prefs = new Preferences_(context);
		if (!prefs.launcherItems().exists()) return initDefaultIcons(context);
		else {
			ArrayList<ListItem> items = gson.fromJson(prefs.launcherItems().get(), listType);
			if (defaultItems == null) initDefaultIcons(context);
			boolean autoAdd = prefs.launcherAutoAddModules().get();
			for (int i = items.size() - 1; i >= 0; i--) {
				if (!defaultItems.contains(items.get(i))) {
					items.remove(i);
				} else {
					try {
						items.get(i).icon = context.getPackageManager().getActivityIcon(ComponentName.unflattenFromString(items.get(i).activity));
					} catch (PackageManager.NameNotFoundException e) {
						items.remove(i);
					}
				}
			}
			for (ListItem item : defaultItems) {
				if (!items.contains(item)) {
					if (!autoAdd) {
						item.enabled = false;
					}
					items.add(item);
				}
			}
			prefs.launcherItems().put(gson.toJson(items, listType));
			return items;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filterAdd = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filterAdd.addDataScheme("package");
		registerReceiver(appInstalledReceiver, filterAdd);
		IntentFilter filterRemove = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
		filterRemove.addDataScheme("package");
		registerReceiver(appRemovedReceiver, filterRemove);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(appInstalledReceiver);
		unregisterReceiver(appRemovedReceiver);
		super.onPause();
	}

	public static class ListItem {
		public String name;
		public Drawable icon;
		public String activity;
		@Since(2.05)
		public boolean enabled;

		public ListItem(String name, Drawable icon, String activity, boolean enabled) {
			this.name = name;
			this.enabled = enabled;
			this.icon = icon;
			this.activity = activity;
		}

		@Override
		public int hashCode() {
			return activity.hashCode() + 29;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj.getClass() == this.getClass()) {
				ListItem item = (ListItem) obj;
				if (this.activity.equals(item.activity)) {
					return true;
				}
			}
			return false;
		}
	}

	private class AppRemovedReceiver extends BroadcastReceiver {
		private CLauncherActivity activity;

		public AppRemovedReceiver(CLauncherActivity CLauncherActivity) {
			activity = CLauncherActivity;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			int uid = b.getInt(Intent.EXTRA_UID);
			String[] packages = context.getPackageManager().getPackagesForUid(uid);
			for (String pkg : packages) {
				for (ListItem item : CLauncherActivity.defaultItems) {
					String itemPkg = ComponentName.unflattenFromString(item.activity).getPackageName();
					if (pkg.equals(itemPkg)) {
						Intent i = activity.getIntent();
						activity.finish();
						defaultItems = null;
						startActivity(i);
						return;
					}
				}
			}
		}
	}

	private class AppInstalledReceiver extends BroadcastReceiver {
		private CLauncherActivity activity;

		public AppInstalledReceiver(CLauncherActivity CLauncherActivity) {
			activity = CLauncherActivity;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			int uid = b.getInt(Intent.EXTRA_UID);
			String[] packages = context.getPackageManager().getPackagesForUid(uid);
			List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(new Intent("com.lge.quickcover"), 0);
			ArrayList<String> quickcircleList = new ArrayList<>(resInfoList.size());
			for (ResolveInfo info : resInfoList) {
				quickcircleList.add(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name).flattenToString());
			}
			for (String pkg : packages) {
				PackageInfo info;
				try {
					info = context.getPackageManager().getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
				} catch (PackageManager.NameNotFoundException e) {
					continue;
				}
				for (ActivityInfo aInfo : info.activities) {
					ComponentName name = new ComponentName(aInfo.applicationInfo.packageName, aInfo.name);
					if (quickcircleList.contains(name.flattenToString())) {
						Intent i = activity.getIntent();
						activity.finish();
						defaultItems = null;
						startActivity(i);
						return;
					}
				}
			}
		}
	}

	public static class ComponentWrapper {
		private final ComponentName component;
		private final int id;

		public ComponentWrapper(ComponentName component, int id) {
			this.component = component;
			this.id = id;
		}

		public ComponentName getComponent() {
			return component;
		}

		public int getId() {
			return id;
		}
	}
}