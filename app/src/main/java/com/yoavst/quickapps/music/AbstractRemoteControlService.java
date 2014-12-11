package com.yoavst.quickapps.music;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Created by Yoav.
 */
public abstract class AbstractRemoteControlService extends NotificationListenerService {
	protected static final int BITMAP_HEIGHT = 1100;
	protected static final int BITMAP_WIDTH = 1100;
	private IBinder mBinder = new RCBinder();
	public class RCBinder extends Binder {
		public AbstractRemoteControlService getService() {
			return AbstractRemoteControlService.this;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopSelf();
		return false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (intent.getAction().startsWith("com.yoavst.quickmusic.BIND_RC_CONTROL")) {
			return mBinder;
		} else {
			return super.onBind(intent);
		}
	}
	abstract Intent getCurrentClientIntent();

	abstract boolean setRemoteControllerEnabled();

	abstract void setRemoteControllerDisabled();

	abstract void sendNextKey();

	abstract void sendPauseKey();

	abstract void sendPlayKey();

	abstract void sendPreviousKey();

	@Override
	public void onNotificationPosted(StatusBarNotification notification) {
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification notification) {
	}

	@Override
	public void onDestroy() {
		setRemoteControllerDisabled();
	}
}
