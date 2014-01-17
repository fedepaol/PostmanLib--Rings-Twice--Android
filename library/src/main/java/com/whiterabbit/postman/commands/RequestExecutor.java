package com.whiterabbit.postman.commands;

import android.content.Context;
import com.whiterabbit.postman.exceptions.PostmanException;

/**
 * Utility interface to be implemented in order to execute requests
 */
public interface RequestExecutor {
    /**
     * Tries to execute the given request
     *
     * @param s the strategy to be executed
     * @param c
     * @throws PostmanException
     */
    void executeRequest(RestServerRequest s, Context c) throws PostmanException;

}
