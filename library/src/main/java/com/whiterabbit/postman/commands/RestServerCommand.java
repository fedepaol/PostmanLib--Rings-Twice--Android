package com.whiterabbit.postman.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
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
public abstract class RestServerCommand extends ServerCommand  {
    private String mUrl;
    private Verb mVerb;
    private String mOAuthSigner;
    private boolean mustSign;

    public RestServerCommand(Verb v, String url){
        mVerb = v;
        mUrl = url;
        mustSign = false;
    }

    public void setOAuthSigner(String signer){
        mOAuthSigner = signer;
        mustSign = true;
    }


    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mVerb.ordinal());
        parcel.writeString(mUrl);
        parcel.writeString(mOAuthSigner);
    }

    protected RestServerCommand(Parcel in){
        mVerb = Verb.values()[in.readInt()];
        mUrl = in.readString();
        mOAuthSigner = in.readString();
        if(mOAuthSigner == null){
            mustSign = false;
        }else{
            mustSign = true;
        }
    }



	/**
	 * To be implemented to process the result of the http call
	 * throw ResultParseException to notify the caller that result parsing failed
     * NOTE: this method is executed in a background thread
	 * @param result
	 * @param context
	 */
	protected abstract void processHttpResult(String result, Context context) throws ResultParseException;


    /**
     * To be implemented to add parameters to the request according to scribe documentation.
     * To be used to set requests values
     * @param request
     */
    protected abstract void addParamsToRequest(OAuthRequest request);


	/**
	 * The real execution of the command. Performs the basic rest interaction
	 */
	@Override
	public void execute(Context c) {

        OAuthRequest request = new OAuthRequest(mVerb, mUrl);
        addParamsToRequest(request);

        if(mustSign){
            OAuthServiceInfo s = ServerInteractionHelper.getInstance().getRegisteredService(mOAuthSigner);
            s.getService().signRequest(s.getAccessToken(), request);
        }
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
