

package com.blueserial;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;

/**
 *I used this class to enable switch from landscape to potrait mode and 
 *vice versa according to orientation of accelerometer 
 */
public class ActivityHelper {
	public static void initialize(Activity activity) {

		/** 
		 *gets a SharedPreferences instance that points to the default file that is used by the preference framework 
		 */
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
																						  
		/**
		 * used to store latest orientation of android device
		 */
		String orientation = prefs.getString("prefOrientation", "Null");
		if ("Landscape".equals(orientation)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if ("Portrait".equals(orientation)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}
	}
}
