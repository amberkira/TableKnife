package com.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sleepeanuty on 2017/11/6.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface NotNullAndRex {
    int value();
    String rex();
}
