package com.glodon.gvoice;

public class ChatMsgPureText extends ChatMsgWithText {
	//���Ǵ��ı����ݵ�ShowMsg
	public ChatMsgPureText(String title, String content, String linkURL, int type){
		super(title, content, linkURL, type);
	}
	
	protected ChatMsgPureText(String content, int type){
		// ����ֻ�����ݵĹ��캯����һ�����û��Ĵ��ı����ʣ���ӭ��Ҳ�����õ�
		super("", content, "", type);
	}
}
