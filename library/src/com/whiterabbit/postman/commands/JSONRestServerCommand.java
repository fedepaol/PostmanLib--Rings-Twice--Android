package com.whiterabbit.postman.commands;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;

public abstract class JSONRestServerCommand extends RestServerCommand {

	public JSONRestServerCommand(Action a){
		super(a);
	}
	
	abstract String getJSONString();
	
	abstract void processJSONResult(String result);
	
	@Override
	protected
	void setCallPayload(HttpEntityEnclosingRequestBase call) {
		StringEntity se;
		try {
			se = new StringEntity(getJSONString(), "UTF-8");
			call.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			this.notifyError(e.getMessage(), getContext());
		}
		call.setHeader("Accept", "application/json");
		call.setHeader("Content-type", "application/json");

	}


	@Override
	protected
	void processHttpResult(String result) {
		processJSONResult(result);
	}



}
