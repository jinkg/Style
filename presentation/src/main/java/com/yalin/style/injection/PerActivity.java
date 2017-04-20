package com.yalin.style.injection;

import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */
@Scope
@Retention(RUNTIME)
public @interface PerActivity {
}
