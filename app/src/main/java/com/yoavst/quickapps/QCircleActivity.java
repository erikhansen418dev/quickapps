package com.yoavst.quickapps;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lge.qcircle.template.QCircleDialog;
import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.QCircleTitle;
import com.lge.qcircle.template.TemplateTag;
import com.yoavst.quickapps.desktop.LaunchAdminActivity;

import java.lang.reflect.Field;

/**
 * Created by Yoav.
 */
public abstract class QCircleActivity extends Activity {
	protected QCircleTemplate template;
	protected ComponentName deviceAdminReceiverComponentName;
	protected Preferences_ preferences;
	public GestureDetector gestureDetector;
	boolean shouldShowDeviceAdminDialog = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = new Preferences_(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		deviceAdminReceiverComponentName = new ComponentName(this, AdminListener.class);
		View circle = findViewById(R.id.circle);
		if (circle != null && new Preferences_(this).g2Mode().getOr(false))
			circle.setVisibility(View.GONE);
		try {
			template.registerIntentReceiver();
		} catch (Exception ignored) {
		}
		gestureDetector = new GestureDetector(this, new SimpleOnGestureListenerWithDoubleTapHandler());
		template.getLayoutById(TemplateTag.CONTENT_MAIN).setOnTouchListener((view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent));
		try {
			Field title = getField(QCircleTemplate.class, "mTitle");
			title.setAccessible(true);
			QCircleTitle qCircleTitle = ((QCircleTitle) title.get(template));
			if (qCircleTitle != null) qCircleTitle.getView().setOnTouchListener((view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		template.setFullscreenIntent(this::getIntentToLaunch);

	}

	@Override
	protected void onPause() {
		try {
			super.onPause();
			template.unregisterReceiver();
		} catch (Exception ignored) {
		}
	}

	public class SimpleOnGestureListenerWithDoubleTapHandler extends GestureDetector.SimpleOnGestureListener {
		private final String TAG = SimpleOnGestureListenerWithDoubleTapHandler.class.getSimpleName();

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			return QCircleActivity.this.onSingleTapConfirmed();
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.d(TAG, "onDoubleTap");
			DevicePolicyManager devicePolicyManager =
					(DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
			if (devicePolicyManager.isAdminActive(deviceAdminReceiverComponentName)) {
				devicePolicyManager.lockNow();
				finish();
			} else {
				requirePermissionForLockTheScreen();
			}
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}
	}

	void requirePermissionForLockTheScreen() {
		if (preferences.showDoubleTapDialog().get()) {
			shouldShowDeviceAdminDialog = true;
			QCircleDialog.Builder builder = new QCircleDialog.Builder()
					.setMode(QCircleDialog.DialogMode.YesNo)
					.setTitle(getString(R.string.missing_permissions))
					.setText(getString(R.string.error_no_permission_for_lock_the_screen));
			builder.setPositiveButtonListener(v -> shouldShowDeviceAdminDialog = false).setPositiveButtonText(getString(android.R.string.ok));
			builder.setNegativeButtonListener(v -> {
				preferences.showDoubleTapDialog().put(false);
				shouldShowDeviceAdminDialog = false;
			}).setNegativeButtonText(getString(R.string.dont_show_again));
			builder.create().show(this, template);
			Button negative = ((Button)findViewById(R.id.negative));
			ViewGroup.LayoutParams params= ((View)negative.getParent()).getLayoutParams();
			params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
			((View)negative.getParent()).setLayoutParams(params);
			negative.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
			((Button)findViewById(R.id.positive)).setGravity(Gravity.CENTER);
		}
	}

	Intent getIntentToLaunch() {
		if (shouldShowDeviceAdminDialog) {
			return new Intent(this, LaunchAdminActivity.class);
		} else return getIntentToShow();
	}

	protected abstract Intent getIntentToShow();

	protected boolean onSingleTapConfirmed() {
		return false;
	}

	private static Field getField(Class clazz, String fieldName)
			throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}
}

