package com.whiterabbit.postman.test;

import com.whiterabbit.postman.ServerInteractionHelper;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class ServerInteractionHelperTest {
    ServerInteractionHelper mHelper;


	@Before
    public void setUp() throws Exception {
        mHelper = ServerInteractionHelper.getInstance();


    }



	@Test
    public void testSendsCommand(){

	}

}

