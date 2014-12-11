package com.yoavst.quickapps.dialer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

import org.androidannotations.api.BackgroundExecutor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Yoav.
 */
public class CDialerActivity extends QCircleActivity {
	TextView number;
	TextView name;
	int suggestionColor;
	String originalOldText = "";
	String oldName = "";
	Handler handler = new Handler();
	ArrayList<Pair<String, String>> phoneNumbers = new ArrayList<>();
	HashMap<Integer, Pair<String, String>> quickNumbers;
	public static final Type QUICK_NUMBERS_TYPE = new TypeToken<HashMap<Integer, Pair<String, String>>>() {
	}.getType();
	private static String countryRegion;
	private static boolean hasLeadingZero = false;
	PhoneNumberUtil numberUtil = PhoneNumberUtil.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this);
		template.setFullscreenIntent(new Intent().setClassName("com.android.contacts",
				"alias.PeopleFloatingActivity").putExtra("com.lge.app.floating.launchAsFloating", true));
		RelativeLayout main = template.getLayoutById(TemplateTag.CONTENT_MAIN);
		main.addView(LayoutInflater.from(this).inflate(R.layout.dialer_circle_layout, main, false));
		setContentView(template.getView());
		init();
	}

	private void init() {
		number = (TextView) findViewById(R.id.number);
		name = (TextView) findViewById(R.id.name);
		findViewById(R.id.dial).setOnClickListener(v -> dial());
		findViewById(R.id.delete).setOnClickListener(v -> onDelete());
		findViewById(R.id.delete).setOnLongClickListener(v -> {
			deleteAll();
			return true;
		});
		findViewById(R.id.number).setOnClickListener(v -> onClickOnText());
		findViewById(R.id.quick_circle_back_btn).setOnClickListener(v -> finish());
		for (int i : new int[]{R.id.digit0, R.id.digit1, R.id.digit2, R.id.digit3,
				R.id.digit4, R.id.digit5, R.id.digit6, R.id.digit7, R.id.digit8, R.id.digit9}) {
			View v = findViewById(i);
			v.setOnClickListener(this::onNumberClicked);
			v.setOnLongClickListener(this::onNumberLongClicked);
		}
		suggestionColor = getResources().getColor(android.R.color.darker_gray);
		if (countryRegion == null) {
			try {
				countryRegion = numberUtil.getRegionCodeForCountryCode(Integer.parseInt(GetCountryZipCode(this)));
			} catch (Exception e) {
				countryRegion = "001";
			}
			Phonenumber.PhoneNumber number = numberUtil.getExampleNumber(countryRegion);
			if (number == null) hasLeadingZero = false;
			else {
				String formatted = numberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
				hasLeadingZero = !(formatted == null || formatted.length() == 0) && formatted.startsWith("0");
			}
		}
		String quickDials = new Preferences_(this).quickDials().get();
		quickNumbers = new Gson().fromJson(quickDials, QUICK_NUMBERS_TYPE);
		if (quickNumbers == null) quickNumbers = new HashMap<>(0);
		name.setSelected(true);
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
		phones.moveToFirst();
		handleContacts(phones);

	}

	void handleContacts(Cursor cursor) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		while (cursor.moveToNext()) {
			try {
				Phonenumber.PhoneNumber phone = phoneUtil.parse(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
						getResources().getConfiguration().locale.getCountry());
				phoneNumbers.add(Pair.create(
						cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
						(hasLeadingZero ? "0" : "") + phone.getNationalNumber()));
			} catch (NumberParseException e) {
				break;
			}
		}
		closeCursor(cursor);
	}

	void closeCursor(Cursor cursor) {
		runOnUiThread(cursor::close);
	}

	void onClickOnText() {
		SpannableString s = new SpannableString(number.getText());
		ForegroundColorSpan[] spans = s.getSpans(0,
				s.length(),
				ForegroundColorSpan.class);
		for (ForegroundColorSpan span : spans) {
			s.removeSpan(span);
		}
		number.setText(s);
		originalOldText = s.toString();
	}

	void onNumberClicked(View view) {
		removeSuggestion();
		number.append((String) view.getTag());
		updateSuggestion(number.getText().toString());
	}

	boolean onNumberLongClicked(View view) {
		int num = Integer.parseInt((String) view.getTag());
		if (quickNumbers.containsKey(num)) {
			Pair<String, String> contact = quickNumbers.get(num);
			try {
				Phonenumber.PhoneNumber phone = numberUtil.parse(contact.second, countryRegion);
				number.setText((hasLeadingZero ? "0" : "") + phone.getNationalNumber());
				originalOldText = number.getText().toString();
				name.setText(contact.first);
				oldName = contact.first;
			} catch (NumberParseException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	void onDelete() {
		removeSuggestion();
		CharSequence text = number.getText();
		if (text.length() > 0) {
			number.setText(text.subSequence(0, text.length() - 1));
			originalOldText = number.getText().toString();
			oldName = "";
			updateSuggestion(number.getText().toString());
		}
	}

	void deleteAll() {
		number.setText("");
		originalOldText = "";
		name.setText("");

	}

	void updateSuggestion(String text) {
		BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {
			                           @Override
			                           public void execute() {
				                           try {
					                           originalOldText = text;
					                           if (originalOldText.length() >= 2) {
						                           for (Pair<String, String> num : phoneNumbers) {
							                           boolean zero = hasLeadingZero && !originalOldText.startsWith("0");
							                           if (num.second.startsWith((zero ? "0" : "") + originalOldText)) {
								                           setText(num, zero);
								                           return;
							                           }
						                           }
					                           } else handler.post(() -> name.setText(""));
				                           } catch (Throwable e) {
					                           Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
				                           }
			                           }

		                           }
		);
	}

	void setText(Pair<String, String> num, boolean removeZero) {
		runOnUiThread(() -> {
			SpannableString text = new SpannableString(removeZero ? num.second.substring(1) : num.second);
			text.setSpan(new ForegroundColorSpan(suggestionColor), originalOldText.length(),
					text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			number.setText(text);
			if (!name.getText().equals(num.first)) {
				name.setText(num.first);
				oldName = num.first;
			}
		});
	}

	void removeSuggestion() {
		number.setText(originalOldText);
		name.setText("");
		oldName = "";
	}

	void dial() {
		if (number.getText().length() >= 3)
			startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number.getText())));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		phoneNumbers.clear();
		phoneNumbers = null;
	}

	public static String GetCountryZipCode(Context context) {
		String CountryID = "";
		String CountryZipCode = "";
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		CountryID = manager.getSimCountryIso().toUpperCase();
		String[] rl = context.getResources().getStringArray(R.array.CountryCodes);
		for (String aRl : rl) {
			String[] g = aRl.split(",");
			if (g[1].trim().equals(CountryID.trim())) {
				CountryZipCode = g[0];
				break;
			}
		}
		return CountryZipCode;
	}
}
