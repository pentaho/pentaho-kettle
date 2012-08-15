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

package org.pentaho.hadoop.shim.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.ram.RamFileProvider;
import org.junit.Test;
import org.pentaho.hadoop.shim.ConfigurationException;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hadoop.shim.HadoopConfigurationFileSystemManager;
import org.pentaho.hadoop.shim.MockHadoopConfigurationProvider;
import org.pentaho.hadoop.shim.spi.HadoopConfigurationProvider;
import org.pentaho.hadoop.shim.spi.MockHadoopShim;

public class HadoopConfigurationFileSystemManagerTest {

  @Test(expected = NullPointerException.class)
  public void constructor_null_check_HadoopConfigurationProvider() {
    new HadoopConfigurationFileSystemManager(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void constructor_null_check_DefaultFileSystemManager() {
    new HadoopConfigurationFileSystemManager(new MockHadoopConfigurationProvider(), null);
  }

  @Test
  public void addProvider() throws Exception {
    String scheme = "scheme";
    DefaultFileSystemManager def = new DefaultFileSystemManager();
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim());
    HadoopConfigurationProvider configProvider = new MockHadoopConfigurationProvider();
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, def);
    FileProvider provider = new MockFileProvider();
    fsm.addProvider(config, scheme, "alias", provider);
    assertNotNull(fsm.getFileProvider(config, scheme));
    assertTrue(fsm.hasProvider(scheme));
    assertTrue(fsm.hasProvider(scheme + "-alias"));
  }

  @Test
  public void getActiveFileProvider() throws Exception {
    String scheme = "scheme";
    DefaultFileSystemManager def = new DefaultFileSystemManager();
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim());
    HadoopConfigurationProvider configProvider = new MockHadoopConfigurationProvider(Arrays.asList(config),
        config.getIdentifier());
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, def);
    FileProvider provider = new MockFileProvider();
    fsm.addProvider(config, scheme, "alias", provider);

    assertEquals(provider, fsm.getActiveFileProvider(scheme));
  }
  
  @Test(expected=FileSystemException.class)
  public void getActiveFileProvider_no_active() throws Exception {
    DefaultFileSystemManager def = new DefaultFileSystemManager();
    HadoopConfigurationProvider configProvider = new MockHadoopConfigurationProvider() {
      public HadoopConfiguration getActiveConfiguration() throws org.pentaho.hadoop.shim.ConfigurationException {
        throw new ConfigurationException("no active config");
      };
    };
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, def);
    
    fsm.getActiveFileProvider("scheme");
  }

  @Test
  public void getActiveFileProvider_multipleProviders() throws Exception {
    String scheme = "scheme";
    DefaultFileSystemManager def = new DefaultFileSystemManager();
    HadoopConfiguration config1 = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "1", "one", new MockHadoopShim());
    HadoopConfiguration config2 = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "2", "two", new MockHadoopShim());
    HadoopConfigurationProvider configProvider = new MockHadoopConfigurationProvider(Arrays.asList(config1, config2),
        config2.getIdentifier());
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, def);
    FileProvider provider1 = new MockFileProvider();
    FileProvider provider2 = new MockFileProvider();
    fsm.addProvider(config1, scheme, "one", provider1);
    fsm.addProvider(config2, scheme, "2", provider2);

    // make sure all providers were registered with their aliases
    assertTrue(fsm.hasProvider(scheme));
    assertTrue(fsm.hasProvider(scheme + "-one"));
    assertTrue(fsm.hasProvider(scheme + "-2"));

    // Make sure the active config's provider is returned when asked
    assertEquals(provider2, fsm.getActiveFileProvider(scheme));
  }

  @Test
  public void multipleProviders_singleConfig() throws Exception {
    String scheme = "scheme";
    String scheme2 = "scheme2";
    DefaultFileSystemManager def = new DefaultFileSystemManager();
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "1", "one", new MockHadoopShim());
    HadoopConfigurationProvider configProvider = new MockHadoopConfigurationProvider(Arrays.asList(config),
        config.getIdentifier());
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, def);
    FileProvider provider1 = new MockFileProvider();
    FileProvider provider2 = new MockFileProvider();
    fsm.addProvider(config, scheme, "one", provider1);
    try {
      fsm.addProvider(config, scheme, "two", provider2);
      fail("Expected exception");
    } catch (FileSystemException ex) {
      assertTrue(ex.getMessage(), ex.getMessage().contains("Scheme already registered: scheme"));
    }
    fsm.addProvider(config, scheme2, "two", provider2);

    // make sure all providers were registered with their aliases
    assertTrue(fsm.hasProvider(scheme));
    assertTrue(fsm.hasProvider(scheme + "-one"));
    assertTrue(fsm.hasProvider(scheme2 + "-two"));

    // Make sure the active config's provider is returned when asked
    assertEquals(provider1, fsm.getActiveFileProvider(scheme));
  }

  @Test(expected = FileSystemException.class)
  public void invalidSchemeForConfig_unregistered_config() throws Exception {
    DefaultFileSystemManager def = new DefaultFileSystemManager();
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim());
    HadoopConfigurationProvider configProvider = new MockHadoopConfigurationProvider();
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, def);
    fsm.getFileProvider(config, "invalid");
  }

  @Test(expected = FileSystemException.class)
  public void invalidSchemeForConfig_unregistered_scheme() throws Exception {
    DefaultFileSystemManager def = new DefaultFileSystemManager();
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim());
    HadoopConfigurationProvider configProvider = new MockHadoopConfigurationProvider();
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, def);
    fsm.addProvider(config, "scheme", "alias", new MockFileProvider());
    fsm.getFileProvider(config, "invalid");
  }
}
