package com.blueserial;

import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.t27.blueserial.R;

/**
 *class for an activity to show a hierarchy of preferences to the user
 */
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	/**
	 * class is used to initilaization purpose
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityHelper.initialize(this);
		addPreferencesFromResource(R.xml.preferences); // Using this for compatibility with Android 2.2 devices
	}

	/**
	 * Method is called when a shared preference is changed, added, or removed
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);

		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
			ActivityHelper.initialize(this);
		}

		if (pref instanceof EditTextPreference) {
			EditTextPreference editPref = (EditTextPreference) pref;
			pref.setSummary(editPref.getText());
		}
	}

	/**
	 *Method is called anytime when an activity is hidden from view
	 */
	@Override
	protected void onPause() {

		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	/**
	 *onResume() is called whenever you navigate back to the activity from a call or something else
	 */
	@Override
	protected void onResume() {
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		Map<String, ?> keys = PreferenceManager.getDefaultSharedPreferences(this).getAll();

		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			// Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
			Preference pref = findPreference(entry.getKey());
			if (pref != null) {
				pref.setSummary(entry.getValue().toString());
			}
		}

		super.onResume();
	}

}
