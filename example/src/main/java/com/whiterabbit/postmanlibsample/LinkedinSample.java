package com.whiterabbit.postmanlibsample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.whiterabbit.postmanlibsample.commands.LinkedinGetCurrentUserRequest;
import org.scribe.builder.api.LinkedInApi;

/**
 * Sample activity to show linkedin interaction.
 * ApiKey / Secret must be set with real one
 */
public class LinkedinSample extends SherlockFragmentActivity implements ServerInteractionResponseInterface, OAuthResponseInterface, View.OnClickListener {
    static final String REQUEST_CURRENT_USER_DETAILS = "LinkedinUser";
    TextView mAuthStatus;
    Button mGetHeadlineButtn;
    TextView mLinkedinHeadLine;
    ServerInteractionHelper mServer;
    private final static String mApiKey = "APIKEY";// <- must be set from the real one got from www.linkedin.com
    private final static String mApiSecret = "APISECRET";// <- must be set from the real one got from www.linkedin.com

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setupViews();

        mServer = ServerInteractionHelper.getInstance(this);

        if (mApiKey.equals("APIKEY")) {
            Toast toast = Toast.makeText(this, "A real apikey must be provided", Toast.LENGTH_SHORT);
            toast.show();
        }

        registerToLinkedin();
    }

    private void setupViews() {
        setContentView(R.layout.linkedin);
        mAuthStatus = (TextView) findViewById(R.id.LinkedinAuthStatus);
        mLinkedinHeadLine = (TextView) findViewById(R.id.LinkedinHeadline);
        mGetHeadlineButtn = (Button) findViewById(R.id.LinkedinUpdateHeadlineButton);
    }

    private void registerToLinkedin() {
        final StorableServiceBuilder builder = new StorableServiceBuilder("Linkedin")
                .provider(LinkedInApi.class)
                .apiKey(mApiKey)
                .apiSecret(mApiSecret)
                .callback("www.mycallback.com", "auth_verifier");

        OAuthHelper o = OAuthHelper.getInstance();
        o.registerOAuthService(builder, this);

        if (!o.isAlreadyAuthenticated("Linkedin", this)) {
            mAuthStatus.setText("Authenticating..");
            o.authenticate(this, "Linkedin");
        } else {
            mAuthStatus.setText("Authenticated");
            enableButtons();
        }
    }

    private void enableButtons() {
        mGetHeadlineButtn.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        mServer.unregisterEventListener(this, this);
        OAuthHelper.getInstance().unregisterListener();
        super.onPause();
    }

    @Override
    protected void onResume() {
        OAuthHelper.getInstance().registerListener(this);

        mServer.registerEventListener(this, this);
        if (mServer.isRequestAlreadyPending(REQUEST_CURRENT_USER_DETAILS)) {
            setUpdating();
        }
        super.onResume();
    }

    @Override
    public void onServerResult(String result, String requestId) {
        if (requestId.equals(REQUEST_CURRENT_USER_DETAILS)) {
            mLinkedinHeadLine.setText(StoreUtils.getLinkedinUserDetails(this));
            updateDone();
        }
    }

    @Override
    public void onServerError(String result, String requestId) {
        updateDone();
    }

    @Override
    public void onServiceAuthenticated(String serviceName) {
        mAuthStatus.setText("Authenticated");
        Toast toast = Toast.makeText(this, String.format("%s authenticated", serviceName), Toast.LENGTH_SHORT);
        toast.show();
        enableButtons();
    }

    @Override
    public void onServiceAuthenticationFailed(String serviceName, String reason) {
        mAuthStatus.setText("Authentication failed");
        Toast toast = Toast.makeText(this, String.format("Failed to authenticate %s: %s", serviceName, reason), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.LinkedinUpdateHeadlineButton:
                LinkedinGetCurrentUserRequest statusStrategy = new LinkedinGetCurrentUserRequest();
                try {
                    mServer.sendRestAction(this, REQUEST_CURRENT_USER_DETAILS, statusStrategy);
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