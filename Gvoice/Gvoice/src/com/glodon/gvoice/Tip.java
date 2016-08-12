package com.glodon.gvoice;

import android.content.Context;
import android.widget.Toast;

public class Tip {
	private Toast mToast;
	//初始化Toast
	public Tip(Context context, int toastLength){
		mToast = Toast.makeText(context, "", toastLength);
	}
	
	//显示Toast
	public void showTip(final String msg){
		mToast.setText(msg);
		mToast.show();
	}
	
	//按照显示时间要求显示toast
	public void showTip(final String msg, int toastLength){
		int dura = mToast.getDuration();
		mToast.setText(msg);
		mToast.setDuration(toastLength);
		mToast.show();
		mToast.setDuration(dura);
	}
}
