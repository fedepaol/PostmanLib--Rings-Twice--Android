
package com.whiterabbit.postman;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.whiterabbit.postman.commands.CommandFactory;
import com.whiterabbit.postman.commands.ServerCommand;
import com.whiterabbit.postman.commands.UnknownCommandException;
import com.whiterabbit.postman.utils.Constants;
import com.whiterabbit.serverintaraction.R;

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
    	try{
    		ServerCommand c = f.createCommand(ServerCommand.getTypeFromFilledIntent(intent));
    		if(c == null){
    			throw new UnknownCommandException();
    		}
    		
    		c.fillFromIntent(intent);
    		c.execute(this);
    	}catch (UnknownCommandException e){
    		Log.e(Constants.LOG_TAG, "Unable to convert message");
    		ServerCommand.notifyUnrecoverableError(intent, getString(R.string.unable_to_convert), this);
    	}
    }

}
