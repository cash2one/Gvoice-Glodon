package com.glodon.gvoice;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class ChatMsgBitmap extends ChatMsgWithText {
	private Bitmap bitmap;
	
	public ChatMsgBitmap(String title, String content, Bitmap fBitmap, String linkURL, int type) {
		super(title, content, linkURL, type);
		bitmap = fBitmap;
	}
	
	public Bitmap getBitmap(){
		return bitmap;
	}
	
	public void setImage(ImageView imageView){
		imageView.setImageBitmap(bitmap);
	}
}
