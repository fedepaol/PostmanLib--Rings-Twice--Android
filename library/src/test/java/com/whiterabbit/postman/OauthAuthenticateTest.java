package com.whiterabbit.postman;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.OAuthServiceException;
import com.whiterabbit.postman.oauth.OAuthFragment;
import com.whiterabbit.postman.oauth.OAuthServiceInfo;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowDialogFragment;
import com.xtremelabs.robolectric.shadows.ShadowWebView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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

    private ServerInteractionHelper mHelper;
    private SimpleClientActivity mActivity;
    private ShadowActivity mShadowOfActivity;
    private final static String SERVICE_NAME = "Service";


    @Before
    public void setUp() throws Exception {
        mHelper = ServerInteractionHelper.getInstance();
        mActivity = new SimpleClientActivity();
        mShadowOfActivity = shadowOf(mActivity);
    }



    @Test
    public void testRegisterServiceNotAuth(){
        OAuthService mockedOAuthService = mock(OAuthService.class);
        mHelper.registerOAuthService(mockedOAuthService, SERVICE_NAME, mActivity);
        OAuthRequest mockedRequest = mock(OAuthRequest.class);
        SimpleRestCommand mCommand = new SimpleRestCommand(mockedRequest);

        boolean exceptionThrown = false;
        try {
            mCommand.setOAuthSigner(SERVICE_NAME);
        } catch (OAuthServiceException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

    }


    @Test
    public void testServiceRegistration(){
        OAuthService mockedOAuthService = mock(OAuthService.class);
        Token requestToken = new Token(REQUEST_TOKEN, REQUEST_SECRET, REQUEST_RAW);
        when(mockedOAuthService.getRequestToken()).thenReturn(requestToken);
        when(mockedOAuthService.getAuthorizationUrl(any(Token.class))).thenReturn(AUTH_URL);
        ServerInteractionHelper.getInstance().registerEventListener(mActivity, mActivity);


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

        Token accessToken = new Token(ACCESS_TOKEN, ACCESS_SECRET, ACCESS_RAW);
        when(mockedOAuthService.getAccessToken(any(Token.class), any(Verifier.class))).thenReturn(accessToken);

        Robolectric.getBackgroundScheduler().runOneTask();
        assertTrue(mActivity.isServiceAuthenticatedSuccess());
        assertEquals(mActivity.getServiceAuthenticated(), SERVICE_NAME);
        try {
            OAuthServiceInfo sInfo = ServerInteractionHelper.getInstance().getRegisteredService(SERVICE_NAME);
            assertEquals(sInfo.getAccessToken(), accessToken);

        } catch (OAuthServiceException e) {
            fail();
        }


    }

}

