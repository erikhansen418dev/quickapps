package com.yoavst.quickapps.toggles.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;

import com.yoavst.quickapps.R;
import com.yoavst.quickapps.toggles.CTogglesActivity;
import com.yoavst.quickapps.toggles.ToggleFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.StringRes;

/**
 * Created by Yoav.
 */
@EFragment
public class SoundFragment extends ToggleFragment {
	@StringRes(R.string.sound_sound)
	String SOUND;
	@StringRes(R.string.sound_silent)
	String SILENT;
	@StringRes(R.string.sound_vibrate)
	String VIBRATE;
	@SystemService
	AudioManager mAudioManager;
	BroadcastReceiver mRingerReceiver;
	Resources mSystemUiResources;
	// resources id of system ui stuff
	static int soundIcon = -1;
	static int vibrateIcon = -1;
	static int silentIcon = -1;

	@AfterViews
	void init() {
		mToggleTitle.setText(SOUND);
		mSystemUiResources = ((CTogglesActivity) getActivity()).getSystemUiResource();
		if (mRingerReceiver == null) {
			mRingerReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					setToggleData();
				}
			};
		}
		if (soundIcon == -1 || vibrateIcon == -1 || silentIcon == -1) {
			soundIcon = mSystemUiResources.getIdentifier("indi_noti_sound_on", "drawable", "com.android.systemui");
			vibrateIcon = mSystemUiResources.getIdentifier("indi_noti_sound_vib_on", "drawable", "com.android.systemui");
			silentIcon = mSystemUiResources.getIdentifier("indi_noti_silent_on", "drawable", "com.android.systemui");
		}
		IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
		getActivity().registerReceiver(mRingerReceiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(mRingerReceiver);
		} catch (Exception ignored) {
			// Do nothing - receiver not registered
		}
	}

	public void setToggleData() {
		switch (mAudioManager.getRingerMode()) {
			case AudioManager.RINGER_MODE_NORMAL:
				mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(soundIcon));
				mToggleText.setText(SOUND);
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(vibrateIcon));
				mToggleText.setText(VIBRATE);
				break;
			case AudioManager.RINGER_MODE_SILENT:
				mToggleIcon.setImageDrawable(mSystemUiResources.getDrawable(silentIcon));
				mToggleText.setText(SILENT);
				break;
		}
	}

	@Override
	public void onToggleButtonClicked() {
		switch (mAudioManager.getRingerMode()) {
			case AudioManager.RINGER_MODE_NORMAL:
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				else mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				break;
			case AudioManager.RINGER_MODE_SILENT:
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				break;
		}
	}

	@Override
	public Intent getIntentForLaunch() {
		return new Intent(Settings.ACTION_SOUND_SETTINGS);
	}
}
