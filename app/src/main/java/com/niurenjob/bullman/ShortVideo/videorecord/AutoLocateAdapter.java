package com.niurenjob.bullman.ShortVideo.videorecord;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.niurenjob.bullman.ShortVideo.R;

import java.util.ArrayList;

/**
 * Created by yuejiaoli on 2017/9/8.
 */

public class AutoLocateAdapter extends RecyclerView.Adapter<AutoLocateAdapter.ViewHolder> {
    public static int ITEM_NUM = 5;
    private static int mItemWidth;
    private Context mContext;
    private ArrayList<String> mData;
    private View view;
    private int mHighlight;
    private RecyclerView mRecyclerView;
    private int mX;
    private int mIndex;
    private TCVideoRecordActivity.OnSpeedItemClickListener mOnItemClickListener;

    public AutoLocateAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<String>();
        mItemWidth = mContext.getResources().getDisplayMetrics().widthPixels / ITEM_NUM;
    }

    public void addAll(ArrayList<String> recordSpeed) {
        mData.addAll(recordSpeed);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.item_record_speed, parent, false);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = mItemWidth;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.textView.setText(mData.get(position));

        // 高亮显示
        if (isSelected(position)) {
            holder.textView.setTextColor(mContext.getResources().getColor(R.color.record_speed_text_select));
        } else {
            holder.textView.setTextColor(mContext.getResources().getColor(R.color.record_speed_text_normal));
        }
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = position - mIndex;
                mRecyclerView.scrollBy(pos * mItemWidth, 0);
                highlightItem(getMiddlePosition());
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onRecordSpeedItemSelected(getMiddlePosition() - 2);
            }
        });
    }

    // 高亮中心, 更新前后位置
    public void highlightItem(int position) {
        mHighlight = position;
        int offset = ITEM_NUM / 2;
        for (int i = position - offset; i <= position + offset; ++i)
            notifyItemChanged(i);
    }

    // 判断是否是高亮
    public boolean isSelected(int position) {
        return mHighlight == position;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setView(final RecyclerView view) {
        mRecyclerView = view;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        view.setLayoutManager(linearLayoutManager);
        view.setAdapter(this);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return;
                highlightItem(getMiddlePosition());
                int position = getScrollPosition();
                view.scrollToPosition(position);
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onRecordSpeedItemSelected(getMiddlePosition() - 2);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mX = dx;
                mIndex = getMiddlePosition();
            }
        });
        highlightItem(getMiddlePosition());
    }

    /**
     * 获取中间位置
     *
     * @return 当前值
     */
    private int getMiddlePosition() {
        return getScrollPosition() + (AutoLocateAdapter.ITEM_NUM / 2);
    }

    /**
     * 获取滑动值, 滑动偏移 / 每个格子宽度
     *
     * @return 当前值
     */
    private int getScrollPosition() {
        return (int) ((double) mRecyclerView.computeHorizontalScrollOffset() / (double) mItemWidth);
    }

    public void setOnItemClickListener(TCVideoRecordActivity.OnSpeedItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }
    }
}
