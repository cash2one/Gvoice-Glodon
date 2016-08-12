package com.glodon.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import net.sf.json.JSONObject;

public class HttpRequest {

	private static final int TIME_OUT = 10 * 10000000; // 超时时间
	private static final String CHARSET = "utf-8"; // 设置编码
	private static final String USER_AGENT = "Mozilla/5.0"; // 设置用户代理

	// 从服务器得到分配的userid
	@SuppressWarnings("finally")
	public static String getUseridFromServer(String serverUrl) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpRequest = new HttpGet(serverUrl);
		String userid = "";
		try {
			HttpResponse response = client.execute(httpRequest);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(response.getEntity());
				JSONObject jsonObject = JSONObject.fromObject(result);
				userid = jsonObject.getString("userid");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return userid;
		}
	}

	// 从服务器下载语音文件保存到本地
	public static void getSynthesisFromServer(String serverUrl, String path) {
		URL url;
		try {
			url = new URL(serverUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			if (responseCode == HttpStatus.SC_OK) {
				File file = new File(path + File.separator + "tts.wav");
				InputStream in = con.getInputStream();
				OutputStream os = new FileOutputStream(file);
				int bytesRead = 0;
				byte[] buffer = new byte[8192];
				while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
					os.write(buffer, 0, bytesRead);
				}
				os.close();
				in.close();
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 将JSON串提交到服务器，并获得服务器返回的结果
	@SuppressWarnings("finally")
	public static JSONObject postQuerytextToServer(String serverUrl, JSONObject json) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(serverUrl);
		JSONObject response = null;
		try {
			StringEntity s = new StringEntity(json.toString(), HTTP.UTF_8);
			s.setContentEncoding(CHARSET);
			s.setContentType("application/json, charset=UTF-8");
			post.setEntity(s);
			HttpResponse res = client.execute(post);
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(res.getEntity());
				response = JSONObject.fromObject(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return response;
		}
	}

	// 将语音文件上传到服务器，获得识别结果
	@SuppressWarnings("finally")
	public static JSONObject uploadFileToServer(String serverUrl, File file) {
		String BOUNDARY = UUID.randomUUID().toString();
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data";
		JSONObject response = null;
		try {
			URL url = new URL(serverUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Charset", CHARSET);
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
			// 当文件不为空，把文件包装并且上传
			if (file != null) {
				OutputStream outputSteam = conn.getOutputStream();
				DataOutputStream dos = new DataOutputStream(outputSteam);
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);

				sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\""
						+ LINE_END);
				sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
				sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
				dos.write(end_data);
				dos.flush();

				int res = conn.getResponseCode();
				if (res == HttpStatus.SC_OK) {
					BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
					StringBuilder builder = new StringBuilder();
					String aux = "";
					while ((aux = br.readLine()) != null) {
						builder.append(aux);
					}
					String result = builder.toString();
					response = JSONObject.fromObject(result);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return response;
		}
	}
}
