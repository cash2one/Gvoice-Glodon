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
		// 根据context上下文加载布局
		this.mInflater = LayoutInflater.from(context);
		// 将传入的数据保存在mData中
		this.mData = msgList;
	}

	@Override
	public int getCount() {
		// 在此适配器中所代表的数据集中的条目数
		return mData.size();
	}

	@Override
	public ChatMsg getItem(int position) {
		// 获取数据集中与指定索引对应的数据项
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		/* 获取在列表中与指定索引对应的行id。实际上这里并没有实现这个功能，而只是随便将一个int型变量
		进行了返回。因为Adapter必须要求实现该方法，Android系统没有用它，而我们也没有用它，因此就随
		便赋值了。我看到Android一些社区中的做法都是这样的。
		*/
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			// 首次绘制此listView
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
		} else { // 已经绘制过此listView，不用重新读取Tag
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Log.i(mTAG, "Ready to draw MsgView " + position);
		if (mData.get(position).getType() == ChatMsg.TYPE_ANSWER) {
			// 是回答信息，应显示在左边
			viewHolder.leftLayout.setVisibility(View.VISIBLE);
			viewHolder.rightLayout.setVisibility(View.GONE);
			mData.get(position).setTitle(viewHolder.leftTitle);
			mData.get(position).setImage(viewHolder.leftImg);
			mData.get(position).setContent(viewHolder.leftContent);
		} else if (mData.get(position).getType() == ChatMsg.TYPE_QUESTION) {
			// 是用户提问，应显示在右边
			viewHolder.rightLayout.setVisibility(View.VISIBLE);
			viewHolder.leftLayout.setVisibility(View.GONE);
			mData.get(position).setTitle(viewHolder.rightTitle);
			mData.get(position).setImage(viewHolder.rightImg);
			mData.get(position).setContent(viewHolder.rightContent);
		}

		return convertView;
	}

	// ViewHolder静态类
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
