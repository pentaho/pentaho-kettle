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
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.JobEntryPluginType;
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
 * Provides an anchor point for all Hadoop Configuration-related lookups to happen.
 * This is a singleton so external code can find a reference to it via reflection.
 * <p>
 * This should be removed in favor of hanging a {@link HadoopConfigurationLocator}
 * off the HadoopSpoonPlugin instead of having an entire wrapper facade now that
 * we have {@link org.pentaho.di.core.lifecycle.LifecycleListener#onEnvironmentInit()}.
 * </p>
 */
public class HadoopConfigurationRegistry implements HadoopConfigurationProvider, ActiveHadoopConfigurationLocator {
  private static Class<?> PKG = HadoopConfigurationRegistry.class;

  private static HadoopConfigurationRegistry instance;

  private HadoopConfigurationProvider provider;

  private static LogChannelInterface log = new LogChannel(BaseMessages.getString(PKG,
      "HadoopConfigurationRegistry.LoggingPrefix"));

  private HadoopConfigurationRegistry() {
  }

  public static void setHadoopConfigurationProvider(HadoopConfigurationProvider provider) {
    instance = new HadoopConfigurationRegistry();
    instance.provider = provider;
  }

  public static HadoopConfigurationRegistry getInstance() throws ConfigurationException {
    if (instance == null) {
      FileObject pluginDirectory = null;
      HadoopConfigurationRegistry registry = new HadoopConfigurationRegistry();
      try {
        pluginDirectory = registry.locatePluginDirectory();
      } catch (Exception ex) {
        throw new ConfigurationException(BaseMessages.getString(PKG,
            "HadoopConfigurationRegistry.PluginDirectoryNotFound"));
      }
      try {
        registry.initialize(log, pluginDirectory);
      } catch (Exception ex) {
        log.logError(BaseMessages.getString(PKG, "HadoopConfigurationRegistry.HadoopConfiguration.StartupError"));
      }
      instance = registry;
    }
    return instance;
  }

  private PluginInterface getPluginInterface() throws KettleException {
    PluginInterface pi = PluginRegistry.getInstance().findPluginWithId(JobEntryPluginType.class,
        "HadoopJobExecutorPlugin");
    if (pi == null) {
      throw new KettleException("Unable to determine plugin interface");
    }
    return pi;
  }

  /**
   * Retrieves the plugin properties from disk every call. This allows the plugin 
   * properties to change at runtime.
   * @return Properties loaded from "$PLUGIN_DIR/plugin.properties".
   * @throws IOException Error loading properties file
   * @throws KettleException Error locating Big Data plugin 
   */
  private Properties getPluginProperties() throws IOException, KettleException {
    return new PluginPropertiesUtil().loadPluginProperties(getPluginInterface());
  }

  /**
   * Find the location of the big data plugin. This relies on the Hadoop Job
   * Executor job entry existing within the big data plugin.
   * 
   * @return The VFS location of the big data plugin
   * @throws KettleException 
   */
  private FileObject locatePluginDirectory() throws KettleException {
    return KettleVFS.getFileObject(getPluginInterface().getPluginDirectory().getPath());
  }

  public synchronized void initialize(LogChannelInterface log, FileObject pluginDir) throws KettleFileException,
      ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    FileObject hadoopConfBaseDir = KettleVFS.getFileObject(pluginDir.getName().getPath() + "/hadoop-configurations");
    locator.init(hadoopConfBaseDir, this, (DefaultFileSystemManager) KettleVFS.getInstance().getFileSystemManager());
    provider = locator;
    log.logDetailed(BaseMessages.getString(PKG, "HadoopConfigurationRegistry.HadoopConfiguration.Loaded"), provider
        .getConfigurations().size(), hadoopConfBaseDir);
  }

  /**
   * @return The active Hadoop configuration
   * @throws ConfigurationException unknown configuration
   */
  public HadoopConfiguration getActiveConfiguration() throws ConfigurationException {
    return provider.getActiveConfiguration();
  }

  @Override
  public HadoopConfiguration getConfiguration(String id) throws ConfigurationException {
    if (provider == null) {
      throw new ConfigurationException("Hadoop Configuration Registry is not initialized");
    }
    return provider.getConfiguration(id);
  }

  @Override
  public List<? extends HadoopConfiguration> getConfigurations() {
    if (provider == null) {
      return Collections.emptyList();
    }
    return provider.getConfigurations();
  }

  @Override
  public boolean hasConfiguration(String id) {
    return provider == null ? false : provider.hasConfiguration(id);
  }

  @Override
  public String getActiveConfigurationId() throws ConfigurationException {
    try {
      return getPluginProperties().getProperty("active.hadoop.configuration");
    } catch (Exception ex) {
      throw new ConfigurationException("Error determining active Hadoop configuration", ex);
    }
  }
}