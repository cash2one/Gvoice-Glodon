package com.glodon.model;

import com.google.gson.Gson;

public class WebMsgRecognize extends WebMsg {
	private String text;

	public WebMsgRecognize() {
		super();
		this.text = "";
	}

	public WebMsgRecognize(StatusCode statusCode, String textString) {
		super(statusCode);
		this.text = textString;
	}

	public String getText() {
		return text;
	}

	public void setText(String textString) {
		this.text = textString;
	}

	@Override
	public String toJsonMessage() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
