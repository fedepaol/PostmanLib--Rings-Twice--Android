package com.whiterabbit.postman.oauth;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 1/14/13
 * Time: 9:06 PM
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
