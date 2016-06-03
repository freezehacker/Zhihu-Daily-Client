package com.vita.sjk.zhihudaily.bean;

import java.util.List;

/**
 * Created by sjk on 2016/5/27.
 * 故事，其实是一条新闻的简略版
 * 是为了消息返回的时候符合json格式而设
 * 另外，News类才是真正的新闻类
 */
public class Story {

    /**
     * images : ["http://pic3.zhimg.com/50ad10fb39e29c7999a29ffdf2dfab66.jpg"]
     * type : 0
     * id : 8363667
     * ga_prefix : 052718
     * title : 刚开始租房的年轻人，你可能需要这些整理房间的思路
     */

    private int type;
    private long id;
    private String ga_prefix;
    private String title;
    private List<String> images;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGa_prefix() {
        return ga_prefix;
    }

    public void setGa_prefix(String ga_prefix) {
        this.ga_prefix = ga_prefix;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
