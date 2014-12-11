package com.yoavst.quickapps.clock;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yoav.
 */
public class CStopwatchActivity extends QCircleActivity {
	private static final String DEFAULT_STOPWATCH = "<big>00:00:00</big><small>.00</small>";
	private static final String DEFAULT_STOPWATCH_NO_MILLIS = "<big>00:00:00</big>";
	private static final String TIME_FORMATTING = "<big>{0}:{1}:{2}</big><small>.{3}</small>";
	private static final String TIME_FORMATTING_NO_MILLIS = "<big>{0}:{1}:{2}</big>";
	private Handler handler;
	private boolean showMillis;
	TextView time;
	Button start;
	String RESUME;
	String PAUSE;
	LinearLayout running;
	Runnable callback;
	Button pause;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showMillis = new Preferences_(this).stopwatchShowMillis().get();
		RESUME = getString(R.string.resume);
		PAUSE = getString(R.string.pause);
		template = new QCircleTemplate(this);
		template.setBackButton();
		template.setTitle(getString(R.string.clock_module_name), Color.WHITE, getResources().getColor(R.color.clock_theme_color));
		template.setTitleTextSize(17);
		template.setFullscreenIntent(PhoneActivity_.intent(this).get().putExtra("com.lge.app.floating.launchAsFloating", true));
		RelativeLayout main = template.getLayoutById(TemplateTag.CONTENT_MAIN);
		RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.stopwatch_circle_layout, main, false);
		main.addView(relativeLayout);
		time = (TextView) relativeLayout.findViewById(R.id.time);
		start = (Button) relativeLayout.findViewById(R.id.start);
		start.setOnClickListener(v -> startStopwatch());
		pause = (Button) relativeLayout.findViewById(R.id.pause);
		pause.setOnClickListener(v -> pauseOrResumeStopwatch());
		running = (LinearLayout) relativeLayout.findViewById(R.id.running_layout);
		relativeLayout.findViewById(R.id.stop).setOnClickListener(v -> stopStopwatch());
				time.setText(Html.fromHtml(showMillis ? DEFAULT_STOPWATCH : DEFAULT_STOPWATCH_NO_MILLIS));
		setContentView(template.getView());
		handler = new Handler();
		initCallback();
		if (StopwatchManager.hasOldData()) {
			StopwatchManager.runOnUi(callback);
			setLookRunning();
			setLookForPauseOrResume();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		StopwatchManager.runOnBackground();
	}

	void initCallback() {
		callback = () -> handler.post(() -> {
			long millis = StopwatchManager.getMillis();
			// Updating the UI
			int num = (int) (millis % 1000 / 10);
			if (showMillis)
				time.setText(Html.fromHtml(MessageFormat.format(TIME_FORMATTING, format((int) TimeUnit.MILLISECONDS.toHours(millis)),
						format((int) TimeUnit.MILLISECONDS.toMinutes(millis)), format((int) TimeUnit.MILLISECONDS.toSeconds(millis)), format(num))));
			else
				time.setText(Html.fromHtml(MessageFormat.format(TIME_FORMATTING_NO_MILLIS, format((int) TimeUnit.MILLISECONDS.toHours(millis)),
						format((int) TimeUnit.MILLISECONDS.toMinutes(millis)), format((int) TimeUnit.MILLISECONDS.toSeconds(millis)))));
		});
	}

	void startStopwatch() {
		setLookRunning();
		StopwatchManager.startTimer(10, callback);
	}

	void setLookRunning() {
		start.setVisibility(View.GONE);
		running.setVisibility(View.VISIBLE);
		pause.setText(PAUSE);
	}

	void setLookForPauseOrResume() {
		if (StopwatchManager.isRunning())
			pause.setText(PAUSE);
		else pause.setText(RESUME);
	}

		void stopStopwatch() {
		StopwatchManager.stopTimer();
		handler.postDelayed(() -> {
			running.setVisibility(View.GONE);
			start.setVisibility(View.VISIBLE);
			time.setText(Html.fromHtml(DEFAULT_STOPWATCH));
		}, 100);
	}

	void pauseOrResumeStopwatch() {
		if (StopwatchManager.isRunning()) StopwatchManager.PauseTimer();
		else StopwatchManager.ResumeTimer();
		setLookForPauseOrResume();
	}


	public static String format(int num) {
		return num < 10 ? "0" + num : Integer.toString(num);
	}
}
