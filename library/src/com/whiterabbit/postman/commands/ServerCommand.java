package com.whiterabbit.postman.commands;

import com.whiterabbit.postman.utils.Constants;

import android.content.Context;
import android.content.Intent;

public abstract class ServerCommand {
	private String mRequestId;
	
	/**
	 * fills the given intent with values to be serialized and found back in from intent method
	 * @param i
	 */
	protected abstract void fillIntent(Intent i);
	
	/**
	 * takes the needed values from the given intent
	 * @param i
	 */
	protected abstract void fromIntent(Intent i);
	
	
	/* requestid getter and setter */
	public void setRequestId(String reqId){
		mRequestId = reqId;
	}
	
	public String getRequestId(){
		return mRequestId;
	}
	
	/**
	 * Static method to be used to retrieve the message type 
	 * from an intent that contains it
	 * @param i
	 * @return
	 */
	public static String getTypeFromFilledIntent(Intent i){
		return i.getExtras().getString(Constants.MESSAGE_TYPE);
	}
	
	
	/**
	 * Returns the type of the command to be used in CommandFactory to
	 * create the right command
	 * @return
	 */
	public String getType(){
		return getClass().getSimpleName();
	}
	
	/** 
	 * Prepares the intent to be serialized 
	 * @param i
	 */
	public void putToIntent(Intent i){
		i.putExtra(Constants.REQUEST_ID, getRequestId());
		i.putExtra(Constants.MESSAGE_TYPE, getType());
		fillIntent(i);
	}
	
	
	public void fillFromIntent(Intent i){
		String reqID = i.getExtras().getString(Constants.REQUEST_ID);
		setRequestId(reqID);
		fromIntent(i);
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
        c.sendBroadcast(intent);
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
        c.sendBroadcast(intent);
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
        c.sendBroadcast(intent);
	}
}
