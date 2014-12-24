package com.yoavst.quickapps.notifications;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.viewpagerindicator.CirclePageIndicator;
import com.yoavst.quickapps.App;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class CNotificationActivity extends QCircleActivity implements ServiceConnection, NotificationService.Callback {
	NotificationService service;
	boolean bound = false;
	public boolean shouldRegister = false;
	ViewPager pager;
	NotificationAdapter mAdapter;
	TextView titleError;
	TextView imageError;
	TextView extraError;
	RelativeLayout errorLayout;
	CirclePageIndicator indicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this);
		template.setTitle(getString(R.string.notification_module_name), Color.WHITE, getResources().getColor(R.color.md_orange_A400));
		template.setTitleTextSize(17);
		template.setBackButton();
		template.setFullscreenIntent(this::getIntentForOpenCase);
		RelativeLayout main = template.getLayoutById(TemplateTag.CONTENT_MAIN);
		main.addView(LayoutInflater.from(this).inflate(R.layout.notification_circle_container_layout, main, false));
		setContentView(template.getView());
		init();
	}

	private void init() {
		titleError = (TextView) findViewById(R.id.title_error);
		imageError = (TextView) findViewById(R.id.image_error);
		extraError = (TextView) findViewById(R.id.extra_error);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
		indicator = (CirclePageIndicator) findViewById(R.id.notification_indicator);
		pager = (ViewPager) findViewById(R.id.notification_pager);
	}

	public void initNotifications() {
		if (bound && !shouldRegister) {
			service.setActiveNotifications();
			mAdapter = new NotificationAdapter(getFragmentManager());
			pager.setAdapter(mAdapter);
			indicator.setViewPager(pager);
			if (NotificationsManager.getCount() == 0)
				showEmpty();
			else {
				hideError();
			}
		}
	}

	public void cancelNotification(StatusBarNotification notification) {
		if (notification != null && bound && !shouldRegister && service != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
				service.cancelNotification(notification.getPackageName(), notification.getTag(), notification.getId());
			else
				service.cancelNotification(notification.getKey());
			Toast toast = Toast.makeText(this, R.string.notification_removed, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		}
	}

	@Override
	public void onNotificationPosted(StatusBarNotification statusBarNotification) {
		onNotificationPostedUi(statusBarNotification);
	}

	protected void onNotificationPostedUi(StatusBarNotification statusBarNotification) {
		runOnUiThread(() -> {
			NotificationsManager.addNotification(statusBarNotification);
			updateAdapter();
			hideError();
			showContent();
		});
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
		onNotificationRemovedUi(statusBarNotification);
	}

	protected void onNotificationRemovedUi(StatusBarNotification statusBarNotification) {
		runOnUiThread(() -> {
			if (!isDestroyed()) {
				NotificationsManager.removeNotification(statusBarNotification);
				updateAdapter();
				if (NotificationsManager.getCount() == 0) {
					showEmpty();
				}
			}
		});
	}

	void updateAdapter() {
		runOnUiThread(() -> {
			if (!isDestroyed()) {
				pager.getAdapter().notifyDataSetChanged();
				indicator.notifyDataSetChanged();
			}
		});
	}

	private NotificationsFragment getActiveFragment() {
		try {
			return (NotificationsFragment) mAdapter.getActiveFragment(pager.getCurrentItem());
		} catch (Exception e) {
			return null;
		}
	}

	protected Intent getIntentForOpenCase() {
		if (shouldRegister)
			return new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
		else {
			try {
				StatusBarNotification statusBarNotification = getActiveFragment().getNotification();
				statusBarNotification.getNotification().contentIntent.send();
			} catch (Exception e) {
				// Do nothing
			}
			return null;
		}
	}

	private void showEmpty() {
		hideContent();
		errorLayout.setVisibility(View.VISIBLE);
		titleError.setText(R.string.notification_empty);
		extraError.setText("");
	}

	protected void hideError() {
		runOnUiThread(() -> errorLayout.setVisibility(View.GONE));
	}

	protected void hideContent() {
		runOnUiThread(() -> {
			pager.setVisibility(View.INVISIBLE);
			indicator.setVisibility(View.INVISIBLE);
		});
	}

	protected void showContent() {
		runOnUiThread(() -> {
			pager.setVisibility(View.VISIBLE);
			indicator.setVisibility(View.VISIBLE);
		});

	}

	//<editor-fold desc="Service bounding">
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		bound = true;
		this.service = ((NotificationService.LocalBinder) service).getService();
		this.service.setCallback(this, this::initNotifications);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		bound = false;
		service = null;
	}

	@Override
	public void onStart() {
		super.onStart();
		bindService(App.createExplicitFromImplicitIntent(this, new Intent(this,
						NotificationService.class).setAction(NotificationService.NOTIFICATION_ACTION)), this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void noPermissionForNotifications() {
		shouldRegister = true;
		errorLayout.setVisibility(View.VISIBLE);
		titleError.setText(R.string.open_the_case);
		extraError.setText(R.string.register_us_please);
		indicator.setVisibility(View.GONE);
	}

	@Override
	public void onDestroy() {
		unbindService(this);
		service = null;
		NotificationsManager.clean();
		super.onDestroy();
	}
	//</editor-fold>
}
