package com.whiterabbit.postman.oauth;

/**
 * Callback interface for asynchronous notifications of
 * oauth result
 */
interface OAuthReceivedInterface {
    public void onAuthReceived(String url);
    public void onAuthFailed(String reason);

}
