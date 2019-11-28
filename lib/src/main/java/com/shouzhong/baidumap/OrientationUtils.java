package com.shouzhong.baidumap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


/**
 * Created by Administrator on 2017-09-12.
 *
 * 手机指向传感器监听
 */

public class OrientationUtils implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float lastX;
    private OnOrientationListener mOnOrientationListener;

    public OrientationUtils() {
    }

    public void start() {
        mSensorManager = (SensorManager) MapUtils.getApp().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            //获得方向传感器
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        //判断是否有方向传感器
        if (mSensor != null) {
            //注册监听器
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        mSensorManager.unregisterListener(this);

    }

    //方向改变
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float x = event.values[SensorManager.DATA_X];
            if (Math.abs(x - lastX) > 1.0) {
                if (mOnOrientationListener != null) {
                    mOnOrientationListener.onOrientationChanged(x);
                }
            }
            lastX = x;

        }

    }

    public void setOnOrientationListener(OnOrientationListener listener) {
        mOnOrientationListener = listener;
    }

    public interface OnOrientationListener {
        void onOrientationChanged(float x);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
