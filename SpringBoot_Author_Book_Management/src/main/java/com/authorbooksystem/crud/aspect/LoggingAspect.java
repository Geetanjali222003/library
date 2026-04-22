package com.authorbooksystem.crud.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Pointcut targeting all methods in service layer
    @Before("execution(* com.authorbooksystem.crud.service.*.*(..))")
    public void logMethodEntry(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("Entering method: {}.{}", className, methodName);
    }

    // Log successful method execution
    @AfterReturning("execution(* com.authorbooksystem.crud.service.*.*(..))")
    public void logMethodExit(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("Successfully executed method: {}.{}", className, methodName);
    }

    // Log method execution when exception occurs
    @AfterThrowing(pointcut = "execution(* com.authorbooksystem.crud.service.*.*(..))", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.error("Exception in method: {}.{} - Error: {}", 
                    className, methodName, exception.getMessage());
    }
}
