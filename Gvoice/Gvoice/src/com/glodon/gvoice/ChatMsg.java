package com.glodon.gvoice;

import android.widget.ImageView;
import android.widget.TextView;

abstract public class ChatMsg {
	//������ʾ�ڶԻ����������Ϣ��������ShowMsg�ĸ���
	public static final int TYPE_ANSWER = 0;
	public static final int TYPE_QUESTION = 1;
	
	protected int type; //��ʾ�ڶԻ�����໹���Ҳ�
	
	protected ChatMsg(int fType){
		type = fType;
	}

	public int getType() {
		return type;
	}
	
	public String getLinkAddr(){
		//���ؿ������ӵĵ�ַ
		return "";
	}
	
	public void setTitle(TextView textView){
		//����Ϣ�ı���������textView
	}
	
	public void setContent(TextView textView){
		//����Ϣ������������textView
	}
	
	public void setImage(ImageView imageView){
		//����Ϣ��ͼƬ������imageView
	}
}
