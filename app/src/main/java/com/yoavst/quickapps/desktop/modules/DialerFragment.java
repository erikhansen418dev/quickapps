package com.yoavst.quickapps.desktop.modules;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.R;
import com.yoavst.quickapps.dialer.CDialerActivity;

import java.util.HashMap;

/**
 * Created by Yoav.
 */
public class DialerFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.desktop_module_dialer, container, false);
		for (int id : new int[]{R.id.digit1, R.id.digit2, R.id.digit3,
				R.id.digit4, R.id.digit5, R.id.digit6, R.id.digit7, R.id.digit8, R.id.digit9}) {
			View digit = v.findViewById(id);
			digit.setOnClickListener(this::onQuickDialClicked);
			digit.setOnLongClickListener(this::onQuickDialLongClicked);
		}
		prefs = new Preferences_(getActivity());
		quickNumbers = new Gson().fromJson(prefs.quickDials().get(), CDialerActivity.QUICK_NUMBERS_TYPE);
		if (quickNumbers == null) quickNumbers = new HashMap<>(10);
		return v;
	}

	Preferences_ prefs;
	HashMap<Integer, Pair<String, String>> quickNumbers;
	int lastNum = -1;
	public static final int PICK_CONTACT_REQUEST = 42;

	void onQuickDialClicked(View view) {
		lastNum = Integer.parseInt((String) view.getTag());
		startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), PICK_CONTACT_REQUEST);
	}

	boolean onQuickDialLongClicked(View view) {
		lastNum = Integer.parseInt((String) view.getTag());
		if (quickNumbers.containsKey(lastNum)) {
			Pair<String, String> number = quickNumbers.get(lastNum);
			Toast.makeText(getActivity(), number.first + " " + number.second, Toast.LENGTH_SHORT).show();
		} else Toast.makeText(getActivity(), R.string.empty_speed_dial, Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_CONTACT_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				Uri contentUri = data.getData();
				String contactId = contentUri.getLastPathSegment();
				Cursor cursor = getActivity().getContentResolver().query(
						ContactsContract.Data.CONTENT_URI,
						new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
						new String[]{contactId},
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC");
				if (cursor.moveToFirst()) {
					final String name = cursor.getString(0);
					Cursor numberCursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
							new String[]{contactId},
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC");
					numberCursor.moveToFirst();
					int count = numberCursor.getCount();
					if (count != 0) {
						if (count == 1)
							putNumber(name, numberCursor.getString(numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
						else {
							final String[] phones = new String[count];
							int index = numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
							for (int i = 0; i < count; i++) {
								phones[i] = numberCursor.getString(index);
								numberCursor.moveToNext();
							}
							new AlertDialog.Builder(getActivity())
									.setTitle(R.string.choose_number)
									.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss()).setItems(phones, (dialog, which) -> putNumber(name, phones[which])).show();
						}
					} else
						Toast.makeText(getActivity(), android.R.string.emptyPhoneNumber, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	void putNumber(String name, String number) {
		quickNumbers.put(lastNum, Pair.create(name, number));
		prefs.quickDials().put(new Gson().toJson(quickNumbers, CDialerActivity.QUICK_NUMBERS_TYPE));
		lastNum = -1;
	}

}
