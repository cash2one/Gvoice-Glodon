package com.glodon.gvoice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingActivity extends Activity {
	// 需返回的设置数据
	Bundle returnSettingData;
	// 原来的设置数据
	Bundle settingDataBefore;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		// 按照原来的设置显示设置页面
		settingDataBefore = getIntent().getExtras();
		returnSettingData = (Bundle) settingDataBefore.clone();

		initBind();
	}

	private void initBind() { 
		// 设置收到的回复是否自动朗读
		RadioGroup voiceAutoReadSetting = (RadioGroup) findViewById(R.id.auto_read_answer);
		final RadioButton voiceAutoRead = (RadioButton) findViewById(R.id.auto_read_on);
		final RadioButton voiceNotRead = (RadioButton) findViewById(R.id.auto_read_off);

		Button confirm = (Button) findViewById(R.id.confirm);
		Button cancel = (Button) findViewById(R.id.cancel);

		// 设置RadioButton变化的监听器
		voiceAutoReadSetting.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == voiceAutoRead.getId())
					returnSettingData.putBoolean("autoReadMsg", true);
				else if (checkedId == voiceNotRead.getId())
					returnSettingData.putBoolean("autoReadMsg", false);
			}
		});

		confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 将新的设置数据回传给MainActivity
				Intent intent = new Intent();
				intent.putExtras(returnSettingData);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 取消并返回，不改变原数据设置
				Intent intent = new Intent();
				setResult(RESULT_CANCELED, intent);
				finish();
			}
		});

		if (settingDataBefore.getBoolean("autoReadMsg"))
			voiceAutoReadSetting.check(R.id.auto_read_on);
		else
			voiceAutoReadSetting.check(R.id.auto_read_off);
	}
}
