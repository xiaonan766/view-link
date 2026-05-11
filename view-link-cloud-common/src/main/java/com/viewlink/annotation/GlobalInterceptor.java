package com.viewlink.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//作用在方法上
@Retention(RetentionPolicy.RUNTIME)//指定自定义注解的保留策略
public @interface GlobalInterceptor {
    //在注解里，属性是以抽象方法的形式来定义的
    boolean checkLogin() default false;////定义属性checkLogin，默认为false
}
