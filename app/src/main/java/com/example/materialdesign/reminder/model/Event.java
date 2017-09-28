package com.example.materialdesign.reminder.model;

public class Event {

    public static final int EVENT_TYPE = 1;

    private int id;
    private String title;
    private String description;
    private String place;
    private String category;
    private String time;
    private String date;
    private boolean isShow;
    private int type;
    private String notify;
    private String repeatMode;
    private String repeatCount;
    private String repeatType;

    public Event(String title, String description, String place, String category, String time, String date,int type,String notify,String repeatMode,String repeatType,String repeatCount) {
        this.title = title;
        this.description = description;
        this.place = place;
        this.category = category;
        this.time = time;
        this.date = date;
        this.type = type;
        this.notify = notify;
        this.repeatMode = repeatMode;
        this.repeatType = repeatType;
        this.repeatCount = repeatCount;
    }

    public Event(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getNotify() {
        return notify;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }


    public String getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(String repeatMode) {
        this.repeatMode = repeatMode;
    }

    public String getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(String repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }
}
