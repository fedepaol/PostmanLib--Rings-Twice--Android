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
	private final static String LATEST_TWEET = "LatestTweet";
	private final static String PREF_NAME = "MyPrefs";
    private final static String LINKEDIN_HEADLINE = "LinkedinHeadline";
    private final static String FB_NAME = "FbName";
    private final static String FB_LINK = "FbLink";

    public static String getFBName(Context c)
    {
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences mySharedPreferences = c.getSharedPreferences(PREF_NAME, mode);
        return mySharedPreferences.getString(FB_NAME, "Nones");
    }

	public static String getFbLink(Context c)
	{
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences mySharedPreferences = c.getSharedPreferences(PREF_NAME, mode);
		return mySharedPreferences.getString(FB_LINK, "Nones");
	}

	public static void setFbProperties(String name, String link, Context c)
	{
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences mySharedPreferences = c.getSharedPreferences(PREF_NAME, mode);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putString(FB_NAME, name);
        editor.putString(FB_LINK, link);
		editor.commit();

	}


	public static String getLatestTweet(Context c)
	{
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences mySharedPreferences = c.getSharedPreferences(PREF_NAME, mode);		
		return mySharedPreferences.getString(LATEST_TWEET, "Nones");
	}
	
	public static void setLatestTweet(String latest, Context c)
	{
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences mySharedPreferences = c.getSharedPreferences(PREF_NAME, mode);		
		SharedPreferences.Editor editor = mySharedPreferences.edit();	
		editor.putString(LATEST_TWEET, latest);
		editor.commit();

	}

 	public static void setLinkedinUserDetails(String latest, Context c)
	{
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences mySharedPreferences = c.getSharedPreferences(PREF_NAME, mode);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putString(LINKEDIN_HEADLINE, latest);
		editor.commit();

	}

    public static String getLinkedinUserDetails(Context c)
    {
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences mySharedPreferences = c.getSharedPreferences(PREF_NAME, mode);
        return mySharedPreferences.getString(LINKEDIN_HEADLINE, "Nones");
    }


}
