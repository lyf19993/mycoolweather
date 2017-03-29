package com.lyf.mycoolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.lyf.mycoolweather.Contast;
import com.lyf.mycoolweather.bean.Weather;
import com.lyf.mycoolweather.util.Utility;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * Created by 刘亚飞 on 2017/3/29.
 */

public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //8个小时对应的毫秒数
        int anhour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anhour;
        Intent intent1 = new Intent(this,AutoUpdateService.class);
        PendingIntent intent2 = PendingIntent.getService(this,0,intent1,0);
        manager.cancel(intent2);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,intent2);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新图片信息
     */
    private void updateBingPic() {
        final String requestBingPic = Contast.url + "/bing_pic";
        OkHttpUtils.get().url(requestBingPic).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                String bing_pic = response;
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bing_pic);
                editor.apply();


            }
        });


    }


    /**
     * 更新天气信息
     */
    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = Contast.url + "/weather?cityid=" + weatherId
                    + "&key=a8dd8997274444719fea3d163824740b";

            OkHttpUtils.get().url(weatherUrl).build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {

                }

                @Override
                public void onResponse(String response, int id) {
                    Weather weather1 = Utility.handleWeatherResponse(response);
                    if (weather1 != null && "ok".equals(weather1.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", response);
                        editor.apply();

                    }

                }
            });
        }

    }
}
