package com.yoavst.quickapps;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.yoavst.quickapps.launcher.CLauncherActivity;

import java.util.List;

/**
 * Created by Yoav.
 */
public class App extends Application {
	public static App instance;
	public static LGLauncherChangeObserver observer;
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		if (!new Preferences_(this).showAppsThatInLg().get())
			CLauncherActivity.updateComponents(this);
		if (new Preferences_(this).launcherLoadExternalModules().get())
		CLauncherActivity.initDefaultIcons(this);
		observer = new LGLauncherChangeObserver(new Handler());
		getContentResolver().
				registerContentObserver(Uri.parse("content://com.lge.lockscreensettings/quickwindow"),true,observer);
		// The following lines trigger the
		// initialization of RichInsights SDK
		/*int sensorConfig = PGAgent.APP; // for including App Sensor
		PGAgent.init(this, "539:6mrGrKvjx9ktpNyv", sensorConfig, true);*/
	}





	public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			PackageManager pm = context.getPackageManager();
			List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
			if (resolveInfo == null || resolveInfo.size() != 1) {
				return implicitIntent;
			}
			ResolveInfo serviceInfo = resolveInfo.get(0);
			String packageName = serviceInfo.serviceInfo.packageName;
			String className = serviceInfo.serviceInfo.name;
			ComponentName component = new ComponentName(packageName, className);
			Intent explicitIntent = new Intent(implicitIntent);
			explicitIntent.setComponent(component);
			return explicitIntent;
		} else return implicitIntent;
	}
}
