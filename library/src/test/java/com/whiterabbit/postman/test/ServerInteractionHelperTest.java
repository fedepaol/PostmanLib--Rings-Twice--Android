package com.whiterabbit.postman.test;

import android.content.Intent;
import com.whiterabbit.postman.InteractionService;
import com.whiterabbit.postman.ServerInteractionHelper;
import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.SendingCommandException;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class ServerInteractionHelperTest {
    private ServerInteractionHelper mHelper;
    private SimpleClientActivity mActivity;
    private ShadowActivity mShadowOfActivity;
    private SimpleServerCommand mCommand;
    private static final String COMMAND_REQUEST = "MyRequestId";


	@Before
    public void setUp() throws Exception {
        mHelper = ServerInteractionHelper.getInstance();
        mActivity = new SimpleClientActivity();
        mShadowOfActivity = shadowOf(mActivity);
        mCommand = new SimpleServerCommand();
    }



	@Test
    public void testSendsCommand(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);
        try {
            mHelper.sendCommand(mActivity, mCommand, COMMAND_REQUEST);
        } catch (SendingCommandException e) {
            fail("sending command exception");
        }

        Intent sentIntent = mShadowOfActivity.getNextStartedService();
        ShadowIntent shIntent = shadowOf(sentIntent);
        assertEquals(shIntent.getIntentClass(), InteractionService.class);


    }

}

