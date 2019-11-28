package com.shouzhong.baidumap;

import android.annotation.SuppressLint;
import android.app.Application;

import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.shouzhong.baidumap.overlayutil.BikingRouteOverlay;
import com.shouzhong.baidumap.overlayutil.DrivingRouteOverlay;
import com.shouzhong.baidumap.overlayutil.OverlayManager;
import com.shouzhong.baidumap.overlayutil.WalkingRouteOverlay;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by Administrator on 2017-09-18.
 * <p>
 * 一次获取地址
 */

public class MapUtils {

    /**
     * 设置中心位置
     *
     * @param baiduMap
     * @param center
     */
    public static void setCenter(BaiduMap baiduMap, LatLng center) {
        if (baiduMap == null || center == null) return;
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(center)
                .zoom(15.1f)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        baiduMap.setMapStatus(mMapStatusUpdate);
    }

    /**
     * 设置我的位置
     *
     * @param baiduMap
     * @param currentMarker
     */
    public static void setMyLocationIcon(BaiduMap baiduMap, BitmapDescriptor currentMarker) {
        // 设置当前位置
        baiduMap.setMyLocationEnabled(true);
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, currentMarker);
        baiduMap.setMyLocationConfiguration(config);
    }

    /**
     * 设置我的位置经纬度和方向
     *
     * @param baiduMap
     * @param lat
     * @param lng
     * @param radius
     * @param direction
     */
    public static void setMyLocation(BaiduMap baiduMap, double lat, double lng, float radius, float direction) {
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(radius)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(direction)
                .latitude(lat).longitude(lng)
                .build();
        // 设置定位数据
        baiduMap.setMyLocationData(locData);
    }

    /**
     * 绘制标记物
     *
     * @param baiduMap
     * @param point
     * @param icon
     * @return
     */
    public static Overlay addMarker(BaiduMap baiduMap, LatLng point, BitmapDescriptor icon) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(point)
                .icon(icon);
        return baiduMap.addOverlay(markerOptions);
    }

    /**
     * 绘制折线
     *
     * @param baiduMap
     * @param points
     * @param colors
     * @param width
     * @return
     */
    public static Overlay addPolyline(BaiduMap baiduMap, List<LatLng> points, List<Integer> colors, int width) {
        PolylineOptions overlayOptions = new PolylineOptions()
                .width(width)
                .points(points);
        if (colors != null) {
            if (colors.size() == points.size() - 1) {
                overlayOptions.colorsValues(colors);
            } else {
                overlayOptions.color(colors.get(0));
            }
        }
        return baiduMap.addOverlay(overlayOptions);
    }

    /**
     * 绘制圆弧
     *
     * @param baiduMap
     * @param p1
     * @param p2
     * @param p3
     * @param color
     * @param width
     * @return
     */
    public static Overlay addArc(BaiduMap baiduMap, LatLng p1, LatLng p2, LatLng p3, int color, int width) {
        ArcOptions arcOptions = new ArcOptions()
                .color(color)
                .width(width)
                .points(p1, p2, p3);
        return baiduMap.addOverlay(arcOptions);
    }

    /**
     * 绘制圆
     *
     * @param baiduMap
     * @param center
     * @param radius
     * @param fillColor
     * @param stroke
     * @return
     */
    public static Overlay addCircle(BaiduMap baiduMap, LatLng center, int radius, int fillColor, Stroke stroke) {
        CircleOptions circleOptions = new CircleOptions().center(center)
                .radius(radius)// 半径
                .fillColor(fillColor) // 填充颜色
                .stroke(stroke); // 边框宽和边框颜色
        return baiduMap.addOverlay(circleOptions);
    }

    /**
     * 绘制多边形
     *
     * @param baiduMap
     * @param points
     * @param fillColor
     * @param stroke
     * @return
     */
    public static Overlay addPolygon(BaiduMap baiduMap, List<LatLng> points, int fillColor, Stroke stroke) {
        PolygonOptions polygonOptions = new PolygonOptions()
                .points(points)
                .fillColor(fillColor) //填充颜色
                .stroke(stroke); //边框宽度和颜色
        return baiduMap.addOverlay(polygonOptions);
    }

    /**
     * 设置路线
     *
     * @param overlay
     * @param routeLine
     * @return
     */
    public static void addRouteLine(OverlayManager overlay, RouteLine routeLine) {
        if (overlay == null || routeLine == null) return;
        if (overlay instanceof BikingRouteOverlay && routeLine instanceof BikingRouteLine) {
            BikingRouteOverlay bikingRouteOverlay = (BikingRouteOverlay) overlay;
            bikingRouteOverlay.setData((BikingRouteLine) routeLine);
            bikingRouteOverlay.addToMap();
        } else if (overlay instanceof DrivingRouteOverlay && routeLine instanceof DrivingRouteLine) {
            DrivingRouteOverlay drivingRouteOverlay = (DrivingRouteOverlay) overlay;
            drivingRouteOverlay.setData((DrivingRouteLine) routeLine);
            drivingRouteOverlay.addToMap();
        } else if (overlay instanceof WalkingRouteOverlay && routeLine instanceof WalkingRouteLine) {
            WalkingRouteOverlay walkingRouteOverlay = (WalkingRouteOverlay) overlay;
            walkingRouteOverlay.setData((WalkingRouteLine) routeLine);
            walkingRouteOverlay.addToMap();
        }
    }

    /**
     * 获取同一经度上距离纬度增量
     *
     * @param d 距离，米
     * @return
     */
    public static double distanceLatExtra(int d) {
        return d * 180 / (6378137 * Math.PI);
    }

    /**
     * 获取同一纬度距离经度增量
     *
     * @param lat
     * @param d
     * @return
     */
    public static double distanceLngExtra(double lat, int d) {
        return d * 180 / (6378137 * Math.PI) / Math.cos(lat * Math.PI / 180);
    }

    static Application getApp() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }
            return (Application) app;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("u should init first");
    }
}
