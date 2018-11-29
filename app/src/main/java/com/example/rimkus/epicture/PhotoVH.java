package com.example.rimkus.epicture;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoVH extends RecyclerView.ViewHolder {
    ImageView photo;
    TextView title;
    ImageButton delete;

    public PhotoVH(View itemView) {
        super(itemView);
    }
}