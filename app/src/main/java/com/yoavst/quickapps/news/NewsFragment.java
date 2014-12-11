package com.yoavst.quickapps.news;

import android.app.Fragment;
import android.widget.TextView;

import com.yoavst.quickapps.R;
import com.yoavst.quickapps.news.types.Entry;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Yoav.
 */
@EFragment(R.layout.news_circle_fragment)
public class NewsFragment extends Fragment {
	@ViewById(R.id.news_time)
	TextView mTime;
	@ViewById(R.id.news_title)
	TextView mTitle;
	@ViewById(R.id.news_source)
	TextView mSource;
	@FragmentArg
	int entryNumber = -1;
	private static final SimpleDateFormat dayFormatter = new SimpleDateFormat("MMM d, HH:mm");

	@AfterViews
	void init() {
		Entry entry =  NewsAdapter.getEntry(entryNumber);
		if (entry != null) {
			mTitle.setText(entry.getTitle());
			mSource.setText(entry.getOrigin().getTitle());
			mTime.setText(dayFormatter.format(new Date(entry.getPublished())));
		}
	}
}
