package com.whiterabbit.postmanlibsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.whiterabbit.postman.ServerInteractionHelper;
import com.whiterabbit.postman.ServerInteractionResponseInterface;
import com.whiterabbit.postman.exceptions.SendingCommandException;
import com.whiterabbit.postmanlibsample.commands.NoAuthRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Sample class to show the download of multiple images with one call
 */
public class NoAuthSample extends SherlockFragmentActivity implements ServerInteractionResponseInterface, View.OnClickListener {
    static final String DOWNLOAD_IMAGE = "DownloadImage";
    ImageView mImage;
    ImageView mImage1;
    Button mDownloadButton;
    ServerInteractionHelper mServerHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setupViews();
    }

    private void setupViews() {
        setContentView(R.layout.no_auth);
        mImage = (ImageView) findViewById(R.id.noauth_downloaded_image);
        mImage1 = (ImageView) findViewById(R.id.noauth_downloaded_image1);
        mDownloadButton = (Button) findViewById(R.id.no_auth_download_button);
        mDownloadButton.setOnClickListener(this);
        mServerHelper = ServerInteractionHelper.getInstance(this);
    }

    @Override
    protected void onPause() {
        ServerInteractionHelper.getInstance(this).unregisterEventListener(this, this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mServerHelper.registerEventListener(this, this);
        if (mServerHelper.isRequestAlreadyPending(DOWNLOAD_IMAGE)) {
            setUpdating();
        }
        super.onResume();
    }

    @Override
    public void onServerResult(String result, String requestId) {
        if (requestId.equals(DOWNLOAD_IMAGE)) {
            AsyncTask<Void, Void, Bitmap[]> loadImage = new AsyncTask<Void, Void, Bitmap[]>() {
                @Override
                protected Bitmap[] doInBackground(Void... voids) {
                    Bitmap[] res = new Bitmap[2];
                    File path = NoAuthSample.this.getExternalFilesDir(null);
                    File polle = new File(path, "polle.png");
                    File abetone = new File(path, "abetone.png");

                    try {
                        InputStream s = new FileInputStream(polle);
                        Bitmap bitmap = BitmapFactory.decodeStream(s);
                        res[0] = bitmap;
                        s = new FileInputStream(abetone);
                        Bitmap bitmap1 = BitmapFactory.decodeStream(s);
                        res[1] = bitmap1;
                        return res;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap[] bitmap) {
                    if (bitmap == null) {
                        return;
                    }
                    mImage.setImageBitmap(bitmap[0]);
                    mImage1.setImageBitmap(bitmap[1]);
                }
            };
            loadImage.execute();
            updateDone();

        }
    }

    @Override
    public void onServerError(String result, String requestId) {
        updateDone();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.no_auth_download_button:
                NoAuthRequest polle = new NoAuthRequest("http://www.cimonesci.it/cams/polle.jpg", "polle.png");
                NoAuthRequest abetone = new NoAuthRequest("http://www.aptabetone.it/abetone/pics/lat001.jpg", "abetone.png");
                try {
                    mServerHelper.sendRestAction(this, DOWNLOAD_IMAGE, polle, abetone);
                    setUpdating();
                } catch (SendingCommandException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void setUpdating() {
        setSupportProgressBarIndeterminateVisibility(true);
        invalidateOptionsMenu();
    }

    private void updateDone() {
        setSupportProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();
    }
}