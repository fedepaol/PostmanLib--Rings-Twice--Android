package com.whiterabbit.postman.oauth;

import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.whiterabbit.postman.R;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/23/12
 * Time: 8:53 PM
 */
public class OAuthFragment extends DialogFragment {

    private WebView webViewOauth;
    String mUrl;
    OAuthReceivedInterface mReceivedInterface;

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
        mUrl = getArguments().getString("URL");
    }


    public void setReceivedInterface(OAuthReceivedInterface mReceivedInterface) {
        this.mReceivedInterface = mReceivedInterface;
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //check if the login was successful and the access token returned
            //this test depend of your API
            if (url.contains("oauth_verifier=")) {
                //save your token
                saveAccessToken(url);
                return true;
            }else{
                notifyAuthenticationFailed();
                return false;
            }
        }
    }

    private void saveAccessToken(String url) {
        if(mReceivedInterface != null){
            Uri uri=Uri.parse(url);
            String verifier = uri.getQueryParameter("oauth_verifier");
            mReceivedInterface.onAuthReceived(verifier);
        }
        getDialog().dismiss();
    }

    private void notifyAuthenticationFailed(){
        mReceivedInterface.onAuthFailed("Could not verify the auth token, wrong callback url");
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
        getDialog().setTitle("Use your Instagram account");
        return v;
    }
}
