package com.whiterabbit.postman;

import android.content.Intent;
import com.whiterabbit.postman.commands.RestServerCommand;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class RestCommandTest{
    private ServerInteractionHelper mHelper;
    private SimpleClientActivity mActivity;


    @Before
    public void setUp() throws Exception {
        mActivity = new SimpleClientActivity();
        mHelper.resetInstance();
        mHelper = ServerInteractionHelper.getInstance(mActivity);
    }



    @Test
    public void testCommandExecution(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);

        final OAuthRequest mockedRequest = mock(OAuthRequest.class);
        SimpleRestRequest s = new SimpleRestRequest(mockedRequest, false);
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

        assertEquals(s.getResultStatus(), 200);
        assertEquals(mActivity.getServerResult(), "Ok");

    }

   @Test
    public void testCommandExecutionBulk(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);

        final OAuthRequest mockedRequest = mock(OAuthRequest.class);
        SimpleRestRequest s = new SimpleRestRequest(mockedRequest, false);
        SimpleRestRequest s1 = new SimpleRestRequest(mockedRequest, false);
        SimpleRestRequest s2 = new SimpleRestRequest(mockedRequest, false);
        RestServerCommand command = new RestServerCommand(s, s1, s2){

            @Override
            protected OAuthRequest getRequest(Verb v, String url) {
                return mockedRequest;
            }
        };

        Intent i = new Intent();
        i.putExtra("command", command);
        RestServerCommand newCommand = i.getParcelableExtra("command");
        Response mockedResponse = mock(Response.class);
        when(mockedRequest.send()).thenReturn(mockedResponse);

        when(mockedResponse.getBody()).thenReturn("Ciao");
        when(mockedResponse.getCode()).thenReturn(200);

        newCommand.execute(mActivity);
        verify(mockedRequest, times(3)).addHeader("Key", "Value");
        verify(mockedRequest, times(3)).send();

        assertEquals(mActivity.getServerResult(), "Ok");
        assertEquals(s.getResultString(), "Ciao");
        assertEquals(s1.getResultString(), "Ciao");
        assertEquals(s2.getResultString(), "Ciao");

    }



    @Test
    public void testCommandFailure(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);

        final OAuthRequest mockedRequest = mock(OAuthRequest.class);
        SimpleRestRequest s = new SimpleRestRequest(mockedRequest, false);
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
        assertTrue(s.isExceptionThrown());

    }

    @Test
    public void testCommandExecutionNested(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);

        final OAuthRequest mockedRequest = mock(OAuthRequest.class);
        NestedRestRequest s = new NestedRestRequest(mockedRequest, false);
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
        verify(mockedRequest, times(2)).addHeader("Key", "Value");
        verify(mockedRequest, times(2)).send();

        assertEquals(mActivity.getServerResult(), "Ok");

    }


    @Test
    public void testCommandNestedCommandFailure(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);

        final OAuthRequest mockedRequest = mock(OAuthRequest.class);
        NestedRestRequest s = new NestedRestRequest(mockedRequest, false);
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




    @Test
    public void testCommand401(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);

        final OAuthRequest mockedRequest = mock(OAuthRequest.class);
        SimpleRestRequest s = new SimpleRestRequest(mockedRequest, false);
        RestServerCommand command = new RestServerCommand(s){

            @Override
            protected OAuthRequest getRequest(Verb v, String url) {
                return mockedRequest;
            }
        };

        Response mockedResponse = mock(Response.class);
        when(mockedRequest.send()).thenReturn(mockedResponse);

        when(mockedResponse.getBody()).thenReturn(null);
        when(mockedResponse.getCode()).thenReturn(401);

        command.execute(mActivity);
        verify(mockedRequest).addHeader("Key", "Value");
        verify(mockedRequest).send();

        assertTrue(mActivity.isIsFailure());
        assertEquals(s.getResultStatus(), 401);
    }




}

