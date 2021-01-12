package com.dieam.reactnativepushnotification.dst.models;

public class DSTData {
    private String country = null;
    private String start_week_day = null;
    private String start_month = null;
    private Integer start_month_index = null;
    private Integer start_time = null;
    private String end_week_day = null;
    private String end_month = null;
    private Integer end_month_index = null;
    private Integer end_time = null;
    private Integer start_day = null;
    private Integer end_day = null;


    // Getter Methods 

    public String getCountry() {
        return country;
    }

    public String getStart_week_day() {
        return start_week_day;
    }

    public String getStart_month() {
        return start_month;
    }

    public int getStart_month_index() {
        return start_month_index;
    }

    public int getStart_time() {
        return start_time;
    }

    public String getEnd_week_day() {
        return end_week_day;
    }

    public String getEnd_month() {
        return end_month;
    }

    public int getEnd_month_index() {
        return end_month_index;
    }

    public int getEnd_time() {
        return end_time;
    }

    public int getStart_day() {
        return start_day;
    }

    public int getEnd_day() {
        return end_day;
    }

    // Setter Methods 

    public void setCountry(String country) {
        this.country = country;
    }

    public void setStart_week_day(String start_week_day) {
        this.start_week_day = start_week_day;
    }

    public void setStart_month(String start_month) {
        this.start_month = start_month;
    }

    public void setStart_month_index(int start_month_index) {
        this.start_month_index = start_month_index;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    public void setEnd_week_day(String end_week_day) {
        this.end_week_day = end_week_day;
    }

    public void setEnd_month(String end_month) {
        this.end_month = end_month;
    }

    public void setEnd_month_index(int end_month_index) {
        this.end_month_index = end_month_index;
    }

    public void setEnd_time(int end_time) {
        this.end_time = end_time;
    }

    public void setStart_day(int start_day) {
        this.start_day = start_day;
    }

    public void setEnd_day(int end_day) {
        this.end_day = end_day;
    }
}
