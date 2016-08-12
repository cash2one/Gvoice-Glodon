package com.glodon.test;

import java.io.File;

import com.glodon.util.HttpRequest;

import net.sf.json.JSONObject;

public class SearchUtilTest {

	public static void main(String[] args) throws Exception {
		String userid = HttpRequest.getUseridFromServer("http://192.168.132.51:5000/gvoice/api/v1.0/id");

		JSONObject params = new JSONObject();
		JSONObject response;
		// 测试合成
		params.put("text", "智能机器人");
		response = HttpRequest.postQuerytextToServer("http://192.168.132.51:5000/gvoice/api/v1.0/audio", params);
		System.out.println(response);
		HttpRequest.getSynthesisFromServer("http://192.168.132.51:5000/gvoice/api/v1.0/audio",
				"D:\\GitHub\\Gvoice-api-2.7\\wav");
		// 测试识别
		File file = new File("D:\\GitHub\\Gvoice-api-2.7\\wav\\tts.wav");
		response = HttpRequest.uploadFileToServer("http://192.168.132.51:5000/gvoice/api/v1.0/upload", file);
		System.out.println(response);
		// 测试查询
		params.clear();
		params.put("query", response.getString("result"));
		response = HttpRequest.postQuerytextToServer("http://192.168.132.51:5000/gvoice/api/v1.0/query", params);
		System.out.println(response);
	}
}
