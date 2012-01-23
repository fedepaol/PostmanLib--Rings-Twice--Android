package com.whiterabbit.postman;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class IntentServiceTest {
	private class MyService extends InteractionService {
		public void onHandleIntentPub(Intent i){
			onHandleIntent(i);
		}
	}
	
	private class MyCommand extends TestServerCommand{
		public MyCommand(String desc, Long code){
			super(desc, code);
		}

		@Override
		public void execute(Context c) {
			
			super.execute(c);
			org.junit.Assert.assertEquals(this.getRequestId(), mReqId);
		}
		
	}
	
	
	TestServerCommand mCommand;
	
	Activity mContext;
	MyService mService;
	final static String mReqId = "REQUEST";
	final static String mResultString = "RESULTSTRING";
	
	@Before
    public void setUp() throws Exception {
		mCommand = new MyCommand("prova", Long.valueOf(23));
		mCommand.setRequestId(mReqId);
		mService = new MyService();
		mContext = new Activity();
		
    }

    @Test
    public void testService() throws Exception {
    	ServerInteractionHelper h = ServerInteractionHelper.initWithCommandFactory(new TestCommandFactory());
    	mCommand.setRequestId(mReqId);
    	Intent i = new Intent();
    	mCommand.fillIntent(i);
    	mService.onHandleIntentPub(i);
    }
    

}