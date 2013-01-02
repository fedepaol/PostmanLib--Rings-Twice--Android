package com.whiterabbit.postman;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.OAuthServiceException;
import com.whiterabbit.postman.oauth.OAuthFragment;
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OauthAuthenticateTest {
    private final String REQUEST_TOKEN = "reqToken";
    private final String REQUEST_SECRET = "reqSecret";
    private final String ACCESS_TOKEN = "accToken";
    private final String ACCESS_SECRET = "accSecret";
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
        Token requestToken = new Token(REQUEST_TOKEN, REQUEST_SECRET);
        when(mockedOAuthService.getRequestToken()).thenReturn(requestToken);
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
        client.shouldOverrideUrlLoading(webView, "www.adasda.com/oauth_verifier=dasdasda");

        Token accessToken = new Token(ACCESS_TOKEN, ACCESS_SECRET);
        when(mockedOAuthService.getAccessToken(any(Token.class), any(Verifier.class))).thenReturn(accessToken);

        Robolectric.getBackgroundScheduler().runOneTask();




    }

}

