package org.pentaho.di.core.plugins;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation signals to the plugin system that the class is a database metadata plugin.
 * 
 * @author matt
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DatabaseMetaPlugin {
	String type();
	String typeDescription();
}
