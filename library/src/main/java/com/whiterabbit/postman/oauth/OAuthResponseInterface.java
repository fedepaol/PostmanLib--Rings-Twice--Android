package com.whiterabbit.postman.oauth;

/**
 * Listener interface to be used to get notifications of
 * oauth process result
 */
public interface OAuthResponseInterface {
   /**
     * Listener interface to get notified of the end of an oauth authentication process
     * @param serviceName
     */
    public void onServiceAuthenticated(String serviceName);

    /**
     * Listener interface to get notified of the failure of an oauth authentication process
     * @param serviceName
     * @param reason
     */
    public void onServiceAuthenticationFailed(String serviceName, String reason);
}
