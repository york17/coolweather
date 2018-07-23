package com.coolweather.android.db;

import org.litepal.crud.LitePalSupport;

/**
 * Created by lee on 2018/7/23.
 */

public class City extends LitePalSupport {

    private int id;
    private String cityName;  //城市名称
    private int cityCode;  //城市编码
    private int provinceId;  //省份Id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
