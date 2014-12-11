package com.yoavst.quickapps.ball;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.nineoldandroids.animation.Animator;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

import java.util.Random;

/**
 * Created by Yoav.
 */
public class CMagic8BallActivity extends QCircleActivity {
	TextView text;
	String[] answers;
	Random random = new Random();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		answers = getResources().getStringArray(R.array.magic_ball);
		template = new QCircleTemplate(this);
		template.setBackButton();
		template.setBackButtonTheme(true);
		template.setBackgroundColor(getResources().getColor(R.color.md_indigo_500), true);
		text = generateTextView();
		RelativeLayout layout = template.getLayoutById(TemplateTag.CONTENT_MAIN);
		layout.setOnClickListener(v -> YoYo.with(Techniques.Shake)
				.duration(1000)
				.withListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						text.setText(R.string.thinking);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						int selected = random.nextInt(answers.length);
						text.setText(answers[selected]);
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}
				})
				.playOn(v));
		layout.addView(text);
		setContentView(template.getView());
	}

	private TextView generateTextView() {
		TextView textView = new TextView(this);
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(35);
		textView.setGravity(Gravity.CENTER);
		textView.setText(R.string.ask_a_question);
		textView.setId(android.R.id.text1);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.padding_start));
		params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.padding_end));
		textView.setLayoutParams(params);
		return textView;
	}
}
