package com.yoavst.quickapps.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * A class that get the updates on the orientation sensor and and rotate
 * the needle according to the change.
 * @author Marco Kirchner
 */
public class Compass implements SensorEventListener {
	ImageView mNeedle;
	private float currentDegree = 0f;
	private SensorManager mSensorManager;

	public Compass(Context context, ImageView needle) {
		mNeedle = needle;
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// get the angle around the z-axis rotated
		float degree = Math.round(sensorEvent.values[0]);
		// create a rotation animation (reverse turn degree degrees)
		RotateAnimation ra = new RotateAnimation(
				currentDegree,
				-degree,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF,
				0.5f);
		// how long the animation will take place
		ra.setDuration(210);
		// set the animation after the end of the reservation status
		ra.setFillAfter(true);
		// Start the animation
		mNeedle.startAnimation(ra);
		currentDegree = -degree;
	}

	public void registerService() {
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
	}

	public void unregisterService() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
	}
}
