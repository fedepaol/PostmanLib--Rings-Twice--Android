package com.whiterabbit.postmanlibsample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


/**
 * Implements a simple storage to be fed in the server interaction and to be shown in the activity
 * Storing in shared prefs is not the best way but it's just for demo purpouses
 * @author fede
 *
 */
public class StoreUtils {
	private final static String PROFILE = "Profile";
	private final static String PREF_NAME = "MyPrefs";
	
	public static String getProfile(Context c)
	{
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences mySharedPreferences = c.getSharedPreferences(PREF_NAME, mode);		
		return mySharedPreferences.getString(PROFILE, "Nones");
	}
	
	public static void setProfile(String profile, Context c)
	{
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences mySharedPreferences = c.getSharedPreferences(PREF_NAME, mode);		
		SharedPreferences.Editor editor = mySharedPreferences.edit();	
		editor.putString(PROFILE, profile);
		editor.commit();

	}
}
