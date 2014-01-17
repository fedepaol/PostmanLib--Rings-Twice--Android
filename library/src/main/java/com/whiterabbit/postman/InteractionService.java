
package com.whiterabbit.postman;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.whiterabbit.postman.commands.ServerCommand;
import com.whiterabbit.postman.utils.Constants;


/**
 * Intent service used to interact with rest
 *
 * @author fede
 */
public class InteractionService extends IntentService {

    public InteractionService() {
        super("InteractionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ServerCommand c = (ServerCommand) intent.getParcelableExtra(Constants.PAYLOAD);
        c.fillFromIntent(intent);

        if (c != null) {
            c.execute(this);
        } else {
            Log.e(Constants.LOG_TAG, "Unable to convert message");
            ServerCommand.notifyUnrecoverableError(intent, getString(R.string.unable_to_convert), this);
        }

    }

}
