package com.whiterabbit.postmanlibsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.whiterabbit.postman.ServerInteractionHelper;
import com.whiterabbit.postman.ServerInteractionResponseInterface;
import com.whiterabbit.postman.exceptions.SendingCommandException;
import com.whiterabbit.postmanlibsample.com.whiterabbit.postmanlibsample.commands.NoAuthCommand;

import java.io.*;

public class NoAuthSample extends FragmentActivity implements ServerInteractionResponseInterface, View.OnClickListener {
	static final String DOWNLOAD_IMAGE = "DownloadImage";
    ImageView mImage;
    TextView mStatusText;
    Button mDownloadButton;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_auth);

        mImage = (ImageView) findViewById(R.id.noauth_downloaded_image);
        mStatusText = (TextView) findViewById(R.id.noauth_status);
        mDownloadButton = (Button) findViewById(R.id.no_auth_download_button);
        mDownloadButton.setOnClickListener(this);
    }



	@Override
	protected void onPause() {
		ServerInteractionHelper.getInstance().unregisterEventListener(this, this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		ServerInteractionHelper.getInstance().registerEventListener(this, this);
		if(ServerInteractionHelper.getInstance().isRequestAlreadyPending(DOWNLOAD_IMAGE)){
			mStatusText.setText("Request in progress...");
		}
		super.onResume();
	}

	@Override
	public void onServerResult(String result, String requestId) {
        if(requestId.equals(DOWNLOAD_IMAGE)){
            AsyncTask<Void, Void, Bitmap> loadImage = new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... voids) {
                    File path = NoAuthSample.this.getExternalFilesDir(null);
                    File target = new File(path, "picture.png");

                    try {
                        InputStream s = new FileInputStream(target);
                        Bitmap bitmap = BitmapFactory.decodeStream(s);
                        return bitmap;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if(bitmap == null){
                        return;
                    }
                    mImage.setImageBitmap(bitmap);
                }
            };
            loadImage.execute();

        }
	}

	@Override
	public void onServerError(String result, String requestId) {
		mStatusText.setText(result);
	}



    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.no_auth_download_button:
                NoAuthCommand c = new NoAuthCommand();
                try {
                    ServerInteractionHelper.getInstance().sendCommand(this, c, DOWNLOAD_IMAGE);
                } catch (SendingCommandException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}