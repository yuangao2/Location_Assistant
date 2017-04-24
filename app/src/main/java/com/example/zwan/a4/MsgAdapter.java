package com.example.zwan.a4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sersh on 16/3/12.
 */
public class MsgAdapter extends ArrayAdapter<Msg> {
    private int resourceId;

    public MsgAdapter(Context context, int textViewResourceId, List<Msg> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Msg msg = getItem(position);
        View view;
        ViewHolder viewholder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewholder = new ViewHolder();
            viewholder.leftLayout = (LinearLayout)view.findViewById(R.id.left_layout);
            viewholder.rightLayout = (LinearLayout)view.findViewById(R.id.right_layout);
            viewholder.leftMsg = (TextView)view.findViewById(R.id.left_text);
            viewholder.rightMsg = (TextView)view.findViewById(R.id.right_text);
            view.setTag(viewholder);
        }
        else {
            view = convertView;
            viewholder = (ViewHolder)view.getTag();
        }
        if (msg.getType() == Msg.TYPE_RECEIVED){
            //如果是收到的消息则显示左边布局，右边布局隐藏
            viewholder.leftLayout.setVisibility(View.VISIBLE);
            viewholder.rightLayout.setVisibility(View.GONE);
            viewholder.leftMsg.setText(msg.getContent());
        }
        else if(msg.getType() == Msg.TYPE_SENT){
            viewholder.rightLayout.setVisibility(View.VISIBLE);
            viewholder.leftLayout.setVisibility(View.GONE);
            viewholder.rightMsg.setText(msg.getContent());
        }
        return view;
    }

    class ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
    }
}
