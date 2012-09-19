/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.hadoop.shim.cdh4;

import java.util.concurrent.Callable;

import org.pentaho.hadoop.shim.HadoopConfigurationClassLoader;
import org.pentaho.hadoop.shim.api.Configuration;
import org.pentaho.hadoop.shim.common.CommonSqoopShim;

/**
 * Sqoop shim to handle working around Sqoop's ConfigurationManager
 * and how it builds up the class path when compiling temporary files.
 * 
 * <p>
 * This applies to any version of Hadoop that bundles classes in separate 
 * jars instead of the conglomerate "core" jar.
 * </p>
 * 
 */
public class ClassPathModifyingSqoopShim extends CommonSqoopShim {
  private static final String PROPERTY_JAVA_CLASS_PATH = "java.class.path";

  /**
   * Run a given {@link Callable} within a code block that sets the 
   * {@code "java.class.path"} property to the path defined for the {@link HadoopConfigurationClassLoader}
   * used to load this class, if it was used. If not, this is the same as calling {@code callable.call()}.
   * @param callable Callable to execute with a modified class path set.
   * @return
   */
  public int runWithModifiedClassPathProperty(Callable<Integer> callable) {
    /**
     * WARNING: This is non-thread safe. This is only to work around the 
     * deficiencies of the Sqoop CompilationManager (as of 1.4.1) as it will 
     * only look for the Hadoop "core" jar. For CDH4 the necessary classes are contained within a 
     * "common" jar.
     */
    String newClassPath = getClassPathString();
    String originalClassPath = System.getProperty(PROPERTY_JAVA_CLASS_PATH);
    if (newClassPath != null) {
      System.setProperty(PROPERTY_JAVA_CLASS_PATH, newClassPath);
    }
    try {
      Integer returnVal = callable.call();
      return returnVal == null ? Integer.MIN_VALUE : returnVal.intValue();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      if (originalClassPath != null) {
        System.setProperty(PROPERTY_JAVA_CLASS_PATH, originalClassPath);
      }
    }
  }

  @Override
  public int runTool(final String[] args, final Configuration c) {
    return runWithModifiedClassPathProperty(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return ClassPathModifyingSqoopShim.super.runTool(args, c);
      }
    });
  }

  /**
   * @return the class path to set for each run of {@link #runWithModifiedClassPathProperty(Callable)}. 
   */
  protected String getClassPathString() {
    ClassLoader cl = getClass().getClassLoader();
    if (HadoopConfigurationClassLoader.class.isAssignableFrom(cl.getClass())) {
      HadoopConfigurationClassLoader hccl = (HadoopConfigurationClassLoader) cl;
      return hccl.generateClassPathString();
    }
    return null;
  }
}
