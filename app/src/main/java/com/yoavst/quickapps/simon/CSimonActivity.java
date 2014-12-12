package com.yoavst.quickapps.simon;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lge.qcircle.template.QCircleDialog;
import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

import java.util.ArrayList;

/**
 * Created by Yoav.
 */
public class CSimonActivity extends QCircleActivity {
	boolean isGameRunning = false;
	boolean isShowing = false;
	int position = 0;
	Handler handler = new Handler();
	SimonGame game;
	MediaPlayer currentlyPlaying = new MediaPlayer();
	TextView round;
	Preferences_ preferences;
	String roundText;
	int[] ids = new int[]{R.id.red, R.id.blue, R.id.green, R.id.yellow};
	int[] pressedResources = new int[]{R.color.md_red_500, R.color.md_blue_500, R.color.md_green_500, R.color.md_yellow_500};
	int[] regularResources = new int[]{R.color.md_red_700, R.color.md_blue_700, R.color.md_green_700, R.color.md_yellow_700};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this);
		RelativeLayout layout = template.getLayoutById(TemplateTag.CONTENT_MAIN);
		layout.addView(LayoutInflater.from(this).inflate(R.layout.simon_circle_layout, layout, false));
		setContentView(template.getView());
		roundText = getString(R.string.round);
		round = (TextView) findViewById(R.id.round);
		for (int id : ids) {
			findViewById(id).setOnClickListener(v -> {
				if (isGameRunning && !isShowing) {
					playByColor(SimonGame.Color.generateFrom(Integer.parseInt((String) v.getTag())));
					Boolean b = game.press(SimonGame.Color.generateFrom(Integer.parseInt((String) v.getTag())));
					if (b == null) handler.postDelayed(this::newRound, 1000);
					else if (!b) lose();
				}
			});
			findViewById(id).setOnTouchListener((v, event) -> {
				if (isGameRunning && !isShowing) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.setBackgroundColor(getResources().getColor(pressedResources[Integer.parseInt((String) v.getTag())]));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						v.setBackgroundColor(getResources().getColor(regularResources[Integer.parseInt((String) v.getTag())]));
					}
				}
				return false;
			});
		}
		findViewById(R.id.back).setOnClickListener(v -> {
			finish();
			template.unregisterReceiver();
		});
		findViewById(R.id.restart).setOnClickListener(v -> {
			handler.removeCallbacksAndMessages(null);
			for (SimonGame.Color color : SimonGame.Color.values()) {
				fakeUnPress(color);
			}
			Toast toast = Toast.makeText(this, R.string.start_game, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
			handler.postDelayed(() -> {
				game = new SimonGame();
				isGameRunning = true;
				newRound();
			}, 1500);
		});
	}

	void newRound() {
		position = 0;
		ArrayList<SimonGame.Color> colors = game.generateNext();
		round.setText(String.format(roundText, game.getRound()));
		new GameRunnable(colors).run();
	}

	void lose() {
		isGameRunning = false;
		if (preferences == null) preferences = new Preferences_(this);
		if (preferences.highScoreInSimon().get() < game.getRound()) {
			preferences.highScoreInSimon().put(game.getRound());
		}
		QCircleDialog dialog = new QCircleDialog.Builder()
				.setMode(QCircleDialog.DialogMode.Ok)
				.setTitle(getString(R.string.game_over))
				.setText(getString(R.string.your_score, game.getRound()))
				.setPositiveButtonListener(v -> round.setText(""))
				.setImage(getResources().getDrawable(R.drawable.game_over))
				.create();
		dialog.show(this, template);
	}

	void fakePress(SimonGame.Color color) {
		View view = getViewByColor(color);
		switch (color) {
			case Red:
				view.setBackgroundColor(getResources().getColor(pressedResources[0]));
				break;
			case Blue:
				view.setBackgroundColor(getResources().getColor(pressedResources[1]));
				break;
			case Green:
				view.setBackgroundColor(getResources().getColor(pressedResources[2]));
				break;
			case Yellow:
				view.setBackgroundColor(getResources().getColor(pressedResources[3]));
				break;
		}
	}

	void fakeUnPress(SimonGame.Color color) {
		View view = getViewByColor(color);
		switch (color) {
			case Red:
				view.setBackgroundColor(getResources().getColor(regularResources[0]));
				break;
			case Blue:
				view.setBackgroundColor(getResources().getColor(regularResources[1]));
				break;
			case Green:
				view.setBackgroundColor(getResources().getColor(regularResources[2]));
				break;
			case Yellow:
				view.setBackgroundColor(getResources().getColor(regularResources[3]));
				break;
		}
	}

	View getViewByColor(SimonGame.Color color) {
		switch (color) {
			case Red:
				return findViewById(ids[0]);
			case Blue:
				return findViewById(ids[1]);
			case Green:
				return findViewById(ids[2]);
			case Yellow:
				return findViewById(ids[3]);
		}
		return null;
	}

	class GameRunnable implements Runnable {
		ArrayList<SimonGame.Color> colors;

		GameRunnable(ArrayList<SimonGame.Color> colors) {
			this.colors = colors;
		}

		@Override
		public void run() {
			isShowing = true;
			playByColor(colors.get(position));
			fakePress(colors.get(position));
			position++;
			handler.postDelayed(() -> {
				fakeUnPress(colors.get(position - 1));
				if (position < colors.size()) {
					handler.postDelayed(this, 200);
				} else isShowing = false;
			}, 1000);
		}
	}

	void playByColor(SimonGame.Color color) {
		try {
			currentlyPlaying.stop();
		} catch (Exception ignored) {
		}
		currentlyPlaying.release();
		switch (color) {
			case Red:
				currentlyPlaying = MediaPlayer.create(this, R.raw.a);
				currentlyPlaying.setOnCompletionListener(MediaPlayer::release);
				currentlyPlaying.start();
				break;
			case Blue:
				currentlyPlaying = MediaPlayer.create(this, R.raw.b);
				currentlyPlaying.setOnCompletionListener(MediaPlayer::release);
				currentlyPlaying.start();
				break;
			case Green:
				currentlyPlaying = MediaPlayer.create(this, R.raw.c);
				currentlyPlaying.setOnCompletionListener(MediaPlayer::release);
				currentlyPlaying.start();
				break;
			case Yellow:
				currentlyPlaying = MediaPlayer.create(this, R.raw.d);
				currentlyPlaying.setOnCompletionListener(MediaPlayer::release);
				currentlyPlaying.start();
				break;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacksAndMessages(null);
		try {
			currentlyPlaying.stop();
			currentlyPlaying.release();
		} catch (Exception ignored) {}
	}
}
