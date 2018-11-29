package com.example.rimkus.epicture;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class profileActivity extends AppCompatActivity {

    /**
     * Navmenu
     * Nav on different activity
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent home = new Intent(profileActivity.this, MainActivity.class);
                    finish();
                    startActivity(home);
                    return true;
                case R.id.navigation_dashboard:
                    Intent dashboard = new Intent(profileActivity.this, DashboardActivity.class);
                    finish();
                    startActivity(dashboard);
                    return true;
                case R.id.navigation_notifications:
                    Intent notif = new Intent(profileActivity.this, FavActivity.class);
                    finish();
                    startActivity(notif);
                    return true;
                case R.id.navigation_add:
                    Intent photo = new Intent(profileActivity.this, PhotoActivity.class);
                    finish();
                    startActivity(photo);
                    return true;
                case R.id.navigation_profile:
                    Intent profile = new Intent(profileActivity.this, profileActivity.class);
                    finish();
                    startActivity(profile);
                    return true;
            }
            return false;
        }
    };

    private OkHttpClient httpClient;
    private ImgurApi api;
    private List<Photo> photos = new ArrayList<Photo>();

    /**
     * init OkHttpClient
     */
    private void fetchData() {
        httpClient = new OkHttpClient.Builder().build();
    }

    /**
     *  Create the RecyclerView and the Adaptater
     *  And fill image and title in the RecyclerView
     */
    private void render(final List<Photo> photos) {
        RecyclerView rv = (RecyclerView)findViewById(R.id.rv_of_photos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(null);
        RecyclerView.Adapter<PhotoVH> adapter = new RecyclerView.Adapter<PhotoVH>() {
            @Override
            public PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
                PhotoVH vh = new PhotoVH(getLayoutInflater().inflate(R.layout.item, null));
                vh.photo = (ImageView) vh.itemView.findViewById(R.id.photo);
                vh.title = (TextView) vh.itemView.findViewById(R.id.title);
                vh.delete = vh.itemView.findViewById(R.id.delete);
                return vh;
            }

            @Override
            public void onBindViewHolder(final PhotoVH holder, int position) {
                final int pos = position;
                Glide.with(profileActivity.this).load("https://i.imgur.com/" +
                        photos.get(position).id + ".jpg").into(holder.photo);
                holder.title.setText(photos.get(position).title);
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        api.deleteImage(photos.get(pos).id);
                        Intent refresh = new Intent(profileActivity.this, profileActivity.class);
                        finish();
                        startActivity(refresh);
                        //Toast.makeText(profileActivity.this, "https://i.imgur.com/" + photos.get(position).id + ".jpg", Toast.LENGTH_LONG).show();

                    }
                });
            }

            @Override
            public int getItemCount() {
                return photos.size();
            }
        };
        rv.setAdapter(adapter);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 16; // Gap of 16px
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        api = (ImgurApi) getApplicationContext();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_profile);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fetchData();
        /**
         * request account image
         */
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/account/me/images/")
                .header("Authorization","Bearer " + api.getAccessToken())
                .header("User-Agent","My Little App")
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", "An error has  occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JSONObject data = null;

                try {
                    data = new JSONObject(response.body().string());
                    JSONArray items = data.getJSONArray("data");

                    Log.e("DATA", response.toString());
                    for(int i=0; i<items.length();i++) {
                        JSONObject item = items.getJSONObject(i);
                        Photo photo = new Photo();
                        if (item.has("is_album")) {
                            if(item.getBoolean("is_album"))
                                photo.id = item.getString("cover");
                            else
                                photo.id = item.getString("id");
                        } else {
                            photo.id = item.getString("id");
                        }
                        photo.title = item.getString("title");
                        if (item.getString("title") == "null")
                            photo.title = "No Title";
                        photos.add(photo); // Add photo to list
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        render(photos);
                    }
                });
            }
        });
    }
}
