package com.glodon.gvoice;

import android.content.Context;
import android.widget.Toast;

public class Tip {
	private Toast mToast;
	//��ʼ��Toast
	public Tip(Context context, int toastLength){
		mToast = Toast.makeText(context, "", toastLength);
	}
	
	//��ʾToast
	public void showTip(final String msg){
		mToast.setText(msg);
		mToast.show();
	}
	
	//������ʾʱ��Ҫ����ʾtoast
	public void showTip(final String msg, int toastLength){
		int dura = mToast.getDuration();
		mToast.setText(msg);
		mToast.setDuration(toastLength);
		mToast.show();
		mToast.setDuration(dura);
	}
}
