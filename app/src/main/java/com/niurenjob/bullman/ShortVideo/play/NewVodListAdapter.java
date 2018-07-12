package com.niurenjob.bullman.ShortVideo.play;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.niurenjob.bullman.ShortVideo.R;
import com.niurenjob.bullman.ShortVideo.common.utils.TCUtils;
import com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.view.VodRspData;

import java.util.ArrayList;


/**
 * Created by liyuejiao on 2018/1/31.
 */

public class NewVodListAdapter extends RecyclerView.Adapter<NewVodListAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<VodRspData> mVodList;
    private OnItemClickLitener mOnItemClickLitener;

    public NewVodListAdapter(Context context) {
        mContext = context;
        mVodList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_new_vod, null);
        return new NewVodListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final VodRspData data = mVodList.get(position);
        Glide.with(mContext).load(data.cover).into(holder.thumb);
        holder.thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickLitener != null) {
                    mOnItemClickLitener.onItemClick(position, data.title, data.url);
                }
            }
        });
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickLitener != null) {
                    mOnItemClickLitener.onItemClick(position, data.title, data.url);
                }
            }
        });
        holder.duration.setText(TCUtils.formattedTime(data.duration));
        if (data.title != null) {
            holder.title.setText(data.title);
        }
    }

    @Override
    public int getItemCount() {
        return mVodList.size();
    }

    public void clear(){
        mVodList.clear();
        notifyDataSetChanged();
    }

    public void addVideoInfo(VodRspData data) {
        notifyItemInserted(mVodList.size());
        mVodList.add(data);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView duration;
        private TextView title;
        private ImageView thumb;

        public ViewHolder(final View itemView) {
            super(itemView);
            thumb = (ImageView) itemView.findViewById(R.id.imageView);
            title = (TextView) itemView.findViewById(R.id.textview);
            duration = (TextView) itemView.findViewById(R.id.tv_duration);
        }
    }

    public void setOnItemClickLitener(OnItemClickLitener listener) {
        mOnItemClickLitener = listener;
    }

    public interface OnItemClickLitener {
        void onItemClick(int position, String title, String url);
    }
}
