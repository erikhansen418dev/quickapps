package com.yoavst.quickapps.desktop.modules;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class MusicFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.desktop_module_music, container, false);
		view.findViewById(R.id.listener_row).setOnClickListener(v -> getActivity().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
		return view;
	}
}
