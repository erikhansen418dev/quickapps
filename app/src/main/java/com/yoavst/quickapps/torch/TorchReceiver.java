package com.yoavst.quickapps.torch;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Yoav.
 */
public class TorchReceiver extends BroadcastReceiver {
	NotificationManager mNotificationManager;
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			CameraManager.init(context);
			if (mNotificationManager == null) mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			PhoneActivity.createNotification(context);
			if (CameraManager.toggleTorch()) {
				mNotificationManager.notify(NotificationReceiver.NOTIFICATION_ID, PhoneActivity.notification);
			} else {
				mNotificationManager.cancel(NotificationReceiver.NOTIFICATION_ID);
				CameraManager.destroy();
			}
		}
	}
}
