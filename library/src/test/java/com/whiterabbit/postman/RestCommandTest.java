package com.whiterabbit.postman;

import com.whiterabbit.postman.commands.RestServerCommand;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class RestCommandTest{
    private ServerInteractionHelper mHelper;
    private SimpleClientActivity mActivity;
    private ShadowActivity mShadowOfActivity;


    @Before
    public void setUp() throws Exception {
        mHelper = ServerInteractionHelper.getInstance();
        mActivity = new SimpleClientActivity();
        mShadowOfActivity = shadowOf(mActivity);
    }



    @Test
    public void testCommandExecution(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);

        final OAuthRequest mockedRequest = mock(OAuthRequest.class);
        SimpleRestStrategy s = new SimpleRestStrategy(mockedRequest, false);
        RestServerCommand command = new RestServerCommand(s){

            @Override
            protected OAuthRequest getRequest(Verb v, String url) {
                return mockedRequest;
            }
        };

        Response mockedResponse = mock(Response.class);
        when(mockedRequest.send()).thenReturn(mockedResponse);

        when(mockedResponse.getBody()).thenReturn("Ciao");
        when(mockedResponse.getCode()).thenReturn(200);

        command.execute(mActivity);
        verify(mockedRequest).addHeader("Key", "Value");
        verify(mockedRequest).send();

        assertEquals(mActivity.getServerResult(), "Ok");

    }


    @Test
    public void testCommandFailure(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);

        final OAuthRequest mockedRequest = mock(OAuthRequest.class);
        SimpleRestStrategy s = new SimpleRestStrategy(mockedRequest, false);
        RestServerCommand command = new RestServerCommand(s){

            @Override
            protected OAuthRequest getRequest(Verb v, String url) {
                return mockedRequest;
            }
        };

        when(mockedRequest.send()).thenThrow(new OAuthException("Fava"));

        command.execute(mActivity);
        verify(mockedRequest).addHeader("Key", "Value");
        verify(mockedRequest).send();

        assertTrue(mActivity.isIsFailure());

    }

}

