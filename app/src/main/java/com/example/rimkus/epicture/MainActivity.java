package com.example.rimkus.epicture;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    private TextView mTextMessage;
    private RecyclerView recyclerhome;
    private ImgurApi api;
    private OkHttpClient httpClient;
    private List<Photo> photos = new ArrayList<Photo>();
    private Boolean fil = true;
    private Spinner spinner;
    private Spinner spinner2;
    private Spinner spinner3;
    private String section = "hot";
    private String period = "day";
    private String sort = "viral";
    private int i = 0;

    /**
     * Bottom Navigation bar
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent home = new Intent(MainActivity.this, MainActivity.class);
                    finish();
                    startActivity(home);
                    return true;
                case R.id.navigation_dashboard:
                    Intent dashboard = new Intent(MainActivity.this, DashboardActivity.class);
                    finish();
                    startActivity(dashboard);
                    return true;
                case R.id.navigation_notifications:
                    Intent notif = new Intent(MainActivity.this, FavActivity.class);
                    finish();
                    startActivity(notif);
                    return true;
                case R.id.navigation_add:
                    Intent photo = new Intent(MainActivity.this, PhotoActivity.class);
                    finish();
                    startActivity(photo);
                    return true;
                case R.id.navigation_profile:
                    Intent profile = new Intent(MainActivity.this, profileActivity.class);
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
        RecyclerView rv = (RecyclerView)findViewById(R.id.Recyclerhome);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(null);
        RecyclerView.Adapter<PhotoVH> adapter = new RecyclerView.Adapter<PhotoVH>() {
            @Override
            public PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
                PhotoVH vh = new PhotoVH(getLayoutInflater().inflate(R.layout.home_item, null));
                vh.photo = (ImageView) vh.itemView.findViewById(R.id.photo);
                vh.title = (TextView) vh.itemView.findViewById(R.id.title);
                vh.delete = vh.itemView.findViewById(R.id.delete);
                return vh;
            }

            @Override
            public void onBindViewHolder(final PhotoVH holder,  int position) {
                final int pos = position;
                Glide.with(MainActivity.this).load("https://i.imgur.com/" +
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
     * Create the view
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = (ImgurApi) getApplicationContext();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        fetchData();
        spinner = findViewById(R.id.hotorviral);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numbers,R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        spinner2 = findViewById(R.id.period);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.sort,R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(this);

        spinner3 = findViewById(R.id.sort);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.period,R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);
        spinner3.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.hotorviral:
                section = parent.getItemAtPosition(position).toString();
                break;
            case R.id.period:
                period = parent.getItemAtPosition(position).toString();
                break;
            case R.id.sort :
                sort = parent.getItemAtPosition(position).toString();
         default:
             break;
        }
        i++;
        if (i >= 3){
            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/gallery/"+ section +"/"+period+"/" + sort+ "/1?showViral=true&mature=true&album_previews=true")
                    .header("Authorization","Bearer " + api.getAccessToken())
                    .header("User-Agent","My Little App")
                    .build();
            photos.clear();
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
                            if (item.getString( "title" ).equals( "null" ))
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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
