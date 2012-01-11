package com.whiterabbit.postman;

import com.whiterabbit.postman.commands.CommandFactory;
import com.whiterabbit.postman.commands.ServerCommand;

public class TestCommandFactory extends CommandFactory {

	@Override
	public ServerCommand createCommand() {
		return new TestServerCommand();
	}

}
