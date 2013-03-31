package com.whiterabbit.postman;


import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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


    private enum ServiceChoiceType {
        ROUND_ROBIN_SERVICE,
        FIRST_SERVICE
    }

	

	private ServerInteractionHelper(Context c){
		mReceiver = new ServiceResultReceiver();
		mPendingRequests = Collections.synchronizedMap(new HashMap<String, Boolean>());
		mFilter = new IntentFilter();
        mFilter.addAction(Constants.SERVER_RESULT);
		mFilter.addAction(Constants.SERVER_ERROR);
        mCachingEnabled = false;
        mServices = new ArrayList<Class<? extends InteractionService>>(4);


        LocalBroadcastManager.getInstance(c.getApplicationContext()).registerReceiver(mReceiver, mFilter);

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
            ServerInteractionResponseInterface listener = null;
            if(mListener != null){
                listener = mListener.get();
            }

            String action = intent.getAction();
            if (action.equals(Constants.SERVER_RESULT)) {
                Bundle extras = intent.getExtras();
                String requestId = extras.getString(Constants.REQUEST_ID);
                String message = extras.getString(Constants.MESSAGE_ID);
                boolean ignorePending = extras.getBoolean(Constants.IGNORE_PENDING_ID);

                if (listener != null) {
                    listener.onServerResult(message, requestId);
                }
                if(!ignorePending){
                    requestDone(requestId);
                }
            }
            if (action.equals(Constants.SERVER_ERROR)) {
                Bundle extras = intent.getExtras();
                String requestId = extras.getString(Constants.REQUEST_ID);
                String message = extras.getString(Constants.MESSAGE_ID);
                boolean ignorePending = extras.getBoolean(Constants.IGNORE_PENDING_ID);

                if (listener != null) {
                    listener.onServerError(message, requestId);
                }
                if(!ignorePending){
                    requestDone(requestId);
                }
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
     * Returns the first available service. This is useful because I must be sure that I always have
     * the same service in order to be able to retrieve the same pending intent for cancel it
     * @param c
     * @return
     */
    private Class<? extends InteractionService> getServiceToSchedule(Context c){
        initEnabledServices(c);
        return mServices.get(0);    // hopefully at least one service will be available
    }


    private Intent getIntentFromCommand(Context c, ServerCommand msg, String requestId, ServiceChoiceType type){
        msg.setRequestId(requestId);
        Intent i;
        switch(type){
            case FIRST_SERVICE:
                i = new Intent(c, getServiceToSchedule(c));
            break;
            case ROUND_ROBIN_SERVICE:
            default:
                i = new Intent(c, getTargetService(c));
            break;
        }
        msg.fillIntent(i);
        return i;
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

        if (!isRequestAlreadyPending(requestId) || msg.getIgnorePending()) {

            if(!msg.getIgnorePending()){
                setRequestPending(requestId);
            }

            Intent i = getIntentFromCommand(c, msg, requestId, ServiceChoiceType.ROUND_ROBIN_SERVICE);
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
     * @param moreRequests
     *        optional other strategies to be executed together
     * @throws SendingCommandException
     */
    public void sendRestAction(Context c, String requestId, RestServerRequest s, RestServerRequest... moreRequests) throws SendingCommandException {
        RestServerCommand command = new RestServerCommand(s, moreRequests);   // TODO use a pool of commands
        sendCommand(c, command, requestId);

    }


    /**
     * In some cases the asynchronous requests must be schedule. The library only provides a shortcut to get a pending intent
     * which must be attached to an alarmmanager. This is done to provide the same amount of flexibility given by alarmmanager without wrapping them
     * inside the library
     * @param c
     *         the context of the caller
     * @param requestId
     *         a unique identifier of this request, to be linked with the results
     * @param s
     *         a mandatory request to be scheduled
     * @param moreRequests
     *         optional other requests to be scheduled in the same pending intent
     * @return
     */
    public PendingIntent getActionToSchedule(Context c, String requestId, RestServerRequest s, RestServerRequest... moreRequests){
        RestServerCommand command = new RestServerCommand(s, moreRequests);
        command.setIgnorePending(true);
        Intent toSchedule = getIntentFromCommand(c, command, requestId, ServiceChoiceType.FIRST_SERVICE);
        toSchedule.setAction(requestId);
        PendingIntent res = PendingIntent.getService(c, 0, toSchedule, 0);
        return res;
    }

    /**
     * Resets instance for unit tests
     */
    static void resetInstance(){
        mInstance = null;
    }





}
