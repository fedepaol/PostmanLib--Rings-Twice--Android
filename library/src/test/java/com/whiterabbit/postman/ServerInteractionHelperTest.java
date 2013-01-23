package com.whiterabbit.postman;

import android.content.Intent;
import com.whiterabbit.postman.exceptions.SendingCommandException;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ServerInteractionHelperTest {
    private ServerInteractionHelper mHelper;
    private SimpleClientActivity mActivity;
    private ShadowActivity mShadowOfActivity;
    private static final String COMMAND_REQUEST = "MyRequestId";
    private static final String RESULT_MESSAGE = "this is a result message";


	@Before
    public void setUp() throws Exception {
        mHelper = ServerInteractionHelper.getInstance();
        mActivity = new SimpleClientActivity();
        mShadowOfActivity = shadowOf(mActivity);
    }


    /**
     * To be called after a sendCommand to shortcut the startService call
     * and the handleIntent method of the intent service
     */
    private void shortcutIntentService(){
        Intent sentIntent = mShadowOfActivity.getNextStartedService();
        ShadowIntent shIntent = shadowOf(sentIntent);
        assertEquals(shIntent.getIntentClass(), InteractionService.class);
        InteractionService service = new InteractionService();
        service.onHandleIntent(sentIntent);

    }

	@Test
    public void testSendsCommand(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);

        SimpleServerCommand command = new SimpleServerCommand(true, RESULT_MESSAGE);
        try {
            mHelper.sendCommand(mActivity, command, COMMAND_REQUEST);
        } catch (SendingCommandException e) {
            fail("sending command exception");
        }

        shortcutIntentService();

    }



	@Test
    public void testCommandSuccess(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);
        SimpleServerCommand command = new SimpleServerCommand(true, RESULT_MESSAGE);
        try {
            mHelper.sendCommand(mActivity, command, COMMAND_REQUEST);
        } catch (SendingCommandException e) {
            fail("sending command exception");
        }

        shortcutIntentService();
        assertFalse(mActivity.isIsFailure());
        assertEquals(mActivity.getRequestReceived(), COMMAND_REQUEST);
        assertEquals(mActivity.getServerResult(), RESULT_MESSAGE);
        assertFalse(mHelper.isRequestAlreadyPending(COMMAND_REQUEST));
    }


	@Test
    public void testCommandFail(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);
        SimpleServerCommand command = new SimpleServerCommand(false, RESULT_MESSAGE);
        try {
            mHelper.sendCommand(mActivity, command, COMMAND_REQUEST);
        } catch (SendingCommandException e) {
            fail("sending command exception");
        }

        shortcutIntentService();
        assertTrue(mActivity.isIsFailure());
        assertEquals(mActivity.getRequestReceived(), COMMAND_REQUEST);
        assertEquals(mActivity.getServerResult(), RESULT_MESSAGE);
    }


	@Test
    public void testTwoSameCommandsAndPending(){
        mActivity.onCreate(null);
        mHelper.registerEventListener(mActivity, mActivity);
        SimpleServerCommand command = new SimpleServerCommand(false, RESULT_MESSAGE);
        try {
            mHelper.sendCommand(mActivity, command, COMMAND_REQUEST);
        } catch (SendingCommandException e) {
            fail("sending command exception");
        }

        boolean exceptionThrown = false;
        try {
            mHelper.sendCommand(mActivity, command, COMMAND_REQUEST);
            assertTrue(mHelper.isRequestAlreadyPending(COMMAND_REQUEST));
        } catch (SendingCommandException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

    }

}

