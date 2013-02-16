package com.whiterabbit.postman.oauth;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.whiterabbit.postman.R;
import com.whiterabbit.postman.utils.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/23/12
 * Time: 8:53 PM
 */

/**
 * Dialog fragment that hosts the webview responsible for
 * the oauth authentication
 */
class OAuthFragment extends DialogFragment {

    private WebView webViewOauth;
    String mUrl;
    OAuthReceivedInterface mReceivedInterface;
    private boolean mAuthFound;

    public static OAuthFragment newInstance(String url, OAuthReceivedInterface receivedInterface) {
        OAuthFragment f = new OAuthFragment();
        f.setReceivedInterface(receivedInterface);  // TODO weak reference
        Bundle args = new Bundle();
        args.putString("URL", url);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuthFound = false;
        mUrl = getArguments().getString("URL");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.i(Constants.LOG_TAG, "Oauth dialog dismissed with no authentication");
        if(!mAuthFound){
            notifyAuthenticationFailed();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.i(Constants.LOG_TAG, "Oauth dialog dismissed with no authentication");
        if(!mAuthFound){
            notifyAuthenticationFailed();
        }
    }


    public void setReceivedInterface(OAuthReceivedInterface receivedInterface) {
        mReceivedInterface = receivedInterface;
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //check if the login was successful and the access token returned
            //this test depend of your API
            if (url.contains("oauth_verifier=")) {
                //save your token
                saveAccessToken(url);
                getDialog().dismiss();
                return true;
            }else{
                Log.d(Constants.LOG_TAG,
                       String.format("Could not find oauth_verifier in callback url, %s , are you sure you set a callback url in your service?",
                               url));
                return false;
            }
        }
    }

    private void saveAccessToken(String url) {
        if(mReceivedInterface != null){
            Uri uri=Uri.parse(url);
            String verifier = uri.getQueryParameter("oauth_verifier");
            mReceivedInterface.onAuthReceived(verifier);
            mAuthFound = true;
        }
    }

    private void notifyAuthenticationFailed(){
        if(mReceivedInterface != null){
            mReceivedInterface.onAuthFailed("Could not verify the auth token, wrong callback url");
        }
    }


    @Override
    public void onViewCreated(View arg0, Bundle arg1) {
        super.onViewCreated(arg0, arg1);
        //load the url of the oAuth login page
        webViewOauth.loadUrl(mUrl);
        //set the web client
        webViewOauth.setWebViewClient(new MyWebViewClient());
        //activates JavaScript (just in case)
        WebSettings webSettings = webViewOauth.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Retrieve the webview
        View v = inflater.inflate(R.layout.oauth_screen, container, false);
        webViewOauth = (WebView) v.findViewById(R.id.web_oauth);
        return v;
    }
}
