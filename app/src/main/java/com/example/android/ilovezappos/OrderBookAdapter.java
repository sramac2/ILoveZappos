package com.example.android.ilovezappos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrderBookAdapter extends RecyclerView.Adapter<OrderBookAdapter.OrderBookViewHolder> {

    private ArrayList<OrderBookValue> orderBookValues;



    public static class OrderBookViewHolder extends RecyclerView.ViewHolder{

        private TextView bidTextView;
        private TextView bidAmountTextView;
        private TextView askTextView;
        private TextView askAmountTextView;
        public OrderBookViewHolder(@NonNull View itemView) {
            super(itemView);
            bidTextView = itemView.findViewById(R.id.bid);
            bidAmountTextView = itemView.findViewById(R.id.bidAmount);
            askTextView = itemView.findViewById(R.id.ask);
            askAmountTextView = itemView.findViewById(R.id.askAmount);

        }
    }
    public OrderBookAdapter(ArrayList<OrderBookValue> orderBookValues){
        this.orderBookValues = orderBookValues;
    }

    @NonNull
    @Override
    public OrderBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_order_book,parent,false);
        OrderBookViewHolder orderBookViewHolder = new OrderBookViewHolder(v);
        return orderBookViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OrderBookViewHolder holder, int position) {
        OrderBookValue orderBookValue = orderBookValues.get(position);
        holder.bidTextView.setText(orderBookValue.getBidPrice());
        holder.askTextView.setText(orderBookValue.getAskPrice());
        holder.bidAmountTextView.setText(orderBookValue.getBidAmount());
        holder.askAmountTextView.setText(orderBookValue.getAskAmount());
    }


    @Override
    public int getItemCount() {
        return orderBookValues.size();
    }
}
