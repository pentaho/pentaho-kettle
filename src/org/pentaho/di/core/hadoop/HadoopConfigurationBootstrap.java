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

package org.pentaho.di.core.hadoop;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hadoop.PluginPropertiesUtil;
import org.pentaho.hadoop.shim.ConfigurationException;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hadoop.shim.HadoopConfigurationLocator;
import org.pentaho.hadoop.shim.api.ActiveHadoopConfigurationLocator;
import org.pentaho.hadoop.shim.spi.HadoopConfigurationProvider;

/**
 * This class serves to initialize the Hadoop Configuration subsystem. This class
 * provides an anchor point for all Hadoop Configuration-related lookups to happen.
 */
@KettleLifecyclePlugin(id = "HadoopConfigurationBootstrap", name = "Hadoop Configuration Bootstrap")
public class HadoopConfigurationBootstrap implements KettleLifecycleListener, ActiveHadoopConfigurationLocator {
  private static final Class<?> PKG = HadoopConfigurationBootstrap.class;

  public static final String PLUGIN_ID = "HadoopConfigurationBootstrap";

  public static final String PROPERTY_ACTIVE_HADOOP_CONFIGURATION = "active.hadoop.configuration";

  public static final String PROPERTY_HADOOP_CONFIGURATIONS_PATH = "hadoop.configurations.path";

  public static final String DEFAULT_FOLDER_HADOOP_CONFIGURATIONS = "hadoop-configurations";

  private static HadoopConfigurationProvider provider;

  private static LogChannelInterface log = new LogChannel(BaseMessages.getString(PKG,
      "HadoopConfigurationBootstrap.LoggingPrefix"));

  /**
   * Cached plugin description for locating Plugin
   */
  private PluginInterface plugin;

  /**
   * @return A Hadoop configuration provider capable of finding Hadoop configurations loaded for this Big Data Plugin instance
   * @throws ConfigurationException The provider is not initialized (KettleEnvironment.init() has not been called)
   */
  public static synchronized HadoopConfigurationProvider getHadoopConfigurationProvider() throws ConfigurationException {
    if (provider == null) {
      throw new ConfigurationException(BaseMessages.getString(PKG, "HadoopConfigurationBootstrap.NotInitialized"));
    }
    return provider;
  }

  @Override
  public void onEnvironmentInit() throws LifecycleException {
    // Initialize the HadoopConfigurationProvider
    try {
      FileObject hadoopConfigurationsDir = resolveHadoopConfigurationsDirectory();
      HadoopConfigurationProvider p = initializeHadoopConfigurationProvider(hadoopConfigurationsDir);

      // verify the active configuration exists
      HadoopConfiguration activeConfig = null;
      try {
        activeConfig = p.getActiveConfiguration();
      } catch (Exception ex) {
        throw new ConfigurationException(BaseMessages.getString(PKG, "HadoopConfigurationBootstrap.HadoopConfiguration.InvalidActiveConfiguration", getActiveConfigurationId()), ex);
      }
      if (activeConfig == null) {
        throw new ConfigurationException(BaseMessages.getString(PKG, "HadoopConfigurationBootstrap.HadoopConfiguration.InvalidActiveConfiguration", getActiveConfigurationId()));
      }

      synchronized (this) {
        provider = p;
      }
      log.logDetailed(BaseMessages.getString(PKG, "HadoopConfigurationBootstrap.HadoopConfiguration.Loaded"), provider
          .getConfigurations().size(), hadoopConfigurationsDir);
    } catch (Exception ex) {
      throw new LifecycleException(BaseMessages.getString(PKG,
          "HadoopConfigurationBootstrap.HadoopConfiguration.StartupError"), ex, true);
    }
  }

  @Override
  public void onEnvironmentShutdown() {
  }

  /**
   * Initialize the Hadoop configuration provider for the plugin. We're currently
   * relying on a file-based configuration provider: {@link HadoopConfigurationLocator}.
   * 
   * @param hadoopConfigurationsDir
   * @return
   * @throws ConfigurationException
   */
  protected HadoopConfigurationProvider initializeHadoopConfigurationProvider(FileObject hadoopConfigurationsDir)
      throws ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.init(hadoopConfigurationsDir, this, (DefaultFileSystemManager) KettleVFS.getInstance()
        .getFileSystemManager());
    return locator;
  }

  /**
   * Retrieves the plugin properties from disk every call. This allows the plugin 
   * properties to change at runtime.
   * @return Properties loaded from "$PLUGIN_DIR/plugin.properties".
   * @throws ConfigurationException Error loading properties file
   */
  protected Properties getPluginProperties() throws ConfigurationException {
    try {
      return new PluginPropertiesUtil().loadPluginProperties(getPluginInterface());
    } catch (Exception ex) {
      throw new ConfigurationException(BaseMessages.getString(PKG, "HadoopConfigurationBootstrap.UnableToLoadPluginProperties"), ex);
    }
  }

  /**
   * @return the {@link PluginInterface} for ourself.
   * @throws KettleException Unable to locate ourself in the Plugin Registry
   */
  protected PluginInterface getPluginInterface() throws KettleException {
    if (plugin == null) {
      PluginInterface pi = PluginRegistry.getInstance().findPluginWithId(KettleLifecyclePluginType.class, PLUGIN_ID);
      if (pi == null) {
        throw new KettleException(BaseMessages.getString(PKG, "HadoopConfigurationBootstrap.CannotLocatePlugin"));
      }
      plugin = pi;
    }
    return plugin;
  }

  /**
   * Find the location of the big data plugin. This relies on the Hadoop Job
   * Executor job entry existing within the big data plugin.
   * 
   * @return The VFS location of the big data plugin
   * @throws KettleException 
   */
  public FileObject locatePluginDirectory() throws ConfigurationException {
    FileObject dir = null;
    boolean exists = false;
    try {
      dir = KettleVFS.getFileObject(getPluginInterface().getPluginDirectory().toExternalForm());
      exists = dir.exists();
    } catch (Exception e) {
      throw new ConfigurationException(BaseMessages.getString(PKG,
          "HadoopConfigurationBootstrap.PluginDirectoryNotFound"), e);
    }
    if (!exists) {
      throw new ConfigurationException(BaseMessages.getString(PKG,
          "HadoopConfigurationBootstrap.PluginDirectoryNotFound"));
    }
    return dir;
  }

  /**
   * Resolve the directory to look for Hadoop configurations in. This is based on the
   * plugin property {@link #PROPERTY_HADOOP_CONFIGURATIONS_PATH} in the plugin's properties file.
   * 
   * @return Folder to look for Hadoop configurations within
   * @throws ConfigurationException Error locating plugin directory 
   * @throws KettleException Error resolving hadoop configuration's path
   * @throws IOException Error loading plugin properties
   */
  public FileObject resolveHadoopConfigurationsDirectory() throws ConfigurationException, IOException, KettleException {
    String hadoopConfigurationPath = getPluginProperties().getProperty(PROPERTY_HADOOP_CONFIGURATIONS_PATH,
        DEFAULT_FOLDER_HADOOP_CONFIGURATIONS);
    return locatePluginDirectory().resolveFile(hadoopConfigurationPath);
  }

  @Override
  public String getActiveConfigurationId() throws ConfigurationException {
    Properties p = null;
    try {
      p = getPluginProperties();
    } catch (Exception ex) {
      throw new ConfigurationException(BaseMessages.getString(PKG,
          "HadoopConfigurationBootstrap.UnableToDetermineActiveConfiguration"), ex);
    }
    if (!p.containsKey(PROPERTY_ACTIVE_HADOOP_CONFIGURATION)) {
      throw new ConfigurationException(BaseMessages.getString(PKG,
          "HadoopConfigurationBootstrap.MissingActiveConfigurationProperty", PROPERTY_ACTIVE_HADOOP_CONFIGURATION));
    }
    return p.getProperty(PROPERTY_ACTIVE_HADOOP_CONFIGURATION);
  }

}
