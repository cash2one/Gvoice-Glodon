package com.glodon.model;

import com.google.gson.Gson;

public class WebMsgSynthesis extends WebMsg {
	private String audioPath;
	
	public WebMsgSynthesis() {
		super();
		this.audioPath = "";
	}
	
	public WebMsgSynthesis(StatusCode statusCode, String audioPathString) {
		super(statusCode);
		this.audioPath = audioPathString;
	}
	
	public String getAudioPath() {
		return this.audioPath;
	}

	public void setAudioPath(String audioPathString) {
		this.audioPath = audioPathString;
	}
	
	@Override
	public String toJsonMessage() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
