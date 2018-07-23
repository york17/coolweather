package com.coolweather.android.db;

import org.litepal.crud.LitePalSupport;

/**
 * Created by lee on 2018/7/23.
 */

public class Province extends LitePalSupport {

    private int id;
    private String provinceName;  //省份名称
    private int provinceCode;  //省份编码


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
