package com.yoavst.quickapps.desktop.modules;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yoavst.quickapps.R;
import com.yoavst.quickapps.news.Prefs_;

/**
 * Created by Yoav.
 */
public class NewsFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.desktop_module_news, container, false);
		view.findViewById(R.id.logout_row).setOnClickListener(v -> {
			new Prefs_(getActivity()).clear();
			Toast.makeText(getActivity(), R.string.news_logout_feedly, Toast.LENGTH_SHORT).show();
		});
		return view;
	}

}
