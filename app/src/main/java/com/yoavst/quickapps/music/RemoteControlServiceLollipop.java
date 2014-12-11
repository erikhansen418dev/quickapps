package com.yoavst.quickapps.music;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.RemoteController;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RemoteControlServiceLollipop extends AbstractRemoteControlService implements MediaSessionManager.OnActiveSessionsChangedListener {
	ArrayList<MediaController> controllers;
	MediaControllerListener listener;
	@Override
	Intent getCurrentClientIntent() {
		if (controllers != null) {
			if (listener != null) {
				for (MediaController controller : controllers) {
					if (controller.getPlaybackState() != null && controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
						return getPackageManager().getLaunchIntentForPackage(controller.getPackageName());
					}
				}
				for (MediaController controller : controllers) {
					if (controller.getPlaybackState() != null && controller.getPlaybackState().getState() == PlaybackState.STATE_PAUSED) {
						return getPackageManager().getLaunchIntentForPackage(controller.getPackageName());
					}
				}
			}
		}
		return null;
	}

	/**
	 * Enables the RemoteController thus allowing us to receive metadata updates.
	 *
	 * @return true if registered successfully
	 */
	public boolean setRemoteControllerEnabled() {
		try {
			MediaSessionManager manager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
			manager.addOnActiveSessionsChangedListener(this, new ComponentName(this, getClass()));
			onActiveSessionsChanged(manager.getActiveSessions(new ComponentName(this, getClass())));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	void notifyListener() {
		if (listener != null && controllers != null && controllers.size() != 0) {
			for (MediaController controller : controllers) {
				if (controller.getPlaybackState() != null && controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
					listener.onMetadataChanged(controller.getMetadata());
					return;
				}
			}
			for (MediaController controller : controllers) {
				if (controller.getPlaybackState() != null && controller.getPlaybackState().getState() == PlaybackState.STATE_PAUSED) {
					listener.onMetadataChanged(controller.getMetadata());
					listener.onPlaybackStateChanged(controller.getPlaybackState());
					return;
				}
			}
		}
	}

	@Override
	public void onActiveSessionsChanged(List<MediaController> mediaControllers) {
		if (controllers != null && controllers.size() > 0) {
			for (MediaController controller : controllers) {
				controller.unregisterCallback(callback);
			}
		}
		this.controllers = new ArrayList<>(mediaControllers);
		for (MediaController controller : controllers) {
			controller.registerCallback(callback);
		}
		notifyListener();
	}

	MediaController.Callback callback = new MediaController.Callback() {
		@Override
		public void onSessionDestroyed() {
			super.onSessionDestroyed();
			if (listener != null) {
				listener.onSessionDestroyed();
			}
		}

		@Override
		public void onSessionEvent(String event, Bundle extras) {
			super.onSessionEvent(event, extras);
			if (listener != null) {
				listener.onSessionEvent(event, extras);
			}
		}

		@Override
		public void onPlaybackStateChanged(PlaybackState state) {
			super.onPlaybackStateChanged(state);
			if (listener != null) {
				listener.onPlaybackStateChanged(state);
			}
		}

		@Override
		public void onMetadataChanged(MediaMetadata metadata) {
			super.onMetadataChanged(metadata);
			if (listener != null) {
				listener.onMetadataChanged(metadata);
			}
		}

		@Override
		public void onQueueChanged(List<MediaSession.QueueItem> queue) {
			super.onQueueChanged(queue);
			if (listener != null) {
				listener.onQueueChanged(queue);
			}
		}

		@Override
		public void onQueueTitleChanged(CharSequence title) {
			super.onQueueTitleChanged(title);
			if (listener != null) {
				listener.onQueueTitleChanged(title);
			}
		}

		@Override
		public void onExtrasChanged(Bundle extras) {
			super.onExtrasChanged(extras);
			if (listener != null) {
				listener.onExtrasChanged(extras);
			}
		}

		@Override
		public void onAudioInfoChanged(MediaController.PlaybackInfo info) {
			super.onAudioInfoChanged(info);
			if (listener != null) {
				listener.onAudioInfoChanged(info);
			}
		}
	};

	/**
	 * Disables RemoteController.
	 */
	public void setRemoteControllerDisabled() {
		listener = null;
		MediaSessionManager manager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
		manager.removeOnActiveSessionsChangedListener(this);
		if (controllers != null) {
			for (MediaController controller : controllers) {
				controller.unregisterCallback(callback);
			}
		}
	}
	//region KeyEvents

	/**
	 * Sends "next" media key press.
	 */
	public void sendNextKey() {
		if (controllers != null) {
			for (MediaController controller : controllers) {
				if (controller.getPlaybackState() != null && controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
					MediaController.TransportControls controls = controller.getTransportControls();
					controls.skipToNext();
				}
			}
		}
	}

	/**
	 * Sends "previous" media key press.
	 */
	public void sendPreviousKey() {
		if (controllers != null) {
			for (MediaController controller : controllers) {
				if (controller.getPlaybackState() != null && controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
					MediaController.TransportControls controls = controller.getTransportControls();
					controls.skipToPrevious();
				}
			}
		}
	}

	/**
	 * Sends "pause" media key press, or, if player ignored this button, "play/pause".
	 */

	public void sendPauseKey() {
		if (controllers != null) {
			for (MediaController controller : controllers) {
				if (controller.getPlaybackState() != null && controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
					MediaController.TransportControls controls = controller.getTransportControls();
					controls.pause();
				}
			}
		}
	}

	/**
	 * Sends "play" button press, or, if player ignored it, "play/pause".
	 */
	public void sendPlayKey() {
		if (controllers != null) {
			for (MediaController controller : controllers) {
				if (controller.getPlaybackState() != null && controller.getPlaybackState().getState() == PlaybackState.STATE_PAUSED) {
					MediaController.TransportControls controls = controller.getTransportControls();
					controls.play();
					return;
				}
			}
		}
	}
	//endregion

	/**
	 * Sets up external callback for client update events.
	 *
	 * @param listener External callback.
	 */
	public void setClientUpdateListener(MediaControllerListener listener) {
		this.listener = listener;
		notifyListener();
	}


//endregion

public interface MediaControllerListener {
	/**
	 * Override to handle the session being destroyed. The session is no
	 * longer valid after this call and calls to it will be ignored.
	 */
	public void onSessionDestroyed();

	/**
	 * Override to handle custom events sent by the session owner without a
	 * specified interface. Controllers should only handle these for
	 * sessions they own.
	 *
	 * @param event  The event from the session.
	 * @param extras Optional parameters for the event, may be null.
	 */
	public void onSessionEvent(@NonNull String event, @Nullable Bundle extras);

	/**
	 * Override to handle changes in playback state.
	 *
	 * @param state The new playback state of the session
	 */
	public void onPlaybackStateChanged(@NonNull PlaybackState state);

	/**
	 * Override to handle changes to the current metadata.
	 *
	 * @param metadata The current metadata for the session or null if none.
	 * @see MediaMetadata
	 */
	public void onMetadataChanged(@Nullable MediaMetadata metadata);

	/**
	 * Override to handle changes to items in the queue.
	 *
	 * @param queue A list of items in the current play queue. It should
	 *              include the currently playing item as well as previous and
	 *              upcoming items if applicable.
	 * @see MediaSession.QueueItem
	 */
	public void onQueueChanged(@Nullable List<MediaSession.QueueItem> queue);

	/**
	 * Override to handle changes to the queue title.
	 *
	 * @param title The title that should be displayed along with the play queue such as
	 *              "Now Playing". May be null if there is no such title.
	 */
	public void onQueueTitleChanged(@Nullable CharSequence title);

	/**
	 * Override to handle changes to the {@link MediaSession} extras.
	 *
	 * @param extras The extras that can include other information associated with the
	 *               {@link MediaSession}.
	 */
	public void onExtrasChanged(@Nullable Bundle extras);

	/**
	 * Override to handle changes to the audio info.
	 *
	 * @param info The current audio info for this session.
	 */
	public void onAudioInfoChanged(MediaController.PlaybackInfo info);
}
}