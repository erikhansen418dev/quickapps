package com.yoavst.quickapps.desktop;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.yoavst.quickapps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.util.Locale;

import at.markushi.ui.CircleButton;

/**
 * Created by Yoav.
 */
@EFragment(R.layout.desktop_fragment_about)
public class AboutFragment extends Fragment {
	@ViewById(R.id.about)
	TextView about;
	@ColorRes(R.color.primary_color_dark)
	int blueColor;
	@ColorRes(R.color.primary_color)
	int blueLightColor;
	@ViewById(R.id.message)
	CircleButton message;
	@ViewById(R.id.donate)
	CircleButton donation;

	@AfterViews
	void init() {
		if (Locale.getDefault().getLanguage().startsWith("en")) {
			final SpannableString text1 = new SpannableString(getString(R.string.about));
			colorize(setBigger(text1, 1.5f, 0, 17), blueColor, 0, 17);
			bold(colorize(setBigger(text1, 2f, 44, 49), blueColor, 44, 49), 44, 49);
			colorize(setBigger(text1, 1.5f, 50, 67), blueColor, 50, 67);
			colorize(setBigger(text1, 1.5f, 96, 102), blueColor, 96, 102);
			about.setText(text1);
		}
		donation.setImageDrawable(new IconDrawable(getActivity(), Iconify.IconValue.md_payment).sizeDp(32).color(Color.WHITE));
		donation.setOnClickListener(v -> getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/donatetome.php?u=5053440"))));
		message.setImageDrawable(new IconDrawable(getActivity(), Iconify.IconValue.md_messenger).sizeDp(32).color(Color.WHITE));
	}

	@Click(R.id.message)
	void onMessageClicked() {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri
				.fromParts("mailto", "yoav.goop@gmail.com", null));
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				getString(R.string.app_name));
		try {
			getActivity().startActivity(Intent.createChooser(emailIntent, getActivity().getString(R.string.about_mail_chooser)));
		} catch (ActivityNotFoundException exception) {
			Toast.makeText(getActivity(), getActivity().getString(R.string.about_intent_failed),
					Toast.LENGTH_LONG).show();
		}
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

	private SpannableString bold(SpannableString string, int start, int end) {
		string.setSpan(new StyleSpan(Typeface.BOLD), start,
				end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return string;
	}

}
