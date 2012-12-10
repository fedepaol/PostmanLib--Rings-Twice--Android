package com.whiterabbit.postmanlibsample;

import com.whiterabbit.postman.commands.CommandFactory;
import com.whiterabbit.postman.commands.ServerCommand;
import com.whiterabbit.postman.commands.UnknownCommandException;

public class TwitterCommandFactory extends CommandFactory {



	@Override
	public ServerCommand createCommand(String simpleClassName) throws UnknownCommandException{
		if(simpleClassName.equals(TwitterUserGetCommand.class.getSimpleName())){
			TwitterUserGetCommand c = new TwitterUserGetCommand();
			return c;
		}
		
		throw new UnknownCommandException();
		
	}

}
