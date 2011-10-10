package org.pentaho.di.core.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: nbaker
 * Date: 3/14/11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegistryPlugin {

  String id();

  String name();

  String description() default "Registry Plugin";

}

