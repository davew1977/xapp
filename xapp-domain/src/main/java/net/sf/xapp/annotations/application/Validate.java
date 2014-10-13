package net.sf.xapp.annotations.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Validate {
    String regexp() default "";
    String errorMsg() default "";
}
