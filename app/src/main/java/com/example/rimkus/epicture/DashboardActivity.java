package com.example.rimkus.epicture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

public class DashboardActivity extends AppCompatActivity {

    private OkHttpClient httpClient;
    private List<Photo> photos = new ArrayList<Photo>();
    private ImgurApi api;
    RecyclerView.Adapter<PhotoVH> adapter;
    RecyclerView rv;

    /**
     * Bottom Navigation bar
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent home = new Intent(DashboardActivity.this, MainActivity.class);
                    finish();
                    startActivity(home);
                    return true;
                case R.id.navigation_dashboard:
                    Intent dashboard = new Intent(DashboardActivity.this, DashboardActivity.class);
                    finish();
                    startActivity(dashboard);
                    return true;
                case R.id.navigation_notifications:
                    Intent notif = new Intent(DashboardActivity.this, FavActivity.class);
                    finish();
                    startActivity(notif);
                    return true;                case R.id.navigation_add:
                    Intent photo = new Intent(DashboardActivity.this, PhotoActivity.class);
                    finish();
                    startActivity(photo);
                    return true;
                case R.id.navigation_profile:
                    Intent profile = new Intent(DashboardActivity.this, profileActivity.class);
                    finish();
                    startActivity(profile);
                    return true;
            }
            return false;
        }
    };

    /**
     * Init OkHttpClient
     */
    private void fetchData() {
        httpClient = new OkHttpClient.Builder().build();
    }

    /**
     * Create the RecyclerView and the adaptater
     * @param photos
     */
    private void render(final List<Photo> photos) {
        rv = (RecyclerView)findViewById(R.id.rv_of_photos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(null);
        adapter = new RecyclerView.Adapter<PhotoVH>() {
            @Override
            public PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
                PhotoVH vh = new PhotoVH(getLayoutInflater().inflate(R.layout.home_item, null));
                vh.photo = (ImageView) vh.itemView.findViewById(R.id.photo);
                vh.title = (TextView) vh.itemView.findViewById(R.id.title);
                vh.delete = vh.itemView.findViewById(R.id.delete);
                return vh;
            }

            @Override
            public void onBindViewHolder(final PhotoVH holder, int position) {
                final int pos = position;
                Glide.with(DashboardActivity.this).load("https://i.imgur.com/" +
                        photos.get(position).id + ".jpg").into(holder.photo);
                holder.title.setText(photos.get(position).title);
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        api.favoriteImage(photos.get(pos).id);
                        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_black_24dp);
                        holder.delete.setImageDrawable(drawable);
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

    /**
     * Do the request with the text in the search bar
     * @param editText
     */
    private void performSearch(TextView editText) {
        InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        View searchEditText = this.findViewById(android.R.id.content);
        in.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        fetchData();
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/gallery/search/viral/month?q=" + editText.getText())
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
                photos.clear();
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

    /**
     * Create the view
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = (ImgurApi) getApplicationContext();
        setContentView(R.layout.activity_dashboard);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_dashboard);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        final TextView editText = findViewById(R.id.bar);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(editText);
                    return true;
                }
                return false;
            }
        });
    }
}
