package com.lyf.mycoolweather.bean;

/**
 * Created by 刘亚飞 on 2017/3/28.
 */

public class AQI {
    public AQICity city;

    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
