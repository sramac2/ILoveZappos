package com.example.android.ilovezappos;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerViewadapter;
    private RecyclerView.LayoutManager recyclerViewManager;
    private ArrayList<OrderBookValue> orderBookValues;

    public static String value;
    private Retrofit retrofit;
    private FloatingActionButton floatingActionButton;
    private TextView textView;
    private BitstampApi bitstampApi;
    private Disposable disposable;

    private LineChart lineChart;
    private SeekBar seekBarX, seekBarY;
    private TextView tvX, tvY;
    private ArrayList<Entry> entries;
    XAxis xAxis;
    YAxis yAxis;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Just a moment");
        progressDialog.show();


        //setting up recycler view
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerViewManager = new LinearLayoutManager(MainActivity.this);

        OrderBookValue orderBookValue = new OrderBookValue("Amount","Bids","Asks","Amount");
        ArrayList<OrderBookValue> list = new ArrayList<>();
        list.add(orderBookValue);
        recyclerViewadapter = new OrderBookAdapter(list);
        recyclerViewadapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(recyclerViewManager);
        recyclerView.setAdapter(recyclerViewadapter);


        lineChart = findViewById(R.id.chart1);
        lineChart.setTouchEnabled(true);
        lineChart.getDescription().setEnabled(false);
        lineChart.setOnChartValueSelectedListener(this);
        lineChart.setDrawGridBackground(false);

        tvX = findViewById(R.id.tvXMax);
        tvY = findViewById(R.id.tvYMax);

        seekBarX = findViewById(R.id.seekBar1);
        seekBarX.setOnSeekBarChangeListener(this);

        seekBarY = findViewById(R.id.seekBar2);
        seekBarY.setMax(10000);
        seekBarY.setOnSeekBarChangeListener(this);

        BitcoinMarkerView bitcoinMarkerView = new BitcoinMarkerView(MainActivity.this, R.layout.custom_marker_view);
        bitcoinMarkerView.setChartView(lineChart);
        lineChart.setMarker(bitcoinMarkerView);


        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        lineChart.setPinchZoom(true);


        xAxis = lineChart.getXAxis();
        yAxis = lineChart.getAxisLeft();

        lineChart.getAxisRight().setEnabled(false);


        yAxis.enableGridDashedLine(10f, 10f, 0f);

        yAxis.setAxisMaximum(8580f);
        yAxis.setAxisMinimum(8530f);

        LimitLine llXAxis = new LimitLine(9f, "Index 10");
        llXAxis.setLineWidth(10f);
        //llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);


        LimitLine ll1 = new LimitLine(150f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);


        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);


        yAxis.setDrawLimitLinesBehindData(true);
        xAxis.setDrawLimitLinesBehindData(true);

        yAxis.addLimitLine(ll1);
        yAxis.addLimitLine(ll2);
        getBitcoinValueData();



        //Adding notification using floating action button
        floatingActionButton=findViewById(R.id.floatingActionButton);

        //An alertdialog to prompt the user for value
        AlertDialog.Builder builder = new AlertDialog
                .Builder(MainActivity.this)
                .setMessage("Enables notification when price goes below: ")
                .setTitle("Notify Price Change");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!TextUtils.isEmpty(value = input.getText().toString())){
                    //Toast.makeText(MainActivity.this,value,Toast.LENGTH_SHORT).show();
                    scheduleJob(MainActivity.this, value);
                }
                else{
                    Toast.makeText(MainActivity.this,"Please enter a valid value",Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        //listener for floating action Button
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.show();

            }
        });
        seekBarX.setProgress(45);


        getOrderBookData();
        progressDialog.cancel();
    }

    private void getBitcoinValueData(){

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        //initializaing Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("https://www.bitstamp.net/api/v2/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        bitstampApi = retrofit.create(BitstampApi.class);
        disposable = Observable.interval(1000, 5000,
                TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::callBitcoinValuesEndpoint, this::onError);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (disposable.isDisposed()) {
            disposable = Observable.interval(1000, 30000,
                    TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::callBitcoinValuesEndpoint, this::onError);
        }
    }



    private void callBitcoinValuesEndpoint(Long aLong) {

        Observable<List<BitcoinValue> > observable = bitstampApi.listBitcoinValues("transactions/btcusd/");
        observable.subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread())
                .map(result -> result)
                .subscribe(this::handleBitcoinValueResults, this::handleError);
    }

    private void onError(Throwable throwable) {
        Toast.makeText(this, "OnError in Observable Timer",
                Toast.LENGTH_LONG).show();
    }


    //Returned result handled here appropriately
    private void handleBitcoinValueResults(List<BitcoinValue> bitcoinValues) {
        entries = new ArrayList<>();
        String[] labels = new String[bitcoinValues.size()];
        int value=0;
        float max=0;
        float min=1000000;
        int day = 0;
        if (bitcoinValues!=null) {
            for(int i=0;i<bitcoinValues.size();i++){
                value=Integer.parseInt(bitcoinValues.get(bitcoinValues.size()-1-i).getDate());
                Date date = new Date(value*1000L);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                if (day!=0&&day<date.getDay()){
                    labels[i] = date.getDay()+"";
                    day =date.getDay();
                }

                else {
                    if(cal.get(Calendar.AM_PM)==Calendar.PM){
                        labels[i]= date.getHours() +":"+ date.getMinutes()+"pm";
                    }
                    else {
                        labels[i]= date.getHours() +":"+ date.getMinutes()+"am";
                    }
                }
                float price = Float.parseFloat(bitcoinValues.get(bitcoinValues.size()-1-i).getPrice());
                entries.add(new Entry(i+1,price));
                if(max<price){
                    max= price;
                }
                if(min>price){
                    min=price;
                }
            }
            //Setting the maximum value of Y axis
            yAxis.setAxisMaximum(max+5);
            //Setting the minimum value of Y axis
            yAxis.setAxisMinimum(min-5);
            seekBarY.setProgress((int)min-100);

            //intializing line data set with the data
            LineDataSet set1;
            if (lineChart.getData() != null &&
                    lineChart.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
                set1.setValues(entries);
                set1.notifyDataSetChanged();
                lineChart.getData().notifyDataChanged();
                lineChart.notifyDataSetChanged();
            }
            else {

                set1 = new LineDataSet(entries, "BitCoin Graph");

                set1.setDrawIcons(false);

                set1.enableDashedLine(10f, 5f, 0f);

                set1.setColor(Color.BLACK);

                set1.setLineWidth(1f);

                set1.setDrawCircles(false);

                set1.setFormLineWidth(1f);
                set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
                set1.setFormSize(15.f);
                set1.setValueTextSize(9f);
                set1.enableDashedHighlightLine(10f, 5f, 0f);

                set1.setDrawFilled(true);
                set1.setFillFormatter(new IFillFormatter() {
                    @Override
                    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                        return lineChart.getAxisLeft().getAxisMinimum();
                    }
                });
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1); // adding the data to the data sets

                LineData data = new LineData(dataSets);

                // seting the data to the linechart
                lineChart.setData(data);
                lineChart.invalidate();

                //Animating both X and Y axis
                lineChart.animateXY(2000, 2000);
            }
            lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        } else {
            Toast.makeText(this, "NO RESULTS FOUND",
                    Toast.LENGTH_LONG).show();
        }


    }

    private void handleError(Throwable t) {

    }

    @Override
    protected void onPause() {
        super.onPause();

        disposable.dispose();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        tvX.setText(String.valueOf(seekBarX.getProgress()));
        tvY.setText(String.valueOf(seekBarY.getProgress()));

        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(seekBarX.getProgress());



        yAxis.setAxisMaximum(seekBarY.getProgress());
        //xAxis.setAxisMinimum(seekBarX);
        //setData(seekBarX.getProgress(), seekBarY.getProgress());
        LineDataSet set1;

        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set1.setValues(entries);
            set1.notifyDataSetChanged();
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
            // redraw
        lineChart.invalidate();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    //Used to schedule job for sending notifications through Firebase Job Dispatcher
    public static void scheduleJob(Context context,String value) {

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job job = createJob(dispatcher);
        dispatcher.mustSchedule(job);
    }
    public static Job createJob(FirebaseJobDispatcher dispatcher){

        Bundle bundle = new Bundle();
        bundle.putString("value",value);
        Job job = dispatcher.newJobBuilder()
                .setExtras(bundle)
                .setLifetime(Lifetime.FOREVER)
                .setService(JobService.class)
                .setTag("firebasejobdispatcher1")
                .setReplaceCurrent(true)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(3600, 3600))

                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)

                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        return job;
    }

    //Getting the Orderbook details from the API
    private void getOrderBookData(){

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://www.bitstamp.net/api/v2/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        bitstampApi = retrofit.create(BitstampApi.class);
        disposable = Observable.interval(1000, 5000,
                TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::callBitcoinBidAskEndpoint, this::onError);
    }

    private void callBitcoinBidAskEndpoint(Long aLong) {

        Observable<OrderBook> observable = bitstampApi.listOrderBook("order_book/btcusd/");
        observable.subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread())
                .map(result -> result)
                .subscribe(this::handleBitcoinBidAskResults, this::handleError);
    }

    private void handleBitcoinBidAskResults(OrderBook orderBook) {


        orderBookValues = new ArrayList<>();
        orderBookValues.clear();
        OrderBookValue orderBookValue = new OrderBookValue("Amount","Bids","Asks","Amount");
        orderBookValues.add(orderBookValue);

        for(int i=0;i<orderBook.getBids().size();i++){
            orderBookValue = new OrderBookValue(
                    orderBook.getBids().get(i).get(0),
                    orderBook.getBids().get(i).get(1),
                    orderBook.getAsks().get(i).get(0),
                    orderBook.getAsks().get(i).get(1));
            orderBookValues.add(orderBookValue);


        }

        recyclerViewadapter = new OrderBookAdapter(orderBookValues);
        recyclerView.setAdapter(recyclerViewadapter);
    }




}
