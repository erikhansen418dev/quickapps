package com.yoavst.quickapps.calendar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class CCalendarActivity extends QCircleActivity {
	ViewPager pager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this);
		template.setTitle(getString(R.string.calendar_module_name), Color.WHITE, getResources().getColor(R.color.md_green_500));
		template.setTitleTextSize(17);
		template.setBackButton();
		template.setFullscreenIntent(this::getIntentForOpenCase);
		pager = new ViewPager(this);
		pager.setId(R.id.calendar_pager);
		pager.setAdapter(new EventsAdapter(getFragmentManager(), this));
		template.getLayoutById(TemplateTag.CONTENT_MAIN).addView(pager);
		setContentView(template.getView());
	}

	protected Intent getIntentForOpenCase() {
		try {
			long id = ((EventsFragment_) (getFragmentManager().findFragmentByTag("android:switcher:" + R.id.calendar_pager + ":" + pager.getCurrentItem()))).event.getId();
			return CalendarUtil.launchEventById(id);
		} catch (Exception exception) {
			return null;
		}
	}
}
