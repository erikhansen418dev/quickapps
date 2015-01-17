package com.yoavst.quickapps.torch;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.lge.qcircle.template.TemplateType;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

/**
 * Created by Yoav.
 */
public class CTorchActivity extends QCircleActivity {
	private static final String TORCH_OFF = "{md-flash-off}";
	private static final String TORCH_ON = "{md-flash-on}";
	private int colorTorchOn;
	private int colorTorchOff;
	private NotificationManager notificationManager;
	private TextView icon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		colorTorchOff = getResources().getColor(R.color.torch_color_off);
		colorTorchOn = getResources().getColor(R.color.torch_color_on);
		template = new QCircleTemplate(this, TemplateType.CIRCLE_EMPTY);
		RelativeLayout main = template.getLayoutById(TemplateTag.CONTENT_MAIN);
		icon = (TextView) getLayoutInflater().inflate(R.layout.torch_layout, main, false);
		main.addView(icon);
		template.setBackButton((view) -> {
			CameraManager.disableTorch();
			CameraManager.destroy();
			notificationManager.cancel(NotificationReceiver.NOTIFICATION_ID);
		});
		setContentView(template.getView());
	}

	@Override
	protected Intent getIntentToShow() {
		return new Intent(this, PhoneActivity.class);
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			CameraManager.init(this);
		} catch (RuntimeException e) {
			Toast toast = Toast.makeText(this, "Error connect camera", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		}
		if (CameraManager.isTorchOn()) {
			showTorchOn();
		} else showTorchOff();
	}

	public void toggleTorch() {
		if (CameraManager.toggleTorch()) {
			showTorchOn();
		} else {
			showTorchOff();
		}
	}

	private void showTorchOn() {
		icon.setText(TORCH_ON);
		icon.setTextColor(colorTorchOn);
		template.setBackButtonTheme(false);
		template.setBackgroundColor(getResources().getColor(R.color.torch_background_color_on), true);
	}

	private void showTorchOff() {
		icon.setText(TORCH_OFF);
		icon.setTextColor(colorTorchOff);
		template.setBackButtonTheme(true);
		template.setBackgroundColor(getResources().getColor(R.color.torch_background_color_off), true);
	}

	@Override
	protected boolean onSingleTapConfirmed() {
		toggleTorch();
		return true;
	}
}
