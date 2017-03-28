package com.lyf.mycoolweather.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by 刘亚飞 on 2017/3/28.
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
