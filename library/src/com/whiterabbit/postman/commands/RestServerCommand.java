package com.whiterabbit.postman.commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.DateUtils;
import android.util.Log;

import com.whiterabbit.postman.utils.Constants;


/**
 * Server command implementation intended to be used to interact with a rest server
 * @author fede
 *
 */
public abstract class RestServerCommand extends ServerCommand  implements ResponseHandler<String>{
	
	public enum Action {
        GET, CREATE, UPDATE, DELETE 
    }
	
	public enum RestResult{
		RESULT_OK, RESULT_NETWORK_ERROR, RESULT_INVALID_PERMISSION, RESULT_DATA_NOT_FOUND, RESULT_GENERIC_ERROR
	}
	
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";
	private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;
	
	private Action mAction;
	private Context mContext;
	
	
	public RestServerCommand(Action a){
		mAction = a;
	}
	
	public RestServerCommand(){
		
	}
	
	@Override
	public void putToIntent(Intent i){
		i.putExtra(Constants.ACTION, mAction.toString());
		super.putToIntent(i);
	}
	
	@Override
	public void fillFromIntent(Intent i){
		Action a = Action.valueOf(i.getExtras().getString(Constants.ACTION));
		mAction = a;
		super.fillFromIntent(i);
	}
	
	
	/**
	 * implement this call to set the http request header and entity
	 * @param call
	 */
	protected abstract void setCallPayload(HttpEntityEnclosingRequestBase call);

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
	 * To be implemented to set authentication if needed 
	 * @param req
	 */
	protected abstract void authenticate(HttpRequestBase req);
	
	
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
	
	
	/**
	 * The real execution of the command. Performs the basic rest interaction
	 */
	@Override
	public void execute(Context c) {
		mContext = c;
		HttpClient client= getHttpClient(c);
		try{
			String url= getUrl(mAction);
			HttpRequestBase httpCall = getRequest(url);
			
			if(mAction == Action.CREATE || mAction == Action.UPDATE){
				HttpEntityEnclosingRequestBase casted = (HttpEntityEnclosingRequestBase) httpCall;
				setCallPayload(casted);
			}
			
			authenticate(httpCall);
			String responseBody=client.execute(httpCall, this);
		} catch (HttpHostConnectException e){
			notifyResult("Host not found", c);
		} catch(UnknownHostException e){
			notifyResult("Network error", c);
		}
		catch (Exception e){
			 e.printStackTrace();
		}
		client.getConnectionManager().shutdown(); 
	}
	
	


	HttpRequestBase getRequest(String url){
		switch(mAction){
		case GET:
			return new HttpGet(url);
		case CREATE:
			return new HttpPost(url);
		case UPDATE:
			return new HttpPut(url);
		case DELETE:
			return new HttpDelete(url);
		default:
			return new HttpGet(url);
		}
	}
	

	@Override
	public String handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		String res = null;
		try{
			int statusCode = response.getStatusLine().getStatusCode();
			switch(statusCode){
				case 200:
					res = EntityUtils.toString(response.getEntity());
					if(res != null){
						processHttpResult(res, mContext);
					}
					notifyResult("Ok",  mContext);
				break;
				case 204:
					notifyResult("Ok",  mContext);
				break;
				case 404:
					notifyError("Not found" ,  mContext);
				break;
				case 401:
					notifyError("No permission" ,  mContext);
				break;
				default:
					notifyError("Generic error " + statusCode,  mContext);
			}
		}catch(ResultParseException e){
			notifyError("Failed to parse result " + e.getMessage(), mContext);
			Log.e(Constants.LOG_TAG, "Result parse failed: " + res);
		}
		
		return res;
	}
	
	
/*** TAKEN FROM GOOGLE IO APP */
	
	private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }
	
    private static String buildUserAgent(Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            // Some APIs require "(gzip)" in the user-agent string.
            return info.packageName + "/" + info.versionName
                    + " (" + info.versionCode + ") (gzip)";
        } catch (NameNotFoundException e) {
            return null;
        }
    }
	
	
	/**
     * Generate and return a {@link HttpClient} configured for general use,
     * including setting an application-specific user-agent string.
     */
    public static HttpClient getHttpClient(Context context) {
        final HttpParams params = new BasicHttpParams();

        // Use generous timeouts for slow mobile networks
        HttpConnectionParams.setConnectionTimeout(params, 20 * SECOND_IN_MILLIS);
        HttpConnectionParams.setSoTimeout(params, 20 * SECOND_IN_MILLIS);

        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpProtocolParams.setUserAgent(params, buildUserAgent(context));

        final DefaultHttpClient client = new DefaultHttpClient(params);

        client.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                // Add header to accept gzip content
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
            }
        });

        client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) {
                // Inflate any responses compressed with gzip
                final HttpEntity entity = response.getEntity();
                if(entity == null)
                	return;
                
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            break;
                        }
                    }
                }
            }
        });

        return client;
    }

    Context getContext(){
    	return mContext;
    }
}
