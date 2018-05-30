package com.example.user.magicstick;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.magicstick.activite.MainActivity;

import java.util.ArrayList;

/**
 * Created by user on 2018/4/18.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {
    private Context mContext;
    private ArrayList<MainActivity.BlueDevice> mBlueList;
    private OnItemClickLitener mOnItemClickLitener;

    public MyAdapter(Context c, ArrayList<MainActivity.BlueDevice> b) {
        mContext = c;
        mBlueList = b;
    }
    public interface OnItemClickLitener{
        public void OnItemCkickLitener(View v,int position);
    }
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyHolder myHolder = new MyHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.deviceitem, parent, false));
            return myHolder;

    }

    public void setOnItemClick(OnItemClickLitener onItemClickLitener){
        mOnItemClickLitener = onItemClickLitener;
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {

        holder.name.setText(mBlueList.get(position).device.getName());
        holder.rssi.setText(String.valueOf(mBlueList.get(position).rssi));
        if (mOnItemClickLitener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickLitener.OnItemCkickLitener(holder.itemView,position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mBlueList.size();
    }

    public class MyHolder extends ViewHolder {
        TextView name;
        TextView rssi;

        public MyHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.device_name);
            rssi = itemView.findViewById(R.id.device_rssi);
        }
    }
}
