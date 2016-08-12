package com.glodon.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Downloader {
	private static String TAG = "Downloader";
	
	public void BitmapDownload(final String urlString, final OnBitmapDownloadFinishedListener listener){
		//异步连接bitmap图片资源并下载图片
		Thread bitmapDownloadThread = new Thread(new Runnable(){
			@Override
			public void run() {
				Log.i(TAG,"in BitmapDownload, Start downloading");
				Bitmap bitmap = null;
				try{
		            URL url = new URL(urlString);
		            HttpURLConnection conn  = (HttpURLConnection)url.openConnection();
		            conn.setDoInput(true);
		            conn.connect(); 
		            InputStream inputStream=conn.getInputStream();
		            bitmap = BitmapFactory.decodeStream(inputStream);   
		        } catch (MalformedURLException e1) { 
		        	e1.printStackTrace();
		        } catch (IOException e2) {
		        	e2.printStackTrace();
		        } 
				Log.i(TAG,"in BitmapDownload, has sent back the download bitmap");
				listener.OnBitmapDownloadFinished(bitmap);
			}
		});
		bitmapDownloadThread.start();
	}
	
	public interface OnBitmapDownloadFinishedListener{
		//Bitmap下载完成后回调函数
		public void OnBitmapDownloadFinished(Bitmap bitmap);
	}
}
