package com.whiterabbit.postmanlibsample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.whiterabbit.postman.ServerInteractionHelper;
import com.whiterabbit.postman.ServerInteractionResponseInterface;
import com.whiterabbit.postman.exceptions.SendingCommandException;
import com.whiterabbit.postman.oauth.OAuthHelper;
import com.whiterabbit.postman.oauth.OAuthResponseInterface;
import com.whiterabbit.postman.oauth.StorableServiceBuilder;
import com.whiterabbit.postmanlibsample.commands.TwitterGetLatestTweetRequest;
import com.whiterabbit.postmanlibsample.commands.TwitterUpdateStatusRequest;
import org.scribe.builder.api.TwitterApi;

public class TwitterSample extends SherlockFragmentActivity implements ServerInteractionResponseInterface, OAuthResponseInterface, View.OnClickListener {
	static final String UPDATE_STATUS = "StatusUpdate";
    static final String REQUEST_LATEST_TWEET = "LatestTweet";
    private final static String mApiKey = "COPaViCT6nLRcGROTVZdA"; // <- must be set from the real one got from www.twitter.com
    private final static String mApiSecret = "OseRpVLfo19GP9OAPj9FYwCDV1nyjlWygHyuLixzNPk"; // <- must be set from the real one got from www.twitter.com

    private TextView mAuthenticationStatus;
	private EditText mStatusToSend;
    private TextView mLatestTweet;
	private Button mUpdateStatusButton;
    private Button mGetLatestTweetButton;
    ServerInteractionHelper mServerHelper;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.twitter);
        mServerHelper = ServerInteractionHelper.getInstance(this);

        setupViews();


        if(mApiKey.equals("APIKEY")){
            Toast toast = Toast.makeText(this, "A real apikey must be provided", Toast.LENGTH_SHORT);
            toast.show();
        }

        registerToTwitter();

    }

    private void setupViews(){
        mAuthenticationStatus = (TextView) findViewById(R.id.TwitterAuthStatus);
        mUpdateStatusButton = (Button) findViewById(R.id.TwitterUpdateStatusButton);

        mStatusToSend = (EditText) findViewById(R.id.TwitterStatusToPublish);
        mLatestTweet = (TextView) findViewById(R.id.LatestTweet);
        mGetLatestTweetButton = (Button) findViewById(R.id.TwitterGetLatestTweetButton);

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
            mAuthenticationStatus.setText("Authenticating..");
            o.authenticate(this, "Twitter");
        }else{
            mAuthenticationStatus.setText("Authenticated");
            enableButtons();
        }
    }

    private void enableButtons(){
        mUpdateStatusButton.setOnClickListener(this);
        mGetLatestTweetButton.setOnClickListener(this);
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
		if(mServerHelper.isRequestAlreadyPending(UPDATE_STATUS)){
			mAuthenticationStatus.setText("Authenticated");
		}
		super.onResume();
	}

	@Override
	public void onServerResult(String result, String requestId) {
        if(requestId.equals(REQUEST_LATEST_TWEET)){
            mLatestTweet.setText(StoreUtils.getLatestTweet(this));
            updateDone();
        }
        if(requestId.equals(UPDATE_STATUS)){
            updateDone();
        }
	}

	@Override
	public void onServerError(String result, String requestId) {
        updateDone();
	}


    @Override
    public void onServiceAuthenticated(String serviceName) {
        Toast toast = Toast.makeText(this, String.format("%s authenticated", serviceName), Toast.LENGTH_SHORT);
        mAuthenticationStatus.setText("Authenticated");
        toast.show();
        enableButtons();
    }

    @Override
    public void onServiceAuthenticationFailed(String serviceName, String reason) {
        Toast toast = Toast.makeText(this, String.format("Failed to authenticate %s: %s", serviceName, reason), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.TwitterUpdateStatusButton:
                TwitterUpdateStatusRequest statusStrategy = new TwitterUpdateStatusRequest(mStatusToSend.getText().toString());
                try {
                    mServerHelper.sendRestAction(this, UPDATE_STATUS, statusStrategy);
                    setUpdating();
                } catch (SendingCommandException e) {
                    e.printStackTrace();
                }
            break;

            case R.id.TwitterGetLatestTweetButton:
                TwitterGetLatestTweetRequest c = new TwitterGetLatestTweetRequest();
                try {
                    mServerHelper.sendRestAction(this, REQUEST_LATEST_TWEET, c);
                    setUpdating();
                } catch (SendingCommandException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    private void setUpdating(){
        setSupportProgressBarIndeterminateVisibility(true);
        invalidateOptionsMenu();
    }

    private void updateDone(){
        setSupportProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();
    }
}