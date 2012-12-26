
package com.whiterabbit.postman;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.whiterabbit.postman.commands.ServerCommand;
import com.whiterabbit.postman.com.whiterabbit.postman.exceptions.UnknownCommandException;
import com.whiterabbit.postman.utils.Constants;


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
    	try{
            String messageType = ServerCommand.getTypeFromIntent(intent);
            //try {
                    ServerCommand c = (ServerCommand) intent.getParcelableExtra(Constants.PAYLOAD);


                            //Class.forName(messageType).getConstructor(Intent.class).newInstance(intent);
                    c.execute(this);

            if (null == c){
                throw new UnknownCommandException();
            }
            /*
            } catch (ClassNotFoundException e) {

            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
                throw new UnknownCommandException();
            }
            */

    	}catch (UnknownCommandException e){
    		Log.e(Constants.LOG_TAG, "Unable to convert message");
    		ServerCommand.notifyUnrecoverableError(intent, getString(R.string.unable_to_convert), this);
    	}
    }

}
