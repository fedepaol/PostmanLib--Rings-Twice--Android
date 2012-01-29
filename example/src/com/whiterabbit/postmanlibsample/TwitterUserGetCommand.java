package com.whiterabbit.postmanlibsample;

import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.whiterabbit.postman.commands.JSONRestServerCommand;

public class TwitterUserGetCommand extends JSONRestServerCommand {
	String mUsername;
	private static final String USER = "screen_name";
	private static final String DESCRIPTION = "description";

	public TwitterUserGetCommand(String name){
		super(Action.GET);
		mUsername = name;
		
	}
	
	public TwitterUserGetCommand() {
		super();
	}

	@Override
	protected String getUrl(Action a) {
		return "https://api.twitter.com/1/users/lookup.json?screen_name=" + mUsername;
	}

	@Override
	public void processJSONResult(String result, Context c)  throws JSONException{
		JSONArray jsonResponse = new JSONArray(result);
		JSONObject userJson = jsonResponse.getJSONObject(0);
		String desc = userJson.getString(DESCRIPTION);
		StoreUtils.setProfile(desc, c);
	}

	@Override
	protected void authenticate(HttpRequestBase req) {
		// NO AUTHENTICATION

	}

	@Override
	protected void fillIntent(Intent i) {
		i.putExtra(USER, mUsername);
	}

	@Override
	protected void fromIntent(Intent i) {
		mUsername = i.getExtras().getString(USER);
	}



	@Override
	public String getJSONPayload() {
		// NOT USED BECAUSE ONLY GET METHOD IS IMPLEMENTED
		return null;
	}


}
