package com.glodon.model;

import com.google.gson.Gson;

abstract public class WebMsg {
	public static enum StatusCode {
		Normal, No_Result, Server_Down
	};
	// 0:normal 1:no results 2:can not connect server

	protected StatusCode status;

	public WebMsg() {
		this.status = StatusCode.Normal;
	}

	public WebMsg(StatusCode statusCode) {
		this.status = statusCode;
	}

	public StatusCode getStatus() {
		return this.status;
	}

	public void setStatus(StatusCode statusCode) {
		this.status = statusCode;
	}

	public String toJsonMessage() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
