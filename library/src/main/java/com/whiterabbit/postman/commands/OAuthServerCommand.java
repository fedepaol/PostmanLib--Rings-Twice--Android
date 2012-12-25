package com.whiterabbit.postman.commands;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.whiterabbit.postman.ServerInteractionHelper;
import com.whiterabbit.postman.oauth.OAuthServiceInfo;
import com.whiterabbit.postman.utils.Constants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;


/**
 * Server command implementation intended to be used to interact with a rest server
 * @author fede
 *
 */
public abstract class OAuthServerCommand extends ServerCommand  {

    public static final int BUFFER_SIZE = 1024;
    private char[] mInputBuffer = new char[BUFFER_SIZE];
    private StringBuilder mStringBuilder = new StringBuilder(BUFFER_SIZE);

	public enum RestResult{
		RESULT_OK, RESULT_NETWORK_ERROR, RESULT_INVALID_PERMISSION, RESULT_DATA_NOT_FOUND, RESULT_GENERIC_ERROR
	}



	protected abstract Verb getVerb();
    protected abstract String getOAuthServiceName();

    // TODO Get Action


	@Override
	public void fillIntent(Intent i){
		super.fillIntent(i);
	}

	@Override
	public void fillFromIntent(Intent i){
		Verb a = Verb.valueOf(i.getExtras().getString(Constants.ACTION));
		super.fillFromIntent(i);
	}


	/**
	 * Need to be implemented in order to return the url to be called depending on this object and the given action
	 * @param v
	 * @return
	 */
	protected abstract String getUrl(Verb v);

	/**
	 * To be implemented to process the result of the http call
	 * throw ResultParseException to notify the caller that result parsing failed
	 * @param result
	 * @param context
	 */
	protected abstract void processHttpResult(String result, Context context) throws ResultParseException;


    /**
     * Returns a list of params to be passed to the post / put call
     * @return
     */
    protected abstract String[] getParams();

    /**
     * Must return an array of values related to the params returned by getParams
     * @return
     */
    protected abstract String[] getValues();



	/**
	 * The real execution of the command. Performs the basic rest interaction
	 */
	@Override
	public void execute(Context c) {


        OAuthServiceInfo s = ServerInteractionHelper.getInstance().getRegisteredService(getOAuthServiceName());
        OAuthRequest request = new OAuthRequest(Verb.POST, getUrl(getVerb()));
        request.addBodyParameter("status", "Sent from postmanlib! *");

        s.getService().signRequest(s.getAccessToken(), request);
        Response response = request.send();
        handleResponse(response.getCode(), response.getBody(), c);

    }





	private void handleResponse(int statusCode, String response, Context c) {
		switch(statusCode){
			case 200:
				if(response != null){
                    try {
                        processHttpResult(response, c);
                    }catch(ResultParseException e){
                        notifyError("Failed to parse result " + e.getMessage(), c);
                        Log.e(Constants.LOG_TAG, "Result parse failed: " + response);
                    }
                }
                notifyResult("Ok",  c);
            break;
            case 204:
                notifyResult("Ok",  c);
            break;
            case 404:
                notifyError("Not found" ,  c);
            break;
            case 401:
                notifyError("No permission" ,  c);
            break;
            default:
                notifyError("Generic error " + statusCode, c);
        }
	}
	


}
