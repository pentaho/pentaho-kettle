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

package org.pentaho.hadoop.shim;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.FileProvider;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hadoop.shim.spi.HadoopConfigurationProvider;
/**
 * TODO COmment this guy!
 * @author jordan
 *
 */
public class HadoopConfigurationFileSystemManager {
  
  private static final Class<?> PKG = HadoopConfigurationFileSystemManager.class;
  
  private HadoopConfigurationProvider configProvider;
  private DefaultFileSystemManager delegate;
  
  private Map<String, ActiveHadoopShimFileProvider> providerProxies;
  private Map<HadoopConfiguration, Map<String, FileProvider>> providersByConfiguration;
  
  public HadoopConfigurationFileSystemManager(HadoopConfigurationProvider configProvider, DefaultFileSystemManager delegate) {
    if (configProvider == null || delegate == null) {
      throw new NullPointerException();
    }
    this.configProvider = configProvider;
    this.delegate = delegate;
    providerProxies = new HashMap<String, ActiveHadoopShimFileProvider>();
    providersByConfiguration = new HashMap<HadoopConfiguration, Map<String, FileProvider>>();
  }
  
  /**
   * Register a file provider for a given scheme as well as an alias for it. The
   * alias will be used to register the provider directly under the scheme: 
   * "scheme-alias://..", so it may be directly referenced.
   * 
   * @param scheme Scheme to register the provider under (this will be proxied and referenced via the "active hadoop configuration")
   * @param alias Alias for the provider so a direct reference can be made if desired, "scheme-alias://..." 
   * @param p File provider to register
   * @throws FileSystemException Error registering file provider
   */
  public synchronized void addProvider(HadoopConfiguration config, String scheme, String alias, FileProvider p) throws FileSystemException {
    ActiveHadoopShimFileProvider provider = providerProxies.get(scheme);
    if (provider == null) {
      provider = new ActiveHadoopShimFileProvider(this, scheme);
      providerProxies.put(scheme, provider);
      // Register a proxying provider
      delegate.addProvider(scheme, provider);
    }

    Map<String, FileProvider> providersForConfig = providersByConfiguration.get(config);
    if (providersForConfig == null) {
      providersForConfig = new HashMap<String, FileProvider>();
      providersByConfiguration.put(config, providersForConfig);
    }
    if (providersForConfig.containsKey(scheme)) {
      throw new FileSystemException(BaseMessages.getString(PKG, "Error.SchemeAlreadyRegistered", scheme));
    }
    providersForConfig.put(scheme, p);

    // Register the real provider under the scheme-alias so we can support talking to more than one provider
    // for the same scheme at the same time: scheme-alias://my/file/path
    delegate.addProvider(scheme + "-" + alias, p);
  }

  /**
   * Get the file provider for the scheme registered for the configuration provided
   * 
   * @param config Hadoop configuration to look up scheme by
   * @param scheme URI scheme to look up file provider by
   * @return A File Provider registered for the configuration that supports the provided scheme
   * @throws FileSystemException
   */
  public FileProvider getFileProvider(HadoopConfiguration config, String scheme) throws FileSystemException {
    Map<String, FileProvider> providers = providersByConfiguration.get(config);
    FileProvider p = null;
    if (providers != null) {
      p = providers.get(scheme);
    }
    if (p == null) {
      throw new FileSystemException(BaseMessages.getString(PKG, "Unsupported scheme for Hadoop configuration", config.getName(), scheme));
    }
    return p;
  }

  /**
   * Get the file provider for the active Hadoop configuration that is registered for the scheme provided.
   * 
   * @param scheme URI scheme to look up file provider by
   * @return A File Provider registered for the active Hadoop configuration that supports the provided scheme
   * @throws FileSystemException If the active configuration cannot be determined or the active configuration doesn't support the provided scheme
   */
  public FileProvider getActiveFileProvider(String scheme) throws FileSystemException {
    try {
      return getFileProvider(configProvider.getActiveConfiguration(), scheme);
    } catch (ConfigurationException e) {
      throw new FileSystemException(e);
    }
  }
  
  /**
   * Delegates to the underlying file system manager to determine if the scheme
   * has been registered.
   * 
   * @return {@code true} if the {@code scheme} has been registered and can be used to resolve a file
   * @see DefaultFileSystemManager#hasProvider(String)
   */
  public boolean hasProvider(String scheme) {
    return delegate.hasProvider(scheme);
  }
}
