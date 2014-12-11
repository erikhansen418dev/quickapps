package com.yoavst.quickapps.torch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.lge.app.floating.FloatableActivity;
import com.lge.app.floating.FloatingWindow;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.R;

public class PhoneActivity extends FloatableActivity {
	private static final String TORCH_OFF = "{md-flash-off}";
	private static final String TORCH_ON = "{md-flash-on}";
	private static int colorBackgroundOn;
	private static int colorBackgroundOff;
	private static int colorTorchOn;
	private static int colorTorchOff;
	private NotificationManager notificationManager;
	private TextView icon;
	public static Notification notification;

	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.torch_layout);
		icon = (TextView) findViewById(R.id.icon);
		icon.setOnClickListener(v -> toggleTorch());
		colorBackgroundOn = getResources().getColor(R.color.torch_background_color_on);
		colorBackgroundOff = getResources().getColor(R.color.torch_background_color_off);
		colorTorchOn = getResources().getColor(R.color.torch_color_on);
		colorTorchOff = getResources().getColor(R.color.torch_color_off);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		createNotification(this);
		if (new Preferences_(this).torchForceFloating().get())
			switchToFloatingMode();

	}

	@Override
	public void onAttachedToFloatingWindow(FloatingWindow w) {
		super.onAttachedToFloatingWindow(w);
		icon = (TextView) findViewById(R.id.icon);
		icon.setOnClickListener(v -> toggleTorch());
	}

	@Override
	public boolean onDetachedFromFloatingWindow(FloatingWindow w, boolean isReturningToFullScreen) {
		if (!isReturningToFullScreen) {
			if (CameraManager.isTorchOn())
				notificationManager.notify(NotificationReceiver.NOTIFICATION_ID, notification);
			else CameraManager.destroy();
		}
		return super.onDetachedFromFloatingWindow(w, isReturningToFullScreen);
	}

	public static void createNotification(Context context) {
		if (notification == null) {
			Intent intent = new Intent("com.yoavst.notificationtorch");
			PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			notification = new Notification.Builder(context)
					.setContentTitle(context.getString(R.string.torch_is_on))
					.setContentText(context.getString(R.string.touch_to_turn_off))
					.setSmallIcon(R.drawable.ic_noti_torch)
					.setAutoCancel(true)
					.setContentIntent(pIntent).build();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
		}
	}

	void toggleTorch() {
		if (CameraManager.toggleTorch()) {
			showTorchOn();
		} else {
			showTorchOff();
		}
	}

	private void showTorchOn() {
		icon.setText(TORCH_ON);
		icon.setTextColor(colorTorchOn);
		icon.setBackgroundColor(colorBackgroundOn);
	}

	private void showTorchOff() {
		icon.setText(TORCH_OFF);
		icon.setTextColor(colorTorchOff);
		icon.setBackgroundColor(colorBackgroundOff);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		notificationManager.cancel(NotificationReceiver.NOTIFICATION_ID);
		CameraManager.destroy();
	}

	@Override
	public void onBackPressed() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (CameraManager.isTorchOn())
			notificationManager.notify(NotificationReceiver.NOTIFICATION_ID, notification);
		else CameraManager.destroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		CameraManager.init(this);
		notificationManager.cancel(NotificationReceiver.NOTIFICATION_ID);
		if (CameraManager.isTorchOn()) {
			showTorchOn();
			CameraManager.torch();
		} else
			showTorchOff();
	}
}
