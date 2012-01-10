
package com.whiterabbit.postman;

import com.whiterabbit.postman.commands.CommandFactory;
import com.whiterabbit.postman.commands.ServerCommand;

import android.app.IntentService;
import android.content.Intent;

/**
 * Intent service used to interact with rest
 *
 * @author fede
 *
 */
public class InteractionService extends IntentService {

    public InteractionService() {
        super("InteractionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	CommandFactory f = ServerInteractionHelper.getInstance().getCommandFactory();
    	ServerCommand c = f.createCommand();
    	c.execute(this);
    }

}
