package com.yoavst.quickapps.notifications;

import android.app.Fragment;
import android.app.Notification;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.R;
import com.yoavst.quickapps.calendar.DateUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Yoav.
 */
@EFragment(R.layout.notification_circle_layout)
public class NotificationsFragment extends Fragment {
	@ViewById(R.id.notification_icon)
	ImageView notificationIcon;
	@ViewById(R.id.notification_time)
	TextView notificationTime;
	@ViewById(R.id.notification_title)
	TextView notificationTitle;
	@ViewById(R.id.notification_text)
	TextView notificationText;
	@ViewById(R.id.delete)
	TextView delete;
	@FragmentArg
	StatusBarNotification notification;
	static String today;
	static String yesterday;
	@Pref
	Preferences_ mPrefs;
	private static final SimpleDateFormat dayFormatter = new SimpleDateFormat("MMM d");
	private static final SimpleDateFormat hourFormatter = new SimpleDateFormat("HH:mm");
	private static final SimpleDateFormat hourFormatterAmPm = new SimpleDateFormat("hh:mm a");

	public StatusBarNotification getNotification() {
		return notification;
	}

	@AfterViews
	void init() {
		if (today == null || yesterday == null) {
			today = getString(R.string.today);
			yesterday = getString(R.string.yesterday);
		}
		if (notification != null) {
			delete.setVisibility(notification.isClearable() ? View.VISIBLE : View.GONE);
			Bundle extras = notification.getNotification().extras;
			notificationTitle.setText(extras.getString(Notification.EXTRA_TITLE));
			try {
				notificationIcon.setImageDrawable(getActivity().createPackageContext(notification.getPackageName(), 0).getResources().getDrawable(notification.getNotification().icon));
			} catch (PackageManager.NameNotFoundException | Resources.NotFoundException ignored) {
			}
			if (mPrefs.notificationShowContent().get()) {
				CharSequence preText = extras.getCharSequence(Notification.EXTRA_TEXT);
				String text = preText == null ? null : preText.toString();
				if (text == null || text.length() == 0) {
					CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
					if (lines != null) {
						text = "";
						String newline = System.getProperty("line.separator");
						for (CharSequence line : lines) {
							text += line + newline;
						}
					}
				}
				notificationText.setText(text);
			}
			long time = notification.getPostTime();
			Date date = new Date(time);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(time);
			boolean isAmPm = mPrefs.amPmInNotifications().get();
			if (DateUtils.isToday(date)) {
				notificationTime.setText(today + " " + parseHour(date, isAmPm));
			} else if (DateUtils.isYesterday(calendar)) {
				notificationTime.setText(yesterday + " " + parseHour(date, isAmPm));
			} else {
				notificationTime.setText(dayFormatter.format(date) + ", " + parseHour(date, isAmPm));
			}
		} else delete.setVisibility(View.GONE);
	}

	@Click(R.id.delete)
	void onDeleteMessage() {
		((CNotificationActivity)getActivity()).cancelNotification(notification);
	}

	String parseHour(Date date, boolean isAmPm) {
		return isAmPm ? hourFormatterAmPm.format(date) : hourFormatter.format(date);
	}
}
