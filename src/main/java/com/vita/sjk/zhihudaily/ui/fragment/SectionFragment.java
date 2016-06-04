package com.vita.sjk.zhihudaily.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.adapter.SectionPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sjk on 2016/6/2.
 * <p/>
 * 专栏fragment，包含一个ViewPager，然后嵌套很多个子fragment
 */
public class SectionFragment extends BaseFragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SectionPagerAdapter sectionPagerAdapter;
    private List<SectionSectionFragment> subFragments = new ArrayList<>();

    /**
     * 简单的构造器
     *
     * @return
     */
    public static SectionFragment newInstance() {
        SectionFragment ret = new SectionFragment();
        return ret;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.section_tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.section_view_pager);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * 步骤：
         *  1.配置FragmentPagerAdapter
         *  2.创建ViewPager
         *  3.TabLayout绑定该ViewPager
         */
        for (int i = 2; i <= 13; ++i) {
            subFragments.add(SectionSectionFragment.newInstance(i));
        }
        sectionPagerAdapter = new SectionPagerAdapter(getActivity().getSupportFragmentManager(), subFragments);
        viewPager.setAdapter(sectionPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        /**
         * 对于TabLayout的TabMode，有两种常用的选择：
         *  (1)MODE_SCROLLABLE：相当于wrap_content，适用于很多个tag
         *  (2)MODE_FIXED：根据屏幕宽度平均分配，适用于大约4个以内的tag
         */
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE); // tabLayout样式
    }
}
