package com.glodon.gvoice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class SettingPreferences {
	//使用sharedPreference存储用户偏好设置
	Context mContext;
	
	public SettingPreferences(Context context){
		mContext = context;
	}
	
	//保存参数
	public void save(boolean autoReadMsg){
		//获取SharedPreference对象
		SharedPreferences preferences = mContext.getSharedPreferences("settingPreference", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean("autoReadMsg", autoReadMsg);
		editor.commit();		
	}
	
	//读取参数
	public Bundle getPreferences(){
		Bundle params = new Bundle();
		SharedPreferences preferences = mContext.getSharedPreferences("settingPreference", Context.MODE_PRIVATE);
		params.putBoolean("autoReadMsg", preferences.getBoolean("autoReadMsg", false));
		return params;
	}
	
}
