package com.myhome.wh.mybdmap;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.myhome.wh.mybdmap.Overlay.PoiOverlay;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    MapView mMapView = null;
    //三种地图状态
    private Button normal;
    private Button satellite;
    private Button none;

    private BaiduMap mBaiduMap;
    // 声明一个Handler对象
    private static Handler handler = new Handler();
    private CheckBox traffic;
    private CheckBox baiduHeatMap;
    private Marker marker;
    private PoiSearch mPoiSearch;
    private Button check;
    private EditText etCity;
    private EditText etSearchkey;

    /**
     * 定位SDK的核心类
     */
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private Button btlocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        //获取三种地图类型引用
        normal = (Button) findViewById(R.id.butNORMAL);
        satellite = (Button) findViewById(R.id.butSATELLITE);
        none = (Button) findViewById(R.id.butNONE);
        //获取两种类型图
        traffic = (CheckBox) findViewById(R.id.cbTraffic);
        baiduHeatMap = (CheckBox) findViewById(R.id.cbBaiduHeatMap);
        //but搜索引用
        check = (Button) findViewById(R.id.check);
        //定位按钮引用
        btlocation = (Button) findViewById(R.id.location);

        mBaiduMap = mMapView.getMap();
        //点击事件
        normal.setOnClickListener(this);
        satellite.setOnClickListener(this);
        none.setOnClickListener(this);
        //CheckBox 事件
        traffic.setOnCheckedChangeListener(this);
        baiduHeatMap.setOnCheckedChangeListener(this);
        //but搜索点击事件
        check.setOnClickListener(this);
        //定位的点击事件
        btlocation.setOnClickListener(this);

        //控制地图logo的位置 默认左下角
        mMapView.setLogoPosition(LogoPosition.logoPostionCenterTop);

        //检索方法
        Poi();
        //  定位
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();







    }
    //配置定位SDK参数
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        // 定位SDK可以返回bd09、bd09ll、gcj02三种类型坐标，若需要将定位点的位置通过百度Android地图 SDK进行地图展示，请返回bd09ll，将无偏差的叠加在百度地图上。
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    //检索方法
    public void Poi() {
        mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
            public void onGetPoiResult(PoiResult result) {
                //获取poi检索结果
                if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
                    Toast.makeText(MainActivity.this, "检索失败,未找到结果", Toast.LENGTH_LONG).show();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "检索成功，总共查到" + result.getTotalPoiNum() + "个兴趣点", Toast.LENGTH_SHORT).show();
                    mBaiduMap.clear();
                    PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);//点击的标记图标 传到MyPoiOverlay
                    overlay.setData(result);
                    overlay.addToMap();
                    overlay.zoomToSpan();//缩放地图至合适视野
                }
            }
            public void onGetPoiDetailResult(PoiDetailResult result) {
                //获取Place详情页检索结果
                if (result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "检索失败,未找到结果", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, result.getName() + ":" + result.getAddress(), Toast.LENGTH_LONG).show();
                }
            }
            class MyPoiOverlay extends PoiOverlay {
                /**
                 * 构造函数
                 *
                 * @param baiduMap 该 PoiOverlay 引用的 BaiduMap 对象
                 */
                public MyPoiOverlay(BaiduMap baiduMap) {
                    super(baiduMap);

                }
                @Override
                public boolean onPoiClick(int i) {
                    PoiInfo poiInfo = getPoiResult().getAllPoi().get(i);
                    //uid是POI检索中获取的POI ID信息
                    mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poiInfo.uid));//详细查询 -detailReault
                    return true;
                }
            }
        };
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        //mPoiSearch.destroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //更改地图模式的点击事件
            case R.id.butNORMAL:
                Toast.makeText(getApplicationContext(), "普通地图", Toast.LENGTH_SHORT).show();
                //普通地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.butSATELLITE:
                Toast.makeText(getApplicationContext(), "卫星地图", Toast.LENGTH_SHORT).show();
                //卫星地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.butNONE:
                Toast.makeText(getApplicationContext(), "空白地图", Toast.LENGTH_SHORT).show();
                //空白地图, 基础地图瓦片将不会被渲染。在地图类型中设置为NONE，将不会使用流量下载基础地图瓦片图层。使用场景：与瓦片图层一起使用，节省流量，提升自定义瓦片图下载速度。
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
                break;
            case R.id.check:
                //执行搜索 获取两个输入框值
                etCity = (EditText) findViewById(R.id.etCity);
                etSearchkey = (EditText) findViewById(R.id.etSearchkey);
                Toast.makeText(getApplicationContext(),"搜索"+etCity.getText()+":"+etSearchkey.getText(),Toast.LENGTH_SHORT).show();
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city(etCity.getText().toString())
                        .keyword(etSearchkey.getText().toString())
                        .pageNum(10));
                break;
            case  R.id.location:
                Toast.makeText(getApplicationContext(),"我的位置",Toast.LENGTH_SHORT).show();
                mLocationClient.start();
                break;
        }
    }

    //两种类型图
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cbTraffic:
                if (isChecked == true) {
                    Toast.makeText(getApplicationContext(), "Traffic选中", Toast.LENGTH_SHORT).show();
                    //开启交通图
                    mBaiduMap.setTrafficEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Traffic取消选中", Toast.LENGTH_SHORT).show();
                    //关闭交通图
                    mBaiduMap.setTrafficEnabled(false);
                }
                break;
            case R.id.cbBaiduHeatMap:
                if (isChecked == true) {
                    Toast.makeText(getApplicationContext(), "BaiduHeatMap选中", Toast.LENGTH_SHORT).show();
                    //开启热力图
                    mBaiduMap.setBaiduHeatMapEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "BaiduHeatMap取消选中", Toast.LENGTH_SHORT).show();
                    //关闭热力图
                    mBaiduMap.setBaiduHeatMapEnabled(false);
                }
                break;
        }
    }
}
