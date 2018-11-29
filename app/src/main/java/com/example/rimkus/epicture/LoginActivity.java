package com.example.rimkus.epicture;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginActivity extends AppCompatActivity {

    private static final String client_id = "94315ae4fb02908";
    private static final String client_secret = "7e93decf51c0ba64e4e68e50cbbc1c110a0e2e6b";
    private WebView web;
    private Context context;
    Dialog auth_dialog;

    /**
     * Create the view with a WebView to allow the user to connect to the application
     * @param savedInstanceState
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        context = this;
        auth_dialog = new Dialog(LoginActivity.this);
        web = (WebView) findViewById(R.id.web_view);
        web.getSettings().setJavaScriptEnabled(true);
        final ImgurApi manager_api = new ImgurApi(client_id, client_secret, this);
        web.loadUrl(manager_api.getAuthURL());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url_s) {
                super.onPageFinished(view, url_s);
                String url = url_s.replace('#', '?');
                Uri uri = Uri.parse(url);

                if (uri.toString().contains("client_id"))
                {
                    Log.e("CLIENT_ID", uri.toString());
                } else {
                    uri.getQueryParameter("access_token");
                    Log.e("AccessToken", uri.getQueryParameter("access_token"));
                    Log.e("refreshToken", uri.getQueryParameter("refresh_token"));
                    manager_api.setAccessToken(uri.getQueryParameter("access_token"));
                    manager_api.setRefreshToken(uri.getQueryParameter("refresh_token"));
                    Log.e("URL", url);
                    auth_dialog.dismiss();
                    manager_api.oauth2_0();

                    ImgurApi global_api = (ImgurApi) getApplicationContext();
                    global_api.setclass(manager_api);

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Connected !");
        auth_dialog.setCancelable(true);
    }
}
