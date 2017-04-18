package com.yalin.style.domain.exception;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class DefaultErrorBundle implements ErrorBundle {
    private static final String DEFAULT_ERROR_MSG = "Unknown error";

    private final Exception exception;

    public DefaultErrorBundle(Exception exception) {
        this.exception = exception;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    @Override
    public String getErrorMessage() {
        return (exception != null) ? this.exception.getMessage() : DEFAULT_ERROR_MSG;
    }
}
