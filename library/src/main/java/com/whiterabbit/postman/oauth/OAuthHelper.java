package com.whiterabbit.postman.oauth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.OAuthServiceException;
import com.whiterabbit.postman.utils.Constants;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 1/14/13
 * Time: 9:05 PM
 */
public class OAuthHelper {
    OAuthResponseInterface mListener;
    private Map<String, OAuthServiceInfo> mServices;
    private static OAuthHelper mInstance;


    private OAuthHelper() {
        mServices = new HashMap<String, OAuthServiceInfo>();
    }


   /**
     * Singleton factory method
     *
     * @return
     */
    static public synchronized OAuthHelper getInstance() {
        if (mInstance == null) {
            mInstance = new OAuthHelper();
            return mInstance;
        } else {
            return mInstance;
        }
    }


    /**
     * Register a listener to be used to get feedbacks for the autehntication process
     * @param listener
     */
    public void registerListener(OAuthResponseInterface listener){
        mListener = listener;
    }


    /**
     * Unregisters the listener
     */
    public void unregisterListener(){
        mListener = null;
    }

    /**
     * Asks for the request token and launches the authorization dialog
     */
    private class RequestTask extends AsyncTask<OAuthServiceInfo, Void , List<Object>> {
        private final FragmentActivity mActivity;
        private final OAuthServiceInfo mService;
        private Exception mThrownException;

        public RequestTask(FragmentActivity a, OAuthServiceInfo s){
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

            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
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

                final OAuthServiceInfo s = mServices.get(mService.getServiceName());
                if(s != null){
                    s.setAccessToken(accessToken);
                }else{
                    throw new OAuthException("Service not found");
                }
            }catch (OAuthException e){
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
     * @param name
     * @param c
     */
    public void registerOAuthService(StorableServiceBuilder builder, String name, Context c) {
        OAuthService service = builder.build(c);
        builder.storeToPreferences(name, c);     // Todo on asynctask
        Token t = getAuthTokenForService(name, c);
        mServices.put(name, new OAuthServiceInfo(service, name, t));
    }

    // TODO weak reference to the activity.

    /**
     * Starts the authentication ballet using scribe library
     * An activity is needed because the library will open a dialog fragment to get the user's authorization from
     * @param a
     * @param serviceName
     */
    public void authenticate(final FragmentActivity a, String serviceName) throws OAuthServiceException {
        final OAuthServiceInfo s = mServices.get(serviceName);
        if(s == null){
            throw new OAuthServiceException(String.format("Service %s not found", serviceName));
        }

        RequestTask r = new RequestTask(a, s);
        r.execute(s);
    }


    /**
     * Returns a registered service to be used to authenticate a request.
     * Should be called inside a request message, hosted in a background thread
     * @param serviceName the name of the service to be retrieved
     * @return
     * @throws OAuthServiceException
     */
    public OAuthServiceInfo getRegisteredService(String serviceName, Context c) throws OAuthServiceException {
        OAuthServiceInfo res = mServices.get(serviceName);
        if(res == null){
            StorableServiceBuilder builder = new StorableServiceBuilder(serviceName, c);
            registerOAuthService(builder, serviceName, c);
            res = mServices.get(serviceName);   // just another access to the map
        }

        if(res == null || res.getAccessToken() == null){
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
     * Tells if the token for the given service is already stored or if the service must be authenticated
     * @param serviceName
     * @param c
     * @return
     */
    public boolean isAlreadyAuthenticated(String serviceName, Context c){
        if(mServices.get(serviceName).getAccessToken() != null){
            return true;
        }

        return (getAuthTokenForService(serviceName, c) != null);
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

        OAuthServiceInfo s = getRegisteredService(serviceName, c);
        s.setAccessToken(null);
    }

    public void eraseInstance(){
        mInstance = null;
    }
}
