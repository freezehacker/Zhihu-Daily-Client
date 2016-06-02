package com.vita.sjk.zhihudaily.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by sjk on 2016/6/2.
 *
 * 定制Fragment的基类
 * 这里改成了v4包下的Fragment，因为FragmentPagerAdapter不能使用app下的Fragment...
 */
public class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
