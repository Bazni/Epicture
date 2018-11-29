package com.example.rimkus.epicture;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.GridView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * Created by aurelien on 12/02/18.
 */

public class ImgurApi extends Application {
    public String getClientId() {
        return clientId;
    }

    /**
     * Manage ImgurApi. All the POST and Get requests
     * @param clientId
     * @param clientSecret
     * @param BASE_URL Api uri
     * @param queue queue of HTML requests
     * @param accessToken
     * @param refreshToken
     * @param sInstanceImgurApi Singelton
     * @param limit limit of displayed pictures  on one page
     */
    private String clientId;
    private String clientSecret;
    private String BASE_URL = "https://api.imgur.com/";
    private RequestQueue queue;
    private Context context;
    private String accessToken;
    private String refreshToken;
    private AccountManager accountManager;
    private static ImgurApi sInstanceImgurApi;
    private static final int limit = 50;

    public Vector<HashMap<String, String>> feed;
    public boolean isReady;

    public ImgurApi()
    {
    }

    public void setclass(ImgurApi api){
        this.clientId = api.getClientId();
        this.clientSecret = api.getClientSecret();
        this.context = api.getContext();
        this.queue = api.getRequestQueue();
        this.accountManager = api.getAccountManager();
        sInstanceImgurApi = api.getsInstanceImgurApi();
        accessToken = api.getAccessToken();
        refreshToken = api.getRefreshToken();
        feed = api.feed;
        isReady = api.isReady;
    }

    public ImgurApi(String clientId, String clientSecret, Context context)
    {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.context = context;
        this.queue = Volley.newRequestQueue(context);
        this.accountManager = AccountManager.get(context);
        sInstanceImgurApi = this;
        feed = new Vector<>();
        isReady = false;
    }

    /**
     * Do the request to add the image in favory
     * @param imageId
     */
    public void favoriteImage(String imageId)
    {
        String url = BASE_URL + "3/image/" + imageId + "/favorite";

        JsonObjectRequest jsObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("JSON", "FAVIMAGE en JSON: " + response.toString());
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR", "Error FAVIMAGE en JSON: " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };
        VolleyLog.d("Adding request to queue: %s", jsObjectRequest.getUrl());
        queue.add(jsObjectRequest);
    }

    /**
     * Do the request to delete the image from uploaded image
     * @param deleHash
     */
    public void deleteImage(String deleHash)
    {
        String url = BASE_URL + "3/image/" + deleHash;

        JsonObjectRequest jsObjectRequest = new JsonObjectRequest(
                Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("JSON", "DELEIMAGE en JSON: " + response.toString());
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR", "Error DELEIMAGE en JSON: " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };
        VolleyLog.d("Adding request to queue: %s", jsObjectRequest.getUrl());
        queue.add(jsObjectRequest);
    }

    /**
     * Do the request to upload an image
     * @param uri
     * @param title
     * @param description
     */
    public void uploadImage(Uri uri, String title, String description)
    {
        String url = BASE_URL + "3/image";
        OkHttpClient httpClient;
        httpClient = new OkHttpClient.Builder().build();
        String encodedImage = null;

        Bitmap bm = null;
        try {
            bm = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageByte = baos.toByteArray();
            encodedImage = Base64.encodeToString(imageByte, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", title)
                .addFormDataPart("image", encodedImage)
                .addFormDataPart("description", description)
                .addFormDataPart("type", "jpg")
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .header("Authorization","Bearer " + this.getAccessToken())
                .header("User-Agent","My Little App")
                .post( requestBody )
                .build();
        VolleyLog.d("Adding request to queue: %s", url);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", "An error has  occurred " + e);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.e("SUCESS", "Yeh -> " + response.toString());
            }
        });
    }

    /**
     * Do the request to connect with OAuth2 security
     */
    public void oauth2_0() {
        String url = BASE_URL + "oauth2/token";

        JSONObject postparams = new JSONObject();
        try {
            postparams.put("refresh_token", this.refreshToken);
            postparams.put("client_id", this.clientId);
            postparams.put("client_secret", this.clientSecret);
            postparams.put("grant_type", "refresh_token");
        } catch (JSONException error)
        {
            Log.e("JSON_ERROR", error.toString());
        }
        JsonObjectRequest jsObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("JSON", "OAUTH en JSON: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR", "Error OAUTH en JSON: " + error.toString());
                    }
                });
        VolleyLog.d("Adding request to queue: %s", jsObjectRequest.getUrl());
        queue.add(jsObjectRequest);
    }

    public String getAuthURL()
    {
        return BASE_URL+"oauth2/authorize?client_id="+clientId+"&response_type=token";
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public static synchronized ImgurApi getsInstanceImgurApi(String clientId, String clientSecret, Context context) {
        if (sInstanceImgurApi == null)
           sInstanceImgurApi = new ImgurApi(clientId, clientSecret, context);
        return sInstanceImgurApi;
    }

    public static synchronized ImgurApi getInstance() {
        return sInstanceImgurApi;
    }

    public RequestQueue getRequestQueue() {
        if (this.queue == null)
            this.queue = Volley.newRequestQueue(this.context);
        return this.queue;
    }

    public String getClientSecret() {
        return clientSecret;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public static ImgurApi getsInstanceImgurApi() {
        return sInstanceImgurApi;
    }

    public static void setsInstanceImgurApi(ImgurApi sInstanceImgurApi) {
        ImgurApi.sInstanceImgurApi = sInstanceImgurApi;
    }

    public static int getLimit() {
        return limit;
    }
}
