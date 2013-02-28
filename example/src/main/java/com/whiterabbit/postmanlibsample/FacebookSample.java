package com.whiterabbit.postmanlibsample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.whiterabbit.postman.ServerInteractionHelper;
import com.whiterabbit.postman.ServerInteractionResponseInterface;
import com.whiterabbit.postman.exceptions.SendingCommandException;
import com.whiterabbit.postman.oauth.OAuthHelper;
import com.whiterabbit.postman.oauth.OAuthResponseInterface;
import com.whiterabbit.postman.oauth.StorableServiceBuilder;
import com.whiterabbit.postmanlibsample.com.whiterabbit.postmanlibsample.commands.FacebookGet;
import org.scribe.builder.api.FacebookApi;

public class FacebookSample extends FragmentActivity implements ServerInteractionResponseInterface, OAuthResponseInterface, View.OnClickListener {
	static final String GET_INFOS = "FbGetInfos";
    private final static String mApiKey = "256728337717987";
    private final static String mApiSecret = "76e0eeef1db52fae6c20a8c16324e8cb";

    private TextView mRequestStatus;
    private TextView mFbName;
    private TextView mFbLink;
	private Button mGetInfosButton;
    ServerInteractionHelper mServerHelper;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fb);
        mServerHelper = ServerInteractionHelper.getInstance(this);
        
        
        mRequestStatus = (TextView) findViewById(R.id.FbRequestStatus);
        mFbName = (TextView) findViewById(R.id.FbName);
        mFbLink = (TextView) findViewById(R.id.FbLink);
        mGetInfosButton = (Button) findViewById(R.id.FbGetInfosButton);


        if(mApiKey.equals("APIKEY")){
            Toast toast = Toast.makeText(this, "A real apikey must be provided", Toast.LENGTH_SHORT);
            toast.show();
        }

        registerToFb();

    }

    private void registerToFb(){
       StorableServiceBuilder builder = new StorableServiceBuilder("Facebook")
                .provider(FacebookApi.class)
                .apiKey(mApiKey)
                .apiSecret(mApiSecret)
                .callback("http://www.mydearsanta.info/callback", "code");

        OAuthHelper o = OAuthHelper.getInstance();
        o.registerOAuthService(builder, this);

        if(!o.isAlreadyAuthenticated("Facebook", this)){
            mRequestStatus.setText("Authenticating..");
            o.authenticate(this, "Facebook");
        }else{
            enableButtons();
        }
    }

    private void enableButtons(){
        mGetInfosButton.setOnClickListener(this);

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
		if(mServerHelper.isRequestAlreadyPending(GET_INFOS)){
			mRequestStatus.setText("Request in progress...");
		}
		super.onResume();
	}

	@Override
	public void onServerResult(String result, String requestId) {
        if(requestId.equals(GET_INFOS)){
            mFbLink.setText(StoreUtils.getFbLink(this));
            mFbName.setText(StoreUtils.getFBName(this));
        }
	}

	@Override
	public void onServerError(String result, String requestId) {
		mRequestStatus.setText(result);
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

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.FbGetInfosButton:
                FacebookGet updateStrategy = new FacebookGet();
                try {
                    mServerHelper.sendRestAction(this, GET_INFOS, updateStrategy);
                } catch (SendingCommandException e) {
                    e.printStackTrace();
                }
            break;
        }
    }
}