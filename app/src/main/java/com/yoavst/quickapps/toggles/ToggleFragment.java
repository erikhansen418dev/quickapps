package com.yoavst.quickapps.toggles;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public abstract class ToggleFragment extends Fragment {
	protected ImageButton mToggleIcon;
	protected TextView mToggleText;
	protected TextView mToggleTitle;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.toggles_circle_fragment, container, false);
		mToggleIcon = (ImageButton) relativeLayout.findViewById(R.id.toggle_icon);
		mToggleText = (TextView) relativeLayout.findViewById(R.id.toggle_text);
		mToggleTitle = (TextView) relativeLayout.findViewById(R.id.toggle_title);
		mToggleIcon.setOnClickListener(v -> onToggleButtonClicked());
		relativeLayout.setOnTouchListener((v,e) -> ((QCircleActivity) getActivity()).gestureDetector.onTouchEvent(e));
		return relativeLayout;
	}

	public abstract void onToggleButtonClicked();

	public abstract Intent getIntentForLaunch();
}
