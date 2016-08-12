package com.glodon.gvoice;

import android.widget.ImageView;
import android.widget.TextView;

abstract public class ChatMsg {
	//用于显示在对话窗口里的信息，是其他ShowMsg的父类
	public static final int TYPE_ANSWER = 0;
	public static final int TYPE_QUESTION = 1;
	
	protected int type; //显示在对话框左侧还是右侧
	
	protected ChatMsg(int fType){
		type = fType;
	}

	public int getType() {
		return type;
	}
	
	public String getLinkAddr(){
		//返回可以链接的地址
		return "";
	}
	
	public void setTitle(TextView textView){
		//将信息的标题设置在textView
	}
	
	public void setContent(TextView textView){
		//将信息的内容设置在textView
	}
	
	public void setImage(ImageView imageView){
		//将信息的图片设置在imageView
	}
}
