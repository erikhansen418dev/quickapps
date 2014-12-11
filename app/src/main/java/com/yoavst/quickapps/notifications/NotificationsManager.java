package com.yoavst.quickapps.notifications;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Yoav.
 */
public class NotificationsManager {
	private static ArrayList<StatusBarNotification> notifications;
	private static final Object LOCK = new Object();
	public static ArrayList<StatusBarNotification> getNotifications() {
		return notifications;
	}

	public static int getCount() { return notifications == null ? 0 : notifications.size(); }

	public static void clean() {
		notifications = null;
	}

	public static void setNotifications(ArrayList<StatusBarNotification> newNotifications) {
		synchronized (LOCK) {
			if (newNotifications == null || newNotifications.size() == 0)
				notifications = new ArrayList<>(0);
			else {
				notifications = new ArrayList<>(newNotifications.size());
				for (int i = newNotifications.size() - 1; i >= 0; i--) {
					Bundle extras = newNotifications.get(i).getNotification().extras;
					String title = extras.getString(Notification.EXTRA_TITLE);
					if (!(title == null || title.length() == 0))
						notifications.add(newNotifications.get(i));
				}
				sort();
			}
		}
	}

	public static void addNotification(StatusBarNotification statusBarNotification) {
		synchronized (LOCK) {
			if (notifications == null) {
				notifications = new ArrayList<>();
			}
			notifications.add(statusBarNotification);
			sort();
		}
	}

	public static void removeNotification(StatusBarNotification statusBarNotification) {
		synchronized (LOCK) {
			if (notifications != null) {
				int index = -1;
				for (int i = 0; i < notifications.size(); i++) {
					if (notificationsEquals(notifications.get(i), statusBarNotification)) {
						index = i;
						break;
					}
				}
				if (index != -1) notifications.remove(index);
			}
		}
	}

	public static boolean notificationsEquals(StatusBarNotification first, StatusBarNotification second) {
		if (first == null) return second == null;
		else if (second == null) return false;
		try {
			return first.getPostTime() == second.getPostTime() &&
					first.getId() == second.getId() &&
					equals(first.getPackageName(),second.getPackageName())
					&& equals(first.getTag(), second.getTag());
		} catch (Exception e) {
			return false;
		}
	}

	private static void sort() {
		if (notifications.size() >= 2)
			Collections.sort(notifications, (lhs, rhs) -> lhs.isClearable() ? (rhs.isClearable() ? 0 : -1) : (rhs.isClearable() ? 0 : -1));
	}

	private static boolean equals(String a, String b) {
		return a == null ? b == null : b != null && a.equals(b);
	}

	private NotificationsManager() {
	}
}
