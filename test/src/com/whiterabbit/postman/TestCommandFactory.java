package com.whiterabbit.postman;

import com.whiterabbit.postman.commands.CommandFactory;
import com.whiterabbit.postman.commands.ServerCommand;

public class TestCommandFactory extends CommandFactory {

	@Override
	public ServerCommand createCommand(String name) {
		if(name.equals(TestServerCommand.class.getSimpleName())){
			ServerCommand res = new  TestServerCommand();
			return res;
		}
		return null;
	}

}
