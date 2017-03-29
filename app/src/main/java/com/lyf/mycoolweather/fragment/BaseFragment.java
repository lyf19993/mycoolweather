package com.lyf.mycoolweather.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;


import butterknife.ButterKnife;


/**
 * Created by 刘亚飞 on 2017/3/6.
 * Fragment的基类
 */

public abstract class BaseFragment extends Fragment {

    //根布局
    private View mViewRoot;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    /**
     * 创建布局
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //判断是否为空，为空的时候去加载布局，onCreateView在界面切换的时候被多次调用，防止界面跳转回来的时候显示空白
        if (mViewRoot == null) {
            mViewRoot = addLayout(inflater);
        }
        ButterKnife.bind(this, mViewRoot);
        afterCreate(savedInstanceState);
        return mViewRoot;
    }

    /**
     * 添加布局
     *
     * @param inflater
     * @return
     */
    protected abstract View addLayout(LayoutInflater inflater);

    /**
     * 加载布局后调用的方法
     *
     * @param savedInstanceState
     */
    protected abstract void afterCreate(Bundle savedInstanceState);


    /**
     * 当界面被切换出去调用的方法，解决ViewGroup只有一个子View的bug
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mViewRoot != null) {
            ViewParent parent = mViewRoot.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                viewGroup.removeView(mViewRoot);
            }
        }
    }



}
