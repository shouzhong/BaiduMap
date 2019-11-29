# BaiduMap
## 说明
百度地图资源包
## 使用
### 依赖
```
implementation 'com.shouzhong:BaiduMap:1.0.0'
```
如果使用到定位功能，请添加rxjava，rxjava2就行
```
implementation 'io.reactivex.rxjava2:rxandroid:2.0.2'
implementation 'io.reactivex.rxjava2:rxjava:2.1.10'
```
### 配置
在AndroidManifest的application标签下
```
<meta-data
    android:name="com.baidu.lbsapi.API_KEY"
    android:value="你申请的" />
```
## 混淆
```
-dontwarn com.baidu.**
-keep class com.baidu.**{*;}
-dontwarn mapsdkvi.com.**
-keep class mapsdkvi.com.** {*;}
```