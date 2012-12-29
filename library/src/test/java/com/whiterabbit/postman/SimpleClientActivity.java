package com.whiterabbit.postman;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/29/12
 * Time: 12:00 PM
 */
public class SimpleClientActivity extends Activity implements ServerInteractionResponseInterface{
    private boolean mIsFailure;
    private String mServerResult;
    private String mRequestReceived;
    private boolean mServiceAuthenticatedSuccess;
    private String mServiceAuthenticated;
    private String mServiceAuthenticatedReason;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onServerResult(String result, String requestId) {
        mServerResult = result;
        mRequestReceived = requestId;
        mIsFailure = false;
    }

    @Override
    public void onServerError(String result, String requestId) {
        mServerResult = result;
        mRequestReceived = requestId;
        mIsFailure = true;
    }

    @Override
    public void onServiceAuthenticated(String serviceName) {
        mServiceAuthenticatedSuccess = true;
        mServiceAuthenticated = serviceName;
    }

    @Override
    public void onServiceAuthenticationFailed(String serviceName, String reason) {
        mServiceAuthenticatedSuccess = false;
        mServiceAuthenticated = serviceName;
        mServiceAuthenticatedReason = reason;
    }

    public boolean isIsFailure() {
        return mIsFailure;
    }

    public String getServerResult() {
        return mServerResult;
    }

    public String getRequestReceived() {
        return mRequestReceived;
    }

    public boolean isServiceAuthenticatedSuccess() {
        return mServiceAuthenticatedSuccess;
    }

    public String getServiceAuthenticated() {
        return mServiceAuthenticated;
    }

    public String getmServiceAuthenticatedReason() {
        return mServiceAuthenticatedReason;
    }

}
