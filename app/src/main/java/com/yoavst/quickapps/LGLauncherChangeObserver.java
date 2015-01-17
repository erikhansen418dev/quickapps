package com.yoavst.quickapps;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.yoavst.quickapps.launcher.CLauncherActivity;

/**
 * Created by Yoav.
 */
public class LGLauncherChangeObserver extends ContentObserver {
		public LGLauncherChangeObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			this.onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			Context context = App.instance;
			if (!new Preferences_(context).showAppsThatInLg().get()) {
				CLauncherActivity.updateComponents(context);
			}
			CLauncherActivity.defaultItems = CLauncherActivity.initDefaultIcons(context);
		}

}
