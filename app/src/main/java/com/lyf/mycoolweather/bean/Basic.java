package com.lyf.mycoolweather.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 刘亚飞 on 2017/3/28.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
     @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
