package com.vita.sjk.zhihudaily.bean;

/**
 * Created by sjk on 2016/6/2.
 *
 * 栏目类
 * 即栏目的简略介绍
 */
public class Section {

    /**
     * color : 9699556
     * thumbnail : http://pic2.zhimg.com/98d7b4f8169c596efb6ee8487a30c8ee.jpg
     * description : 把黑客知识科普到你的面前
     * id : 10
     * name : 互联网安全
     */

    private int color;
    private String thumbnail;
    private String description;
    private int id;
    private String name;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
