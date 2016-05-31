package com.vita.sjk.zhihudaily.bean;

import java.util.List;

/**
 * Created by sjk on 2016/5/27.
 *
 * 最新新闻系列:消息体（也就是直接解析json的）
 * 然后会进一步解析成很多类
 */
public class ResponseLatest {

    private String date;

    private List<Story> stories;

    public List<Story> getStories() {
        return stories;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStories(List<Story> stories) {
        this.stories = stories;
    }
}
