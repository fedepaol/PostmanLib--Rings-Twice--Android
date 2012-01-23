package com.whiterabbit.postman;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Intent;

import com.whiterabbit.postman.commands.CommandFactory;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

@RunWith(RobolectricTestRunner.class)
public class HelperTest {
	TestServerCommand mCommand;
	Activity mContext;
	final static String mReqId = "REQUEST";
	final static String mResultString = "RESULTSTRING";
	
	@Before
    public void setUp() throws Exception {
		mCommand = new TestServerCommand("prova", Long.valueOf(23));
		mCommand.setRequestId(mReqId);
		mContext = new Activity();
    }

    @Test
    public void testLaunchesService() throws Exception {
    	ServerInteractionHelper h = ServerInteractionHelper.initWithCommandFactory(new TestCommandFactory());
    	
    	h.sendCommand(mContext, mCommand, "Hello1");
    	
    	ShadowActivity shadowActivity = Robolectric.shadowOf(mContext);
    	Intent i = shadowActivity.getNextStartedService();
        ShadowIntent shadowIntent = Robolectric.shadowOf(i);
        
        assertEquals(shadowIntent.getComponent().getClassName(), InteractionService.class.getName());
    }
    
    @Test
    public void testRequestPending() throws Exception {
    	ServerInteractionHelper h = ServerInteractionHelper.initWithCommandFactory(new TestCommandFactory());
    	
    	h.sendCommand(mContext, mCommand, "Hello");
    	
    	try{
    		h.sendCommand(mContext, mCommand, "Hello");
        	fail();
    	}catch(SendingCommandException e){
    		assertEquals(e.getMessage(), "Same request already pending");
    	}
    }
    
    @Test
    public void testReceivesResult() throws Exception {
    	ServerInteractionHelper h = ServerInteractionHelper.initWithCommandFactory(new TestCommandFactory());
    	
    	ServerInteractionResponseInterface i = new ServerInteractionResponseInterface(){

			@Override
			public void onServerResult(String result, String requestId) {
				assertEquals(requestId, mReqId);	
				assertEquals(result, mResultString);
			}

			@Override
			public void onServerError(String result, String requestId) {
				fail();	// expecting success
			}    		
    	};
    	
    	h.registerEventListener(i, mContext);
    	mCommand.notifyResultToAll(mResultString, mContext);    	
    }

    
    @Test
    public void testReceivesFail() throws Exception {
    	ServerInteractionHelper h = ServerInteractionHelper.initWithCommandFactory(new TestCommandFactory());
    	
    	ServerInteractionResponseInterface i = new ServerInteractionResponseInterface(){

			@Override
			public void onServerResult(String result, String requestId) {
				fail();	// expecting failure
			}

			@Override
			public void onServerError(String result, String requestId) {
				assertEquals(requestId, mReqId);	
				assertEquals(result, mResultString);

			}    		
    	};
    	
    	h.registerEventListener(i, mContext);
    	mCommand.notifyErrorToAll(mResultString, mContext);    	
    }
    
    @Test
    public void testSingleton() throws Exception {
    	CommandFactory c = new TestCommandFactory();
    	ServerInteractionHelper h = ServerInteractionHelper.initWithCommandFactory(c);
    	
    	
    	CommandFactory c1 = ServerInteractionHelper.getInstance().getCommandFactory();
    	assertNotNull(c1);
    }
    
}