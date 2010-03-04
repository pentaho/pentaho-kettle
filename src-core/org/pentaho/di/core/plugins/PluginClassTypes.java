package org.pentaho.di.core.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation describes extra plugin-specific class types to be managed by the registry.
 * <p>The type, implementation and nodeName arrays are correlated 1-to-1.
 * @author nbaker
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginClassTypes {
  /**
   * Returns an array of class types that the PluginType will track and respond to. 
   * these classes are ususaly interfaces and the implementation class needs to decend from them
   * @return array of class types
   */
  Class<?>[] classTypes();
  
  /**
   * Returns as array of implementations that correspond to the class types in the Annotation 
   * @return
   */
  Class<?>[] implementationClass() default {};

  /**
   * As an alternative to Implementation Classes, xml node names can be added that will be scanned 
   * when processing an XML-based plugin. 
   * @return
   */
  String[] xmlNodeNames() default {};
}
