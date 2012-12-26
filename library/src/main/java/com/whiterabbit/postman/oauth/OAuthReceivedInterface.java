package com.whiterabbit.postman.oauth;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/25/12
 * Time: 8:11 PM
 */
public interface OAuthReceivedInterface {
    public void onAuthReceived(String url);
    public void onAuthFailed(String reason);

}
