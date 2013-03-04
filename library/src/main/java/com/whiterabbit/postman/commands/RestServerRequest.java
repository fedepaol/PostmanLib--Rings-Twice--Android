package com.whiterabbit.postman.commands;

import android.content.Context;
import android.os.Parcelable;
import com.whiterabbit.postman.exceptions.ResultParseException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 1/24/13
 * Time: 10:23 PM
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
	 * To be implemented to process the result of the http call
	 * throw ResultParseException to notify the caller that result parsing failed
     * NOTE: this method is executed in a background thread
	 * @param result the result returned by the http call
     * @param executor a request executor that might be used to execute inner requests. The requests passed to the
     *                 executor will be executed immediately in the same thread that is executing the current request, as if
     *                 it was originally sent by the client
	 * @param context an android context, might be used to store data
	 */
	public void processHttpResult(Response result, RequestExecutor executor, Context context) throws ResultParseException;

    /**
     * To be implemented to add parameters to the request according to scribe documentation.
     * To be used to set requests values
     * @param request
     */
    public void addParamsToRequest(OAuthRequest request);


}
