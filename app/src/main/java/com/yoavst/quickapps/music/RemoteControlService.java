package com.yoavst.quickapps.music;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RemoteController;
import android.media.RemoteController.MetadataEditor;
import android.view.KeyEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RemoteControlService extends AbstractRemoteControlService implements RemoteController.OnClientUpdateListener {
	private RemoteController mRemoteController;
	private Context mContext;
	private Field mPendingIntentField;

	//external callback provided by user.
	private RemoteController.OnClientUpdateListener mExternalClientUpdateListener;

	@Override
	public void onCreate() {
		//saving the context for further reuse
		mContext = getApplicationContext();
		mRemoteController = new RemoteController(mContext, this);
	}

	@Override
	public void onDestroy() {
		setRemoteControllerDisabled();
	}
	//Following method will be called by Activity using IBinder

	/**
	 * Enables the RemoteController thus allowing us to receive metadata updates.
	 *
	 * @return true if registered successfully
	 */
	public boolean setRemoteControllerEnabled() {
		if (!((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).registerRemoteController(mRemoteController)) {
			return false;
		} else {
			mRemoteController.setArtworkConfiguration(BITMAP_WIDTH, BITMAP_HEIGHT);
			setSynchronizationMode(mRemoteController, RemoteController.POSITION_SYNCHRONIZATION_CHECK);
			return true;
		}
	}

	/**
	 * Disables RemoteController.
	 */
	public void setRemoteControllerDisabled() {
		((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).unregisterRemoteController(mRemoteController);
	}

	/**
	 * Sets up external callback for client update events.
	 *
	 * @param listener External callback.
	 */
	public void setClientUpdateListener(RemoteController.OnClientUpdateListener listener) {
		mExternalClientUpdateListener = listener;
	}

	/**
	 * Sends "next" media key press.
	 */
	public void sendNextKey() {
		sendKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
	}

	/**
	 * Sends "previous" media key press.
	 */
	public void sendPreviousKey() {
		sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
	}

	/**
	 * Sends "pause" media key press, or, if player ignored this button, "play/pause".
	 */
	public void sendPauseKey() {
		if (!sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE)) {
			sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
		}
	}

	/**
	 * Sends "play" button press, or, if player ignored it, "play/pause".
	 */
	public void sendPlayKey() {
		if (!sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY)) {
			sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
		}
	}

	/**
	 * @return Current song position in milliseconds.
	 */
	public long getEstimatedPosition() {
		return mRemoteController.getEstimatedMediaPosition();
	}


	//end of Binder methods.
	//helper methods

	//this method let us avoid the bug in RemoteController
	//which results in Exception when calling RemoteController#setSynchronizationMode(int)
	//doesn't seem to work though
	private void setSynchronizationMode(RemoteController controller, int sync) {
		if ((sync != RemoteController.POSITION_SYNCHRONIZATION_NONE) && (sync != RemoteController.POSITION_SYNCHRONIZATION_CHECK)) {
			throw new IllegalArgumentException("Unknown synchronization mode " + sync);
		}
		Class<?> iRemoteControlDisplayClass;
		try {
			iRemoteControlDisplayClass = Class.forName("android.media.IRemoteControlDisplay");
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException("Class IRemoteControlDisplay doesn't exist, can't access it with reflection");
		}
		Method remoteControlDisplayWantsPlaybackPositionSyncMethod;
		try {
			remoteControlDisplayWantsPlaybackPositionSyncMethod = AudioManager.class.getDeclaredMethod("remoteControlDisplayWantsPlaybackPositionSync", iRemoteControlDisplayClass, boolean.class);
			remoteControlDisplayWantsPlaybackPositionSyncMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Method remoteControlDisplayWantsPlaybackPositionSync() doesn't exist, can't access it with reflection");
		}
		Object rcDisplay;
		Field rcDisplayField;
		try {
			rcDisplayField = RemoteController.class.getDeclaredField("mRcd");
			rcDisplayField.setAccessible(true);
			rcDisplay = rcDisplayField.get(mRemoteController);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Field mRcd doesn't exist, can't access it with reflection");
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Field mRcd can't be accessed - access denied");
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Field mRcd can't be accessed - invalid argument");
		}
		AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		try {
			remoteControlDisplayWantsPlaybackPositionSyncMethod.invoke(am, iRemoteControlDisplayClass.cast(rcDisplay), true);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Method remoteControlDisplayWantsPlaybackPositionSync() invocation failure - access denied");
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Method remoteControlDisplayWantsPlaybackPositionSync() invocation failure - invalid arguments");
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Method remoteControlDisplayWantsPlaybackPositionSync() invocation failure - invalid invocation target");
		}
		try {
			mPendingIntentField = RemoteController.class.getDeclaredField("mClientPendingIntentCurrent");
			mPendingIntentField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			// Do nothing
		}
	}

	private boolean sendKeyEvent(int keyCode) {
		//send "down" and "up" keyevents.
		KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		boolean first = mRemoteController.sendMediaKeyEvent(keyEvent);
		keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
		boolean second = mRemoteController.sendMediaKeyEvent(keyEvent);
		return first && second; //if both  clicks were delivered successfully
	}
	//end of helper methods.

	public Intent getCurrentClientIntent() {
		PendingIntent clientIntent;
		try {
			clientIntent = (PendingIntent) mPendingIntentField.get(mRemoteController);
			if (clientIntent == null) return null;
			String packageName = clientIntent.getCreatorPackage();
			if (packageName == null) return null;
			Intent result = getPackageManager().getLaunchIntentForPackage(packageName);
			if (result == null) return null;
			result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			return result;
		} catch (Exception exception) {
			return null;
		}
	}

	//implementation of RemoteController.OnClientUpdateListener. Does nothing other than calling external callback.
	@Override
	public void onClientChange(boolean arg0) {
		if (mExternalClientUpdateListener != null) {
			mExternalClientUpdateListener.onClientChange(arg0);
		}
	}

	@Override
	public void onClientMetadataUpdate(MetadataEditor arg0) {
		if (mExternalClientUpdateListener != null) {
			mExternalClientUpdateListener.onClientMetadataUpdate(arg0);
		}
	}

	@Override
	public void onClientPlaybackStateUpdate(int arg0) {
		if (mExternalClientUpdateListener != null) {
			mExternalClientUpdateListener.onClientPlaybackStateUpdate(arg0);
		}
	}

	@Override
	public void onClientPlaybackStateUpdate(int arg0, long arg1, long arg2, float arg3) {
		if (mExternalClientUpdateListener != null) {
			mExternalClientUpdateListener.onClientPlaybackStateUpdate(arg0, arg1, arg2, arg3);
		}
	}

	@Override
	public void onClientTransportControlUpdate(int arg0) {
		if (mExternalClientUpdateListener != null) {
			mExternalClientUpdateListener.onClientTransportControlUpdate(arg0);
		}

	}

}