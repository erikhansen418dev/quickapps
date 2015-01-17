package com.yoavst.quickapps.calendar;

import android.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by Yoav.
 */
@EFragment(R.layout.calendar_circle_layout)
public class EventsFragment extends Fragment {
	@ViewById
	TextView title;
	@ViewById
	TextView location;
	@ViewById
	TextView date;
	@ViewById(R.id.time_left)
	TextView timeLeft;
	@StringRes(R.string.unknown)
	static String UNKNOWN;
	@FragmentArg
	Event event;
	@Pref
	Preferences_ prefs;

	@AfterViews
	void init() {
		getView().setOnTouchListener((v, e) -> ((QCircleActivity) getActivity()).gestureDetector.onTouchEvent(e));
		CalendarUtil.CalendarResources.init(getActivity());
		title.setText(event.getTitle());
		if (!prefs.showLocation().get() || event.getLocation() == null || event.getLocation().length() == 0)
			location.setVisibility(View.GONE);
		else {
			location.setVisibility(View.VISIBLE);
			location.setText("At " + event.getLocation());
		}
		date.setText(CalendarUtil.getDateFromEvent(event));
		timeLeft.setText(CalendarUtil.getTimeToEvent(event));
	}
}
