# BaiduMap
## 说明
百度地图资源包
## 依赖
```
implementation 'com.shouzhong:BaiduMap:1.0.0'
```
## 混淆
```
-dontwarn com.baidu.**
-keep class com.baidu.**{*;}
-dontwarn mapsdkvi.com.**
-keep class mapsdkvi.com.** {*;}
-dontwarn com.google.protobuf.micro.**
-keep class com.google.protobuf.micro.** {*;}
```