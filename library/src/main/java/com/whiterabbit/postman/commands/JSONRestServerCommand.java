package com.whiterabbit.postman.commands;

import android.content.Context;
import android.util.Log;
import com.whiterabbit.postman.utils.Constants;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

public abstract class JSONRestServerCommand extends RestServerCommand {

	public JSONRestServerCommand(Action a){
		super(a);
	}
	
	public JSONRestServerCommand(){
		
	}
	
	
	/**
	 * To be used only in case of create / update, where the json data will
	 * be sent as the post data
	 * @return
	 */
	abstract public String getJSONPayload();
	
	/**
	 * To be overridden to process a string in json format
	 * @param result the res of the http call
	 * @param context might be useful to store somewhere the result
	 * @throws JSONException
	 */
	abstract public void processJSONResult(String result, Context context) throws JSONException;
	
	@Override
	protected
	void setCallPayload(HttpEntityEnclosingRequestBase call) {
		StringEntity se;
		try {
            if(getJSONPayload() != null){
			    se = new StringEntity(getJSONPayload(), "UTF-8");
			    call.setEntity(se);
            }
		} catch (UnsupportedEncodingException e) {
			this.notifyError(e.getMessage(), getContext());
		}
		call.setHeader("Accept", "application/json");
		call.setHeader("Content-type", "application/json");

	}


	@Override
	protected
	void processHttpResult(String result, Context c) throws ResultParseException{
		try {
			processJSONResult(result, c);
		} catch (JSONException e) {
			Log.e(Constants.LOG_TAG, "Failed to parse json result");
			throw new ResultParseException();
		}
	}



}
