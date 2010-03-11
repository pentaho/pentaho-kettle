package org.pentaho.di.ui.spoon;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Xul-based Spoon plugin. Implementations can modify the look of Spoon, register a 
 * SpoonLifecycleListener and add a SpoonPerspective.
 * 
 * @author nbaker
 *
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SpoonPlugin {

  String id();
  
  String name() default "";

  String description() default "";

  String image();

  String version() default "";
  
  int category() default -1;
  
  String categoryDescription() default "";
  
  String i18nPackageName() default "";
  
}
