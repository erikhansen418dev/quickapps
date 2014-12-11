package com.yoavst.quickapps.clock;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lge.app.floating.FloatableActivity;
import com.lge.app.floating.FloatingWindow;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.res.StringRes;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

@EActivity
public class PhoneActivity extends FloatableActivity {
	TextView time;
	Button start;
	Button pause;
	LinearLayout running;
	@StringRes(R.string.resume)
	static String RESUME;
	@StringRes(R.string.pause)
	static String PAUSE;
	public static final String ACTION_FLOATING_CLOSE = "floating_close";

	String DEFAULT_STOPWATCH = "<big>00:00:00</big><small>.00</small>";
	String DEFAULT_STOPWATCH_NO_MILLIS = "<big>00:00:00</big>";
	Handler handler;
	private static final String TIME_FORMATTING = "<big>{0}:{1}:{2}</big><small>.{3}</small>";
	private static final String TIME_FORMATTING_NO_MILLIS = "<big>{0}:{1}:{2}</big>";
	Runnable callback;
	boolean showMillis = true;

	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TextUtils.equals(ACTION_FLOATING_CLOSE, intent.getAction())) {
				finishFloatingMode();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stopwatch_qslide_layout);
		if (isStartedAsFloating()) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_FLOATING_CLOSE);
			this.registerReceiver(mReceiver, filter);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mReceiver);
	}

	@Override
	public void onAttachedToFloatingWindow(FloatingWindow w) {
		super.onAttachedToFloatingWindow(w);
		if (w != null) {
			FloatingWindow.LayoutParams layoutParams = w.getLayoutParams();
			layoutParams.resizeOption = FloatingWindow.ResizeOption.DISABLED;
			w.updateLayoutParams(layoutParams);
			ImageButton fullscreenButton = (ImageButton) w.findViewWithTag
					(FloatingWindow.Tag.FULLSCREEN_BUTTON);
			if (fullscreenButton != null) {
				((ViewGroup) fullscreenButton.getParent()).removeView(fullscreenButton);
			}
			init();
		}
	}

	@Override
	public boolean onDetachedFromFloatingWindow(FloatingWindow w,
	                                            boolean isReturningToFullScreen) {
		if (StopwatchManager.isRunning()) {
			AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
					.setTitle(R.string.stopwatch_run_on_back)
					.setMessage(R.string.stopwatch_run_on_back_message)
					.setPositiveButton(R.string.yes, (dialog, which) -> StopwatchManager.runOnBackground())
					.setNegativeButton(R.string.no, (dialog, which) -> StopwatchManager.stopTimer()).create();
			alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
			alertDialog.show();
		}
		return false;
	}

	void init() {
		showMillis = new Preferences_(this).stopwatchShowMillis().get();
		time = (TextView) findViewById(R.id.time);
		start = (Button) findViewById(R.id.start);
		pause = (Button) findViewById(R.id.pause);
		running = (LinearLayout) findViewById(R.id.running_layout);
		start.setOnClickListener(v -> startStopwatch());
		pause.setOnClickListener(v -> pauseOrResumeStopwatch());
		findViewById(R.id.stop).setOnClickListener(v -> stopStopwatch());
		time.setText(Html.fromHtml(showMillis ? DEFAULT_STOPWATCH : DEFAULT_STOPWATCH_NO_MILLIS));
		handler = new Handler();
		initCallback();
		if (StopwatchManager.hasOldData()) {
			StopwatchManager.runOnUi(callback);
			setLookRunning();
			setLookForPauseOrResume();
		}
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

	public static String format(int num) {
		return num < 10 ? "0" + num : Integer.toString(num);
	}


	void startStopwatch() {
		showMillis = new Preferences_(this).stopwatchShowMillis().get();
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

}
