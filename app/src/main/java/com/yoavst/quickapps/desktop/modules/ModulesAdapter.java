package com.yoavst.quickapps.desktop.modules;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class ModulesAdapter extends FragmentPagerAdapter {
	private final String[] TITLES;
	public ModulesAdapter(FragmentManager fm, Context context) {
		super(fm);
		TITLES = context.getApplicationContext().getResources().getStringArray(R.array.modules);
	}

	@Override
	public Fragment getItem(int i) {
		Fragment fragment;
		switch (i) {
			default:
			case 0:
				fragment = new TorchFragment();
				break;
			case 1:
				fragment = new MusicFragment();
				break;
			case 2:
				fragment = new CalendarFragment();
				break;
			case 3:
				fragment = new NotificationsFragment();
				break;
			case 4:
				fragment = new TogglesFragment();
				break;
			case 5:
				fragment = new LauncherFragment();
				break;
			case 6:
				fragment = new StopwatchFragment();
				break;
			case 7:
				fragment = new CalculatorFragment();
				break;
            case 8:
                fragment = new CompassFragment();
                break;
			case 9:
				fragment = new NewsFragment();
				break;
			case 10:
				fragment = new DialerFragment();
				break;
			case 11:
				fragment = new CompassFragment();
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return TITLES.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return TITLES[position];
	}
}
