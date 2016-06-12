package com.devik.readagain.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by naver on 16. 1. 10..
 */
public class Article extends RealmObject{
    public static final String URL_FEILD_NAME = "url";
    public static final String ARCHIVE_FEILD_NAME = "archive";
    public static final String SAVE_DATE = "date";
    public static final String ALARM = "alarmDate";
    @PrimaryKey
    private String url;
    private String thumbnail;
    private String title;
    private String content;
    private boolean archive = false;
    private boolean read = false;
    private Date date;
    private Date alarmDate;


    public Date getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(Date alarmDate) {
        this.alarmDate = alarmDate;
    }


    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Article(){
        this.date = new Date();
    }

    public Article(String title, String content, String thumbnail, String url){
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
        this.url = url;
        this.read = false;
        this.date = new Date();
        this.alarmDate = null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
