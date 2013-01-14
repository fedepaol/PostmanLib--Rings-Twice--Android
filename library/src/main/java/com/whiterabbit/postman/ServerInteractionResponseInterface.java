package com.whiterabbit.postman;

/**
 * Interface to be implemented to get notified of the result of a server request
 * @author fede
 *
 */
public interface ServerInteractionResponseInterface {

    /**
     * Listener interface to get notified of the result of an asyncronous call
     * @param result
     * @param requestId
     */
	public void onServerResult(String result, String requestId);

    /**
     * Listener interface to get notified while an error occurred during an asyncronous call
     * @param result
     * @param requestId
     */
	public void onServerError(String result, String requestId);


}
