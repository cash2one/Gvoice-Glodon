package com.glodon.gvoice;

import android.widget.ImageView;

public class ChatMsgResource extends ChatMsgWithText {
	private int resourceId;
	
	public ChatMsgResource(String title, String content, int fResourceId, String linkURL, int type) {
		super(title, content, linkURL, type);
		resourceId = fResourceId;
	}
	
	public int getResourceId(){
		return resourceId;
	}
	
	public void setImage(ImageView imageView){
		imageView.setImageResource(resourceId);
	}
}
