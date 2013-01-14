package com.whiterabbit.postman;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.SendingCommandException;
import com.whiterabbit.postman.commands.ServerCommand;
import com.whiterabbit.postman.utils.Constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerInteractionHelper {
	BroadcastReceiver mReceiver;
	private static ServerInteractionHelper mInstance;
	private ServerInteractionResponseInterface mListener;
    private IntentFilter                  		mFilter;
    private IntentFilter                  		mErrorFilter;
    private Map<String, Boolean>	mPendingRequests;   // TODO Sparse array. Change request id to long

	
	
	
	private ServerInteractionHelper(){
		mReceiver = new ServiceResultReceiver();
		mPendingRequests = Collections.synchronizedMap(new HashMap<String, Boolean>());
		mFilter = new IntentFilter(Constants.SERVER_RESULT);
		mErrorFilter = new IntentFilter(Constants.SERVER_ERROR);
	}

	/**
     * Singleton factory method
     *
     * @return
     */
    static public synchronized ServerInteractionHelper getInstance() {
        if (mInstance == null) {
            mInstance = new ServerInteractionHelper();
            return mInstance;
        } else {
            return mInstance;
        }
    }
    
    
    /**
     * Broadcastreceiver that handles the broadcasts sent by the service once the message was processed
     * @author fede
     *
     */
    private class ServiceResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.SERVER_RESULT)) {
                Bundle extras = intent.getExtras();
                String requestId = extras.getString(Constants.REQUEST_ID);
                String message = extras.getString(Constants.MESSAGE_ID);
                if (mListener != null) {
                    mListener.onServerResult(message, requestId);
                }
                requestDone(requestId);
            }
            if (action.equals(Constants.SERVER_ERROR)) {
                Bundle extras = intent.getExtras();
                String requestId = extras.getString(Constants.REQUEST_ID);
                String message = extras.getString(Constants.MESSAGE_ID);
                if (mListener != null) {
                    mListener.onServerError(message, requestId);
                }
                requestDone(requestId);
            }
        }
    }


    /**
     * Registers the given listener as to be
     * notified. To be called into onResume method of the activity
     *
     * @param listener
     * @param c
     */
    public void registerEventListener(ServerInteractionResponseInterface listener, Context c) {
        mListener = listener;// TODO weak reference
        c.registerReceiver(mReceiver, mFilter);
        c.registerReceiver(mReceiver, mErrorFilter);
    }

    /**
     * Unregisters the broadcast receiver
     *
     * @param c
     */
    public void unregisterEventListener(ServerInteractionResponseInterface listener, Context c) {
        mListener = null;
        c.unregisterReceiver(mReceiver);
    }

    /**
     * Tells if the request is already in progress
     *
     * @param requestId
     * @return
     */
    public boolean isRequestAlreadyPending(String requestId) {
    	Boolean pending = mPendingRequests.get(requestId);
    	if(pending == null){
    		return false;
    	}else{
    		return pending;
    	}
    }

    /**
     * Sets the current request as in progress
     *
     * @param requestId
     */
    private void setRequestPending(String requestId) {
    	mPendingRequests.put(requestId, true);
    }

    /**
     * Remove request from pending
     *
     * @param requestId
     */
    private void requestDone(String requestId) {
    	mPendingRequests.remove(requestId);
    }
    
    
    /**
     * Sends the given command to the server
     *
     * @param c
     * @param msg
     *            message to send
     * @param requestId
     *            an id associated to the request. Will be returned along with
     *            the result
     */
    public void sendCommand(Context c, ServerCommand msg, String requestId)
            throws SendingCommandException {

        if (!isRequestAlreadyPending(requestId)) {
            setRequestPending(requestId);

            msg.setRequestId(requestId);
            Intent i = new Intent(c, InteractionService.class);
            msg.fillIntent(i);
            c.startService(i);
        } else {
            throw new SendingCommandException("Same request already pending");
        }
    }






}
