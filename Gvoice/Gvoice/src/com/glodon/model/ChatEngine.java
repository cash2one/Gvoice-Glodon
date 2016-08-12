package com.glodon.model;

import java.io.File;

import com.glodon.gvoice.R;
import com.glodon.util.SearchUtil;
import com.glodon.util.SearchUtil.OnGetRecognizeListener;
import com.glodon.util.SearchUtil.OnGetSynthesisListener;
import com.glodon.util.SearchUtil.OnSearchFinishListener;

import android.util.Log;;

public class ChatEngine {
	//问题类型：文字或语音
	public static final int QUESTION_TYPE_STRING = 0;
	public static final int QUESTION_TYPE_VOICE = 1;
	
	private static String TAG = "ChatEngine";
	
	//收到的信息是否自动朗读开关
	private boolean autoReadMsg;
	//当前位置
	private String location = "";
	
	private OnGetAnswerListener mOnGetAnswerListener = null;
	private OnShowQuestionListener mOnShowQuestionListener = null;
	private OnGetAnswerVoiceListener mOnGetAnswerVoiceListener = null;
	private OnFailedConnectionListener mOnFailedConnectionListener = null;
	private LocationGetter mLocationGetter = null;
	
	SearchUtil mSearchUtil = null;
	
	public ChatEngine(boolean fAutoReadMsg){
		autoReadMsg = fAutoReadMsg;
		mSearchUtil = new SearchUtil();
	}
	
	public void setOnGetAnswerListener(OnGetAnswerListener listener){
		mOnGetAnswerListener = listener;
	}
	
	public void setOnShowQuestionListener(OnShowQuestionListener listener){
		mOnShowQuestionListener = listener;
	}
	
	public void setOnGetAnswerVoiceListener (OnGetAnswerVoiceListener listener){
		mOnGetAnswerVoiceListener = listener;
	}
	
	public void setOnFailedConnectionListener(OnFailedConnectionListener listener){
		mOnFailedConnectionListener = listener;
	}

	public void setLocationGetter(LocationGetter getter){
		mLocationGetter = getter;
	}
	
	public void setAutoReadMsg(boolean fAutoReadMsg){
		autoReadMsg = fAutoReadMsg;
	}
	
	public boolean getAutoReadMsg(){
		return autoReadMsg;
	}
	
	public void askServer(final String question, int question_type) throws Exception{
		switch (question_type){
			case QUESTION_TYPE_STRING:
				//向服务器以文字形式询问
				Log.i(TAG,"in askServer. Receive text querry");
				if(mOnShowQuestionListener != null)
					//回显问题
					mOnShowQuestionListener.OnShowQuestion(question);
				askServerByText(question);
				break;
			case QUESTION_TYPE_VOICE:
				//向服务器以语音形式询问
				Log.i(TAG,"in askServer. Receive voice querry");
				File voiceFile = new File(question);
				if(!voiceFile.exists()){
					Log.e(TAG,"in askServer. Voice record doesn't exist!");
					throw new Exception("Voice record doesn't exist!");
				}
				
				//将用户的提问音频发至服务器，以获取文字解析结果
				mSearchUtil.getRecongnizeResult(voiceFile, new OnGetRecognizeListener(){
					@Override
					public void onRecognizeFinish(WebMsgRecognize webMsgRecognize) {
						//获取解析结果后，将结果发给controller进行显示
						Log.i(TAG,"in onRecognizeFinish. Receive recongnize result");
						if(mOnShowQuestionListener != null){
							if(webMsgRecognize.status == WebMsg.StatusCode.Normal){
								Log.i(TAG,"in onRecognizeFinish. Ready to send back query question");
								mOnShowQuestionListener.OnShowQuestion(webMsgRecognize.getText());
								//再将识别出的问题向服务器提问并收取结果
								Log.i(TAG,"in onRecognizeFinish. Ask server corresponding question");
								askServerByText(webMsgRecognize.getText());
							}
							else{
								Log.w(TAG,"in onRecognizeFinish. WrongConnection!");
								returnWrongConnection();
							}
						} else
							Log.w(TAG,"in onRecognizeFinish. No setting of mOnShowQuestionListener!");
					}
				});
				break;
			default:
				Log.e(TAG,"in onRecognizeFinish. Wrong question type!");
				throw new IllegalStateException("Wrong question type!");
		}
	}
	
	private void askServerByText (String question){
		//这是向服务器以文字形式发送信息并接收回复，然后决定是否朗读的流程。
		Log.i(TAG,"in askServerByText. Begine to ask server");
		if("".equals(location)){
			mLocationGetter.AskForLocation();
			mOnFailedConnectionListener.OnFailedConnection(R.string.locate_fail);
			Log.w(TAG, "Cannot get location!");
		}
		//向服务器发送信息
		mSearchUtil.getSearchContentResult(question, new OnSearchFinishListener(){
			@Override
			public void onSearchFinish(WebMsgSearchContent webMsg) {
				//返回搜索信息给controller显示
				Log.i(TAG,"in askServerByText, onSearchFinish. Receive server responces");
				if(mOnGetAnswerListener != null){
					if(webMsg.status == WebMsg.StatusCode.Normal){
						MsgBody msg = new MsgBody(MsgBody.NORMAL ,webMsg.getTitle(), webMsg.getContent(),
								webMsg.getpictureUrl(), webMsg.getUrl());
						mOnGetAnswerListener.onGetAnswer(msg);
						Log.i(TAG,"in askServerByText, onSearchFinish. Echo back the msg to MainActivity");
						//判断是否需要朗读
						if(autoReadMsg){
							//若需要朗读，则向服务器请求朗读
							Log.i(TAG,"in askServerByText, onSearchFinish. Ask server to read");
							mSearchUtil.getSynthesisResult(webMsg.getContent(),
									new OnGetSynthesisListener(){
										@Override
										public void onSynthesisFinish(WebMsgSynthesis webMsgSynthesis) {
											//要求controller播放朗读音频
											Log.i(TAG,"in askServerByText, onSynthesisFinish. receive server's wav");
											if(mOnGetAnswerVoiceListener != null){
												if(webMsgSynthesis.getStatus() == WebMsg.StatusCode.Normal)
													mOnGetAnswerVoiceListener.OnGetAnswerVoice(webMsgSynthesis.getAudioPath());
												else 
													returnWrongConnection();
											}
										}
									});
						}
					}else if(webMsg.status == WebMsg.StatusCode.No_Result){
						Log.i(TAG,"in askServerByText, onSearchFinish. search No_Result");
						MsgBody msg = new MsgBody(MsgBody.NO_RESULT);
						mOnGetAnswerListener.onGetAnswer(msg);	
					}else if(webMsg.status == WebMsg.StatusCode.Server_Down){
						Log.i(TAG,"in askServerByText, onSearchFinish. search Server_Down");
						MsgBody msg = new MsgBody(MsgBody.SERVER_DOWN);
						mOnGetAnswerListener.onGetAnswer(msg);
						returnWrongConnection();
					}
				}
				
			}
		});
	}
	
	public void setLocation(String fLocation){
		if(!"".equals(fLocation)){
			location = fLocation;
			mSearchUtil.setLocation(location);
			Log.i(TAG, "Refresh the location!");
		}
	}
	
	private void returnWrongConnection(){
		//将无法连接服务器的信息回发给controller
		if(mOnFailedConnectionListener != null)
			mOnFailedConnectionListener.OnFailedConnection(R.string.connection_fail);
	}
	
	public interface OnGetAnswerListener{
		//在askServer之后得到服务器回复监听器
		public void onGetAnswer(MsgBody msg);
	}
	
	public interface OnShowQuestionListener{
		//显示用户提问监听器
		public void OnShowQuestion(String question);
	}
	
	public interface OnGetAnswerVoiceListener{
		//获取服务器回答的朗读结果
		public void OnGetAnswerVoice(String voiceFilePath);
	}
	
	public interface OnFailedConnectionListener{
		//无法连接上服务器，错误返回
		public void OnFailedConnection(int errorCode);
	}
	
	public interface LocationGetter {
		//获取位置信息的工具
		//发起获取位置信息的请求
		abstract public void AskForLocation();
	}
	
	public static final class MsgBody{
		//回传给controller的信息，只是为了将信息组合起来返回
		public static final int NORMAL = 0;
		public static final int NO_RESULT = 1;
		public static final int SERVER_DOWN = 2;

		private int status;
		private String title = "";
		private String content = "";
		private String pictureUrl = "";
		private String linkUrl = "";
		
		public MsgBody(int fStatus){
			status = fStatus;
		}
		
		public MsgBody(int fStatus, String fTitle, String fContent, String fPictureUrl, String fLinkUrl){
			status = fStatus;
			title = fTitle;
			content = fContent;
			pictureUrl = fPictureUrl;
			linkUrl = fLinkUrl;
		}
		
		public int getStatus(){
			return status;
		}
		
		public String getTitle(){
			return title;
		}
		
		public String getContent(){
			return content;
		}
		
		public String getPictureUrl(){
			return pictureUrl;
		}
		
		public String getLinkUrl(){
			return linkUrl;
		}
	}
	
}
