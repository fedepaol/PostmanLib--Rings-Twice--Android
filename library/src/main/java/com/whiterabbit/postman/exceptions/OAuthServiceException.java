package com.whiterabbit.postman.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/26/12
 * Time: 3:50 PM
 */
public class OAuthServiceException extends RuntimeException {

    public OAuthServiceException(String errMessage) {
        super(errMessage);
    }
}
