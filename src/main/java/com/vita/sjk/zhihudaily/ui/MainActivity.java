package com.vita.sjk.zhihudaily.ui;

import android.support.v4.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.constants.Constants;
import com.vita.sjk.zhihudaily.ui.fragment.BaseFragment;
import com.vita.sjk.zhihudaily.ui.fragment.CollectFragment;
import com.vita.sjk.zhihudaily.ui.fragment.LatestFragment;
import com.vita.sjk.zhihudaily.ui.fragment.SectionFragment;
import com.vita.sjk.zhihudaily.utils.CacheUtils;

/**
 * Created by sjk on 2016/6/2.
 */
public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    /**
     * FrameLayout，用来显示任意一个fragment
     * （这里我称作switcher而不是container，因为每次只能显示一个）
     */
    private FrameLayout fragmentSwitcher;

    /**
     * FrameLayout的id，add一个fragment的时候会用到作为参数
     */
    private static final int SWITCHER_ID = R.id.fragment_switcher;

    /**
     * 记录当前fragment
     */
    private BaseFragment currentFragment;

    /**
     * 单选控件，用来选择要显示的fragment
     */
    private RadioGroup radioGroup;
    private RadioButton btn_latest, btn_collect, btn_section;

    /**
     * 所有种类的碎片的实例
     */
    private LatestFragment latestFragment;
    private CollectFragment collectFragment;
    private SectionFragment sectionFragment;

    /**
     * 标题栏
     */
    private Toolbar toolbar;
    private TextView toolbarText;
    private static final String[] titles = {"最新", "专栏", "收藏"};

    /**
     * 底部radioButton中需要显示的drawable的一些参数
     * 因为单位是像素（如果没猜错），最好根据屏幕大小去动态地适配
     * 而不是设成一个定值
     */
    private int drawableBoundRight;
    private int drawableBoundBottom;
    private int drawablePaddingTop;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVariables();
        initializeViews();
        bindViews();
    }

    /**
     * 初始化view
     */
    private void initializeViews() {
        fragmentSwitcher = (FrameLayout) findViewById(R.id.fragment_switcher);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        toolbar = (Toolbar) findViewById(R.id.global_toolbar);
        toolbarText = (TextView) findViewById(R.id.toolbar_text);
        btn_latest = (RadioButton) findViewById(R.id.rb_latest);
        btn_collect = (RadioButton) findViewById(R.id.rb_collect);
        btn_section = (RadioButton) findViewById(R.id.rb_section);
    }

    /**
     * 绑定view的行为
     */
    private void bindViews() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        radioGroup.setOnCheckedChangeListener(this);

        /**
         * 调节radioButtons的图片的大小
         * 注意这个操作只能在java代码中定义，而不能在xml中设置
         * 关键是把drawable放在radioButton中之前要setBound
         * 养成良好习惯：在java代码中显示drawable之前都要setBound
         */
        Drawable drawableLatest = getDrawable(R.drawable.bg_latest_pic);
        drawableLatest.setBounds(0, 0, drawableBoundRight, drawableBoundBottom);
        btn_latest.setPadding(0, drawablePaddingTop, 0, 0);
        btn_latest.setCompoundDrawables(null, drawableLatest, null, null);

        Drawable drawableSection = getDrawable(R.drawable.bg_section_pic);
        drawableSection.setBounds(0, 0, drawableBoundRight, drawableBoundBottom);
        btn_section.setPadding(0, drawablePaddingTop, 0, 0);
        btn_section.setCompoundDrawables(null, drawableSection, null, null);

        Drawable drawableCollect = getDrawable(R.drawable.bg_collect_pic);
        drawableCollect.setBounds(0, 0, drawableBoundRight, drawableBoundBottom);
        btn_collect.setPadding(0, drawablePaddingTop, 0, 0);
        btn_collect.setCompoundDrawables(null, drawableCollect, null, null);

        /**
         * 一开始把第一个RadioButton选上，这样可以初始化第一个fragment
         */
        btn_latest.setChecked(true);
        toolbarText.setText(titles[0]);
    }

    /**
     * 定义一些变量的值，比如适应屏幕大小的值
     */
    private void initVariables() {
        drawableBoundRight = 80;
        drawableBoundBottom = 80;
        drawablePaddingTop = 16;
    }


    /**
     * 单选控件监听器的回调
     * 包括懒加载（高效切换fragment）操作
     *
     * @param group     单选控件
     * @param checkedId 单选按钮的id
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideAllFragments(transaction);
        switch (checkedId) {
            case R.id.rb_latest:
                toolbarText.setText(titles[0]);
                if (latestFragment == null) {
                    latestFragment = LatestFragment.newInstance();
                    transaction.add(SWITCHER_ID, latestFragment);
                } else {
                    transaction.show(latestFragment);
                }
                break;
            case R.id.rb_section:
                toolbarText.setText(titles[1]);
                if (sectionFragment == null) {
                    sectionFragment = SectionFragment.newInstance();
                    transaction.add(SWITCHER_ID, sectionFragment);
                } else {
                    transaction.show(sectionFragment);
                }
                break;
            case R.id.rb_collect:
                toolbarText.setText(titles[2]);
                if (collectFragment == null) {
                    collectFragment = CollectFragment.newInstance();
                    transaction.add(SWITCHER_ID, collectFragment);
                } else {
                    transaction.show(collectFragment);
                }
                break;
            default:
                break;
        }
        transaction.commit();
    }

    /**
     * 将所有碎片设为不可见
     * 这是个辅助操作
     */
    private void hideAllFragments(FragmentTransaction t) {
        if (latestFragment != null) {
            t.hide(latestFragment);
        }
        if (collectFragment != null) {
            t.hide(collectFragment);
        }
        if (sectionFragment != null) {
            t.hide(sectionFragment);
        }
    }

    /**
     * 用户按后退键触发的回调
     */
    long exitTime = 0;

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        if (curTime - exitTime > Constants.EXIT_TIME_INTERVAL) {
            exitTime = curTime;
            Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_main_delete:
                CacheUtils.clearDiskCache();
                Toast.makeText(MainActivity.this, "成功删除本地缓存!", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }
}
