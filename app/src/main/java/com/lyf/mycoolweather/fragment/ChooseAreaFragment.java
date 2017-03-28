package com.lyf.mycoolweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lyf.mycoolweather.R;
import com.lyf.mycoolweather.activity.WeatherActivity;
import com.lyf.mycoolweather.db.City;
import com.lyf.mycoolweather.db.County;
import com.lyf.mycoolweather.db.Province;
import com.lyf.mycoolweather.util.Utility;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import okhttp3.Call;

/**
 * Created by 刘亚飞 on 2017/3/28.
 * 选择城市的fragment
 */

public class ChooseAreaFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    @Bind(R.id.bt_back)
    Button bt_back;

    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.lv)
    ListView lv;

    private ProgressDialog dialog;

    //列表adapter
    private ArrayAdapter<String> adapter;
    //数据源
    private List<String> datas = new ArrayList<>();
    //选择的省
    private Province selectProvince;
    //选择的市
    private City selectCity;
    //选择的县
    private County selectCounty;
    //当前选择的标志
    private int currentLevel;

    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;


    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    @Override
    protected View addLayout(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.choose_area, null);
        return view;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, datas);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_CITY) {
                    querProvince();
                } else if (currentLevel == LEVEL_COUNTY) {
                    querCities();
                }

            }
        });
        querProvince();

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (currentLevel == LEVEL_PROVINCE) {
            selectProvince = provinceList.get(i);
            querCities();
        } else if (currentLevel == LEVEL_CITY) {
            selectCity = cityList.get(i);
            querCounty();
        }else if (currentLevel == LEVEL_COUNTY) {
                String weatherId = countyList.get(i).getWeatherId();
            Intent intent = new Intent(getActivity(), WeatherActivity.class);
            intent.putExtra("weather_id",weatherId);
            startActivity(intent);
            getActivity().finish();
        }
    }

    /**
     * 查找县
     */
    private void querCounty() {
        tv_title.setText(selectCity.getCityName());
        bt_back.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid=?",String.valueOf(selectCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            datas.clear();
            for (County county : countyList) {
                datas.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }


    }

    /**
     * 查找省对应的市
     */
    private void querCities() {
        tv_title.setText(selectProvince.getProvinceName());
        bt_back.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid=?",String.valueOf(selectProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            datas.clear();
            for (City city : cityList) {
                datas.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            currentLevel = LEVEL_CITY;

        } else {
            int provinceCode = selectProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }


    }


    /**
     * 查找省
     */
    private void querProvince() {
        tv_title.setText("中国");
        bt_back.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            datas.clear();
            for (Province province : provinceList) {
                datas.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            //从服务器查询
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }


    }


    /**
     * 从服务器查询
     *
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showDialog();
        OkHttpUtils.get().url(address).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getActivity(),"记载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(String response, int id) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(response);

                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(response, selectProvince.getId());

                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(response, selectCity.getId());
                }

                if (result) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeDialog();
                            if ("province".equals(type)) {
                                querProvince();
                            } else if ("city".equals(type)) {
                                querCities();
                            } else if ("county".equals(type)) {
                                querCounty();
                            }

                        }
                    });

                }
            }
        });

    }

    /**
     * 显示进度对话框
     */
    private void showDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("正在加载");
            dialog.setCanceledOnTouchOutside(false);
        }

        dialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}
