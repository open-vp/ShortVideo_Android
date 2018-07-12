package com.niurenjob.bullman.ShortVideo.roomutil.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.niurenjob.bullman.ShortVideo.R;
import com.niurenjob.bullman.ShortVideo.roomutil.commondef.RoomInfo;
import com.niurenjob.bullman.ShortVideo.roomutil.misc.NameGenerator;

import java.util.List;

/**
 * Created by jac on 2017/11/7.
 * Copyright © 2013-2017 Tencent Cloud. All Rights Reserved.
 */

public class RoomListViewAdapter extends BaseAdapter {

    public static final int ROOM_TYPE_LIVE      = 1;
    public static final int ROOM_TYPE_MULTI     = 2;
    public static final int ROOM_TYPE_DOUBLE    = 3;
    public static final int ROOM_TYPE_ANSWER    = 4;

    private List<RoomInfo> dataList;
    private int roomType = ROOM_TYPE_LIVE;

    private class ConvertView extends FrameLayout{
        private TextView nameView;
        private TextView nbView;
        private TextView idView;

        public ConvertView(Context context, int type) {
            this(context, null, type);
        }

        public ConvertView(@NonNull Context context, @Nullable AttributeSet attrs, int type) {
            super(context, attrs);

            inflate(context, R.layout.rtmproom_roominfo_item, this);
            setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

            nameView = ((TextView) findViewById(R.id.room_name_textview));
            nbView = ((TextView) findViewById(R.id.room_online_nb_textview));
            idView = ((TextView) findViewById(R.id.room_id_textview));

            if (type == ROOM_TYPE_ANSWER) {
                ((TextView) findViewById(R.id.room_name_title)).setText("答题室名称：");
                ((TextView) findViewById(R.id.room_id_title)).setText("答题室ID：");
            }
            else if (type == ROOM_TYPE_LIVE) {
                ((TextView) findViewById(R.id.room_name_title)).setText("直播间名称：");
                ((TextView) findViewById(R.id.room_id_title)).setText("直播间ID：");
            }
            else {
                ((TextView) findViewById(R.id.room_name_title)).setText("会话名：");
                ((TextView) findViewById(R.id.room_id_title)).setText("会话ID：");
            }
        }

        public void setRoomName(String name){
            name = NameGenerator.replaceNonPrintChar(name, 16, "...", false);
            nameView.setText(name);
        }

        public void setMemberCount(int count){
            String format = String.format("%d 人", count);
            nbView.setText(format);
        }

        public void setId(String id) {
            id = NameGenerator.replaceNonPrintChar(id, 20, "...", true);
            this.idView.setText(id);
        }
    }
    public void setDataList(List<RoomInfo> dataList){
        this.dataList = dataList;
    }

    public void setRoomType(int type) {
        roomType = type;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ConvertView view = (ConvertView)convertView;
        if (view == null) {
            view = new ConvertView(parent.getContext(), roomType);
        }
        RoomInfo roomInfo = dataList.get(position);
        view.setMemberCount(roomInfo.pushers == null ? 0: roomInfo.pushers.size());
        view.setRoomName(roomInfo.roomName);
        view.setId(roomInfo.roomID);
        return view;
    }


}
