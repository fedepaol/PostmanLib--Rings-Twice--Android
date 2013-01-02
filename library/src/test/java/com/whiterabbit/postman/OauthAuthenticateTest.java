package com.whiterabbit.postman;

import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.OAuthServiceException;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scribe.model.OAuthRequest;
import org.scribe.oauth.OAuthService;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class OauthAuthenticateTest {
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
        mHelper.registerOAuthService(mockedOAuthService, SERVICE_NAME, mActivity);

        Robolectric.getBackgroundScheduler().pause();

        try {
            mHelper.authenticate(mActivity, SERVICE_NAME);
        } catch (OAuthServiceException e) {
            fail();
        }

        Robolectric.getBackgroundScheduler().runOneTask();


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

}

