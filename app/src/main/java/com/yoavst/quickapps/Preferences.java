package com.yoavst.quickapps;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by Yoav.
 */
@SharedPref(SharedPref.Scope.UNIQUE)
public interface Preferences {
	@DefaultBoolean(false)
	boolean torchForceFloating();

	@DefaultBoolean(true)
	boolean showRepeatingEvents();

	@DefaultBoolean(true)
	boolean showLocation();

	@DefaultBoolean(true)
	boolean notificationShowContent();

	@DefaultBoolean(true)
	boolean launcherIsVertical();

	@DefaultBoolean(false)
	boolean launcherLoadExternalModules();

	@DefaultBoolean(true)
	boolean launcherAutoAddModules();

	@DefaultBoolean(true)
	boolean showBatteryToggle();

	@DefaultBoolean(false)
	boolean calculatorForceFloating();

	@DefaultBoolean(true)
	boolean stopwatchShowMillis();

	@DefaultBoolean(true)
	boolean showAppsThatInLg();

	@DefaultBoolean(false)
	boolean amPmInNotifications();

	@DefaultBoolean(false)
	boolean amPmInCalendar();

	@DefaultBoolean(false)
	boolean g2Mode();

	@DefaultBoolean(false)
	boolean startActivityOnNotification();

	@DefaultInt(0)
	int highScoreInSimon();

	String launcherItems();

	String togglesItems();

	String quickDials();
}
