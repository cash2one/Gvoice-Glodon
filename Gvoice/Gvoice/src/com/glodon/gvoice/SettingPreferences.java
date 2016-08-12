package com.glodon.gvoice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class SettingPreferences {
	//ʹ��sharedPreference�洢�û�ƫ������
	Context mContext;
	
	public SettingPreferences(Context context){
		mContext = context;
	}
	
	//�������
	public void save(boolean autoReadMsg){
		//��ȡSharedPreference����
		SharedPreferences preferences = mContext.getSharedPreferences("settingPreference", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean("autoReadMsg", autoReadMsg);
		editor.commit();		
	}
	
	//��ȡ����
	public Bundle getPreferences(){
		Bundle params = new Bundle();
		SharedPreferences preferences = mContext.getSharedPreferences("settingPreference", Context.MODE_PRIVATE);
		params.putBoolean("autoReadMsg", preferences.getBoolean("autoReadMsg", false));
		return params;
	}
	
}
