package com.example.android.ilovezappos;

public class OrderBookValue {
    private String bidAmount;
    private String bidPrice;
    private String askAmount;
    private String askPrice;

    public OrderBookValue(String bidAmount, String bidPrice, String askAmount, String askPrice) {
        this.bidAmount = bidAmount;
        this.bidPrice = bidPrice;
        this.askAmount = askAmount;
        this.askPrice = askPrice;
    }

    public String getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(String bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(String bidPrice) {
        this.bidPrice = bidPrice;
    }

    public String getAskAmount() {
        return askAmount;
    }

    public void setAskAmount(String askAmount) {
        this.askAmount = askAmount;
    }

    public String getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(String askPrice) {
        this.askPrice = askPrice;
    }
}
