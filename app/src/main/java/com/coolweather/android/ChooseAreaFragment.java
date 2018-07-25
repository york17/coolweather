package com.coolweather.android;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.Country;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by lee on 2018/7/23.
 */

enum AREA_TYPE {
    AREA_TYPE_RPOVINCE,
    AREA_TYPE_CITY,
    AREA_TYPE_COUNTRY
}

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    private ProgressDialog progressDialog = null;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;  //省列表
    private List<City> cityList;  //市列表
    private List<Country> countryList;  //县列表

    private Province selectedProvince;  //选中的省份
    private City selectedCity;  //选中的城市
    private int currentLevel;  //当前选中的级别

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.choose_area, container, false);

        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ( currentLevel == LEVEL_PROVINCE ) {
                    //省份
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if ( currentLevel == LEVEL_CITY ) {
                    //市
                    selectedCity = cityList.get(position);
                    queryCountries();
                } else if ( currentLevel == LEVEL_COUNTRY ) {
                    //县
                    String weatherId = countryList.get(position).getWeatherId();
                    if ( getActivity() instanceof MainActivity ) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if ( getActivity() instanceof WeatherActivity ) {
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.getDrawerLayout().closeDrawers();
                        activity.getSwipeRefresh().setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( currentLevel == LEVEL_CITY ) {
                    queryProvinces();
                } else if ( currentLevel == LEVEL_COUNTRY ) {
                    queryCities();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国的省份
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if ( provinceList.size()  > 0 ) {
            dataList.clear();
            for (Province province:
                    provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, AREA_TYPE.AREA_TYPE_RPOVINCE);
        }
    }

    /**
     * 查询全国市
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList =
                LitePal
                .where("provinceid = ?", String.valueOf(selectedProvince.getId()))
                .find(City.class);
        if ( cityList.size() > 0 ) {
            dataList.clear();
            for (City city:
                 cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
            queryFromServer(address, AREA_TYPE.AREA_TYPE_CITY);
        }
    }

    /**
     * 查询全国县
     */
    private void queryCountries() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countryList = LitePal.where("cityid = ?", String.valueOf(selectedCity.getId())).find(Country.class);
        if ( countryList.size() > 0 ) {

            dataList.clear();
            for (Country country:
                 countryList) {
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, AREA_TYPE.AREA_TYPE_COUNTRY);
        }
    }

    /**
     * 从服务器获取 省份、市、县数据
     * @param address 请求地址
     * @param type  类型
     */
    private void queryFromServer(String address, final AREA_TYPE type) {
        showProgressDialog();  //展示加载框

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;

                if ( type == AREA_TYPE.AREA_TYPE_RPOVINCE ) {
                    //省份
                    result = Utility.handleProvincerResponse(responseText);
                } else if ( type == AREA_TYPE.AREA_TYPE_CITY ) {
                    //市
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ( type == AREA_TYPE.AREA_TYPE_COUNTRY ) {
                    //县
                    result = Utility.handleCountryResponse(responseText, selectedCity.getId());
                }

                if ( result ) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();

                            if ( type == AREA_TYPE.AREA_TYPE_RPOVINCE ) {
                                //省份
                                queryProvinces();
                            } else if ( type == AREA_TYPE.AREA_TYPE_CITY ) {
                                //市
                                queryCities();
                            } else if ( type == AREA_TYPE.AREA_TYPE_COUNTRY ) {
                                //县
                                queryCountries();
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
    private void showProgressDialog() {
        if ( progressDialog == null ) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if ( progressDialog != null ) {
            progressDialog.dismiss();
        }
    }
}
