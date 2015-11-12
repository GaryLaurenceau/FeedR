package com.sokss.feedr.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.ByteArrayOutputStream;
import java.net.DatagramSocket;

public class CreditActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "com.sokss.feedr.app.CreditActivity";

    // Widgets
    private ImageView mRateMe;
    private TextView mRateMeText;
    private ImageView mTNP;
    private TextView mTNPText;
    private ImageView mGithub;
    private TextView mGithubText;
    private TextView mGithubTextAuthors;
    private ImageView mShareApp;

    // Data
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        getUI();
        setListeners();

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .build();

        Slidr.attach(this, config);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }



//        sendDataToWear();
    }

    @Override
     public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_credit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        overridePendingTransition(R.anim.anim_in_left_to_right, R.anim.anim_out_left_to_right);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_in_left_to_right, R.anim.anim_out_left_to_right);
    }

    private void getUI() {
        mRateMe = (ImageView) findViewById(R.id.rate_me);
        mRateMeText = (TextView) findViewById(R.id.rate_me_text);
        mTNP = (ImageView) findViewById(R.id.tnp);
        mTNPText = (TextView) findViewById(R.id.tnp_text);
        mGithub = (ImageView) findViewById(R.id.github);
        mGithubText = (TextView) findViewById(R.id.github_text);
        mGithubTextAuthors = (TextView) findViewById(R.id.github_text_authors);
        mShareApp = (ImageView) findViewById(R.id.share_app);
    }

    private void setListeners() {
        int color = getResources().getColor(R.color.blue);

        mRateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sokss.feedr.app")));
            }
        });

        mTNP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.thenounproject.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        mTNPText.setMovementMethod(LinkMovementMethod.getInstance());

        mGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.github.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        mGithubText.setMovementMethod(LinkMovementMethod.getInstance());
        mGithubTextAuthors.setMovementMethod(LinkMovementMethod.getInstance());

        mShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_app));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    private void sendDataToWear() {
        if (mGoogleApiClient.isConnected()) {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("./");

            // Add data to the request
            putDataMapRequest.getDataMap().putString("text", String.format("hello world!"));

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            Asset asset = createAssetFromBitmap(icon);
            putDataMapRequest.getDataMap().putAsset("image", asset);

            PutDataRequest request = putDataMapRequest.asPutDataRequest();

            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Log.d(TAG, "putDataItem status: " + dataItemResult.getStatus().toString());
                        }
                    });
        }
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
