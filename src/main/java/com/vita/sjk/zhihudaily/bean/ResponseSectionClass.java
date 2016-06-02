package com.vita.sjk.zhihudaily.bean;

import java.util.List;

/**
 * Created by sjk on 2016/6/2.
 */
public class ResponseSectionClass {

    /**
     * limit : 1000
     * subscribed : [""]
     * others : [{"color":15007,"thumbnail":"http://pic3.zhimg.com/0e71e90fd6be47630399d63c58beebfc.jpg","description":"了解自己和别人，了解彼此的欲望和局限。","id":13,"name":"日常心理学"}]
     */

    private int limit;
    private List<String> subscribed;
    private List<Section> others;

    public List<Section> getOthers() {
        return others;
    }

    public void setOthers(List<Section> others) {
        this.others = others;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<String> getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(List<String> subscribed) {
        this.subscribed = subscribed;
    }

}
