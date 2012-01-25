package com.whiterabbit.postman;

import android.content.Intent;

import com.whiterabbit.postman.commands.CommandFactory;
import com.whiterabbit.postman.commands.ServerCommand;

public class TestCommandFactory extends CommandFactory {

	@Override
	public ServerCommand createCommand() {
		return new TestServerCommand();
	}

	@Override
	public ServerCommand createCommand(Intent i) {
		// TODO Auto-generated method stub
		return null;
	}

}
