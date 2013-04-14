package com.whiterabbit.postmanlibsample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.whiterabbit.postman.ServerInteractionHelper;
import com.whiterabbit.postman.ServerInteractionResponseInterface;
import com.whiterabbit.postman.oauth.OAuthHelper;
import com.whiterabbit.postman.oauth.OAuthResponseInterface;
import com.whiterabbit.postman.oauth.StorableServiceBuilder;
import com.whiterabbit.postmanlibsample.wakeful.WakefulAlarmReceiver;
import org.scribe.builder.api.TwitterApi;

public class WakefulAlarmManagerSample extends FragmentActivity implements ServerInteractionResponseInterface, OAuthResponseInterface, View.OnClickListener {
    public static final String WAKEFUL_SCHEDULED_LATEST_TWEET = "WakefulScheduledLatestTweet";
    private final static String mApiKey = "COPaViCT6nLRcGROTVZdA"; // <- must be set from the real one got from www.twitter.com
    private final static String mApiSecret = "OseRpVLfo19GP9OAPj9FYwCDV1nyjlWygHyuLixzNPk"; // <- must be set from the real one got from www.twitter.com

    private TextView mTwitScheduledStatus;
    private TextView mLatestTweet;
	private Button mToggleAutoUpdate;
    ServerInteractionHelper mServerHelper;
    private static final int PERIOD=5000;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_scheduled);
        mServerHelper = ServerInteractionHelper.getInstance(this);

        setupViews();


        if(mApiKey.equals("APIKEY")){
            Toast toast = Toast.makeText(this, "A real apikey must be provided", Toast.LENGTH_SHORT);
            toast.show();
        }

        registerToTwitter();

    }

    private void setupViews(){
        mTwitScheduledStatus = (TextView) findViewById(R.id.TwitScheduledStatus);
        mToggleAutoUpdate = (Button) findViewById(R.id.TwitScheduledEnable);
        mLatestTweet = (TextView) findViewById(R.id.TwitScheduledLatestTweet);
        mLatestTweet.setText(StoreUtils.getLatestTweet(this));

    }

    private void registerToTwitter(){
       StorableServiceBuilder builder = new StorableServiceBuilder("Twitter")
                .provider(TwitterApi.class)
                .apiKey(mApiKey)
                .apiSecret(mApiSecret)
                .callback("www.mycallback.com", "oauth_verifier");

        OAuthHelper o = OAuthHelper.getInstance();
        o.registerOAuthService(builder, this);

        if(!o.isAlreadyAuthenticated("Twitter", this)){
            o.authenticate(this, "Twitter");
        }else{
            enableButtons();
        }
    }

    private void enableButtons(){
        mToggleAutoUpdate.setOnClickListener(this);
    }





	@Override
	protected void onPause() {
		mServerHelper.unregisterEventListener(this, this);
        OAuthHelper.getInstance().unregisterListener();
		super.onPause();
	}

	@Override
	protected void onResume() {
        OAuthHelper.getInstance().registerListener(this);
		
		mServerHelper.registerEventListener(this, this);
        setStatusString(StoreUtils.getTwitScheduleStatus(this));
		super.onResume();
	}

    private void setStatusString(boolean enabled) {
        if(enabled){
            mTwitScheduledStatus.setText("Polling enabled");
        }else{
            mTwitScheduledStatus.setText("Polling disabled");
        }
    }

	@Override
	public void onServerResult(String result, String requestId) {
        if(requestId.equals(WAKEFUL_SCHEDULED_LATEST_TWEET)){
            mLatestTweet.setText(StoreUtils.getLatestTweet(this));
        }
	}

	@Override
	public void onServerError(String result, String requestId) {
        Toast t = Toast.makeText(this, "Server error", Toast.LENGTH_SHORT);
        t.show();
	}


    @Override
    public void onServiceAuthenticated(String serviceName) {
        Toast toast = Toast.makeText(this, String.format("%s authenticated", serviceName), Toast.LENGTH_SHORT);
        toast.show();
        enableButtons();
    }

    @Override
    public void onServiceAuthenticationFailed(String serviceName, String reason) {
        Toast toast = Toast.makeText(this, String.format("Failed to authenticate %s: %s", serviceName, reason), Toast.LENGTH_SHORT);
        toast.show();
    }


    /**
     * Sample code that shows how to toggle a repeating wakeful alarm that will interact with sendWakefulRequest method
     */
    private void toggleWakeful(){
        boolean enabled = StoreUtils.getWakefulTwitScheduleStatus(this);

        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, WakefulAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);


        if(enabled){
            mgr.cancel(pi);
        }else{
            mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 60000,
                    PERIOD,
                    pi);

        }
        boolean newStatus = !enabled;
        setStatusString(newStatus);
        StoreUtils.setWakefulTwitScheduleStatus(newStatus, this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.TwitScheduledEnable:
                toggleWakeful();
            break;
        }
    }
}