package com.whiterabbit.postman.commands;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.whiterabbit.postman.utils.Constants;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.auth.BasicScheme;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;


/**
 * Server command implementation intended to be used to interact with a rest server
 * @author fede
 *
 */
public abstract class RestServerCommand extends ServerCommand  {

    public static final int BUFFER_SIZE = 1024;
    private char[] mInputBuffer = new char[BUFFER_SIZE];
    private StringBuilder mStringBuilder = new StringBuilder(BUFFER_SIZE);
	
	public enum Action {
        GET, CREATE, UPDATE, DELETE 
    }
	
	public enum RestResult{
		RESULT_OK, RESULT_NETWORK_ERROR, RESULT_INVALID_PERMISSION, RESULT_DATA_NOT_FOUND, RESULT_GENERIC_ERROR
	}
	

	private Action mAction;

	abstract protected Action getAction();

    // TODO Get Action

    /**
     * Reads the buffered stream. The loop is needed because of gzip compression
     * @param stream
     * @return
     * @throws IOException
     */
    private String readStream(InputStream stream) throws IOException{
        try {
            InputStreamReader reader = new InputStreamReader(stream);
            int charsRead;
            while((charsRead = reader.read(mInputBuffer)) != -1){
                mStringBuilder.append(mInputBuffer, 0, charsRead);
            }
        }finally {
            stream.close();
        }

        return mStringBuilder.toString();
    }
	
	@Override
	public void fillIntent(Intent i){
		i.putExtra(Constants.ACTION, mAction.toString());
		super.fillIntent(i);
	}
	
	@Override
	public void fillFromIntent(Intent i){
		Action a = Action.valueOf(i.getExtras().getString(Constants.ACTION));
		mAction = a;
		super.fillFromIntent(i);
	}
	
	
	/**
	 * Need to be implemented in order to return the url to be called depending on this object and the given action
	 * @param a
	 * @return
	 */
	protected abstract String getUrl(Action a);
	
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
	 * Helper function to force basic authentication on the request using the given
	 * user / password
	 * @param req
	 * @param user
	 * @param password
	 */
	public static void setBasicAuthentication(HttpRequestBase req, String user, String password)
	{
		req.addHeader(BasicScheme.authenticate(
					  new UsernamePasswordCredentials(user, password),
					  "UTF-8", false));
		
	}

    private String getQuery(){
        String charset = "UTF-8";
        String[] params = getParams();
        String[] values = getValues();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < params.length; i++ ){
            try {
                if(i > 0 ){
                    builder.append("&");
                }
                builder.append(java.lang.String.format("%s=%s", params[i],
                        URLEncoder.encode(values[i], charset)  ));


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        return builder.toString();
    }

	/**
	 * The real execution of the command. Performs the basic rest interaction
	 */
	@Override
	public void execute(Context c) {


        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(getUrl(mAction));

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod(getRequest());   // GET / POST / PUT / DEL

            if(mAction == Action.CREATE ||
               mAction == Action.UPDATE){

                urlConnection.setDoOutput(true);

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(getQuery().getBytes(Charset.forName("UTF-8")));  // Todo fixed lenght
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            int http_status = urlConnection.getResponseCode();
            String result = readStream(in);
            handleResponse(http_status, result, c);

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(null != urlConnection){
                urlConnection.disconnect();
            }
        }
    }




	String getRequest(){
		switch(mAction){
            case GET:
                return "GET";
            case CREATE:
                return "POST";
            case UPDATE:
                return "PUT";
            case DELETE:
                return "DELETE";
        }
        return null;
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
