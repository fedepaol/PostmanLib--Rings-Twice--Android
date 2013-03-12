package com.whiterabbit.postman.commands;

import android.content.Context;
import android.os.Parcelable;
import com.whiterabbit.postman.exceptions.ResultParseException;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

/**
 * Interface that represent a scribe rest request.
 * All the callbacks are executed in a background thread.
 * Since it extends parcelable, instances must have a static CREATOR field
 */
public interface RestServerRequest extends Parcelable{
    /**
     * Must return the signer service to be used in case of oauth request.
     * Return null in case of not signed message
     * @return
     */
    public String getOAuthSigner();

    /**
     * Must return the url to perform the request against
     * @return
     */
    public String getUrl();

    /**
     * Must return the verb to be used in the request
     * @return
     */
    public Verb getVerb();

	/**
	 * To be implemented to process the result of successful http calls
	 * throw ResultParseException to notify the caller that result parsing failed
     * NOTE: this method is executed in a background thread
	 * @param result the result returned by the http call
     * @param statusCode the status code returned by the http call. It is the same returned by result.getCode() but is passed
     *                   here as a facility
     * @param executor a request executor that might be used to execute inner requests. The requests passed to the
     *                 executor will be executed immediately in the same thread that is executing the current request, as if
     *                 it was originally sent by the client
	 * @param context an android context, might be used to store data
	 */
	public void onHttpResult(Response result, int statusCode, RequestExecutor executor, Context context) throws ResultParseException;



    /**
     * To be implemented to process the result of unsuccessful http calls returning 4xx and 5xx codes
     * @param statusCode the status code returned by the http call.
     * @param executor a request executor that might be used to execute inner requests. The requests passed to the
     *                 executor will be executed immediately in the same thread that is executing the current request, as if
     *                 it was originally sent by the client
     * @param context an android context, might be used to store data
     */
    public void onHttpError(int statusCode, RequestExecutor executor, Context context) ;


    /**
     * Gets triggered when the underlying scribe library throws an oauth exception
     * @param exception the thrown exception
     */
    public void onOAuthExceptionThrown(OAuthException exception);

    /**
     * To be implemented to add parameters to the request according to scribe documentation.
     * To be used to set requests values
     * @param request
     */
    public void addParamsToRequest(OAuthRequest request);
}
