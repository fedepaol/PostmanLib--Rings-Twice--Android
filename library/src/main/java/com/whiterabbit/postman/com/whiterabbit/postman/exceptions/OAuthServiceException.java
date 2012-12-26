package com.whiterabbit.postman.com.whiterabbit.postman.exceptions;

import android.content.Context;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/26/12
 * Time: 3:50 PM
 */
public class OAuthServiceException extends PostmanException{
    public OAuthServiceException(int strResId, Context c) {
        super(strResId, c);
    }

    public OAuthServiceException(String errMessage){
        super(errMessage);
    }
}
