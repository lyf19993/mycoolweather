package com.lyf.mycoolweather.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lyf.mycoolweather.R;
import com.lyf.mycoolweather.bean.Forecast;
import com.lyf.mycoolweather.bean.Weather;
import com.lyf.mycoolweather.util.Utility;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;

/**
 * Created by 刘亚飞 on 2017/3/28.
 * 天气activity
 */

public class WeatherActivity extends Activity {
    @Bind(R.id.weather_layout)
    ScrollView weatherLayout;
    //城市
    @Bind(R.id.title_city)
    TextView titleCity;
    //更新时间
    @Bind(R.id.title_update_time)
    TextView titleUpdateTime;
    //度数
    @Bind(R.id.degree_text)
    TextView degreeText;
    //天气状况
    @Bind(R.id.weather_info_text)
    TextView weatherInfotext;
    //预报
    @Bind(R.id.forecast_layout)
    LinearLayout forecastLayout;
    //空气质量
    @Bind(R.id.aqi_text)
    TextView aqiText;
    //PM2.5
    @Bind(R.id.pm25_text)
    TextView pm25Text;
    //舒适度
    @Bind(R.id.comfort_text)
    TextView comfortText;
    //洗车指数
    @Bind(R.id.car_wash_text)
    TextView carWashText;
    //运动建议
    @Bind(R.id.sport_text)
    TextView sportText;

    @Bind(R.id.iv_bg)
    ImageView ivBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVersion();
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = preferences.getString("bing_pic",null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(ivBg);
        }else {
            loadBingPic();
        }
        String weatherString = preferences.getString("weather",null);
        if (weatherString != null) {
            //有缓存数据直接解析天气
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else {
            //去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    /**
     * 判断当前版本信息
     */
    private void initVersion() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorview = getWindow().getDecorView();
            decorview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


    }

    private void loadBingPic() {
        String requestBingpic = "http://guolin.tech/api/bing_pic";
        OkHttpUtils.get().url(requestBingpic).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(final String response, int id) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",response);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      Glide.with(WeatherActivity.this).load(response).into(ivBg);

                    }
                });
            }
        });
    }

    /**
     * 根据Id请求城市天气数据
     * @param weatherId
     */
    private void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=a8dd8997274444719fea3d163824740b";
        OkHttpUtils.get().url(weatherUrl).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(final String response, int id) {
                final Weather weather = Utility.handleWeatherResponse(response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",response);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                loadBingPic();
            }
        });

    }


    /**
     * 展示天气信息
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName =weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfotext.setText(weatherInfo);

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash = "洗车指数："+weather.suggestion.carWash.info;
        String sport = "运动建议" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);




    }
}
