package com.whiterabbit.postman.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import com.whiterabbit.postman.utils.Constants;

public abstract class ServerCommand implements Parcelable{
	private String mRequestId;

	/**
     *  requestid getter and setter
     *  */
	public void setRequestId(String reqId){
		mRequestId = reqId;
	}
	
	public String getRequestId(){
		return mRequestId;
	}
	


	/** 
	 * Prepares the intent to be serialized 
	 * @param i
	 */
	public void fillIntent(Intent i){
		i.putExtra(Constants.REQUEST_ID, getRequestId());
        i.putExtra(Constants.PAYLOAD, this);
	}
	
	
	public void fillFromIntent(Intent i){
		String reqID = i.getExtras().getString(Constants.REQUEST_ID);
		setRequestId(reqID);
	}
	
	public abstract void execute(Context c);
	
	/**
	 * Notifies the sender that the operation completed successfully
	 * @param message
	 * @param c
	 */
	protected void notifyResult(String message,  Context c) {
        Intent intent = new Intent(Constants.SERVER_RESULT);
        intent.putExtra(Constants.MESSAGE_ID, message);
        intent.putExtra(Constants.REQUEST_ID, mRequestId);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }
	
	/**
	 * Notifies the sender that something went wrong
	 * @param message
	 * @param c
	 */
	protected void notifyError(String message,  Context c) {
        Intent intent = new Intent(Constants.SERVER_ERROR);
        intent.putExtra(Constants.MESSAGE_ID, message);
        intent.putExtra(Constants.REQUEST_ID, mRequestId);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }
	
	/**
	 * To be used in any case the application was not able to build a proper message
	 * @param message
	 * @param c
	 */
	public static void notifyUnrecoverableError(Intent i, String message, Context c){
		Intent intent = new Intent(Constants.SERVER_ERROR);
        intent.putExtra(Constants.MESSAGE_ID, message);
		String reqID = i.getExtras().getString(Constants.REQUEST_ID);
        intent.putExtra(Constants.REQUEST_ID, reqID);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
	}

}
