package com.whiterabbit.postman;


import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.whiterabbit.postman.commands.RestServerCommand;
import com.whiterabbit.postman.commands.RestServerRequest;
import com.whiterabbit.postman.commands.ServerCommand;
import com.whiterabbit.postman.exceptions.OAuthServiceException;
import com.whiterabbit.postman.exceptions.SendingCommandException;
import com.whiterabbit.postman.utils.Constants;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerInteractionHelper {
	BroadcastReceiver mReceiver;
	private static ServerInteractionHelper mInstance;
    private WeakReference<ServerInteractionResponseInterface> mListener;
    private IntentFilter                  		mFilter;
    private Map<String, Boolean>	mPendingRequests;   // TODO Sparse array. Change request id to long
    private boolean         mCachingEnabled;
    private int mServiceCounter;
    private int mNumOfServices;
    private ArrayList<Class<? extends InteractionService>> mServices;

	

	private ServerInteractionHelper(Context c){
		mReceiver = new ServiceResultReceiver();
		mPendingRequests = Collections.synchronizedMap(new HashMap<String, Boolean>());
		mFilter = new IntentFilter();
        mFilter.addAction(Constants.SERVER_RESULT);
		mFilter.addAction(Constants.SERVER_ERROR);
        mCachingEnabled = false;
        mServices = new ArrayList<Class<? extends InteractionService>>(4);
        c.getApplicationContext().registerReceiver(mReceiver, mFilter);

	}

    /**
     * From http://android-developers.blogspot.it/2011/09/androids-http-clients.html
     * enabling the http cache results in better performance and low latency.
     * It's automatically called in RestServerCommands.execute
     * @param c
     */
    public void enableHttpResponseCache(Context c) {
        if(mCachingEnabled){
            return;
        }
        try {
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            File httpCacheDir = new File(c.getCacheDir(), "http");
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
        }
    }

	/**
     * Singleton factory method
     *
     * @return
     */
    static public synchronized ServerInteractionHelper getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new ServerInteractionHelper(c);
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

                ServerInteractionResponseInterface listener = mListener.get();
                if (listener != null) {
                    listener.onServerResult(message, requestId);
                }
                requestDone(requestId);
            }
            if (action.equals(Constants.SERVER_ERROR)) {
                Bundle extras = intent.getExtras();
                String requestId = extras.getString(Constants.REQUEST_ID);
                String message = extras.getString(Constants.MESSAGE_ID);
                ServerInteractionResponseInterface listener = mListener.get();
                if (listener != null) {
                    listener.onServerError(message, requestId);
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
        mListener = new WeakReference<ServerInteractionResponseInterface>(listener);
    }

    /**
     * Unregisters the broadcast receiver
     *
     * @param c
     */
    public void unregisterEventListener(ServerInteractionResponseInterface listener, Context c) {
        mListener = null;
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


    private boolean isServiceEnabled(Context c, Class<? extends InteractionService> service){
        if(c.getPackageManager().getComponentEnabledSetting(new ComponentName(c, service)) == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT){
            return true;
        }else{
            return false;
        }
    }

    private void initEnabledServices(Context c){
        if(mNumOfServices > 0)
            return;

        if(isServiceEnabled(c, InteractionService.class)){
            mServices.add(InteractionService.class);
            mNumOfServices++;
        }
        if(isServiceEnabled(c, InteractionService1.class)){
            mServices.add(InteractionService1.class);
            mNumOfServices++;
        }
        if(isServiceEnabled(c, InteractionService2.class)){
            mServices.add(InteractionService2.class);
            mNumOfServices++;
        }
        if(isServiceEnabled(c, InteractionService3.class)){
            mServices.add(InteractionService3.class);
            mNumOfServices++;
        }

        if(mNumOfServices == 0){
            Log.d(Constants.LOG_TAG, "No service available. Did you remember to add at least one interaction service to your manifest?");
            throw new OAuthServiceException("No services available");
        }

    }


    private Class<? extends InteractionService> getTargetService(Context c){
        initEnabledServices(c);
        mServiceCounter = (mServiceCounter + 1) % mNumOfServices;
        return mServices.get(mServiceCounter);
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
            Intent i = new Intent(c, getTargetService(c));
            msg.fillIntent(i);
            c.startService(i);
        } else {
            throw new SendingCommandException("Same request already pending");
        }
    }


    /**
     * Helper method to send rest commands. Can send one or more resttrategies to be executed
     * In case of bulk strategies, a failure will be notified whenever the first execution fails (even if the previous
     * one where successful)
     *
     * @param c
     *         the context of the caller
     * @param requestId
     *         a loopback param with the request id that will be returned with the result
     * @param s
     *         a mandatory strategy
     * @param moreStrategies
     *        optional other strategies to be executed together
     * @throws SendingCommandException
     */
    public void sendRestAction(Context c, String requestId, RestServerRequest s, RestServerRequest... moreStrategies) throws SendingCommandException {
        RestServerCommand command = new RestServerCommand(s, moreStrategies);   // TODO use a pool of commands
        sendCommand(c, command, requestId);

    }

    /**
     * Resets instance for unit tests
     */
    static void resetInstance(){
        mInstance = null;
    }





}
