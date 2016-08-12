package com.glodon.gvoice;

import android.widget.TextView;

abstract class ChatMsgWithText extends ChatMsg {
	//这是带有文字内容的ShowMsg
	protected String title;
	protected String content;
	protected String url;
	
	protected ChatMsgWithText(String fTitle, String fContent, String fUrl, int type){
		super(type);
		title = fTitle;
		content = fContent;
		url = fUrl;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getUrl() {
		return url;
	}
	
	@Override
	public String getLinkAddr(){
		return url;
	}
	
	public void setTitle(TextView textView){
		textView.setText(title);
	}
	
	public void setContent(TextView textView){
		textView.setText(content);
	}
}
