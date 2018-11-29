package com.example.rimkus.epicture;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import java.util.HashMap;
import java.util.Vector;

public class HomeAdaptater extends BaseAdapter {

    private Context context;
    private Vector<HashMap<String, String>> feed;

    HomeAdaptater(Context mcontext,  Vector<HashMap<String, String>> feed){
        this.context = mcontext;
        this.feed = feed;
    }

    @Override
    public int getCount() {
        return feed.size();
    }

    @Override
    public Object getItem(int position) {
        return feed.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.home_format, null);
        ImageView iv_image = view.findViewById(R.id.i_picture);


        Glide.with(context).load(feed.get(position).get("link")).into(iv_image);

        return view;

    }
}
