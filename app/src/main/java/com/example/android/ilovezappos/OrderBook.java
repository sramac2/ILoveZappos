package com.example.android.ilovezappos;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class OrderBook {
    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("bids")
    private ArrayList<List<String>> bids = new ArrayList <> ();

    @SerializedName("asks")
    private ArrayList<List<String>> asks = new ArrayList <> ();

    public ArrayList<List<String>> getBids() {
        return bids;
    }

    public void setBids(ArrayList<List<String>> bids) {
        this.bids = bids;
    }

    public ArrayList<List<String>> getAsks() {
        return asks;
    }

    public void setAsks(ArrayList<List<String>> asks) {
        this.asks = asks;
    }


    // Getter Methods

    public String getTimestamp() {
        return timestamp;
    }

    // Setter Methods

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
