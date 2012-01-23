package com.whiterabbit.postman;

import android.content.Context;
import android.content.Intent;

import com.whiterabbit.postman.commands.ServerCommand;

public class TestServerCommand extends ServerCommand {
	static private String STRING_KEY = "String";
	static private String LONG_KEY = "Long";
	
	private String mTestString;
	private Long mTestLong;
	
	public TestServerCommand(){
		mTestString = "";
		mTestLong = Long.valueOf(0);
	}
	
	public TestServerCommand(String s, Long l){
		mTestString = s;
		mTestLong = l;
	}
	
	@Override
	protected void fillIntent(Intent i) {
		i.putExtra(STRING_KEY, mTestString);
		i.putExtra(LONG_KEY, mTestLong);
		
	}

	@Override
	protected void fromIntent(Intent i) {
		mTestString = i.getExtras().getString(STRING_KEY);
		mTestLong = i.getExtras().getLong(LONG_KEY);
	}

	@Override
	public void execute(Context c) {
		// TODO Auto-generated method stub
		
	}
	
	public void notifyResultToAll(String res, Context c){
		notifyResult(res, c);
	}
	
	public void notifyErrorToAll(String res, Context c){
		notifyError(res, c);
	}

}
