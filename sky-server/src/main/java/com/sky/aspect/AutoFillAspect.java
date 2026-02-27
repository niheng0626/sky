package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

//自定义切面 拦截加了AutoFill注解的方法
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //拦截加了AutoFill注解的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut(){}

    //实现自动填充功能
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行数据填充");
        //获取数据库操作类型
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);//获取方法上的数据库操作类型
        OperationType operationType = autoFill.value();//获取数据库操作类型

        //获取方法参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            return;
        }
        Object object = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据对应的数据库操作类型，为对应的属性赋值
        if (operationType == OperationType.INSERT) {
            //赋值创建时间、更新时间、创建人、更新人
            try {
                Field createTimeField = object.getClass().getDeclaredField("createTime");
                createTimeField.setAccessible(true);
                createTimeField.set(object, now);

                Field updateTimeField = object.getClass().getDeclaredField("updateTime");
                updateTimeField.setAccessible(true);
                updateTimeField.set(object, now);

                Field createUserField = object.getClass().getDeclaredField("createUser");
                createUserField.setAccessible(true);
                createUserField.set(object, currentId);

                Field updateUserField = object.getClass().getDeclaredField("updateUser");
                updateUserField.setAccessible(true);
                updateUserField.set(object, currentId);
            } catch (Exception e) {
                log.error("自动填充失败", e);
            }

        }
        else if (operationType == OperationType.UPDATE) {
            try {
                Field updateTimeField = object.getClass().getDeclaredField("updateTime");
                updateTimeField.setAccessible(true);
                updateTimeField.set(object, now);

                Field updateUserField = object.getClass().getDeclaredField("updateUser");
                updateUserField.setAccessible(true);
                updateUserField.set(object, currentId);
            } catch (Exception e) {
                log.error("自动填充失败", e);
            }
        }
    }
}
