package com.vita.sjk.zhihudaily.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.ui.fragment.SettingsFragment;

/**
 * Created by sjk on 2016/6/5.
 */
public class SettingsActivity extends BaseActivity {

    public static final int CONTAINER_ID = R.id.settings_container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView title = (TextView)findViewById(R.id.settings_title);
        title.setText("设置");

       /**
         * 显示出设置界面（这里注意要用FrameLayout装载一个fragment，而不是直接用fragment）
         */
        getFragmentManager().beginTransaction().replace(CONTAINER_ID, SettingsFragment.newInstance()).commit();
    }
}
