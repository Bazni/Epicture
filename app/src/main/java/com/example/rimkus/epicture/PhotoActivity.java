package com.example.rimkus.epicture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.View.GONE;

public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = PhotoActivity.class.getName();
    private static final int NUM_GRID_COLUMNS = 4;

    private ImgurApi api;
    private TextView mTextMessage;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;;
    private ImageButton SendButton;
    private Button SearchButton;
    private Button TakePhoto;
    private EditText titlezer;
    private EditText description;
    private Uri image;
    String imageFilePath;

    public static final int PICK_IMAGE = 1;
    public static final int REQUEST_CAPTURE = 2 ;

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
                    Intent home = new Intent(PhotoActivity.this, MainActivity.class);
                    finish();
                    startActivity(home);
                    return true;
                case R.id.navigation_dashboard:
                    Intent dashboard = new Intent(PhotoActivity.this, DashboardActivity.class);
                    finish();
                    startActivity(dashboard);
                    return true;
                case R.id.navigation_notifications:
                    Intent notif = new Intent(PhotoActivity.this, FavActivity.class);
                    finish();
                    startActivity(notif);
                    return true;
                case R.id.navigation_add:
                    Intent photo = new Intent(PhotoActivity.this, PhotoActivity.class);
                    finish();
                    startActivity(photo);
                    return true;
                case R.id.navigation_profile:
                    Intent profile = new Intent(PhotoActivity.this, profileActivity.class);
                    finish();
                    startActivity(profile);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.photo_bar);
        setSupportActionBar(toolbar);


        galleryImage = (ImageView) findViewById(R.id.galleryImageView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        SearchButton = (Button) findViewById(R.id.search_photo);
        titlezer = (EditText) findViewById(R.id.title_photo);
        description = (EditText) findViewById(R.id.description_photo);

        SendButton = (ImageButton) findViewById(R.id.send_photo);
        TakePhoto = findViewById(R.id.take_photo);
        mTextMessage = (TextView) findViewById(R.id.message);

        /**
         *  open camera
         */
        TakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               openCameraIntent();
            }
        });


        /**
         * upload image
         */
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image != null && !image.equals(Uri.EMPTY)){

                    api.uploadImage(image,titlezer.getText().toString(),description.getText().toString());
                    Intent refresh = new Intent(PhotoActivity.this, PhotoActivity.class);
                    finish();
                    startActivity(refresh);
                }
                else{
                    Toast.makeText(PhotoActivity.this, "choose a picture",
                            Toast.LENGTH_LONG).show();
                }
            }
        })  ;

        /**
         * take an image form repo
         */
        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        api = (ImgurApi) getApplicationContext();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_add);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
    /**
     * transforme an uri to bitmap and display it
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            image = data.getData();
            try {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                galleryImage.setImageBitmap(bitmap);
                mProgressBar.setVisibility(GONE);
                }
                catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_CAPTURE) {
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
            galleryImage.setImageBitmap(yourSelectedImage);
            mProgressBar.setVisibility(GONE);
        }
    }

    /**
     * create a file for camera picture
     */
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager()) != null){
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.e(TAG, "openCameraIntent: "+ photoFile.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
               image = FileProvider.getUriForFile(this,"com.example.android.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        image);
                startActivityForResult(pictureIntent,
                        REQUEST_CAPTURE);
            }
        }
    }
}
