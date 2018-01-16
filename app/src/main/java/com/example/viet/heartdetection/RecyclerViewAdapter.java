package com.example.viet.heartdetection;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by viet on 16/01/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Object> arrObject;

    public RecyclerViewAdapter(ArrayList<Object> arrObject) {
        this.arrObject = arrObject;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;
        recyclerViewHolder.setData(arrObject.get(position));
    }

    @Override
    public int getItemCount() {
        return arrObject.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvInfo;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvInfo = itemView.findViewById(R.id.tvInfo);
        }

        public void setData(Object object) {
            ivImage.setImageBitmap(object.getBitmap());
            tvInfo.setText(object.getInfo());
        }
    }

    public void addItem(Object object) {
        arrObject.add(object);
        notifyItemInserted(arrObject.size() - 1);
    }

    public void clear() {
        arrObject.clear();
        notifyDataSetChanged();
    }
}
