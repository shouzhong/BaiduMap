package com.shouzhong.baidumap;

import com.baidu.location.BDLocation;

/**
 * Created by Administrator on 2019/05/15.
 */

public class LocationBean {

    public boolean isSuccess;
    public String msg;
    public BDLocation location;

    public LocationBean(boolean isSuccess, BDLocation location) {
        this.isSuccess = isSuccess;
        this.location = location;
    }

    public LocationBean(boolean isSuccess, String msg) {
        this.isSuccess = isSuccess;
        this.msg = msg;
    }
}
