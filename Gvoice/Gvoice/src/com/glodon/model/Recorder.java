package com.glodon.model;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class Recorder {
	private static String TAG = "Recorder";
	
	private String pcmFileName = 
			Environment.getExternalStorageDirectory().getAbsolutePath() +
			"/tempRecord.pcm";
	private String wavFileName = 
			Environment.getExternalStorageDirectory().getAbsolutePath() +
			"/tempRecord.wav";
	//录音开始时间
	private long startTime;	
	private AudioRecord mAudioRecord = null;
	//改变录音过程图片的handler
	private Handler volumeHandler = new ShowVolumeHandler();
	//录音结束标志
	private boolean stopRecording = false;
	//记录录音数据的线程
	Thread calculateThread = null;
	//音频设置
	int frequency = 16000;
	int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	int channels = 1; //单声道
	int bit = 16; //16位录音数据
	//录音的buffer大小
	int bufferSize;
	
	//录音开始监听器
	private OnStartRecordListener startListener = null;
	//录音完成监听器
	private OnFinishedRecordListener finishedListener = null;
	//取消录音监听器
	private OnCancelRecordListener cancelListener = null;
	//录音音量大小改变监听器
	private  OnVolumeChangeListener volumeListener = null;
	
	class ShowVolumeHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			//根据音量大小设置录音图片
			volumeListener.onVolumeChange(msg.what);
		}
	}
	
	public Recorder(){}
	
	public void setFrequency(int fre){
		frequency = fre;
	}
	
	public void setAudioEncoding(int pcmEncoding) throws Exception{
		switch (pcmEncoding){
		case AudioFormat.ENCODING_PCM_16BIT:
			audioEncoding = pcmEncoding;
			bit = 16;
			break;
		case AudioFormat.ENCODING_PCM_8BIT:
			audioEncoding = pcmEncoding;
			bit = 8;
			break;
		default:
			throw new Exception("Invalid setting for AudioFormat!");
		}
	}
	
	public void setOnStartRecordListener (OnStartRecordListener listener){
		startListener = listener;
	}
	
	public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
		finishedListener = listener;
	}
	
	public void setOnCancelRecordListener(OnCancelRecordListener listener) {
		cancelListener = listener;
	}
	
	public void setOnVolumeChangeListener(OnVolumeChangeListener listener) {
		volumeListener = listener;
	}
	
	public void startRecord(){
		//调用后开始录音
		startTime = System.currentTimeMillis();
		if (startListener != null)
			startListener.onStartRecord();
		startRecording();
	}
	
	public void finishRecord() {
		//停止录音
		stopRecording();

		if (finishedListener != null){
			//计算录音时间
			long intervalTime = System.currentTimeMillis() - startTime;
			try {
				calculateThread.join();
			} catch (InterruptedException e) {
				Log.e(TAG,"Failed to wait calculateThread join!!");
			}
			finishedListener.onFinishedRecord(wavFileName, intervalTime);
		}
	}
	
	public void cancelRecord() {
		//取消录音
		stopRecording();
		if (cancelListener != null){
			long intervalTime = System.currentTimeMillis() - startTime;
			cancelListener.onCancelRecord(intervalTime);
		}
	}
	
	
	private void startRecording() {
		//录音过程
		File pcmfile = new File(pcmFileName);
		
        if (pcmfile.exists()){
        	Log.i(TAG, "Overlap the old pcmFile!!");
        	pcmfile.delete();
        }
        try {
        	pcmfile.createNewFile();
        } catch (IOException e) {
        	Log.e(TAG, "Failed to create pcmFile!!");
        }
        
		try {
			final OutputStream os = new FileOutputStream(pcmfile);
			final BufferedOutputStream bos = new BufferedOutputStream(os);
			final DataOutputStream dos = new DataOutputStream(bos);
		
	    	bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
	    	mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
	                                              	frequency, channelConfiguration,
	                                              	audioEncoding, bufferSize);
	    	Log.i(TAG, "new audioRecord!");
	    	mAudioRecord.startRecording();
	    	calculateThread = new Thread(new Runnable(){
				public void run() {
		        	long time1 = System.currentTimeMillis(); 
		        	byte[] newByteData = new byte[bufferSize];
		        	short temp;
					while (!stopRecording) {
		        		int bufferReadResult = mAudioRecord.read(newByteData, 0, bufferSize);
		        		int v = 0; 
		        		
		                for (int i = 0; i < newByteData.length / 2; i++) {
		                	//将byte转化为short，以供计算音量大小
		                	temp = (short) (newByteData[i*2+1] & 0x00FF);
		                	temp = (short) ((temp << 8) & 0xFF00);
		                	temp = (short) (temp | newByteData[i*2]);
		                	v += temp * temp;
		                }
		        		
		        		try {
							dos.write(newByteData, 0, newByteData.length);
						} catch (IOException e) {
							Log.e(TAG,"Fail to record the voice into file!!");
						}
		        		long time2 = System.currentTimeMillis(); 
		                if (time2-time1 >100) {  
		                    if ((float)bufferReadResult > 1) {  
		                    	float f = (float) (10 * Math.log10(v /(float)bufferReadResult));
		                    	volumeHandler.sendEmptyMessage((int) f);
		                    }  
		                    time1 = time2;  
		                }  
		        	}
					mAudioRecord.stop();
					mAudioRecord.release();
					mAudioRecord = null;
			    	convertWaveFile();
			    	stopRecording = false;
			    	Log.i(TAG, "audioRecord has released!");
			    	try {
						dos.close();
						bos.close();
						os.close();
					} catch (IOException e) {
			        	Log.e(TAG,"Fail to close dos!!!");
					}
				}
	    	});
	    	calculateThread.start();
		} catch (FileNotFoundException e1) {
			Log.e(TAG, "Cannot open FileOutPutStream");
		}
	}
	
	private void convertWaveFile() {
    	// 将pcm文件转化为wav文件
    	File wavfile = new File(wavFileName);
    	if (wavfile.exists()){
    		Log.i(TAG, "Overlap the old File!!"); 
        	wavfile.delete();
    	}
    	try {
        	wavfile.createNewFile();
        } catch (IOException e) {
        	Log.i(TAG,"Failed to create wavFile!");
        }
    	Log.i(TAG,"Successfully create the tempRecord.wav");
    	
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = frequency;
        int channels = this.channels;
        long byteRate = this.bit * frequency * channels / 8;
        byte[] data = new byte[bufferSize];
        try {
            in = new FileInputStream(pcmFileName);
            out = new FileOutputStream(wavFileName);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
            Log.i(TAG,"Successfully write into tempRecord.wav");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
  //加入wav文件的头（固定格式）
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, 
    		long totalDataLen, long longSampleRate, 
    		int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
    
    private void stopRecording() {
		//停止录音并关闭录音界面
		stopRecording = true;
		Log.i(TAG, "Set 'isRecording' to false!!");
	}
	
	public interface OnStartRecordListener{
		//录音开始的回调函数
		public void onStartRecord();
	}
	
	public interface OnFinishedRecordListener {
		//录音完成后的回调函数
		public void onFinishedRecord(String audioPath, long intervalTime);
	}
	
	public interface OnCancelRecordListener{
		//录音开始的回调函数
		public void onCancelRecord(long intervalTime);
	}
	
	public interface OnVolumeChangeListener{
		//监听用户说话的声音并返回音量分贝值
		public void onVolumeChange(int volume);
	}
	
}
