package com.example.android.ilovezappos;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

//Firebase Job Dispatcher background notification Service
public class JobService extends com.firebase.jobdispatcher.JobService {
    private String CHANNEL_ID="SSS";
    private Retrofit retrofit;
    private BitstampApi bitstampApi;
    private String expectedValue;
    private Disposable disposable;
    private CompositeDisposable compositeDisposable;
    @Override
    public boolean onStartJob(@NonNull JobParameters job) {
        //Toast.makeText(this,"Heloo",Toast.LENGTH_SHORT).show();
        expectedValue = job.getExtras().getString("value");
        compositeDisposable = new CompositeDisposable();
        getData();
    return false;
    }

    @Override
    public boolean onStopJob(@NonNull JobParameters job) {
        return false;
    }
    private void getData(){


        retrofit = new Retrofit.Builder()
                .baseUrl("https://www.bitstamp.net/api/v2/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        bitstampApi = retrofit.create(BitstampApi.class);
        Call<BitcoinLatestPrice> call = bitstampApi.listLatestBitcoinPrice("ticker_hour/btcusd/");
        call.enqueue(new Callback<BitcoinLatestPrice>() {
            @Override
            public void onResponse(Call<BitcoinLatestPrice> call, Response<BitcoinLatestPrice> response) {
                handleResults(response.body());

            }

            @Override
            public void onFailure(Call<BitcoinLatestPrice> call, Throwable t) {
                Log.e("error",t.getLocalizedMessage());
            }
        });

    }
    private void onError(Throwable throwable) {
        Toast.makeText(this, "OnError in Observable Timer",
                Toast.LENGTH_LONG).show();
    }

    //handle the results coming from request
    private void handleResults(BitcoinLatestPrice bitcoinLatestPrice) {
        float currentValue = Float.parseFloat(bitcoinLatestPrice.getLast());
        if(bitcoinLatestPrice!=null&& expectedValue!=null){
            float v =Float.parseFloat(expectedValue);
            if(currentValue<v){
                sendNotification();
            }
        }
    }

    private void handleError(Throwable t) {
        //Log.e(t.getLocalizedMessage());
    }

    //sends notification to the user
    public void sendNotification(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_add_alarm_black_24dp)
                .setContentTitle("Bitcoin Price Drop")
                .setContentIntent(pendingIntent)
                .setContentText("Bitcoin has reached lower than your requested price of"+expectedValue)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Bitcoin has reached lower than your requested price of"+expectedValue))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(10,builder.build());
    }


}
