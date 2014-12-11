package com.yoavst.quickapps.desktop;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.viewpagerindicator.CirclePageIndicator;
import com.yoavst.quickapps.R;
import com.yoavst.quickapps.desktop.modules.ModulesAdapter;

import java.lang.reflect.Field;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by Yoav.
 */
public class ModulesFragment extends Fragment implements ViewPager.OnPageChangeListener {
	TextView title;
	LinearLayout circleLayout;
	ViewPager pager;
	CirclePageIndicator indicator;
	int[] colors;

	//region Bugfix for Android
	private static final Field sChildFragmentManagerField;

	static {
		Field f = null;
		try {
			f = Fragment.class.getDeclaredField("mChildFragmentManager");
			f.setAccessible(true);
		} catch (NoSuchFieldException e) {
			Log.e("ModulesFragment", "Error getting mChildFragmentManager field", e);
		}
		sChildFragmentManagerField = f;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (sChildFragmentManagerField != null) {
			try {
				sChildFragmentManagerField.set(this, null);
			} catch (Exception e) {
				Log.e("ModulesFragment", "Error setting mChildFragmentManager field", e);
			}
		}
	}
	//endregion

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.desktop_fragment_modules, container, false);
		pager = (ViewPager) view.findViewById(R.id.pager);
		indicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		title = (TextView) getActivity().findViewById(R.id.title);
		circleLayout = (LinearLayout) getActivity().findViewById(R.id.circleLayout);
		TypedArray ta = getActivity().getResources().obtainTypedArray(R.array.icons_colors);
		colors = new int[ta.length()];
		for (int i = 0; i < ta.length(); i++) {
			colors[i] = ta.getColor(i, 0);
		}
		ta.recycle();
		pager.setAdapter(new ModulesAdapter(getChildFragmentManager(), getActivity()));
		indicator.setViewPager(pager);
		indicator.setOnPageChangeListener(this);
		pager.post(() -> onPageSelected(0));
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		title.setText(pager.getAdapter().getPageTitle(position));
		((GradientDrawable) circleLayout.getBackground())
				.setColor(colors[position]);
		((MaterialNavigationDrawer) getActivity()).getToolbar().setBackgroundColor(colors[position]);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getActivity().getWindow().setStatusBarColor(colors[position]);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.modules, menu);
		MenuItem item = menu.findItem(R.id.jump);
		item.setOnMenuItemClickListener(item1 -> onJumpPressed());
		item.setIcon(new IconDrawable(getActivity(), Iconify.IconValue.md_open_with)
				.color(Color.WHITE)
				.sizeDp(24));
	}

	boolean onJumpPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.jump_to_setting);
		builder.setItems(R.array.modules, (dialog, which) -> pager.setCurrentItem(which, true));
		builder.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
		builder.show();
		return true;
	}
}
