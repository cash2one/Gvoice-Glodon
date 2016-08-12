package com.glodon.gvoice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.glodon.model.ChatEngine;
import com.glodon.model.ChatEngine.OnFailedConnectionListener;
import com.glodon.model.ChatEngine.OnGetAnswerListener;
import com.glodon.model.ChatEngine.OnGetAnswerVoiceListener;
import com.glodon.model.ChatEngine.OnShowQuestionListener;
import com.glodon.model.Downloader;
import com.glodon.model.Downloader.OnBitmapDownloadFinishedListener;
import com.glodon.model.Recorder;
import com.glodon.model.Recorder.OnCancelRecordListener;
import com.glodon.model.Recorder.OnFinishedRecordListener;
import com.glodon.model.Recorder.OnStartRecordListener;
import com.glodon.model.Recorder.OnVolumeChangeListener;
import com.glodon.model.ChatEngine.LocationGetter;
import com.glodon.model.ChatEngine.MsgBody;

public class MainActivity extends Activity{
	private static final int REQUEST_CODE_SETTING = 0;
	
	private static final int HANDLE_UPDATE_CHATMSG = 0; 
	
	private static String TAG = "MainActivity";
	
	//弹出的Toast信息
	private Tip mTip;
	//根据服务器返回的信息更新对话界面的handler
	private Handler mHandler;
	//声明AMapLocationClient类对象
	public AMapLocationClient mLocationClient = null;
	//录音工具
	private Recorder mRecorder = null;
	//聊天引擎
	private ChatEngine mChatEngine = null;
	//语音播放器
	private MediaPlayer mMediaPlayer = null;
	//播放器是否正在播放的标志
	private boolean mediaPlayerisPlaying = false;
	//最短录音时间为1.5s
	private static int MIN_INTERVAL_TIME = 1500;
	//录音界面显示
	private static Dialog recordIndicator;
	private static ImageView recordView;
	private ListView msgListView;
	private EditText inputText;
	private MsgAdapter msgAdapter;
	
	//聊天列表
	private List<ChatMsg> msgList = new ArrayList<ChatMsg>(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//初始化mTip
		mTip = new Tip(MainActivity.this, Toast.LENGTH_SHORT);
		
		initChatEngine();
		initMessageHandler();
		initRecorder();
		initBind();
		initAmap();
		mLocationClient.startLocation();
		//初始化mMediaPlayer
		mMediaPlayer = new MediaPlayer();
 	}
	
	private void initBind(){
		//设置各按钮的监听器及其功能
		inputText = (EditText)findViewById(R.id.input_text);
		final Button method = (Button) findViewById(R.id.voice);
		final Button send = (Button) findViewById(R.id.send);
		final Button speak = (Button) findViewById(R.id.speak);
		msgListView = (ListView) findViewById(R.id.msg_list_view);
		//隐藏speak按钮
		speak.setVisibility(View.GONE);
				
		initChatMsg();
		msgAdapter = new MsgAdapter(MainActivity.this, msgList);
		msgListView.setAdapter(msgAdapter);
		msgListView.setSelection(msgList.size());
		
		send.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				String inputContent = inputText.getText().toString();//获取用户输入
				if(!"".equals(inputContent)){//输入非空
					try {
						inputText.setText("");
						Log.i(TAG, "in send.setOnClickListener. askServer");
						mChatEngine.askServer(inputContent, ChatEngine.QUESTION_TYPE_STRING);
					} catch (Exception e) {
						Log.e(TAG, "in send.setOnClickListener. Failed to askServer");
					}
				}			
			}
		});
		
		msgListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//若有超链接（即URL不为空，则跳转超链接）
				ChatMsg msg = (ChatMsg) parent.getAdapter().getItem(position);
				String url = msg.getLinkAddr();
				if(!"".equals(url)){
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}
			}
			
		});
		
		method.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				if(method.getText().equals(getResources().getString(R.string.input_method_audio))){
					method.setText(getResources().getString(R.string.input_method_text));
					//隐藏软键盘
					InputMethodManager inputmanger = (InputMethodManager) getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
		            inputmanger.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
					inputText.setVisibility(View.GONE);
					speak.setVisibility(View.VISIBLE);
				}else{
					method.setText(getResources().getString(R.string.input_method_audio));
					inputText.setVisibility(View.VISIBLE);
					speak.setVisibility(View.GONE);
				}
			}
		});
		
		speak.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.performClick();
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					//按下，开始录音
					Log.i(TAG,"speak button Detect ACTION_DOWN!");
					mRecorder.startRecord();
					break;
				case MotionEvent.ACTION_UP:
					//抬起，录音结束
					Log.i(TAG,"speak button Detect ACTION_UP!");
					mRecorder.finishRecord();
					break;
				case MotionEvent.ACTION_CANCEL:
					// 因意外情况停止录音
					Log.i(TAG,"speak button Detect ACTION_CANCLE!");
					mRecorder.cancelRecord();
					break;
				}
				return true;
			}
		});
	}
	
	private void initRecorder(){
		//初始化录音工具mRecorder
		mRecorder = new Recorder();
		mRecorder.setOnStartRecordListener(new OnStartRecordListener(){
			@Override
			public void onStartRecord() {
				Log.i(TAG, "Start recording");
				recordIndicator = new Dialog(MainActivity.this);
				recordIndicator.requestWindowFeature(Window.FEATURE_NO_TITLE);
				recordView = new ImageView(MainActivity.this);
				recordView.setImageResource(R.drawable.mic_2);
				recordIndicator.setContentView(recordView, new LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				LayoutParams lp = recordIndicator.getWindow().getAttributes();
				lp.gravity = Gravity.CENTER;
				recordIndicator.getWindow().setFlags(LayoutParams.FLAG_NOT_FOCUSABLE, 
						LayoutParams.FLAG_NOT_FOCUSABLE);
				recordIndicator.getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, 
						LayoutParams.FLAG_NOT_TOUCH_MODAL);
				recordIndicator.show();
			}
		});
		
		mRecorder.setOnCancelRecordListener(new OnCancelRecordListener(){
			@Override
			public void onCancelRecord(long intervalTime) {
				recordIndicator.dismiss();
				mTip.showTip(getResources().getString(R.string.cancel_recording));
				Log.w(TAG, "Cancel recording");
			}
		});
		
		mRecorder.setOnFinishedRecordListener(new OnFinishedRecordListener(){
			@Override
			public void onFinishedRecord(String audioPath, long intervalTime) {
				Log.i(TAG, "Record Finish!");
				recordIndicator.dismiss();
				if (intervalTime < MIN_INTERVAL_TIME) {
					mTip.showTip(getResources().getString(R.string.audio_too_short));
					Log.w(TAG, "Record time too short!!");
				}
				else{
					Log.i(TAG,"Receive the audio record! Now start send the question!");
					//用语音向服务器提问
					try {
						mChatEngine.askServer(audioPath, ChatEngine.QUESTION_TYPE_VOICE);
					} catch (Exception e) {
						Log.e(TAG, "in setOnFinishedRecordListener. Failed to askServer");
					}
				}
			}
		});
		
		mRecorder.setOnVolumeChangeListener(new OnVolumeChangeListener(){
			@Override
			public void onVolumeChange(int volume){
				if (volume <= 40) {  
					recordView.setImageResource(R.drawable.mic_2);
                }else if (volume <= 70 && volume > 40) {  
                	recordView.setImageResource(R.drawable.mic_3);
                }else if (volume <= 100 && volume > 70) {  
                	recordView.setImageResource(R.drawable.mic_4);
                }else{  
                	recordView.setImageResource(R.drawable.mic_5);
                }
			}
		});
		
	}
	
	private void initChatEngine(){
		//从sd卡读取用户曾经设置的输入并进行初始化设置
		SettingPreferences setteingPreferences = new SettingPreferences(this);
		Bundle setting = setteingPreferences.getPreferences();
		mChatEngine = new ChatEngine(setting.getBoolean("autoReadMsg"));
		mChatEngine.setOnShowQuestionListener(new OnShowQuestionListener(){
			@Override
			public void OnShowQuestion(String question) {
				//回显用户的提问
				Log.i(TAG,"in OnShowQuestion. Ready to show question");
				if(question != null){
					ChatMsgPureText msg = new ChatMsgPureText(question, ChatMsg.TYPE_QUESTION);
					//将ShowMsg类传递给os.Message对象，使得主线程依此更新UI
					showChatMsg(msg);
					Log.i(TAG, "in onGetAnswer. Has asked Handler to show the msg!");
				} else
					Log.e(TAG,"in OnShowQuestion. Question cannot be null!");
			}
		});
		
		mChatEngine.setOnGetAnswerListener(new OnGetAnswerListener(){
			@Override
			public void onGetAnswer(final MsgBody msgBody) {
				//获得查询信息后的回调处理
				//将网络信息webMsg转换为需要进行显示的ShowMsg类。
				Log.i(TAG, "in onGetAnswer. Receive the answer from server");
				ChatMsg resp;
				if(msgBody.getStatus() == MsgBody.NORMAL){
					//服务器正常返回
					Log.i(TAG, "in onGetAnswer. receive NORMAL MsgBody");
					Bitmap bitmap = null;
					if(!"".equals(msgBody.getPictureUrl())){ //若有图片，则进行下载
						Downloader mDownloader = new Downloader();
						Log.i(TAG, "in onGetAnswer. Start downloading pircture");
						mDownloader.BitmapDownload(msgBody.getPictureUrl(), new OnBitmapDownloadFinishedListener(){
							@Override
							public void OnBitmapDownloadFinished(Bitmap bitmap) {
								Log.i(TAG, "in OnBitmapDownloadFinished. picture download finished!");
								ChatMsg resp = new ChatMsgBitmap(msgBody.getTitle(), msgBody.getContent(),
										bitmap, msgBody.getLinkUrl(), ChatMsg.TYPE_ANSWER);
								//将ShowMsg类传递给os.Message对象，使得主线程依此更新UI
								showChatMsg(resp);
								Log.i(TAG, "in OnBitmapDownloadFinished. ask Handler to show message with picture!");
							}
						});
						return;
					}
					//创建要显示的ShowMsg
					resp = new ChatMsgBitmap(msgBody.getTitle(), msgBody.getContent(),
							bitmap, msgBody.getLinkUrl(), ChatMsg.TYPE_ANSWER);
				}else if(msgBody.getStatus() == MsgBody.NO_RESULT){
					//服务器找不到相关信息
					Log.w(TAG, "in onGetAnswer. Server cannot find reletive info");
					resp = new ChatMsgResource("", 
							getResources().getString(R.string.cannot_understand), 
							R.drawable.no_result_pic, "", ChatMsg.TYPE_ANSWER);
				}else if(msgBody.getStatus() == MsgBody.SERVER_DOWN){
					//服务器连接失败
					Log.w(TAG, "in onGetAnswer. Server down");
					resp = new ChatMsgResource("", 
							getResources().getString(R.string.fail_to_connect_server), 
							R.drawable.server_down_pic, "", ChatMsg.TYPE_ANSWER);
				}else{
					//程序错误，不应该出现，若出现则代码有错！
					resp = new ChatMsgResource("", 
							getResources().getString(R.string.bug_in_show_msg), 
							R.drawable.no_result_pic, "", ChatMsg.TYPE_ANSWER);
				}
				//将ShowMsg类传递给os.Message对象，使得主线程依此更新UI
				showChatMsg(resp);
			}
		});
		
		mChatEngine.setOnGetAnswerVoiceListener(new OnGetAnswerVoiceListener(){
			@Override
			public void OnGetAnswerVoice(String voiceFilePath) {
				Log.i(TAG, "in setOnGetAnswerVoiceListener. receive the voiceFilePath");
				//朗读回复
				PlayRecord(voiceFilePath);
			}
		});
		
		mChatEngine.setOnFailedConnectionListener(new OnFailedConnectionListener(){
			@Override
			public void OnFailedConnection(int errorCode) {
				//若发生无法连接到服务器的错误
				mTip.showTip(getResources().getString(errorCode));			
			}
		});
		
		mChatEngine.setLocationGetter(new LocationGetter(){
			@Override
			public void AskForLocation() {
				//启动定位
				Log.i(TAG, "start locating");
				mLocationClient.startLocation();
			}
		});
	}
	
	private void initMessageHandler(){
		//初始化Handler
		mHandler = new Handler() {
			public void handleMessage(Message msg){
				switch (msg.what){
					case MainActivity.HANDLE_UPDATE_CHATMSG:
						//由服务器的回复显示聊天信息
						msgList.add((ChatMsg)msg.obj);
						msgAdapter.notifyDataSetChanged();
						msgListView.setSelection(msgList.size());
						break;
					default:
						break;
				}
			}
		};
	}
	
	private void initChatMsg(){
		//初始化对话列表，显示欢迎语
		ChatMsg helloMsg = new ChatMsgResource("", getResources().getString(R.string.hello_msg), 
				R.drawable.glodon_pic, "", ChatMsg.TYPE_ANSWER);
		msgList.add(helloMsg);
	}
	
	private void showChatMsg(ChatMsg chatMsg){
		//通知主线程显示chatMsg
		//将ShowMsg类传递给os.Message对象，使得主线程依此更新UI
		Message message = new Message();
		message.obj = chatMsg;
		message.what = MainActivity.HANDLE_UPDATE_CHATMSG;
		mHandler.sendMessage(message);
		Log.i(TAG, "in onGetAnswer. Has asked Handler to show the msg!");
	}
	
	private void initAmap(){
		//初始化高德地图定位工具
		//初始化定位
		mLocationClient = new AMapLocationClient(getApplicationContext());
		//初始化定位参数
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
		//设置定位模式为高精度模式
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		//设置要返回地址信息
		mLocationOption.setNeedAddress(true);
		//设置只定位一次
		mLocationOption.setOnceLocation(true);
		if(mLocationOption.isOnceLocationLatest()){
		   mLocationOption.setOnceLocationLatest(true);
		//设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。
		//如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会。
		}
		//设置强制刷新WIFI
		mLocationOption.setWifiActiveScan(true);
		//设置不允许模拟位置
		mLocationOption.setMockEnable(false);
		//设置定位间隔2000ms
		mLocationOption.setInterval(2000);
		//给定位客户端对象设置定位参数
		mLocationClient.setLocationOption(mLocationOption);
		//设置定位回调监听
		mLocationClient.setLocationListener(new AMapLocationListener(){
			@Override
			public void onLocationChanged(AMapLocation amapLocation) {
				if (amapLocation != null) {
			        if (amapLocation.getErrorCode() == 0) {
			        //定位成功回调信息，设置相关消息
			        String city = amapLocation.getCity();
			        Log.i(TAG, "Successfully get the location at: " + city);
			        mChatEngine.setLocation(city);//获取城市信息
			        mLocationClient.stopLocation();//停止定位
			        } else {
			        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
			        mTip.showTip(getResources().getString(R.string.locate_fail));
			        Log.e(TAG,"AmapError, location Error, ErrCode:"
			            + amapLocation.getErrorCode() + ", errInfo:"
			            + amapLocation.getErrorInfo());
			        }
			    }
			}
		});
	}
	
	//播放filePath位置指定的wav格式音频
	private void PlayRecord(String filePath){
		Log.i(TAG,"in PlayRecord. Initialize playRecord!!");
		if(mediaPlayerisPlaying){	//若正在播放，则停止当前播放
			mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.stop();
				mp.reset();
				mediaPlayerisPlaying = false;
				Log.i(TAG,"in PlayRecord. MediaPlayer stop");
			}
		});
		Log.i(TAG,"in PlayRecord. Successfully create MediaPlayer!");
		try {
			mMediaPlayer.setDataSource(filePath);
			mMediaPlayer.prepare();
		} catch (IOException e) {
			Log.e(TAG,"in PlayRecord. Failed in preparing data!!");
		}
		mMediaPlayer.start();
		mediaPlayerisPlaying = true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//绘制Menu窗口
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 设置点击Menu里的项目后的跳转页面
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			//跳转到设置页面
			Bundle data = new Bundle();
			data.putBoolean("autoReadMsg", mChatEngine.getAutoReadMsg());
			Intent in = new Intent(this, SettingActivity.class);
			in.putExtras(data);
			startActivityForResult(in, REQUEST_CODE_SETTING);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override  
    protected void onActivityResult( int requestCode, int resultCode, Intent data )  
    {  
        switch (requestCode) {  
            case REQUEST_CODE_SETTING :  
                switch (resultCode){
                case RESULT_OK:
                	//根据返回的信息修改设置
                	mChatEngine.setAutoReadMsg(data.getExtras().getBoolean("autoReadMsg"));
                	//将设置存储到sd卡
                	SettingPreferences setting = new SettingPreferences(this);
                	setting.save(mChatEngine.getAutoReadMsg());
		            break;  
		        default :  
		            break;  
                }
                break;
           default:
        	   break;
        }  
    } 
	
	@Override
	protected void onDestroy(){
		if(mediaPlayerisPlaying){
			mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
		mMediaPlayer.release();
		mMediaPlayer = null;
		Log.i(TAG,"in onDestroy. MediaPlayer stop and released!");
		mLocationClient.onDestroy();//销毁定位客户端。
	}
}
