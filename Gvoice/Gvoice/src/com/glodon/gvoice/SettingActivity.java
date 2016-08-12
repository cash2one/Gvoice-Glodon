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
	// �践�ص���������
	Bundle returnSettingData;
	// ԭ������������
	Bundle settingDataBefore;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		// ����ԭ����������ʾ����ҳ��
		settingDataBefore = getIntent().getExtras();
		returnSettingData = (Bundle) settingDataBefore.clone();

		initBind();
	}

	private void initBind() { 
		// �����յ��Ļظ��Ƿ��Զ��ʶ�
		RadioGroup voiceAutoReadSetting = (RadioGroup) findViewById(R.id.auto_read_answer);
		final RadioButton voiceAutoRead = (RadioButton) findViewById(R.id.auto_read_on);
		final RadioButton voiceNotRead = (RadioButton) findViewById(R.id.auto_read_off);

		Button confirm = (Button) findViewById(R.id.confirm);
		Button cancel = (Button) findViewById(R.id.cancel);

		// ����RadioButton�仯�ļ�����
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
				// ���µ��������ݻش���MainActivity
				Intent intent = new Intent();
				intent.putExtras(returnSettingData);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// ȡ�������أ����ı�ԭ��������
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
