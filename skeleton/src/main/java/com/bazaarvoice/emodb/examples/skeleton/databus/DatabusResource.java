package com.bazaarvoice.emodb.examples.skeleton.databus;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention (RetentionPolicy.RUNTIME)
@Target ({ElementType.FIELD})
@BindingAnnotation
/**
 * This annotation is used to restrict certain bindings of high level Java
 * types (like ExecutorService or ScheduledExecutorService) to a specific area
 */
public @interface DatabusResource {
}