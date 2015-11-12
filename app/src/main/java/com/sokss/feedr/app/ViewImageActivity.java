package com.sokss.feedr.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ViewImageActivity extends AppCompatActivity {

    private static final String TAG = "com.sokss.feedr.app.ViewImageActivity";

    // Widgets
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        String bitmapUrl = getIntent().getStringExtra("bitmap_url");

        getActionBar().hide();

        mImageView = (ImageView) findViewById(R.id.image_view);

        if (bitmapUrl != null && bitmapUrl.length() > 0) {
            Picasso.with(this).load(bitmapUrl).into(mImageView);
        }

        new PhotoViewAttacher(mImageView);
    }
}
