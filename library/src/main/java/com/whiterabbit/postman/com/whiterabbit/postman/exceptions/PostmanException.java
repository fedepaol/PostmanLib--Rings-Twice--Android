package com.whiterabbit.postman.com.whiterabbit.postman.exceptions;

import android.content.Context;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/26/12
 * Time: 3:55 PM
 */
public class PostmanException  extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -6762209841735489432L;

    public PostmanException(int strResId, Context c) {
        super(c.getString(strResId));
    }

    /**
     * @param detailMessage
     */
    public PostmanException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * @param throwable
     */
    public PostmanException(Throwable throwable) {
        super(throwable);
    }

    /**
     * @param detailMessage
     * @param throwable
     */
    public PostmanException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}

