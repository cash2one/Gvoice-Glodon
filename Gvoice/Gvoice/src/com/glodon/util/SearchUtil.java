package com.glodon.util;

import java.io.File;

import com.glodon.model.WebMsg.StatusCode;
import com.glodon.model.WebMsgRecognize;
import com.glodon.model.WebMsgSearchContent;
import com.glodon.model.WebMsgSynthesis;

import android.os.Environment;
import android.util.Log;
import net.sf.json.JSONObject;

public class SearchUtil {

	private static String TAG = "SearchUtil";

	// userid
	public static String USERID = "";

	// location
	private String mLocation = "";
	
	public void setLocation(String location){
		if(!"".equals(location)){
			mLocation = location;
		}
	}

	// ��������������
	public void getSearchContentResult(final String question, final OnSearchFinishListener searchFinishListener) {
		if("".equals(mLocation)){
			Log.w(TAG, "Don't have location!");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "in getSearchContentResult, Start asking server");
				JSONObject params = new JSONObject();
				params.put("query", question);
				if (USERID.equals("")) {
					USERID = HttpRequest.getUseridFromServer("http://192.168.132.51:5000/gvoice/api/v1.0/id");
				}
				params.put("userid", USERID);
				params.put("location", mLocation);

				JSONObject response = HttpRequest
						.postQuerytextToServer("http://192.168.132.51:5000/gvoice/api/v1.0/query", params);

				WebMsgSearchContent webMsgSearchContent = new WebMsgSearchContent();

				if (response == null) {
					webMsgSearchContent.setStatus(StatusCode.Server_Down);
				} else {
					if (response.get("result").equals("")) {
						webMsgSearchContent.setStatus(StatusCode.No_Result);
					} else {
						JSONObject message = JSONObject.fromObject(response.get("result"));
						webMsgSearchContent.setTitle(message.getString("title"));
						webMsgSearchContent.setContent(message.getString("content"));
						webMsgSearchContent.setPictureUrl(message.getString("pictureUrl"));
						webMsgSearchContent.setUrl(message.getString("url"));
					}
				}
				Log.i(TAG, "in getSearchContentResult, has send back the webMsgSearchContent");
				searchFinishListener.onSearchFinish(webMsgSearchContent);
			}
		}).start();
	}

	// ��ȡ����ʶ����
	public void getRecongnizeResult(final File uploadFile, final OnGetRecognizeListener getRecognizeListener) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "in getRecongnizeResult, Start asking server");
				JSONObject response = HttpRequest
						.uploadFileToServer("http://192.168.132.51:5000/gvoice/api/v1.0/upload", uploadFile);

				WebMsgRecognize webMsgRecongnize = new WebMsgRecognize();

				if (response == null) {
					webMsgRecongnize.setStatus(StatusCode.Server_Down);
				} else {
					webMsgRecongnize.setText(response.getString("result"));
				}
				Log.i(TAG, "in getRecongnizeResult, has send back the webMsgRecongnize");
				getRecognizeListener.onRecognizeFinish(webMsgRecongnize);
			}
		}).start();
	}

	// ��������ϳɽ��
	public void getSynthesisResult(final String text, final OnGetSynthesisListener getSynthesisListener) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "in getSynthesisResult, Start asking server");
				String tempAnswerVoicePath = Environment.getExternalStorageDirectory().getAbsolutePath(); // �ϳɵ��������·��
				JSONObject params = new JSONObject();
				params.put("text", text);
				JSONObject response = HttpRequest
						.postQuerytextToServer("http://192.168.132.51:5000/gvoice/api/v1.0/audio", params);
				HttpRequest.getSynthesisFromServer("http://192.168.132.51:5000/gvoice/api/v1.0/audio",
						tempAnswerVoicePath);

				WebMsgSynthesis webMsgSynthesis = new WebMsgSynthesis();

				if (response == null) {
					webMsgSynthesis.setStatus(StatusCode.Server_Down);
				} else {
					webMsgSynthesis.setAudioPath(tempAnswerVoicePath + File.separator + response.getString("filename"));
				}
				Log.i(TAG, "in getSynthesisResult, has send back the webMsgSynthesis");
				getSynthesisListener.onSynthesisFinish(webMsgSynthesis);
			}
		}).start();
	}

	public interface OnSearchFinishListener {
		// �ص������ӿڣ�����text���в�ѯ���ش�WebMsgSearchContent��Ϣ
		public void onSearchFinish(WebMsgSearchContent webMsgSearchContent);
	}

	public interface OnGetSynthesisListener {
		// �ص������ӿڣ��������ֽ��кϳɣ��ش�WebMsgSynthesis��Ϣ
		public void onSynthesisFinish(WebMsgSynthesis webMsgSynthesis);
	}

	public interface OnGetRecognizeListener {
		// �ص������ӿڣ�������������ʶ�𣬻ش�WebMsgRecognize��Ϣ
		public void onRecognizeFinish(WebMsgRecognize webMsgRecognize);
	}
}
