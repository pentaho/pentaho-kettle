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

package org.apache.hadoop.hive.jdbc;

import java.lang.reflect.Method;

/**
 * A utility class for locating the Big Data Plugin's {@link HadoopConfigurationRegistry}.
 * 
 */
public class HadoopConfigurationUtil {
  private static final String HADOOP_CONFIGURATION_BOOTSTRAP = "HadoopConfigurationBootstrap";

  private static final String CLASS_PLUGIN_REGISTRY = "org.pentaho.di.core.plugins.PluginRegistry";

  private static final String CLASS_PLUGIN_INTERFACE = "org.pentaho.di.core.plugins.PluginInterface";

  private static final String CLASS_KETTLE_LIFECYCLE_PLUGIN_TYPE = "org.pentaho.di.core.plugins.KettleLifecyclePluginType";

  private static final String CLASS_HADOOP_CONFIGURATION_REGISTRY = "org.pentaho.di.core.hadoop.HadoopConfigurationRegistry";

  private static final String CLASS_HADOOP_CONFIGURATION = "org.pentaho.hadoop.shim.HadoopConfiguration";

  private static Object hadoopConfigurationRegistry;

  // public HadoopConfiguration HadoopConfigurationRegistry#getActiveConfiguration()
  private static Method getActiveConfiguration;

  // public HadoopShim HadoopConfiguration#getHadoopShim()
  private static Method getHadoopShim;

  protected static ClassLoader findBigDataPluginClassLoader() throws Exception {
    Method findPluginById = null;
    Object pluginRegistry = null;
    Method getClassLoader = null;
    try {
      Class<?> pluginRegistryClass = Class.forName(CLASS_PLUGIN_REGISTRY);
      Class<?> pluginInterfaceClass = Class.forName(CLASS_PLUGIN_INTERFACE);
      Method getInstance = pluginRegistryClass.getMethod("getInstance");
      getClassLoader = pluginRegistryClass.getMethod("getClassLoader", pluginInterfaceClass);
      findPluginById = pluginRegistryClass.getMethod("findPluginWithId", Class.class, String.class);
      pluginRegistry = getInstance.invoke(pluginRegistryClass);
    } catch (Exception ex) {
      throw new Exception("Unable to locate Kettle Plugin registry", ex);
    }

    try {
      Class<?> kettleLifecyclePluginTypeClass = Class.forName(CLASS_KETTLE_LIFECYCLE_PLUGIN_TYPE);
      Object hadoopConfigurationBootstrap = findPluginById.invoke(pluginRegistry, kettleLifecyclePluginTypeClass,
          HADOOP_CONFIGURATION_BOOTSTRAP);
      return (ClassLoader) getClassLoader.invoke(pluginRegistry, hadoopConfigurationBootstrap);
    } catch (Exception ex) {
      throw new Exception("Unable to locate Big Data Plugin", ex);
    }
  }

  private static Object findHadoopConfigurationRegistry() throws Exception {
    try {
      ClassLoader bigDataPluginCL = findBigDataPluginClassLoader();
      Class<?> registryClass = Class.forName(CLASS_HADOOP_CONFIGURATION_REGISTRY, true, bigDataPluginCL);
      getActiveConfiguration = registryClass.getMethod("getActiveConfiguration");
      getHadoopShim = Class.forName(CLASS_HADOOP_CONFIGURATION, true, bigDataPluginCL).getMethod("getHadoopShim");
      Method getInstance = registryClass.getMethod("getInstance");
      return getInstance.invoke(registryClass);
    } catch (Exception ex) {
      throw new Exception("Unable to locate Hadoop Configuration Registry", ex);
    }
  }

  private synchronized static Object getRegistry() throws Exception {
    if (hadoopConfigurationRegistry == null) {
      hadoopConfigurationRegistry = findHadoopConfigurationRegistry();
    }
    return hadoopConfigurationRegistry;
  }

  /**
   * Look up the active Hadoop configuration ({@link org.pentaho.hadoop.shim.HadoopConfiguration}).
   * 
   * @return The active Hadoop configuration exception
   * @throws Exception If the Hadoop configuration could not be retrieved. This will likely be a {@link org.pentaho.hadoop.shim.ConfigurationException}.
   */
  public static Object getActiveConfiguration() throws Exception {
    Object registry = getRegistry();
    return getActiveConfiguration.invoke(registry);
  }

  /**
   * Look up the active Hadoop Configuration's {@link HadoopShim}.
   * 
   * @return The {@link HadoopShim} for the active Hadoop Configuration
   * @throws Exception If the active Hadoop configuraiton could not be retrieved.
   */
  public static Object getActiveHadoopShim() throws Exception {
    Object activeConfig = getActiveConfiguration();
    return getHadoopShim.invoke(activeConfig);
  }
}
