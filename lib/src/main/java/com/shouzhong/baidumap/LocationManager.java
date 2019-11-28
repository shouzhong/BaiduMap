package com.shouzhong.baidumap;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2019/01/08.
 */

public class LocationManager {

    public static final int ROUTE_BIKING = 0;
    public static final int ROUTE_DRIVING = 1;
    public static final int ROUTE_WALKING = 2;
    public static final int ROUTE_LINE = 3;

    private int scanSpan;
    private LocationClient locationClient;
    private ObservableEmitter<LocationBean> emitter;
    private BDAbstractLocationListener listener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (emitter == null || emitter.isDisposed()) {
                stop();
                emitter = null;
                return;
            }
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                // 定位成功
                emitter.onNext(new LocationBean(true, bdLocation));
            } else {
                // 定位失败
                emitter.onNext(new LocationBean(false, "定位失败"));
            }
            if (scanSpan < 1000) {
                stop();
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onComplete();
                }
                emitter = null;
            }
        }
    };

    public static LocationManager newInstance() {
        return new LocationManager();
    }

    private LocationManager() {
        locationClient = new LocationClient(MapUtils.getApp());
        LocationClientOption option = locationClient.getLocOption();
        if (option == null) option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIgnoreKillProcess(false);
        option.setScanSpan(0);
        locationClient.setLocOption(option);
    }

    public LocationManager setScanSpan(int i) {
        scanSpan = i < 1000 ? 0 : i;
        LocationClientOption option = locationClient.getLocOption();
        option.setScanSpan(scanSpan);
        locationClient.setLocOption(option);
        return this;
    }

    public LocationManager setIsNeedAddress(boolean b) {
        LocationClientOption option = locationClient.getLocOption();
        option.setIsNeedAddress(b);
        locationClient.setLocOption(option);
        return this;
    }

    public Observable<LocationBean> start() {
        return Observable
                .create(new ObservableOnSubscribe<LocationBean>() {
                    @Override
                    public void subscribe(ObservableEmitter<LocationBean> e) throws Exception {
                        locationClient.registerLocationListener(listener);
                        locationClient.start();
                        emitter = e;
                    }
                });
    }

    /**
     * 获取路线距离
     *
     * @param startPoint
     * @param endPoint
     * @param routeType  0表示骑行路线距离，1表示驾车路线距离，2表示步行路线距离,3代表直线距离
     * @return
     */
    public Observable<Integer> distance(final LatLng startPoint, final LatLng endPoint, final int routeType) {
        if (routeType == ROUTE_LINE) {
            return Observable
                    .just(new LatLng[]{startPoint, endPoint})
                    .flatMap(new Function<LatLng[], ObservableSource<LatLng[]>>() {
                        @Override
                        public ObservableSource<LatLng[]> apply(LatLng[] latLngs) throws Exception {
                            if (latLngs[0] != null && latLngs[1] != null)
                                return Observable.just(latLngs);
                            return start()
                                    .observeOn(Schedulers.io())
                                    .map(new Function<LocationBean, LatLng[]>() {
                                        @Override
                                        public LatLng[] apply(LocationBean locationBean) throws Exception {
                                            if (!locationBean.isSuccess)
                                                throw new Exception(locationBean.msg);
                                            LatLng latLng = new LatLng(locationBean.location.getLatitude(), locationBean.location.getLongitude());
                                            LatLng start = startPoint == null ? latLng : startPoint;
                                            LatLng end = endPoint == null ? latLng : endPoint;
                                            return new LatLng[]{start, end};
                                        }
                                    });
                        }
                    })
                    .map(new Function<LatLng[], Integer>() {
                        @Override
                        public Integer apply(LatLng[] latLngs) throws Exception {
                            return (int) DistanceUtil.getDistance(latLngs[0], latLngs[1]);
                        }
                    })
                    .subscribeOn(Schedulers.io());
        }
        return route(startPoint, endPoint, routeType)
                .map(new Function<Object, Integer>() {
                    @Override
                    public Integer apply(Object o) throws Exception {
                        RouteLine line = (RouteLine) o;
                        return line.getDistance();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * 获取路线
     *
     * @param startPoint 开始位置，空表示当前位置
     * @param endPoint   结束位置，空表示当前位置
     * @param routeType  0表示骑行路线距离，1表示驾车路线距离，2表示步行路线距离
     * @return
     */
    public Observable<RouteLine> route(final LatLng startPoint, final LatLng endPoint, final int routeType) {
        if (routeType < ROUTE_BIKING || routeType > ROUTE_WALKING || startPoint == null && endPoint == null) {
            return Observable
                    .create(new ObservableOnSubscribe<RouteLine>() {
                        @Override
                        public void subscribe(ObservableEmitter<RouteLine> emitter) throws Exception {
                            emitter.onError(new Exception("参数错误"));
                        }
                    });
        }
        Observable<LatLng[]> o;
        if (startPoint == null || endPoint == null) {
            setScanSpan(0);
            o = start()
                    .observeOn(Schedulers.io())
                    .map(new Function<LocationBean, LatLng[]>() {
                        @Override
                        public LatLng[] apply(LocationBean locationBean) throws Exception {
                            if (!locationBean.isSuccess) throw new Exception(locationBean.msg);
                            LatLng latLng = new LatLng(locationBean.location.getLatitude(), locationBean.location.getLongitude());
                            LatLng start = startPoint == null ? latLng : startPoint;
                            LatLng end = endPoint == null ? latLng : endPoint;
                            return new LatLng[]{start, end};
                        }
                    });
        } else {
            o = Observable.just(new LatLng[]{startPoint, endPoint});
        }
        return o.flatMap(new Function<LatLng[], ObservableSource<RouteLine>>() {
            @Override
            public ObservableSource<RouteLine> apply(final LatLng[] latLngs) throws Exception {
                return Observable.create(new ObservableOnSubscribe<RouteLine>() {
                    @Override
                    public void subscribe(ObservableEmitter<RouteLine> emitter) throws Exception {
                        int route = routeType;
                        PlanNode start = PlanNode.withLocation(latLngs[0]);
                        PlanNode end = PlanNode.withLocation(latLngs[1]);
                        final RoutePlanSearch search = RoutePlanSearch.newInstance();
                        search.setOnGetRoutePlanResultListener(new RouteDistanceListener(emitter, search));
                        if (route == ROUTE_BIKING) {
                            BikingRoutePlanOption option = new BikingRoutePlanOption();
                            option.from(start).to(end);
                            if (!search.bikingSearch(option)) throw new Exception("搜索错误");
                            return;
                        }
                        if (route == ROUTE_DRIVING) {
                            DrivingRoutePlanOption option = new DrivingRoutePlanOption();
                            option.from(start).to(end);
                            if (!search.drivingSearch(option)) throw new Exception("搜索错误");
                            return;
                        }
                        if (route == ROUTE_WALKING) {
                            WalkingRoutePlanOption option = new WalkingRoutePlanOption();
                            option.from(start).to(end);
                            if (!search.walkingSearch(option)) throw new Exception("搜索错误");
                            return;
                        }
                    }
                });
            }
        });
    }

    public void stop() {
        locationClient.unRegisterLocationListener(listener);
        locationClient.stop();
    }

    private class RouteDistanceListener implements OnGetRoutePlanResultListener {
        private ObservableEmitter<RouteLine> emitter;
        private RoutePlanSearch search;

        public RouteDistanceListener(ObservableEmitter<RouteLine> emitter, RoutePlanSearch search) {
            this.emitter = emitter;
            this.search = search;
        }

        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            try {
                search.destroy();
                search = null;
            } catch (Exception e) {
            }
            try {
                if (emitter == null || emitter.isDisposed()) {
                    emitter = null;
                    return;
                }
                List<WalkingRouteLine> lines = walkingRouteResult.getRouteLines();
                if (lines == null || lines.size() == 0) {
                    emitter.onError(new Exception("无路线"));
                    emitter = null;
                    return;
                }
                emitter.onNext(lines.get(0));
                emitter.onComplete();
                emitter = null;
            } catch (Exception e) {
            }
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            try {
                search.destroy();
                search = null;
            } catch (Exception e) {
            }
            try {
                if (emitter == null || emitter.isDisposed()) {
                    emitter = null;
                    return;
                }
                List<DrivingRouteLine> lines = drivingRouteResult.getRouteLines();
                if (lines == null || lines.size() == 0) {
                    emitter.onError(new Exception("无路线"));
                    emitter = null;
                    return;
                }
                emitter.onNext(lines.get(0));
                emitter.onComplete();
                emitter = null;
            } catch (Exception e) {
            }
        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
            try {
                search.destroy();
            } catch (Exception e) {
            }
            try {
                if (emitter == null || emitter.isDisposed()) {
                    emitter = null;
                    return;
                }
                List<BikingRouteLine> lines = bikingRouteResult.getRouteLines();
                if (lines == null || lines.size() == 0) {
                    emitter.onError(new Exception("无路线"));
                    emitter = null;
                    return;
                }
                emitter.onNext(lines.get(0));
                emitter.onComplete();
                emitter = null;
            } catch (Exception e) {
            }
        }
    }
}
