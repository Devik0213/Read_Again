package com.devik.readagain.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * domain만 카운트하여 관리.
 * Created by naver on 16. 3. 10..
 */
public class Domain extends RealmObject {
    public static final String DOMAIN = "domain";
    @PrimaryKey
    private String domain;
    private String name;
    private String favicon;
    private int count;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
