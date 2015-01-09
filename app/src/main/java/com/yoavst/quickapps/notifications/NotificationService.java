package com.yoavst.quickapps.notifications;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.yoavst.quickapps.CoverReceiver;
import com.yoavst.quickapps.Preferences_;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Yoav.
 */
public class NotificationService extends NotificationListenerService {
	private final IBinder mBinder = new LocalBinder();
	private Callback callback;
	public static final String NOTIFICATION_ACTION = "notification_action";

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if (callback != null) callback.onNotificationPosted(sbn);
		else if (CoverReceiver.isCoverInUse() && new Preferences_(this).startActivityOnNotification().get()){
			startActivity(new Intent(this, CNotificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		if (callback != null) callback.onNotificationRemoved(sbn);
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (intent.getAction().equals(NOTIFICATION_ACTION)) {
			return mBinder;
		} else {
			return super.onBind(intent);
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		callback = null;
		return super.onUnbind(intent);
	}

	public void setCallback(Callback callback, Runnable runnable) {
		this.callback = callback;
		if (this.callback != null) {
			ContentResolver contentResolver = getContentResolver();
			String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
			String packageName = getPackageName();
			// check to see if the enabledNotificationListeners String contains our package name
			if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName) || !enabledNotificationListeners.contains("NotificationService")) {
				this.callback.noPermissionForNotifications();
			} else runnable.run();
		}
	}

	public void setActiveNotifications() {
		try {
			NotificationsManager.setNotifications(new ArrayList<>(Arrays.asList(getActiveNotifications())));
		} catch (Exception exception) {
			exception.printStackTrace();
			callback.noPermissionForNotifications();
		}
	}

	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		NotificationService getService() {
			// Return this instance of LocalService so clients can call public methods
			return NotificationService.this;
		}
	}

	public static interface Callback {
		public void onNotificationPosted(StatusBarNotification statusBarNotification);

		public void onNotificationRemoved(StatusBarNotification statusBarNotification);

		public void noPermissionForNotifications();
	}
}
