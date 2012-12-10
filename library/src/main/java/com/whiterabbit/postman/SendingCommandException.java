
package com.whiterabbit.postman;

import android.content.Context;

public class SendingCommandException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -6762209841735489432L;

    public SendingCommandException(int strResId, Context ctx) {
        super(ctx.getString(strResId));
    }
    
    /**
     * @param detailMessage
     */
    public SendingCommandException(String detailMessage) {
        super(detailMessage);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param throwable
     */
    public SendingCommandException(Throwable throwable) {
        super(throwable);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param detailMessage
     * @param throwable
     */
    public SendingCommandException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        // TODO Auto-generated constructor stub
    }
}
