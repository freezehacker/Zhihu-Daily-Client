package com.vita.sjk.zhihudaily.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.constants.Constants;
import com.vita.sjk.zhihudaily.utils.LogUtils;

/**
 * Created by sjk on 2016/6/5.
 * <p/>
 * 注意，PreferenceFragment是继承app下的Fragment而不是v4的
 */
public class SettingsFragment extends PreferenceFragment {

    public static final String GAME = "game";
    public static final String MOVIE = "movie";
    public static final String DESIGN = "design";
    public static final String COMPANY = "company";
    public static final String COMMERCIAL = "commercial";
    public static final String MUSIC = "music";
    public static final String SPORTS = "sports";
    public static final String COMIC = "comic";
    public static final String INTERNET = "internet";
    public static final String FUN = "fun";
    public static final String RECOMMEND = "recommend";
    public static final String PSYCHOLOGY = "psychology";


    /**
     * 12个复选框
     */
    CheckBoxPreference pref_game;
    CheckBoxPreference pref_movie;
    CheckBoxPreference pref_design;
    CheckBoxPreference pref_company;
    CheckBoxPreference pref_commercial;
    CheckBoxPreference pref_music;
    CheckBoxPreference pref_sports;
    CheckBoxPreference pref_comic;
    CheckBoxPreference pref_internet;
    CheckBoxPreference pref_fun;
    CheckBoxPreference pref_recommend;
    CheckBoxPreference pref_psychology;

    public static SettingsFragment newInstance() {
        SettingsFragment ret = new SettingsFragment();
        return ret;
    }

    /**
     * 只需要写onCreate，不需要重写onCreateView，有点特殊
     * 即只需要在onCreate里引入xml文件
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        /**
         * initialize views
         */
        pref_game = (CheckBoxPreference) findPreference(GAME);
        pref_movie = (CheckBoxPreference) getPreferenceManager().findPreference(MOVIE);
        pref_design = (CheckBoxPreference) findPreference(DESIGN);
        pref_company = (CheckBoxPreference) findPreference(COMPANY);
        pref_commercial = (CheckBoxPreference) findPreference(COMMERCIAL);
        pref_music = (CheckBoxPreference) findPreference(MUSIC);
        pref_sports = (CheckBoxPreference) findPreference(SPORTS);
        pref_comic = (CheckBoxPreference) findPreference(COMIC);
        pref_internet = (CheckBoxPreference) findPreference(INTERNET);
        pref_fun = (CheckBoxPreference) findPreference(FUN);
        pref_recommend = (CheckBoxPreference) findPreference(RECOMMEND);
        pref_psychology = (CheckBoxPreference) findPreference(PSYCHOLOGY);

        /**
         * initialize last settings
         */
        SharedPreferences sp = getActivity().getSharedPreferences(Constants.SHARED_PREF_SETTINGS, Constants.SHARED_PREF_SETTINGS_MODE);
        pref_game.setChecked(sp.getBoolean(GAME, true));
        pref_movie.setChecked(sp.getBoolean(MOVIE, true));
        pref_design.setChecked(sp.getBoolean(DESIGN, true));
        pref_company.setChecked(sp.getBoolean(COMPANY, true));
        pref_commercial.setChecked(sp.getBoolean(COMMERCIAL, true));
        pref_music.setChecked(sp.getBoolean(MUSIC, true));
        pref_sports.setChecked(sp.getBoolean(SPORTS, true));
        pref_comic.setChecked(sp.getBoolean(COMIC, true));
        pref_internet.setChecked(sp.getBoolean(INTERNET, true));
        pref_fun.setChecked(sp.getBoolean(FUN, true));
        pref_recommend.setChecked(sp.getBoolean(RECOMMEND, true));
        pref_psychology.setChecked(sp.getBoolean(PSYCHOLOGY, true));
    }

    /**
     * 当碎片不可见时，存储用户勾选的选项到SharedPref中
     */
    @Override
    public void onStop() {
        storePref();
        super.onStop();
    }

    /**
     * 存储用户偏好
     */
    private void storePref() {
        SharedPreferences.Editor editor = getActivity()
                .getSharedPreferences(Constants.SHARED_PREF_SETTINGS, Constants.SHARED_PREF_SETTINGS_MODE)
                .edit();
        editor.putBoolean(GAME, pref_game.isChecked());
        editor.putBoolean(MOVIE, pref_movie.isChecked());
        editor.putBoolean(DESIGN, pref_design.isChecked());
        editor.putBoolean(COMPANY, pref_company.isChecked());
        editor.putBoolean(COMMERCIAL, pref_commercial.isChecked());
        editor.putBoolean(MUSIC, pref_music.isChecked());
        editor.putBoolean(SPORTS, pref_sports.isChecked());
        editor.putBoolean(COMIC, pref_comic.isChecked());
        editor.putBoolean(INTERNET, pref_internet.isChecked());
        editor.putBoolean(FUN, pref_fun.isChecked());
        editor.putBoolean(RECOMMEND, pref_recommend.isChecked());
        editor.putBoolean(PSYCHOLOGY, pref_psychology.isChecked());
        editor.commit();

        LogUtils.log("保存好了pref");
    }
}
