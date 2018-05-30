package com.example.user.magicstick;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by user on 2018/4/30.
 */

public class LinearLayout extends LinearLayoutManager {
    public LinearLayout(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
