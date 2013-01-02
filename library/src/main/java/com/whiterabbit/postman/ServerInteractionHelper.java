package com.whiterabbit.postman;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.OAuthServiceException;
import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.SendingCommandException;
import com.whiterabbit.postman.commands.ServerCommand;
import com.whiterabbit.postman.oauth.OAuthFragment;
import com.whiterabbit.postman.oauth.OAuthReceivedInterface;
import com.whiterabbit.postman.oauth.OAuthServiceInfo;
import com.whiterabbit.postman.utils.Constants;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.util.*;

public class ServerInteractionHelper {
	BroadcastReceiver mReceiver;
	private static ServerInteractionHelper mInstance;
	private ServerInteractionResponseInterface mListener;
    private IntentFilter                  		mFilter;
    private IntentFilter                  		mErrorFilter;
    private Map<String, Boolean>	mPendingRequests;   // TODO Sparse array. Change request id to long
    private Map<String, OAuthServiceInfo>	mServices;

	
	
	
	private ServerInteractionHelper(){
		mReceiver = new ServiceResultReceiver();
		mPendingRequests = Collections.synchronizedMap(new HashMap<String, Boolean>());
		mFilter = new IntentFilter(Constants.SERVER_RESULT);
		mErrorFilter = new IntentFilter(Constants.SERVER_ERROR);
        mServices = new HashMap<String, OAuthServiceInfo>();
	}
	
	/**
     * Singleton
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
     * notified
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


    /****** OAUTH - Scribe library wrap up ************/


    /**
     * Asks for the request token and launches the authorization dialog
     */
    private class RequestTask extends AsyncTask<OAuthServiceInfo, Void , List<Object>> {
        private final Activity mActivity;
        private final OAuthServiceInfo mService;
        private Exception mThrownException;

        public RequestTask(Activity a, OAuthServiceInfo s){
            mActivity = a;
            mService = s;

        }
        @Override
        protected List doInBackground(OAuthServiceInfo... oAuthServiceInfos) {
            try{
                Token requestToken = oAuthServiceInfos[0].getService().getRequestToken();
                String url = oAuthServiceInfos[0].getService().getAuthorizationUrl(requestToken);
                List<Object> res = new ArrayList<Object>(2);
                res.add(0, requestToken);
                res.add(1, url);
                return res;
            }catch (OAuthException e) {
                mThrownException = e;
            }
            return null;
        }


        @Override
        protected void onPostExecute(List<Object> res) {
            if(mThrownException != null){
                if(mListener != null){
                    mListener.onServiceAuthenticationFailed(mService.getServiceName(), mThrownException.getMessage());
                }
                return;
            }

            if(mActivity == null){
                Log.d(Constants.LOG_TAG, "Request task: Activity unregistered before showing oauth fragment, exiting...");
                return;
            }

            final Token requestToken = (Token) res.get(0);
            String url = (String) res.get(1);

            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            OAuthFragment newFragment = OAuthFragment.newInstance(url, new OAuthReceivedInterface() {
                @Override
                public void onAuthReceived(String url) {
                    // Gets called when the user authorizes the app
                    AuthTask a = new AuthTask(requestToken, mService, mActivity);
                    a.execute(url);

                }

                @Override
                public void onAuthFailed(String reason) {
                    if(mListener != null){
                        mListener.onServiceAuthenticationFailed(mService.getServiceName(), reason);
                    }
                    Log.e(Constants.LOG_TAG, mService.getServiceName() + " failed to authenticate for " + reason);
                }
            });
            newFragment.show(ft, "dialog");
        }
    };


    /**
     * Used to retrieve the authorization token
     */
    private class AuthTask extends AsyncTask<String, Void, Void>{
        private final Token mRequestToken;
        private final OAuthServiceInfo mService;
        private final Context mContext;
        private Exception mThrownException;

        public AuthTask(Token requestToken, OAuthServiceInfo service, Context c){
            mRequestToken = requestToken;
            mService = service;
            mContext = c;
        }

        @Override
        protected Void doInBackground(String... urls) {
            try{
                Verifier verifier = new Verifier(urls[0]);
                Token accessToken = mService.getService().getAccessToken(mRequestToken, verifier);

                int mode = Activity.MODE_PRIVATE;
                SharedPreferences mySharedPreferences = mContext.getSharedPreferences(mService.getServiceName(), mode);
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                editor.putString(Constants.TOKEN, accessToken.getToken());
                editor.putString(Constants.SECRET, accessToken.getSecret());
                editor.putString(Constants.RAW_RES, accessToken.getRawResponse());
                editor.commit();
                ServerInteractionHelper.this.getRegisteredService(mService.getServiceName()).setAccessToken(accessToken);

            }catch (OAuthException e){
                mThrownException = e;
            }catch(OAuthServiceException e){
                mThrownException = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(mThrownException == null){
                if(mListener != null){
                    mListener.onServiceAuthenticated(mService.getServiceName());
                }
            }else{
                if(mListener != null){
                    mListener.onServiceAuthenticationFailed(mService.getServiceName(), mThrownException.getMessage());
                }
                return;
            }
        }


    }

    /**
     * Registers the given oauth service to the library.
     * A name must be provided to reference the service from the library
     * @param service
     * @param name
     * @param a
     */
    public void registerOAuthService(OAuthService service, String name, Activity a) {
        Token t = getAuthTokenForService(name, a);
        mServices.put(name, new OAuthServiceInfo(service, name, t));
    }

    // TODO weak reference to the activity.

    /**
     * Starts the authentication ballet using scribe library
     * An activity is needed because the library will open a dialog fragment to get the user's authorization from
     * @param a
     * @param serviceName
     */
    public void authenticate(final Activity a, String serviceName) throws OAuthServiceException {
        final OAuthServiceInfo s = mServices.get(serviceName);
        if(s == null){
            throw new OAuthServiceException(String.format("Service %s not found", serviceName));
        }

        RequestTask r = new RequestTask(a, s);
        r.execute(s);
    }



    public OAuthServiceInfo getRegisteredService(String serviceName) throws OAuthServiceException {
        OAuthServiceInfo res = mServices.get(serviceName);
        if(res == null){
            throw new OAuthServiceException(String.format("Service %s not found", serviceName));
        }

        if(res.getAccessToken() == null){
            throw new OAuthServiceException(String.format("Service %s not authenticated yet", serviceName));
        }

        return res;
    }


    /**
     * Returns a stored oauth token for the given service
     * @param serviceName
     * @param c
     * @return
     */
    private Token getAuthTokenForService(String serviceName, Context c){
        SharedPreferences mySharedPreferences = c.getSharedPreferences(serviceName, Activity.MODE_PRIVATE);
        String token = mySharedPreferences.getString(Constants.TOKEN, "");
        String secret = mySharedPreferences.getString(Constants.SECRET, "");
        String raw = mySharedPreferences.getString(Constants.RAW_RES, "");
        if (token.equals("") ||
            secret.equals("") ||
            raw.equals("")){
            return null;
        }

        return new Token(token, secret, raw);

    }


    /**
     * To be used to invalidate the authentication token of the given service.
     * Subsequent registration will start authorization process again
     * @param serviceName
     */
    public void invalidateAuthentication(String serviceName, Context c) throws OAuthServiceException {
        SharedPreferences mySharedPreferences = c.getSharedPreferences(serviceName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString(Constants.TOKEN, "");
        editor.putString(Constants.SECRET, "");
        editor.putString(Constants.RAW_RES, "");
        editor.commit();

        OAuthServiceInfo s = getRegisteredService(serviceName);
        s.setAccessToken(null);
    }




}
