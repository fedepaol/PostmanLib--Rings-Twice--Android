package com.whiterabbit.postmanlibsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.whiterabbit.postman.SendingCommandException;
import com.whiterabbit.postman.ServerInteractionHelper;
import com.whiterabbit.postman.ServerInteractionResponseInterface;

public class SampleActivity extends Activity implements ServerInteractionResponseInterface {
	static final String USER_STATUS_REQUEST = "UserRequest";
	
	TextView mStatus;
	TextView mProfile;
	EditText mName;
	Button mUpdateStatusButton;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	ServerInteractionHelper.initWithCommandFactory(new TwitterCommandFactory());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        mStatus = (TextView) findViewById(R.id.status_string);
        mUpdateStatusButton = (Button) findViewById(R.id.update_status);
        mName = (EditText) findViewById(R.id.user_name);
        mProfile = (TextView) findViewById(R.id.profile);
        
        mUpdateStatusButton.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View v) {
        		String name = mName.getText().toString(); 
				if(!name.equals("")){
					TwitterUserGetCommand c = new TwitterUserGetCommand(name);
					try {
						ServerInteractionHelper.getInstance().sendCommand(SampleActivity.this, c, USER_STATUS_REQUEST);
					} catch (SendingCommandException e) {
						mStatus.setText("Request already pending");
					}
				}
			}
        	
        });
    }

	@Override
	protected void onPause() {
		ServerInteractionHelper.getInstance().unregisterEventListener(this, this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		
		ServerInteractionHelper.getInstance().registerEventListener(this, this);
		if(ServerInteractionHelper.getInstance().isRequestAlreadyPending(USER_STATUS_REQUEST)){
			mStatus.setText("Request in progress...");
		}
		super.onResume();
	}

	@Override
	public void onServerResult(String result, String requestId) {
		String storedProfile = StoreUtils.getProfile(this);
		mProfile.setText(storedProfile);
		
	}

	@Override
	public void onServerError(String result, String requestId) {
		mStatus.setText(result);
	}
    
    
    
}