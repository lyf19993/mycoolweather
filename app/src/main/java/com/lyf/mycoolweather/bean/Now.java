package com.lyf.mycoolweather.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 刘亚飞 on 2017/3/28.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
