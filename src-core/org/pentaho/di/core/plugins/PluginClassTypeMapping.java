/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
public @interface PluginClassTypeMapping {
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

}
