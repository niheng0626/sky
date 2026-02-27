package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

// 自动填充注解
@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface AutoFill {
    OperationType value();
}
