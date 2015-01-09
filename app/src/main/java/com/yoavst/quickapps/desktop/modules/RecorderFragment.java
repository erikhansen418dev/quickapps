package com.yoavst.quickapps.desktop.modules;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class RecorderFragment extends Fragment  {
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.desktop_module_recorder, container, false);
	}
}
