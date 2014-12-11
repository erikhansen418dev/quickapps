package com.yoavst.quickapps.desktop;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.mikepenz.aboutlibraries.Libs;
import com.yoavst.quickapps.AboutLibsView;
import com.yoavst.quickapps.R;

import java.util.Locale;

import at.markushi.ui.CircleButton;

/**
 * Created by Yoav.
 */
public class SourceFragment extends Fragment {
	TextView sourceText;
	AboutLibsView aboutLibsView;
	CircleButton circularButton;
	int blueColor;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.desktop_fragment_source, container, false);
		sourceText = (TextView) layout.findViewById(R.id.source_text);
		aboutLibsView = (AboutLibsView) layout.findViewById(R.id.aboutLibs);
		circularButton = (CircleButton) layout.findViewById(R.id.github);
		blueColor = getResources().getColor(R.color.primary_color);
		init();
		return layout;
	}

	void init() {
		if (Locale.getDefault().getLanguage().startsWith("en")) {
			final SpannableString text1 = new SpannableString(getString(R.string.source_description));
			colorize(setBigger(text1, 1.5f, 15, 32), blueColor, 15, 32);
			colorize(setBigger(text1, 1.5f, 46, 52), blueColor, 46, 52);
			colorize(setBigger(text1, 1.5f, 67, 73), blueColor, 67, 73);
			sourceText.setText(text1);

		}
		circularButton.setImageDrawable(new IconDrawable(getActivity(), Iconify.IconValue.md_public).sizeDp(32).color(Color.WHITE));
		circularButton.setOnClickListener(v -> getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yoavst/quickapps"))));
		Bundle bundle = new Bundle();
		bundle.putBoolean(Libs.BUNDLE_VERSION, true);
		bundle.putBoolean(Libs.BUNDLE_LICENSE, true);
		bundle.putBoolean(Libs.BUNDLE_AUTODETECT,false);
		bundle.putStringArray(Libs.BUNDLE_FIELDS, Libs.toStringArray(R.string.class.getFields()));
		bundle.putStringArray(Libs.BUNDLE_LIBS, getResources().getStringArray(R.array.credits));
		aboutLibsView.configureLibraries(bundle);

	}

	private SpannableString setBigger(SpannableString string, float size, int start, int end) {
		string.setSpan(new RelativeSizeSpan(size), start, end,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return string;
	}

	private SpannableString colorize(SpannableString string, int color, int start, int end) {
		string.setSpan(new ForegroundColorSpan(color), start,
				end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return string;
	}
}
