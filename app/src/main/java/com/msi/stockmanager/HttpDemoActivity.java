package com.msi.stockmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockApi;
import com.msi.stockmanager.data.stock.StockHistory;
import com.msi.stockmanager.data.stock.StockInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class HttpDemoActivity extends AppCompatActivity {

    public static final int MESSAGE_SHOW_RESULT   = 0;
    public static final int MESSAGE_SHOW_PRICE    = 1;
    public static final int MESSAGE_APPEND_RESULT = 2;

    private String TAG = "HttpDemoActivity";
    private EditText stockCodeInput = null;
    private Button btnTest = null;
    private TextView showPrice = null;
    private Spinner spinnerInterval = null;
    private Spinner spinnerRanges = null;
    private Button btnHistory = null;
    private TextView showResult = null;
    private Button Klinebtn = null;
    private  ArrayList<String> stockdataArray = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_demo);

        stockCodeInput = (EditText) findViewById(R.id.input_stock_code);
        stockCodeInput.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        btnTest = (Button) findViewById(R.id.btn_http_test);
        showPrice = (TextView) findViewById(R.id.show_price);
        showPrice.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        /*---------K bar show------------*/
        Klinebtn = (Button) findViewById(R.id.btn_Kline);
        Klinebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(HttpDemoActivity.this, KlineViewActivity.class);
                Intent intent = new Intent(HttpDemoActivity.this, KlineViewActivity.class);
//                startActivity(intent);
                intent.putStringArrayListExtra("stock_history_price", stockdataArray);
                startActivity(intent);

            }
        });
        /*-------------------------------*/


        spinnerInterval = (Spinner) findViewById(R.id.spinner_interval);
        ArrayAdapter adapterInterval = ArrayAdapter.createFromResource(this
                ,R.array.history_interval_string, android.R.layout.simple_dropdown_item_1line);
        spinnerInterval.setAdapter(adapterInterval);
        spinnerInterval.setSelection(8);
        spinnerRanges = (Spinner) findViewById(R.id.spinner_ranges);
        ArrayAdapter adapterRanges = ArrayAdapter.createFromResource(this
                ,R.array.history_valid_ranges_string, android.R.layout.simple_dropdown_item_1line);
        spinnerRanges.setAdapter(adapterRanges);
        spinnerRanges.setSelection(2);
        btnHistory = (Button) findViewById(R.id.btn_http_history);

        showResult = (TextView) findViewById(R.id.show_result);
        showResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        setListener();
        setCloseKeyboardListener();
    }

    private String getInterval() {
        String[] array = getResources().getStringArray(R.array.history_interval);
        int pos = spinnerInterval.getSelectedItemPosition();
        return array[pos];
    };

    private String getRanges() {
        String[] array = getResources().getStringArray(R.array.history_valid_ranges);
        int pos = spinnerRanges.getSelectedItemPosition();
        return array[pos];
    };

    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch(message.what) {
                case MESSAGE_SHOW_RESULT:
                    showResult.setText((String) message.obj);
                    break;
                case MESSAGE_APPEND_RESULT:
                    showResult.append((String) message.obj);
                    break;
                case MESSAGE_SHOW_PRICE:
                    showPrice.setText((String) message.obj);
                    break;
            }
            return false;
        }
    });

    void setListener() {
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = stockCodeInput.getText().toString();
                showResult.setText(code);
                showResult.scrollTo(0 ,0);
                StockApi stock = new StockApi(getApplicationContext());
                stock.getRegularStockPrice(code, new IStockApi.ResultCallback() {
                    @Override
                    public void onResult(StockInfo info) {
                        if(info == null){
                            handler.sendMessage(handler.obtainMessage(MESSAGE_SHOW_RESULT, "ERROR"));
                            handler.sendMessage(handler.obtainMessage(MESSAGE_SHOW_PRICE, String.format("ERROR")));
                        } else {
                            handler.sendMessage(handler.obtainMessage(MESSAGE_SHOW_RESULT, info.toString()));
                            handler.sendMessage(handler.obtainMessage(MESSAGE_SHOW_PRICE, String.valueOf(info.getLastPrice())));
                        }
                    }
                });
            }
        });
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stockdataArray.clear();
                String code = stockCodeInput.getText().toString();
                String interval = getInterval();
                String ranges = getRanges();
                showResult.setText("查詢 " + code + " (interval=" + interval + ", ranges=" + ranges + ")\n");
                showResult.scrollTo(0 ,0);
                StockApi stock = new StockApi(getApplicationContext());
                stock.getHistoryStockData(code, interval, ranges, new IStockApi.HistoryCallback() {
                    @Override
                    public void onResult(List<StockHistory> data) {
                        if (data.isEmpty()) {
                            handler.sendMessage(handler.obtainMessage(MESSAGE_APPEND_RESULT, "EMPTY\n"));
                            return;
                        }
                        handler.sendMessage(handler.obtainMessage(MESSAGE_APPEND_RESULT, "DATE                    OPEN  CLOSE   HIGH   LOW        VOLUME\n"));
                        handler.sendMessage(handler.obtainMessage(MESSAGE_APPEND_RESULT, "===================== ====== ====== ====== ====== ============\n"));
                        for (StockHistory a : data) {
//                            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(a.date_timestamp * 1000));
                            String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date(a.date_timestamp * 1000));
                            handler.sendMessage(handler.obtainMessage(MESSAGE_APPEND_RESULT, "[" + timestamp + "] " +
                                    String.format("%6.1f", a.price_open) + " " + String.format("%6.1f", a.price_close) + " " +
                                    String.format("%6.1f", a.price_high) + " " + String.format("%6.1f", a.price_low) + " " +
                                    String.format("%f", a.price_volume) + "\n"));


                            stockdataArray.add(timestamp+"\t"+a.price_open+"\t"+a.price_close+"\t"+a.price_high+"\t"+a.price_low+"\t"+String.format("%12.3f",a.price_volume/10000));


                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        //Log.e(TAG, "getHistoryStockData err: " + e.getMessage());
                        handler.sendMessage(handler.obtainMessage(MESSAGE_APPEND_RESULT, "ERROR: " + e.getMessage() + "\n"));
                    }
                });
            }
        });
    }

    void setCloseKeyboardListener() {
        stockCodeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(stockCodeInput.getWindowToken(), 0);
                return false;
            }
        });
    }


}