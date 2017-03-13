package com.qqdemo.administrator.ad0313;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.List;

public class MainActivity extends AppCompatActivity{
    public static final String TAG = "MainActivityAAAAAAAAAA";

    private MapView mMapView;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private Button mbtn,mbtn2;
    private BusLineSearch mBusLineSearch;
    private PoiSearch mPoiSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);


        mbtn= (Button) findViewById(R.id.btn);
        mbtn2= (Button) findViewById(R.id.btn2);
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        mbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLocation();
                mLocationClient.start();
            }
        });

        mbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomizeDialog();



            }
        });


    }
    private void showCustomizeDialog() {
    /* @setView 装入自定义View ==> R.layout.dialog_customize
     * 由于dialog_customize.xml只放置了一个EditView，因此和图8一样
     * dialog_customize.xml可自定义更复杂的View
     */
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_customize,null);
        customizeDialog.setTitle("附近搜索");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容
                        EditText add =  (EditText) dialogView.findViewById(R.id.address);
                        EditText something =  (EditText) dialogView.findViewById(R.id.something);
                        mPoiSearch = PoiSearch.newInstance();

                        // 第四步，发起检索请求；
                        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
                        mPoiSearch.searchInCity((new PoiCitySearchOption())
                                .city(add.getText().toString().trim())
                                .keyword(something.getText().toString().trim())
                                .pageNum(10));
                    }
                });
        customizeDialog.show();
    }
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){
        public void onGetPoiResult(PoiResult result){


            if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(MainActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                List<PoiInfo> allAddr = result.getAllPoi();
                String []ss=new String[10];
                for(int i=0;i<10;i++){
                    PoiInfo p= allAddr.get(i);
                    ss[i]=p.name+" "+p.address;
                }

                AlertDialog.Builder listDialog =
                        new AlertDialog.Builder(MainActivity.this);
                listDialog.setTitle("搜索结果");
                listDialog.setItems(ss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                listDialog.show();
            }
        }
        public void onGetPoiDetailResult(PoiDetailResult result){
            //获取Place详情页检索结果
            Log.i(TAG, "onGetPoiDetailResult: "+result);
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            Log.i(TAG, "onGetPoiIndoorResult: "+poiIndoorResult);
        }
    };




    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mPoiSearch.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }




    private class MyLocationListener implements BDLocationListener {
        private BitmapDescriptor mCurrentMarker;

        @Override
        public void onReceiveLocation(BDLocation location) {


            // 开启定位图层
            mMapView.getMap().setMyLocationEnabled(true);
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mMapView.getMap().setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(ll);
            mMapView.getMap().setMapStatus(status);//直接到中间

            mLocationClient.stop();
        }


        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

//        int span = 1000;
        option.setScanSpan(0);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }
}