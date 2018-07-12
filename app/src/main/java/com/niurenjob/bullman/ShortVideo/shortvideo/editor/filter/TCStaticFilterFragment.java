package com.niurenjob.bullman.ShortVideo.editor.filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.niurenjob.bullman.ShortVideo.R;
import com.niurenjob.bullman.ShortVideo.editor.TCVideoEditerWrapper;
import com.niurenjob.bullman.ShortVideo.editor.common.widget.BaseRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hans on 2017/11/6.
 * 静态滤镜的Fragment
 */
public class TCStaticFilterFragment extends Fragment implements BaseRecyclerAdapter.OnItemClickListener {
    private static final int[] FILTER_ARR = {
            R.drawable.filter_langman, R.drawable.filter_qingxin,
            R.drawable.filter_weimei, R.drawable.filter_fennen,
            R.drawable.filter_huaijiu, R.drawable.filter_landiao,
            R.drawable.filter_qingliang, R.drawable.filter_rixi};

    private List<Integer> mFilterList;
    private RecyclerView mRvFilter;
    private StaticFilterAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_static_filter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFilterList = new ArrayList<Integer>();
        mFilterList.add(R.drawable.orginal);
        mFilterList.add(R.drawable.langman);
        mFilterList.add(R.drawable.qingxin);
        mFilterList.add(R.drawable.weimei);
        mFilterList.add(R.drawable.fennen);
        mFilterList.add(R.drawable.huaijiu);
        mFilterList.add(R.drawable.landiao);
        mFilterList.add(R.drawable.qingliang);
        mFilterList.add(R.drawable.rixi);

        mRvFilter = (RecyclerView) view.findViewById(R.id.filter_rv_list);
        mRvFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new StaticFilterAdapter(mFilterList);
        mAdapter.setOnItemClickListener(this);
        mRvFilter.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Bitmap bitmap = null;
        if (position == 0) {
            bitmap = null;  // 没有滤镜
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), FILTER_ARR[position - 1]);
        }
        mAdapter.setCurrentSelectedPos(position);
        // 设置滤镜图片
        TCVideoEditerWrapper.getInstance().getEditer().setFilter(bitmap);
    }

}
