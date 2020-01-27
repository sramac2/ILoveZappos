package com.example.android.ilovezappos;

public class BitcoinValue {
    private String date;
    private String tid;
    private String price;
    private String type;
    private String amount;


    // Getter Methods

    public String getDate() {
        return date;
    }

    public String getTid() {
        return tid;
    }

    public String getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public String getAmount() {
        return amount;
    }

    // Setter Methods

    public void setDate(String date) {
        this.date = date;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}