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
	
	//������Toast��Ϣ
	private Tip mTip;
	//���ݷ��������ص���Ϣ���¶Ի������handler
	private Handler mHandler;
	//����AMapLocationClient�����
	public AMapLocationClient mLocationClient = null;
	//¼������
	private Recorder mRecorder = null;
	//��������
	private ChatEngine mChatEngine = null;
	//����������
	private MediaPlayer mMediaPlayer = null;
	//�������Ƿ����ڲ��ŵı�־
	private boolean mediaPlayerisPlaying = false;
	//���¼��ʱ��Ϊ1.5s
	private static int MIN_INTERVAL_TIME = 1500;
	//¼��������ʾ
	private static Dialog recordIndicator;
	private static ImageView recordView;
	private ListView msgListView;
	private EditText inputText;
	private MsgAdapter msgAdapter;
	
	//�����б�
	private List<ChatMsg> msgList = new ArrayList<ChatMsg>(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//��ʼ��mTip
		mTip = new Tip(MainActivity.this, Toast.LENGTH_SHORT);
		
		initChatEngine();
		initMessageHandler();
		initRecorder();
		initBind();
		initAmap();
		mLocationClient.startLocation();
		//��ʼ��mMediaPlayer
		mMediaPlayer = new MediaPlayer();
 	}
	
	private void initBind(){
		//���ø���ť�ļ��������书��
		inputText = (EditText)findViewById(R.id.input_text);
		final Button method = (Button) findViewById(R.id.voice);
		final Button send = (Button) findViewById(R.id.send);
		final Button speak = (Button) findViewById(R.id.speak);
		msgListView = (ListView) findViewById(R.id.msg_list_view);
		//����speak��ť
		speak.setVisibility(View.GONE);
				
		initChatMsg();
		msgAdapter = new MsgAdapter(MainActivity.this, msgList);
		msgListView.setAdapter(msgAdapter);
		msgListView.setSelection(msgList.size());
		
		send.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				String inputContent = inputText.getText().toString();//��ȡ�û�����
				if(!"".equals(inputContent)){//����ǿ�
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
				//���г����ӣ���URL��Ϊ�գ�����ת�����ӣ�
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
					//���������
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
					//���£���ʼ¼��
					Log.i(TAG,"speak button Detect ACTION_DOWN!");
					mRecorder.startRecord();
					break;
				case MotionEvent.ACTION_UP:
					//̧��¼������
					Log.i(TAG,"speak button Detect ACTION_UP!");
					mRecorder.finishRecord();
					break;
				case MotionEvent.ACTION_CANCEL:
					// ���������ֹͣ¼��
					Log.i(TAG,"speak button Detect ACTION_CANCLE!");
					mRecorder.cancelRecord();
					break;
				}
				return true;
			}
		});
	}
	
	private void initRecorder(){
		//��ʼ��¼������mRecorder
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
					//�����������������
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
		//��sd����ȡ�û��������õ����벢���г�ʼ������
		SettingPreferences setteingPreferences = new SettingPreferences(this);
		Bundle setting = setteingPreferences.getPreferences();
		mChatEngine = new ChatEngine(setting.getBoolean("autoReadMsg"));
		mChatEngine.setOnShowQuestionListener(new OnShowQuestionListener(){
			@Override
			public void OnShowQuestion(String question) {
				//�����û�������
				Log.i(TAG,"in OnShowQuestion. Ready to show question");
				if(question != null){
					ChatMsgPureText msg = new ChatMsgPureText(question, ChatMsg.TYPE_QUESTION);
					//��ShowMsg�ഫ�ݸ�os.Message����ʹ�����߳����˸���UI
					showChatMsg(msg);
					Log.i(TAG, "in onGetAnswer. Has asked Handler to show the msg!");
				} else
					Log.e(TAG,"in OnShowQuestion. Question cannot be null!");
			}
		});
		
		mChatEngine.setOnGetAnswerListener(new OnGetAnswerListener(){
			@Override
			public void onGetAnswer(final MsgBody msgBody) {
				//��ò�ѯ��Ϣ��Ļص�����
				//��������ϢwebMsgת��Ϊ��Ҫ������ʾ��ShowMsg�ࡣ
				Log.i(TAG, "in onGetAnswer. Receive the answer from server");
				ChatMsg resp;
				if(msgBody.getStatus() == MsgBody.NORMAL){
					//��������������
					Log.i(TAG, "in onGetAnswer. receive NORMAL MsgBody");
					Bitmap bitmap = null;
					if(!"".equals(msgBody.getPictureUrl())){ //����ͼƬ�����������
						Downloader mDownloader = new Downloader();
						Log.i(TAG, "in onGetAnswer. Start downloading pircture");
						mDownloader.BitmapDownload(msgBody.getPictureUrl(), new OnBitmapDownloadFinishedListener(){
							@Override
							public void OnBitmapDownloadFinished(Bitmap bitmap) {
								Log.i(TAG, "in OnBitmapDownloadFinished. picture download finished!");
								ChatMsg resp = new ChatMsgBitmap(msgBody.getTitle(), msgBody.getContent(),
										bitmap, msgBody.getLinkUrl(), ChatMsg.TYPE_ANSWER);
								//��ShowMsg�ഫ�ݸ�os.Message����ʹ�����߳����˸���UI
								showChatMsg(resp);
								Log.i(TAG, "in OnBitmapDownloadFinished. ask Handler to show message with picture!");
							}
						});
						return;
					}
					//����Ҫ��ʾ��ShowMsg
					resp = new ChatMsgBitmap(msgBody.getTitle(), msgBody.getContent(),
							bitmap, msgBody.getLinkUrl(), ChatMsg.TYPE_ANSWER);
				}else if(msgBody.getStatus() == MsgBody.NO_RESULT){
					//�������Ҳ��������Ϣ
					Log.w(TAG, "in onGetAnswer. Server cannot find reletive info");
					resp = new ChatMsgResource("", 
							getResources().getString(R.string.cannot_understand), 
							R.drawable.no_result_pic, "", ChatMsg.TYPE_ANSWER);
				}else if(msgBody.getStatus() == MsgBody.SERVER_DOWN){
					//����������ʧ��
					Log.w(TAG, "in onGetAnswer. Server down");
					resp = new ChatMsgResource("", 
							getResources().getString(R.string.fail_to_connect_server), 
							R.drawable.server_down_pic, "", ChatMsg.TYPE_ANSWER);
				}else{
					//������󣬲�Ӧ�ó��֣�������������д�
					resp = new ChatMsgResource("", 
							getResources().getString(R.string.bug_in_show_msg), 
							R.drawable.no_result_pic, "", ChatMsg.TYPE_ANSWER);
				}
				//��ShowMsg�ഫ�ݸ�os.Message����ʹ�����߳����˸���UI
				showChatMsg(resp);
			}
		});
		
		mChatEngine.setOnGetAnswerVoiceListener(new OnGetAnswerVoiceListener(){
			@Override
			public void OnGetAnswerVoice(String voiceFilePath) {
				Log.i(TAG, "in setOnGetAnswerVoiceListener. receive the voiceFilePath");
				//�ʶ��ظ�
				PlayRecord(voiceFilePath);
			}
		});
		
		mChatEngine.setOnFailedConnectionListener(new OnFailedConnectionListener(){
			@Override
			public void OnFailedConnection(int errorCode) {
				//�������޷����ӵ��������Ĵ���
				mTip.showTip(getResources().getString(errorCode));			
			}
		});
		
		mChatEngine.setLocationGetter(new LocationGetter(){
			@Override
			public void AskForLocation() {
				//������λ
				Log.i(TAG, "start locating");
				mLocationClient.startLocation();
			}
		});
	}
	
	private void initMessageHandler(){
		//��ʼ��Handler
		mHandler = new Handler() {
			public void handleMessage(Message msg){
				switch (msg.what){
					case MainActivity.HANDLE_UPDATE_CHATMSG:
						//�ɷ������Ļظ���ʾ������Ϣ
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
		//��ʼ���Ի��б���ʾ��ӭ��
		ChatMsg helloMsg = new ChatMsgResource("", getResources().getString(R.string.hello_msg), 
				R.drawable.glodon_pic, "", ChatMsg.TYPE_ANSWER);
		msgList.add(helloMsg);
	}
	
	private void showChatMsg(ChatMsg chatMsg){
		//֪ͨ���߳���ʾchatMsg
		//��ShowMsg�ഫ�ݸ�os.Message����ʹ�����߳����˸���UI
		Message message = new Message();
		message.obj = chatMsg;
		message.what = MainActivity.HANDLE_UPDATE_CHATMSG;
		mHandler.sendMessage(message);
		Log.i(TAG, "in onGetAnswer. Has asked Handler to show the msg!");
	}
	
	private void initAmap(){
		//��ʼ���ߵµ�ͼ��λ����
		//��ʼ����λ
		mLocationClient = new AMapLocationClient(getApplicationContext());
		//��ʼ����λ����
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
		//���ö�λģʽΪ�߾���ģʽ
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		//����Ҫ���ص�ַ��Ϣ
		mLocationOption.setNeedAddress(true);
		//����ֻ��λһ��
		mLocationOption.setOnceLocation(true);
		if(mLocationOption.isOnceLocationLatest()){
		   mLocationOption.setOnceLocationLatest(true);
		//����setOnceLocationLatest(boolean b)�ӿ�Ϊtrue��������λʱSDK�᷵�����3s�ھ�����ߵ�һ�ζ�λ�����
		//���������Ϊtrue��setOnceLocation(boolean b)�ӿ�Ҳ�ᱻ����Ϊtrue����֮���ᡣ
		}
		//����ǿ��ˢ��WIFI
		mLocationOption.setWifiActiveScan(true);
		//���ò�����ģ��λ��
		mLocationOption.setMockEnable(false);
		//���ö�λ���2000ms
		mLocationOption.setInterval(2000);
		//����λ�ͻ��˶������ö�λ����
		mLocationClient.setLocationOption(mLocationOption);
		//���ö�λ�ص�����
		mLocationClient.setLocationListener(new AMapLocationListener(){
			@Override
			public void onLocationChanged(AMapLocation amapLocation) {
				if (amapLocation != null) {
			        if (amapLocation.getErrorCode() == 0) {
			        //��λ�ɹ��ص���Ϣ�����������Ϣ
			        String city = amapLocation.getCity();
			        Log.i(TAG, "Successfully get the location at: " + city);
			        mChatEngine.setLocation(city);//��ȡ������Ϣ
			        mLocationClient.stopLocation();//ֹͣ��λ
			        } else {
			        //��ʾ������ϢErrCode�Ǵ����룬errInfo�Ǵ�����Ϣ������������
			        mTip.showTip(getResources().getString(R.string.locate_fail));
			        Log.e(TAG,"AmapError, location Error, ErrCode:"
			            + amapLocation.getErrorCode() + ", errInfo:"
			            + amapLocation.getErrorInfo());
			        }
			    }
			}
		});
	}
	
	//����filePathλ��ָ����wav��ʽ��Ƶ
	private void PlayRecord(String filePath){
		Log.i(TAG,"in PlayRecord. Initialize playRecord!!");
		if(mediaPlayerisPlaying){	//�����ڲ��ţ���ֹͣ��ǰ����
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
		//����Menu����
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// ���õ��Menu�����Ŀ�����תҳ��
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			//��ת������ҳ��
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
                	//���ݷ��ص���Ϣ�޸�����
                	mChatEngine.setAutoReadMsg(data.getExtras().getBoolean("autoReadMsg"));
                	//�����ô洢��sd��
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
		mLocationClient.onDestroy();//���ٶ�λ�ͻ��ˡ�
	}
}
