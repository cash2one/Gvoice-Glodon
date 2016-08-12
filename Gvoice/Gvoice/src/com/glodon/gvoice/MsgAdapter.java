package com.glodon.gvoice;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MsgAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<ChatMsg> mData;
	
	private String mTAG = "MsgAdapter";

	public MsgAdapter(Context context, List<ChatMsg> msgList) {
		// ����context�����ļ��ز���
		this.mInflater = LayoutInflater.from(context);
		// ����������ݱ�����mData��
		this.mData = msgList;
	}

	@Override
	public int getCount() {
		// �ڴ�������������������ݼ��е���Ŀ��
		return mData.size();
	}

	@Override
	public ChatMsg getItem(int position) {
		// ��ȡ���ݼ�����ָ��������Ӧ��������
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		/* ��ȡ���б�����ָ��������Ӧ����id��ʵ�������ﲢû��ʵ��������ܣ���ֻ����㽫һ��int�ͱ���
		�����˷��ء���ΪAdapter����Ҫ��ʵ�ָ÷�����Androidϵͳû��������������Ҳû����������˾���
		�㸳ֵ�ˡ��ҿ���AndroidһЩ�����е��������������ġ�
		*/
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			// �״λ��ƴ�listView
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.msg_item, null);
			viewHolder.leftLayout = (LinearLayout) convertView.findViewById(R.id.left_msg_layout);
			viewHolder.rightLayout = (LinearLayout) convertView.findViewById(R.id.right_msg_layout);
			viewHolder.leftTitle = (TextView) convertView.findViewById(R.id.left_title);
			viewHolder.rightTitle = (TextView) convertView.findViewById(R.id.right_title);
			viewHolder.leftContent = (TextView) convertView.findViewById(R.id.left_content);
			viewHolder.rightContent = (TextView) convertView.findViewById(R.id.right_content);
			viewHolder.leftImg = (ImageView) convertView.findViewById(R.id.left_pic);
			viewHolder.rightImg = (ImageView) convertView.findViewById(R.id.right_pic);
			convertView.setTag(viewHolder);
		} else { // �Ѿ����ƹ���listView���������¶�ȡTag
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Log.i(mTAG, "Ready to draw MsgView " + position);
		if (mData.get(position).getType() == ChatMsg.TYPE_ANSWER) {
			// �ǻش���Ϣ��Ӧ��ʾ�����
			viewHolder.leftLayout.setVisibility(View.VISIBLE);
			viewHolder.rightLayout.setVisibility(View.GONE);
			mData.get(position).setTitle(viewHolder.leftTitle);
			mData.get(position).setImage(viewHolder.leftImg);
			mData.get(position).setContent(viewHolder.leftContent);
		} else if (mData.get(position).getType() == ChatMsg.TYPE_QUESTION) {
			// ���û����ʣ�Ӧ��ʾ���ұ�
			viewHolder.rightLayout.setVisibility(View.VISIBLE);
			viewHolder.leftLayout.setVisibility(View.GONE);
			mData.get(position).setTitle(viewHolder.rightTitle);
			mData.get(position).setImage(viewHolder.rightImg);
			mData.get(position).setContent(viewHolder.rightContent);
		}

		return convertView;
	}

	// ViewHolder��̬��
	public final class ViewHolder {
		public LinearLayout leftLayout;
		public LinearLayout rightLayout;
		public TextView leftTitle;
		public TextView rightTitle;
		public TextView leftContent;
		public TextView rightContent;
		public ImageView leftImg;
		public ImageView rightImg;
	}

}
