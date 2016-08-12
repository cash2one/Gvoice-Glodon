package com.glodon.model;

import com.google.gson.Gson;

public class WebMsgSearchContent extends WebMsg {
	private String title;
	private String content;
	private String pictureUrl;
	private String url;
	
	public WebMsgSearchContent() {
		super();
		this.title = "";
		this.content = "";
		this.pictureUrl = "";
		this.url = "";
	}
	
	public WebMsgSearchContent(StatusCode statusCode, String titleString, String contentString, String pictureUrlString, String urlString) {
		super(statusCode);
		this.title = titleString;
		this.content = contentString;
		this.pictureUrl = pictureUrlString;
		this.url = urlString;
	}
	
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String titleString) {
		this.title = titleString;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String contentString) {
		this.content = contentString;
	}

	public String getpictureUrl() {
		return this.pictureUrl;
	}

	public void setPictureUrl(String pictureUrlString) {
		this.pictureUrl = pictureUrlString;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String urlString) {
		this.url = urlString;
	}
	
	@Override
	public String toJsonMessage() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
