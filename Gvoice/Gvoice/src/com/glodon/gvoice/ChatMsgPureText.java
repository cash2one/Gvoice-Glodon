package com.glodon.gvoice;

public class ChatMsgPureText extends ChatMsgWithText {
	//这是纯文本内容的ShowMsg
	public ChatMsgPureText(String title, String content, String linkURL, int type){
		super(title, content, linkURL, type);
	}
	
	protected ChatMsgPureText(String content, int type){
		// 这是只有内容的构造函数，一般是用户的纯文本提问，欢迎语也可以用等
		super("", content, "", type);
	}
}
