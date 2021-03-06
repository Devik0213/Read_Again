package com.devik.readagain.model;

import java.util.Date;

import io.realm.RealmObject;

import com.devik.readagain.ActionType;

/**
 * Created by naver on 16. 1. 10..
 */
public class Time extends RealmObject {
    /**
     * @see com.devik.readfav.ActionType
     * 0 : read
     * 1 : save
     */
    private int actionType;
    private Date date = new Date();

    public Time(){
        this.date = new Date();
    }

    public Time(ActionType actionType){
        this.actionType = actionType.ordinal();
        this.date = new Date();
    }

    public Time(Date date) {
        this.date = date;
    }

    public Time(int ordinal, Date date) {
        this.actionType = ordinal;
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

}
