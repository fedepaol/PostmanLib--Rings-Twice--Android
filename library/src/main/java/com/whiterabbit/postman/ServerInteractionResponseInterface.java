package com.whiterabbit.postman;

/**
 * Interface to be implemented to get notified of the result of a server request
 * @author fede
 *
 */
public interface ServerInteractionResponseInterface {
	   
	public void onServerResult(String result, String requestId);

	public void onServerError(String result, String requestId);
}
