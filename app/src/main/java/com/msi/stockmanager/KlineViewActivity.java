package com.msi.stockmanager;

import android.content.res.Configuration;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import androidx.appcompat.app.AppCompatActivity;

import com.msi.stockmanager.kline.KData;
import com.msi.stockmanager.kline.KLineView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KlineViewActivity extends AppCompatActivity implements View.OnClickListener {

    private Handler mHandler;
    private KLineView kLineView;
    private Button deputyBtn, maBtn,  macdBtn, kdjBtn, rsiBtn;
    private CheckBox ChtoEnBox,RedtoGreenBox;
    private Runnable dataListAddRunnable, singleDataAddRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kline_view);

        initView();
        initData();
        setListener();

        //切换横屏适配测试
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dp2px(280));
            kLineView.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dp2px(630));
            kLineView.setLayoutParams(params);
        }
    }

    private void initView() {
        kLineView = findViewById(R.id.klv_main);
        deputyBtn = findViewById(R.id.btn_deputy);
        maBtn = findViewById(R.id.btn_ma);
        macdBtn = findViewById(R.id.btn_macd);
        kdjBtn = findViewById(R.id.btn_kdj);
        rsiBtn = findViewById(R.id.btn_rsi);
        ChtoEnBox = findViewById(R.id.checkBox);
        RedtoGreenBox = findViewById(R.id.checkBox2);
    }

    private void initData() {
        //初始化控件加载数据，仅限于首次初始化赋值，不可用于更新数据
        kLineView.initKDataList(getKDataList(10));
        //设置十字线移动模式，默认为0：固定指向收盘价
        kLineView.setCrossHairMoveMode(KLineView.CROSS_HAIR_MOVE_OPEN);

        mHandler = new Handler();
        dataListAddRunnable = new Runnable() {
            @Override
            public void run() {
                //分页加载时添加多条数据
                kLineView.addPreDataList(getKDataList(10), true);
//                kLineView.addPreDataList(null, true);
            }
        };

        singleDataAddRunnable = new Runnable() {
            @Override
            public void run() {
                //实时刷新时添加单条数据
                /*KData kData = kLineView.getTotalDataList().get(kLineView.getTotalDataList().size() - 1);
                KData kData1 = new KData(kData.getTime(), kData.getOpenPrice(), kData.getOpenPrice(),
                        kData.getMaxPrice(), kData.getMinPrice(), kData.getVolume());
                kLineView.addSingleData(kData1);*/
                kLineView.addSingleData(getKDataList(0.1).get(0));
//                mHandler.postDelayed(this, 1000);
            }
        };
//        mHandler.postDelayed(singleDataAddRunnable, 2000);

    }

    private void setListener() {
        deputyBtn.setOnClickListener(this);
        maBtn.setOnClickListener(this);
        macdBtn.setOnClickListener(this);
        kdjBtn.setOnClickListener(this);
        rsiBtn.setOnClickListener(this);

        ChtoEnBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(ChtoEnBox.isChecked()==true){
                    kLineView.setLanguageChange(true);
                }else{
                    kLineView.setLanguageChange(false);
                }
            }
        });

        RedtoGreenBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(RedtoGreenBox.isChecked()==true){
                    kLineView.setColorChange(true);
                }else{
                    kLineView.setColorChange(false);
                }
            }

        });

        //当控件显示数据属于总数据量的前三分之一时，会自动调用该接口，用于预加载数据，保证控件操作过程中的流畅性，
        //虽然做了预加载，当总数据量较小时，也会出现用户滑到左边界了，但数据还未获取到，依然会有停顿。
        //所以数据量越大，越不会出现停顿，也就越流畅
        kLineView.setOnRequestDataListListener(new KLineView.OnRequestDataListListener() {
            @Override
            public void requestData() {
                mHandler.postDelayed(dataListAddRunnable, 3000);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_deputy:
                //是否显示副图
                kLineView.setDeputyPicShow(!kLineView.getVicePicShow());
                break;

            case R.id.btn_ma:
                //主图展示MA
                kLineView.setMainImgType(KLineView.MAIN_IMG_MA);
                break;


            case R.id.btn_macd:
                //副图展示MACD
                kLineView.setDeputyImgType(KLineView.DEPUTY_IMG_MACD);
                break;

            case R.id.btn_kdj:
                //副图展示KDJ
                kLineView.setDeputyImgType(KLineView.DEPUTY_IMG_KDJ);
                break;

            case R.id.btn_rsi:
                //副图展示RSI
                kLineView.setDeputyImgType(KLineView.DEPUTY_IMG_RSI);
                break;
            default:
                break;
        }
    }

    //模拟K线数据
    private List<KData> getKDataList(double num) {
        long start = 0;
//        String date = "";
//        Random random = new Random();
        List<KData> dataList = new ArrayList<>();
        double openPrice = 0;
        double closePrice = 0;
        double maxPrice = 0;
        double minPrice = 0;
        double volume = 0;



        ArrayList<String> stringList = getIntent().getStringArrayListExtra("stock_history_price");

        for (int i = 0; i < stringList.size(); i++) {
            String[] stock_data_sp = stringList.get(i).split("\t");
            try {
                start = new SimpleDateFormat("yyyy-MM-dd").parse(stock_data_sp[0]).getTime();
                dataList.add(new KData(start, Double.parseDouble(stock_data_sp[1]), Double.parseDouble(stock_data_sp[2]), Double.parseDouble(stock_data_sp[3]),
                        Double.parseDouble(stock_data_sp[4]), Double.parseDouble(stock_data_sp[5])));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        return dataList;
    }

    private double getAddRandomDouble() {
        Random random = new Random();
        return random.nextInt(5) * 5 + random.nextDouble();
    }

    private double getSubRandomDouble() {
        Random random = new Random();
        return random.nextInt(5) * 5 - random.nextDouble();
    }

    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出页面时停止子线程并置空，便于回收，避免内存泄露
        kLineView.cancelQuotaThread();
    }

}
