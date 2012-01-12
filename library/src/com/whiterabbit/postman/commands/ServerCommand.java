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
	 * Prepares the intent to be serialized 
	 * @param i
	 */
	public void putToIntent(Intent i){
		i.putExtra(Constants.REQUEST_ID, getRequestId());
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
	void notifyResult(String message,  Context c) {
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
	void notifyError(String message,  Context c) {
        Intent intent = new Intent(Constants.SERVER_ERROR);
        intent.putExtra(Constants.MESSAGE_ID, message);
        intent.putExtra(Constants.REQUEST_ID, mRequestId);
        c.sendBroadcast(intent);
    }
}
