package com.yoavst.quickapps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/**
 * Created by Yoav.
 */
public class CoverReceiver extends BroadcastReceiver {
	/**
	 * True if is in cover mode, false if regular.
	 */
	private static boolean isCoverInUse = false;

	protected static final int EXTRA_ACCESSORY_COVER_OPENED = 0;
	protected static final int EXTRA_ACCESSORY_COVER_CLOSED = 1;
	protected static final String EXTRA_ACCESSORY_COVER_STATE = "com.lge.intent.extra.ACCESSORY_COVER_STATE";
	protected static final String ACTION_ACCESSORY_COVER_EVENT = "com.lge.android.intent.action.ACCESSORY_COVER_EVENT";
	protected static final int QUICKCOVERSETTINGS_QUICKCIRCLE = 3;
	protected static final int QUICKCOVERSETTINGS_USEQUICKCIRCLE = 1;

	public static boolean isCoverInUse() {
		return isCoverInUse;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d("tag", "onReceive: " + intent.getAction());
		String action = intent.getAction();
		if (action == null) {
			return;
		}
		int quickCaseType = Settings.Global.getInt(context.getContentResolver(),
				"cover_type", 0);
		int quickCircleEnabled = Settings.Global.getInt(context.getContentResolver(),
				"quick_view_enable", 0);
		// Receives a LG QCirle intent for the cover event
		if (ACTION_ACCESSORY_COVER_EVENT.equals(action)
				&& quickCaseType == QUICKCOVERSETTINGS_QUICKCIRCLE
				&& quickCircleEnabled == QUICKCOVERSETTINGS_USEQUICKCIRCLE) {
			// Gets the current state of the cover
			int quickCoverState = intent.getIntExtra(EXTRA_ACCESSORY_COVER_STATE,
					EXTRA_ACCESSORY_COVER_OPENED);
			if (quickCoverState == EXTRA_ACCESSORY_COVER_CLOSED) { // closed
				isCoverInUse = true;
			} else if (quickCoverState == EXTRA_ACCESSORY_COVER_OPENED) { // opened
				isCoverInUse = false;
			}
		}
	}
}
