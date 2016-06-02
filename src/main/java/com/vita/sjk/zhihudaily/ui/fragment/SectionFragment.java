package com.vita.sjk.zhihudaily.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vita.sjk.zhihudaily.R;

/**
 * Created by sjk on 2016/6/2.
 */
public class SectionFragment extends BaseFragment {

    public static SectionFragment newInstance() {
        SectionFragment ret = new SectionFragment();
        return ret;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section, container, false);
        return view;
    }
}
