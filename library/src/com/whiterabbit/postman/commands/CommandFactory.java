package com.whiterabbit.postman.commands;


/**
 * Class used by the intentservice to build the correct 
 * server command
 * @author fede
 *
 */
public abstract class CommandFactory {
	

	
	/**
	 * Abstract method to create an empty server command which will be filled by an intent
	 *
	 * Usage: compare the simpleClassName to the one of your servercommand implementation and return the right class
	 * throw UnknownCommandException if the name is unknown
	 * 
	 * @param simpleClassName the name of the class to be constructed
	 * @return the empty servercommand of the given class 
	 */
	public abstract ServerCommand createCommand(String simpleClassName) throws UnknownCommandException;
	
}
