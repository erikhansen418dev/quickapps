package com.yoavst.quickapps.recorder;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lge.qcircle.template.QCircleDialog;
import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;
import com.yoavst.quickapps.clock.StopwatchManager;

import java.io.File;
import java.util.Timer;

import at.markushi.ui.CircleButton;

/**
 * Created by Yoav.
 */
public class CRecorderActivity extends QCircleActivity {
	TextView time;
	RecordButton recorder;
	CircleButton trash, pause;
	AudioRecorder audioRecorder;
	Drawable pauseDrawable, resumeDrawable;
	Timer timer;
	StopwatchManager.Stopwatch timerTask;
	long timeSinceStart;
	String recordName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this);
		RelativeLayout layout = template.getLayoutById(TemplateTag.CONTENT_MAIN);
		layout.addView(LayoutInflater.from(this).inflate(R.layout.recorder_circle_layout, layout, false));
		template.setBackButton(v -> {
			if (audioRecorder != null && audioRecorder.isRecording()) {
				audioRecorder.pause(new AudioRecorder.OnPauseListener() {
					@Override
					public void onPaused(String activeRecordFileName) {
						try {
							new File(activeRecordFileName).delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onException(Exception e) {
					}
				});
			}
		});
		pauseDrawable = new IconDrawable(this, Iconify.IconValue.md_pause)
				.colorRes(R.color.md_red_500).sizeDp(32);
		resumeDrawable = new IconDrawable(this, Iconify.IconValue.md_play_arrow)
				.colorRes(R.color.md_red_500).sizeDp(32);
		template.setBackButtonTheme(true);
		template.setBackgroundColor(getResources().getColor(R.color.md_red_500), true);
		setContentView(template.getView());
		pause = (CircleButton) findViewById(R.id.pause);
		pause.setImageDrawable(pauseDrawable);
		pause.setOnClickListener(this::onPauseClicked);
		trash = (CircleButton) findViewById(R.id.trash);
		trash.setImageDrawable(new IconDrawable(this, Iconify.IconValue.md_delete)
				.colorRes(R.color.md_red_500).sizeDp(32));
		trash.setOnClickListener(this::onTrashClicked);
		time = (TextView) findViewById(R.id.time);
		recorder = (RecordButton) findViewById(R.id.recorder);
		recorder.setOnClickListener(this::recordOrStop);
		hideHelperButtons();
	}

	@Override
	protected Intent getIntentToShow() {
		pause(() -> audioRecorder = null);
		try {
			return getPackageManager().getLaunchIntentForPackage("com.lge.voicerecorder").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} catch (Exception e) {
			return null;
		}
	}

	void hideHelperButtons() {
		trash.setVisibility(View.GONE);
		pause.setVisibility(View.GONE);
	}

	void showHelperButtons() {
		trash.setVisibility(View.VISIBLE);
		pause.setVisibility(View.VISIBLE);
	}

	void onTrashClicked(View v) {
		if (audioRecorder != null) {
			v.setEnabled(false);
			if (audioRecorder.isRecording())
				audioRecorder.pause(new AudioRecorder.OnPauseListener() {
					@Override
					public void onPaused(String activeRecordFileName) {
						try {
							doDelete(activeRecordFileName);
						} catch (Exception e) {
							e.printStackTrace();
						}
						v.setEnabled(true);
					}

					@Override
					public void onException(Exception e) {
						v.setEnabled(true);

					}
				});
			else {
				doDelete(recordName);
				v.setEnabled(true);
			}
		}
	}

	void doDelete(String filename) {
		new File(filename).delete();
		hideHelperButtons();
		stopCounting();
		recorder.setNotRecording();
		audioRecorder = null;
		Toast toast = Toast.makeText(CRecorderActivity.this, R.string.delete_record, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}

	void recordOrStop(View v) {
		recorder.setEnabled(false);
		if (audioRecorder == null) {
			File folder = new File(Environment.getExternalStorageDirectory(), "VoiceRecorder");
			if (!folder.exists()) folder.mkdir();
			audioRecorder = AudioRecorder.build(this, new File(folder, (recordName = "record_" + System.currentTimeMillis() + ".mp4")).getAbsolutePath());
			startRecording();
			new Handler().postDelayed(() -> recorder.setEnabled(true), 500);
		} else {
			hideHelperButtons();
			if (audioRecorder.isRecording())
				pause(this::showRecordedDialog);
			else showRecordedDialog();
			stopCounting();
			recorder.setNotRecording();
			audioRecorder = null;
		}
	}

	void showRecordedDialog() {
		new QCircleDialog.Builder()
				.setTitle(getString(R.string.voice_recorded))
				.setMode(QCircleDialog.DialogMode.Ok)
				.setText(getString(R.string.successfully_record_audio))
				.setPositiveButtonListener(v1 -> recorder.setEnabled(true))
				.create()
				.show(this, template);
	}

	void pause(Runnable onSuccess) {
		if (audioRecorder != null) {
			audioRecorder.pause(new AudioRecorder.OnPauseListener() {
				@Override
				public void onPaused(String activeRecordFileName) {
					if (onSuccess != null) onSuccess.run();
				}

				@Override
				public void onException(Exception e) {
					Toast toast = Toast.makeText(CRecorderActivity.this, "Error with pause recording", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
					audioRecorder = null;
				}
			});
		}
	}

	void startRecording() {
		audioRecorder.start(new AudioRecorder.OnStartListener() {
			@Override
			public void onStarted() {
				showHelperButtons();
				if (timeSinceStart == 0) startCounting();
				else resumeCounting();
				runOnUiThread(recorder::setRecording);
			}

			@Override
			public void onException(Exception e) {
				Toast toast = Toast.makeText(CRecorderActivity.this, "Error with recording", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
				audioRecorder = null;
			}
		});
	}

	void startCounting() {
		stopCounting();
		timer = new Timer();
		timerTask = new StopwatchManager.Stopwatch() {
			@Override
			protected void runCode() {
				timeSinceStart += 1;
				runOnUiThread(() -> time.setText(wrap((int) (timeSinceStart / 60 / 60)) + ":" + wrap((int) (timeSinceStart / 60 % 60)) + ":" + wrap((int) (timeSinceStart % 60))));
			}
		};
		timer.schedule(timerTask, 0, 1000);
	}

	void pauseCounting() {
		timerTask.isRunning(false);
	}

	void resumeCounting() {
		timerTask.isRunning(true);
	}

	void stopCounting() {
		if (timer != null) timer.cancel();
		timeSinceStart = 0;
		timer = null;
		time.setText(R.string.start_record);
	}

	void onPauseClicked(View v) {
		if (audioRecorder != null) {
			v.setEnabled(false);
			if (audioRecorder.isRecording()) {
				pause.setImageDrawable(resumeDrawable);
				recorder.setPause();
				pauseCounting();
				pause(() -> v.setEnabled(true));
			} else {
				pause.setImageDrawable(pauseDrawable);
				resumeCounting();
				startRecording();
				v.setEnabled(true);
			}
		}
	}

	private String wrap(int num) {
		if (num < 10) return "0" + num;
		else return "" + num;
	}
}
