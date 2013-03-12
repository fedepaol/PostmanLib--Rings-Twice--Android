package com.whiterabbit.postman;

import android.content.Context;
import android.os.Parcel;
import com.whiterabbit.postman.commands.RequestExecutor;
import com.whiterabbit.postman.commands.RestServerRequest;
import com.whiterabbit.postman.exceptions.ResultParseException;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/30/12
 * Time: 2:25 PM
 */
public class SimpleRestRequest implements RestServerRequest {
    public final String KEY = "key";
    public final String VALUE = "value";
    private String mResultString;
    private OAuthRequest mMockedRequest;
    private boolean mMustSign;
    private int mResultStatus = 0;
    private boolean mExceptionThrown = false;



    public final static String SERVICE_NAME = "Service";

    public SimpleRestRequest(OAuthRequest request, boolean mustSign){
        mMockedRequest = request;
        mMustSign = mustSign;
    }

    protected SimpleRestRequest(Parcel in) {
    }


    @Override
    public String getOAuthSigner() {
        if(mMustSign)
            return SERVICE_NAME;
        else
            return null;
    }

    @Override
    public String getUrl() {
        return "www.google.com";
    }

    @Override
    public Verb getVerb() {
        return null;
    }

    @Override
    public void onHttpError(int statusCode, RequestExecutor executor, Context context) {
        mResultStatus = statusCode;
    }

    @Override
    public void onOAuthExceptionThrown(OAuthException exception) {
        mExceptionThrown = true;
    }

    @Override
    public void onHttpResult(Response result, int statusCode, RequestExecutor executor, Context context) throws ResultParseException {
        mResultStatus = statusCode;
        if(statusCode == 200){
            mResultString = result.getBody();
        }
    }

    @Override
    public void addParamsToRequest(OAuthRequest request) {
        request.addHeader("Key", "Value");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public String getResultString(){
        return mResultString;
    }

    public int getResultStatus(){
        return mResultStatus;
    }


    public boolean isExceptionThrown(){
        return mExceptionThrown;
    }
}
