package com.yalin.style.exception;

import android.content.Context;

import com.yalin.style.R;
import com.yalin.style.data.exception.NetworkConnectionException;
import com.yalin.style.data.exception.ReswitchException;

import java.net.SocketTimeoutException;

/**
 * @author jinyalin
 * @since 2017/4/29.
 */

public class ErrorMessageFactory {
    private ErrorMessageFactory() {
        //empty
    }

    /**
     * Creates a String representing an error message.
     *
     * @param context   Context needed to retrieve string resources.
     * @param exception An exception used as a condition to retrieve the correct error message.
     * @return {@link String} an error message.
     */
    public static String create(Context context, Exception exception) {
        String message = context.getString(R.string.exception_message_generic);

        if (exception instanceof NetworkConnectionException) {
            message = context.getString(R.string.exception_message_no_connection);
        } else if (exception instanceof ReswitchException) {
            message = context.getString(R.string.exception_message_resync);
        } else if (exception instanceof SocketTimeoutException) {
            message = context.getString(R.string.exception_message_remote_service);
        }

        return message;
    }
}
