package com.whiterabbit.postman;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Intent;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

@RunWith(RobolectricTestRunner.class)
public class HelperTest {
	TestServerCommand mCommand;
	Activity mContext;
	
	@Before
    public void setUp() throws Exception {
		mCommand = new TestServerCommand("prova", Long.valueOf(23));
		mContext = new Activity();
    }

    @Test
    public void testLaunchesService() throws Exception {
    	ServerInteractionHelper h = ServerInteractionHelper.initWithCommandFactory(new TestCommandFactory());
    	
    	h.sendCommand(mContext, mCommand, "Hello");
    	
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
    
}