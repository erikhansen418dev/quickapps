package com.yoavst.quickapps.music;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lge.qcircle.template.QCircleBackButton;
import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.yoavst.quickapps.App;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yoav.
 */
public class CMusicActivity extends QCircleActivity {
	private TextView playPauseButton;
	private ProgressBar progressBar;
	private TextView artistText;
	private TextView titleText;
	private AbstractRemoteControlService mRCService;
	private boolean bound = false;
	private boolean isPlaying = false;
	protected long songDuration = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this);
		template.setBackButton();
		// Semi transparent back button
		template.setBackButtonTheme(true);
		template.setBackgroundColor(Color.WHITE, true);
		template.setFullscreenIntent(() -> {
			if (bound && mRCService != null) return mRCService.getCurrentClientIntent();
			return null;
		});
		RelativeLayout contentParent = (RelativeLayout) template.getLayoutById(TemplateTag.CONTENT).getParent();
		LinearLayout layoutForButtons = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.music_circle_button_layout, contentParent, false);
		playPauseButton = (TextView) layoutForButtons.findViewById(R.id.pause_start);
		TextView backButton = (TextView) layoutForButtons.findViewById(R.id.back);
		TextView nextButton = (TextView) layoutForButtons.findViewById(R.id.next);
		playPauseButton.setOnTouchListener(this::onPlusTouched);
		backButton.setOnTouchListener(this::onPlusTouched);
		nextButton.setOnTouchListener(this::onPlusTouched);
		progressBar = (ProgressBar) layoutForButtons.findViewById(R.id.music_progress);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				getResources().getDimensionPixelSize(R.dimen.control_buttons_height));
		params.addRule(RelativeLayout.ABOVE, QCircleBackButton.getId());
		layoutForButtons.setLayoutParams(params);
		contentParent.addView(layoutForButtons);
		RelativeLayout main = template.getLayoutById(TemplateTag.CONTENT);
		FrameLayout headerLayout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.music_circle_header_layout, main, false);
		artistText = (TextView) headerLayout.findViewById(R.id.artist_text);
		titleText = (TextView) headerLayout.findViewById(R.id.title_text);
		titleText.setSelected(true);
		artistText.setSelected(true);
		headerLayout.findViewById(R.id.volume_control).setOnClickListener(v -> ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).adjustStreamVolume(3, 0, 1));
		main.addView(headerLayout);
		setContentView(template.getView());
		playPauseButton.setOnClickListener(v -> {
			if (bound) {
				if (isPlaying)
					mRCService.sendPauseKey();
				else mRCService.sendPlayKey();
			}
		});
		backButton.setOnClickListener(v -> {
			if (bound) {
				if (!isPlaying) {
					mRCService.sendPlayKey();
					new Handler().postDelayed(mRCService::sendPreviousKey, 500);
				} else mRCService.sendPreviousKey();

			}
		});
		nextButton.setOnClickListener(v -> {
			if (bound) {
				if (!isPlaying) {
					mRCService.sendPlayKey();
					new Handler().postDelayed(mRCService::sendNextKey, 500);
				} else mRCService.sendNextKey();
			}
		});
	}

	boolean onPlusTouched(View view, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			((TextView) view).setTextColor(getResources().getColor(R.color.md_pink_500));
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			((TextView) view).setTextColor(Color.WHITE);
		}
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();
		Intent intent;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			intent = new Intent("com.yoavst.quickmusic.BIND_RC_CONTROL_SERVICE");
		else
			intent = App.createExplicitFromImplicitIntent(this, new Intent("com.yoavst.quickmusic.BIND_RC_CONTROL_SERVICE_LOLLIPOP"));
		try {
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		} catch (RuntimeException e) {
			e.printStackTrace();
			showUnregistered();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (bound) {
			mRCService.setRemoteControllerDisabled();
		}
		unbindService(serviceConnection);
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			//Getting the binder and activating RemoteController instantly
				AbstractRemoteControlService.RCBinder binder = (AbstractRemoteControlService.RCBinder) service;
				mRCService = binder.getService();

			if (!mRCService.setRemoteControllerEnabled()) {
				// Not registered on the settings
				showUnregistered();
			}
			if (mRCService instanceof RemoteControlService)
				((RemoteControlService) mRCService).setClientUpdateListener(listener);
			else
				((RemoteControlServiceLollipop) mRCService).setClientUpdateListener(new RemoteControlServiceLollipop.MediaControllerListener() {
					@Override
					public void onSessionDestroyed() {
						listener.onClientChange(true);
					}

					@Override
					public void onSessionEvent(@NonNull String event, @Nullable Bundle extras) {
					}

					@TargetApi(Build.VERSION_CODES.LOLLIPOP)
					@Override
					public void onPlaybackStateChanged(@NonNull PlaybackState state) {
						listener.onClientPlaybackStateUpdate(state.getState() == PlaybackState.STATE_PLAYING ? RemoteControlClient.PLAYSTATE_PLAYING : -1);
					}

					@TargetApi(Build.VERSION_CODES.LOLLIPOP)
					@Override
					public void onMetadataChanged(@Nullable MediaMetadata metadata) {
						if (metadata != null) {
							playPauseButton.setText("{md-pause}");
							isPlaying = true;
							String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
							if (artist == null)
								artist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
							if (artist == null) artist = getString(R.string.unknown);
							String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
							songDuration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
							artistText.setText(artist);
							titleText.setText(title);
							Bitmap bitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
							if (bitmap == null)
								bitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
							if (bitmap != null) {
								template.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap), false);
							} else {
								template.setBackgroundColor(Color.WHITE, false);
							}
						} else {
							artistText.setText(R.string.unknown);
							titleText.setText(R.string.unknown);
							isPlaying = false;
							playPauseButton.setText("{md-play-arrow}");
							template.setBackgroundColor(Color.WHITE, false);
						}
					}

					@Override
					public void onQueueChanged(@Nullable List<MediaSession.QueueItem> queue) {
					}

					@Override
					public void onQueueTitleChanged(@Nullable CharSequence title) {
					}

					@Override
					public void onExtrasChanged(@Nullable Bundle extras) {
					}

					@Override
					public void onAudioInfoChanged(MediaController.PlaybackInfo info) {
					}
				});
			bound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
		}
	};

	void showUnregistered() {
		template.setFullscreenIntent(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
		artistText.setText(R.string.register_us_please);
		titleText.setText(R.string.open_the_case);
	}

	RemoteController.OnClientUpdateListener listener = new RemoteController.OnClientUpdateListener() {
		@Override
		public void onClientChange(boolean clearing) {
			if (clearing) {
				template.setBackgroundColor(Color.WHITE, false);
				artistText.setText(R.string.unknown);
				titleText.setText(R.string.unknown);
			}
		}

		@Override
		public void onClientPlaybackStateUpdate(int state) {
			switch (state) {
				case RemoteControlClient.PLAYSTATE_PLAYING:
					isPlaying = true;
					playPauseButton.setText("{md-pause}");
					break;
				default:
					isPlaying = false;
					playPauseButton.setText("{md-play-arrow}");
					break;
			}
		}

		@Override
		public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
			progressBar.setProgress((int) TimeUnit.MILLISECONDS.toSeconds(currentPosMs));
			switch (state) {
				case RemoteControlClient.PLAYSTATE_PLAYING:
					isPlaying = true;
					playPauseButton.setText("{md-pause}");
					break;
				default:
					isPlaying = false;
					playPauseButton.setText("{md-play-arrow}");
					break;
			}
		}

		@Override
		public void onClientTransportControlUpdate(int transportControlFlags) {
		}

		@Override
		public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
			//some players write artist name to METADATA_KEY_ALBUMARTIST instead of METADATA_KEY_ARTIST, so we should double-check it
			String artist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
					metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, getString(R.string.unknown)));
			String title = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, getString(R.string.unknown));
			songDuration = metadataEditor.getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, 1);
			Log.e("Tag", artist + " " + title);
			artistText.setText(artist);
			titleText.setText(title);
			Bitmap bitmap = metadataEditor.getBitmap(RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, null);
			if (bitmap != null) {
				template.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap), false);
			} else {
				template.setBackgroundColor(Color.WHITE, false);
			}
		}
	};
}
