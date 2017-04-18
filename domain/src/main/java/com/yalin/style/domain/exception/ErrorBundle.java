package com.yalin.style.domain.exception;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public interface ErrorBundle {
    Exception getException();

    String getErrorMessage();
}
