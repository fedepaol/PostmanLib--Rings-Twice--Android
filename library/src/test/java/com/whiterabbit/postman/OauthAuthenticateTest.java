package com.whiterabbit.postman;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.OAuthServiceException;
import com.whiterabbit.postman.oauth.OAuthFragment;
import com.whiterabbit.postman.oauth.OAuthServiceInfo;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowDialogFragment;
import com.xtremelabs.robolectric.shadows.ShadowWebView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OauthAuthenticateTest {
    private final String REQUEST_TOKEN = "reqToken";
    private final String REQUEST_SECRET = "reqSecret";
    private final String REQUEST_RAW = "reqRawString";
    private final String ACCESS_TOKEN = "accToken";
    private final String ACCESS_SECRET = "accSecret";
    private final String ACCESS_RAW = "accRaw";
    private final String AUTH_URL = "www.request_url.com";

    private Token mRequestToken;
    private Token mAuthToken;
    private ServerInteractionHelper mHelper;
    private SimpleClientActivity mActivity;
    private final static String SERVICE_NAME = "Service";
    private OAuthService mockedOAuthService;



    @Before
    public void setUp() throws Exception {
        mHelper = ServerInteractionHelper.getInstance();
        mActivity = new SimpleClientActivity();



        mRequestToken = new Token(REQUEST_TOKEN, REQUEST_SECRET, REQUEST_RAW);
        mAuthToken = new Token(ACCESS_TOKEN, ACCESS_SECRET, ACCESS_RAW);


        mockedOAuthService = mock(OAuthService.class);
        mHelper.registerOAuthService(mockedOAuthService, SERVICE_NAME, mActivity);

        ServerInteractionHelper.getInstance().registerEventListener(mActivity, mActivity);

    }



    @Test
    public void testRegisterServiceNotAuth(){
        OAuthRequest mockedRequest = mock(OAuthRequest.class);
        SimpleRestCommand mCommand = new SimpleRestCommand(mockedRequest);
        mCommand.setOAuthSigner(SERVICE_NAME);

        boolean exceptionThrown = false;
            mCommand.execute(mActivity); // TODO Check if throws the correct exception
        assertTrue(exceptionThrown);

    }


    @Test
    public void testServiceRegistration(){
        when(mockedOAuthService.getRequestToken()).thenReturn(mRequestToken);
        when(mockedOAuthService.getAuthorizationUrl(any(Token.class))).thenReturn(AUTH_URL);


        mHelper.registerOAuthService(mockedOAuthService, SERVICE_NAME, mActivity);

        Robolectric.getBackgroundScheduler().pause();

        try {
            mHelper.authenticate(mActivity, SERVICE_NAME);
        } catch (OAuthServiceException e) {
            fail();
        }

        Robolectric.getBackgroundScheduler().runOneTask();
        Robolectric.getUiThreadScheduler().runOneTask();

        OAuthFragment s = (OAuthFragment) ShadowDialogFragment.getLatestDialogFragment();
        WebView webView = (WebView) s.getView().findViewById(R.id.web_oauth);
        ShadowWebView webShadow = shadowOf(webView);
        WebViewClient client = webShadow.getWebViewClient();

        Robolectric.getBackgroundScheduler().pause();
        client.shouldOverrideUrlLoading(webView, "https://api.twitter.com/oauth/authorize?oauth_verifier=Z6eEdO8MOmk394WozF5oKyuAv855l4Mlqo7hhlSLik");

        when(mockedOAuthService.getAccessToken(any(Token.class), any(Verifier.class))).thenReturn(mAuthToken);

        Robolectric.getBackgroundScheduler().runOneTask();
        assertTrue(mActivity.isServiceAuthenticatedSuccess());
        assertEquals(mActivity.getServiceAuthenticated(), SERVICE_NAME);
        try {
           // even if it should be called on another thread
            OAuthServiceInfo sInfo = ServerInteractionHelper.getInstance().getRegisteredService(SERVICE_NAME, mActivity);
            assertEquals(sInfo.getAccessToken(), mAuthToken);

        } catch (OAuthServiceException e) {
            fail();
        }


    }




    @Test
    public void testServiceRequestFails(){
        when(mockedOAuthService.getRequestToken()).thenReturn(mRequestToken);
        when(mockedOAuthService.getAuthorizationUrl(any(Token.class))).thenThrow(new OAuthException("FAVA") );


        mHelper.registerOAuthService(mockedOAuthService, SERVICE_NAME, mActivity);

        Robolectric.getBackgroundScheduler().pause();

        try {
            mHelper.authenticate(mActivity, SERVICE_NAME);
        } catch (OAuthServiceException e) {
            fail();
        }

        Robolectric.getBackgroundScheduler().runOneTask();
        Robolectric.getUiThreadScheduler().runOneTask();

        assertFalse(mActivity.isServiceAuthenticatedSuccess());
        assertEquals(mActivity.getServiceNotAuthenticatedReason(), "FAVA");

    }


    @Test
    public void testServiceDialogDismissed(){
        when(mockedOAuthService.getRequestToken()).thenReturn(mRequestToken);
        when(mockedOAuthService.getAuthorizationUrl(any(Token.class))).thenReturn(AUTH_URL);


        mHelper.registerOAuthService(mockedOAuthService, SERVICE_NAME, mActivity);

        Robolectric.getBackgroundScheduler().pause();

        try {
            mHelper.authenticate(mActivity, SERVICE_NAME);
        } catch (OAuthServiceException e) {
            fail();
        }

        Robolectric.getBackgroundScheduler().runOneTask();
        Robolectric.getUiThreadScheduler().runOneTask();

        OAuthFragment s = (OAuthFragment) ShadowDialogFragment.getLatestDialogFragment();
        WebView webView = (WebView) s.getView().findViewById(R.id.web_oauth);
        s.dismiss();
        s.onDismiss(null);  // this should be a result of the former. I've should modify robolectric but this is faster

        assertFalse(mActivity.isServiceAuthenticatedSuccess());
        assertEquals(mActivity.getServiceNotAuthenticatedReason(), "Could not verify the auth token, wrong callback url");


    }


    @Test
    public void testServiceWebviewReturnsBadUrl(){
        when(mockedOAuthService.getRequestToken()).thenReturn(mRequestToken);
        when(mockedOAuthService.getAuthorizationUrl(any(Token.class))).thenReturn(AUTH_URL);


        mHelper.registerOAuthService(mockedOAuthService, SERVICE_NAME, mActivity);

        Robolectric.getBackgroundScheduler().pause();

        try {
            mHelper.authenticate(mActivity, SERVICE_NAME);
        } catch (OAuthServiceException e) {
            fail();
        }

        Robolectric.getBackgroundScheduler().runOneTask();
        Robolectric.getUiThreadScheduler().runOneTask();

        OAuthFragment s = (OAuthFragment) ShadowDialogFragment.getLatestDialogFragment();
        WebView webView = (WebView) s.getView().findViewById(R.id.web_oauth);
        ShadowWebView webShadow = shadowOf(webView);
        WebViewClient client = webShadow.getWebViewClient();

        assertEquals(webShadow.getLastLoadedUrl(), AUTH_URL);

        Robolectric.getBackgroundScheduler().pause();
        client.shouldOverrideUrlLoading(webView, "www.google.com");

        s.dismiss();
        s.onDismiss(null);  // this should be a result of the former. I've should modify robolectric but this is faster

        assertFalse(mActivity.isServiceAuthenticatedSuccess());
        assertEquals(mActivity.getServiceNotAuthenticatedReason(), "Could not verify the auth token, wrong callback url");


    }


    @Test
    public void testTokenAuthFails(){
        when(mockedOAuthService.getRequestToken()).thenReturn(mRequestToken);
        when(mockedOAuthService.getAuthorizationUrl(any(Token.class))).thenReturn(AUTH_URL);


        mHelper.registerOAuthService(mockedOAuthService, SERVICE_NAME, mActivity);

        Robolectric.getBackgroundScheduler().pause();

        try {
            mHelper.authenticate(mActivity, SERVICE_NAME);
        } catch (OAuthServiceException e) {
            fail();
        }

        Robolectric.getBackgroundScheduler().runOneTask();
        Robolectric.getUiThreadScheduler().runOneTask();

        OAuthFragment s = (OAuthFragment) ShadowDialogFragment.getLatestDialogFragment();
        WebView webView = (WebView) s.getView().findViewById(R.id.web_oauth);
        ShadowWebView webShadow = shadowOf(webView);
        WebViewClient client = webShadow.getWebViewClient();

        Robolectric.getBackgroundScheduler().pause();
        client.shouldOverrideUrlLoading(webView, "https://api.twitter.com/oauth/authorize?oauth_verifier=Z6eEdO8MOmk394WozF5oKyuAv855l4Mlqo7hhlSLik");

        when(mockedOAuthService.getAccessToken(any(Token.class), any(Verifier.class))).thenThrow(new OAuthException("OAUTHFAILED"));

        Robolectric.getBackgroundScheduler().runOneTask();
        assertFalse(mActivity.isServiceAuthenticatedSuccess());
        assertEquals(mActivity.getServiceNotAuthenticatedReason(), "OAUTHFAILED");
        try {
            OAuthServiceInfo sInfo = ServerInteractionHelper.getInstance().getRegisteredService(SERVICE_NAME, mActivity);
            fail();
        } catch (OAuthServiceException e) {
        }


    }


}

