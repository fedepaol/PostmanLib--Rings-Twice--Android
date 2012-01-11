package com.whiterabbit.postman;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.whiterabbit.postman.commands.CommandFactory;
import com.whiterabbit.postman.commands.ServerCommand;
import com.whiterabbit.postman.utils.Constants;

public class ServerInteractionHelper {
	BroadcastReceiver mReceiver;
	private static ServerInteractionHelper mInstance;
	ServerInteractionResponseInterface mListener;
	CommandFactory				  		mFactory;
    IntentFilter                  		mFilter;
    IntentFilter                  		mErrorFilter;
    private Map<String, Boolean>	mPendingRequests;
	
	
	
	
	private ServerInteractionHelper(){
		mReceiver = new ServiceResultReceiver();
		mPendingRequests = Collections.synchronizedMap(new HashMap<String, Boolean>());
	}
	
	/**
     * Singleton
     *
     * @return
     */
    static public synchronized ServerInteractionHelper getInstance() {
        if (mInstance == null) {
            return new ServerInteractionHelper();
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
            if (action.equals(Constants.SERVER_ERROR_ID)) {
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
     * notified
     *
     * @param listener
     * @param c
     */
    public void registerEventListener(ServerInteractionResponseInterface listener, Context c) {
        mListener = listener;
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
    private boolean isRequestAlreadyPending(String requestId) {
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
     * @param m
     *            message to send
     * @param requestId
     *            an id associated to the request. Will be returned along with
     *            the result
     */
    public void sendCommand(Context c, ServerCommand msg, String requestId)
            throws SendingCommandException {
    	
    	if(mFactory == null){
    		throw new SendingCommandException("Not inited with yet, must assign a factory before sending messages");
    	}
    	
    	
        if (!isRequestAlreadyPending(requestId)) {
            setRequestPending(requestId);

            msg.setRequestId(requestId);
            Intent i = new Intent(c, InteractionService.class);
            msg.putToIntent(i);
            c.startService(i);
        } else {
            throw new SendingCommandException("Same request already pending");
        }
    }	
	
    private void setCommandFactory(CommandFactory f){
    	mFactory = f;
    }
    
    public CommandFactory getCommandFactory(){
    	return mFactory;
    }
    
    
    /**
     * Initializes the singleton with a command factory used to build instances of commands
     * @param f
     */
    public static ServerInteractionHelper initWithCommandFactory(CommandFactory f){
    	ServerInteractionHelper h = ServerInteractionHelper.getInstance();
    	h.setCommandFactory(f);
    	return h;
    }
}
