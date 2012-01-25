package com.whiterabbit.postman.commands;

import android.content.Intent;


public abstract class CommandFactory {
	public abstract ServerCommand createCommand();
	
	public abstract ServerCommand createCommand(Intent i);
}
