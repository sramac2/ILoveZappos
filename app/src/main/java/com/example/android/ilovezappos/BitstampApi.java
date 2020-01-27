package com.example.android.ilovezappos;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BitstampApi {

    @GET("{path}")
    Observable<List<BitcoinValue>> listBitcoinValues(@Path("path") String path);

    @GET("{path}")
    Observable<OrderBook> listOrderBook(@Path("path")String path);

    @GET("{path}")
    Call<BitcoinLatestPrice> listLatestBitcoinPrice(@Path("path")String path);
}
