package com.vita.sjk.zhihudaily.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.vita.sjk.zhihudaily.api.API;
import com.vita.sjk.zhihudaily.ui.fragment.SectionFragment;
import com.vita.sjk.zhihudaily.ui.fragment.SectionSectionFragment;

import java.util.List;

/**
 * Created by sjk on 2016/6/3.
 */
public class SectionPagerAdapter extends FragmentPagerAdapter {

    private List<SectionSectionFragment> fragments;

    /**
     * 可以留意一下构造器传入的参数
     *
     * @param fm
     */
    public SectionPagerAdapter(FragmentManager fm, List<SectionSectionFragment> inFragments) {
        super(fm);
        fragments = inFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return API.SECTION[position + 2];
    }
}
