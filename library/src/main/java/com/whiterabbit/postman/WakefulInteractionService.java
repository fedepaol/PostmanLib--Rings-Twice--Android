package com.whiterabbit.postman;

import android.content.Intent;
import android.util.Log;
import com.whiterabbit.postman.commands.ServerCommand;
import com.whiterabbit.postman.utils.Constants;
import com.whiterabbit.postman.wakeful.WakefulIntentService;

/**
 * WakefulIntentService child. Allows ServerCommands to be executed in a wakeful way.
 */
public class WakefulInteractionService extends WakefulIntentService {
    public WakefulInteractionService() {
        super("WakefulInteraction");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        ServerCommand c = intent.getParcelableExtra(Constants.PAYLOAD);
        c.fillFromIntent(intent);

        if (c != null) {
            c.execute(this);
        } else {
            Log.e(Constants.LOG_TAG, "Unable to convert message");
            ServerCommand.notifyUnrecoverableError(intent, getString(R.string.unable_to_convert), this);
        }
    }

}
