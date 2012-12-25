package com.whiterabbit.postman.oauth;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/23/12
 * Time: 11:03 PM
 */
public class OAuthServiceInfo {
    private OAuthService mService;
    private Token mAccessToken;
    private boolean mAuthenticated;
    private String mServiceName;


    public String getServiceName() {
        return mServiceName;
    }

    public OAuthServiceInfo(OAuthService s, String serviceName, Token accessToken){
        mService = s;
        mServiceName = serviceName;
        mAccessToken = accessToken;
    }

    public OAuthService getService(){
        return mService;
    }

    public Token getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(Token mAccessToken) {
        this.mAccessToken = mAccessToken;
    }

    public boolean isAuthenticated() {
        return mAuthenticated;
    }

    public void setAuthenticated(boolean mAuthenticated) {
        this.mAuthenticated = mAuthenticated;
    }
}
