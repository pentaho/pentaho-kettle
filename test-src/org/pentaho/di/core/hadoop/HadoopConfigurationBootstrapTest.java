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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.hadoop.shim.ConfigurationException;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hadoop.shim.MockHadoopConfigurationProvider;
import org.pentaho.hadoop.shim.spi.HadoopConfigurationProvider;
import org.pentaho.hadoop.shim.spi.MockHadoopShim;

public class HadoopConfigurationBootstrapTest {

  @Test
  public void getActiveConfigurationId_missing_property() {
    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap() {
      protected java.util.Properties getPluginProperties() throws ConfigurationException {
        return new Properties();
      };
    };

    try {
      b.getActiveConfigurationId();
      fail("Expected exception");
    } catch (ConfigurationException ex) {
      assertEquals("Active configuration property is not set in plugin.properties: \""
          + HadoopConfigurationBootstrap.PROPERTY_ACTIVE_HADOOP_CONFIGURATION + "\".", ex.getMessage());
      assertNull(ex.getCause());
    }
  }

  @Test
  public void getActiveConfigurationId_exception_getting_properties() {
    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap() {
      protected java.util.Properties getPluginProperties() throws ConfigurationException {
        throw new NullPointerException();
      };
    };

    try {
      b.getActiveConfigurationId();
      fail("Expected exception");
    } catch (ConfigurationException ex) {
      assertEquals("Unable to determine active Hadoop configuration.", ex.getMessage());
      assertNotNull(ex.getCause());
    }
  }

  @Test
  public void getPluginInterface_not_registered() {
    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap();

    try {
      b.getPluginInterface();
    } catch (KettleException ex) {
      assertEquals("\nError locating plugin. Please make sure the Plugin Registry has been initialized.\n",
          ex.getMessage());
    }
  }

  @Test
  public void getPluginInterface() throws Exception {
    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap();

    PluginMainClassType mainClassTypesAnnotation = KettleLifecyclePluginType.class
        .getAnnotation(PluginMainClassType.class);
    PluginInterface hadoopConfigurationBootstrap = new Plugin(new String[] { "HadoopConfigurationBootstrap" },
        StepPluginType.class, mainClassTypesAnnotation.value(), "", "", "", null, false, false,
        new HashMap<Class<?>, String>(), new ArrayList<String>(), null, null);
    PluginRegistry.getInstance().registerPlugin(KettleLifecyclePluginType.class, hadoopConfigurationBootstrap);
    try {
      PluginInterface retrieved = b.getPluginInterface();
      assertEquals(hadoopConfigurationBootstrap, retrieved);
      b.getPluginInterface();
    } finally {
      // Remove our registered plugin so we don't taint the environment
      Field pluginMapField = PluginRegistry.class.getDeclaredField("pluginMap");
      pluginMapField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<Class<? extends PluginTypeInterface>, List<PluginInterface>> pluginMap = (Map<Class<? extends PluginTypeInterface>, List<PluginInterface>>) pluginMapField
          .get(PluginRegistry.getInstance());
      pluginMap.get(KettleLifecyclePluginType.class).remove(hadoopConfigurationBootstrap);
    }
  }

  @Test
  public void resolveHadoopConfigurationsDirectory() throws Exception {
    final FileObject ramRoot = VFS.getManager().resolveFile("ram://");
    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap() {
      public FileObject locatePluginDirectory() throws ConfigurationException {
        return ramRoot;
      };

      @Override
      protected Properties getPluginProperties() throws ConfigurationException {
        Properties p = new Properties();
        p.setProperty(HadoopConfigurationBootstrap.PROPERTY_HADOOP_CONFIGURATIONS_PATH, "hadoop-configs-go-here");
        return p;
      }
    };

    FileObject hadoopConfigsDir = b.resolveHadoopConfigurationsDirectory();
    assertNotNull(hadoopConfigsDir);

    assertEquals(ramRoot.resolveFile("hadoop-configs-go-here").getURL(), hadoopConfigsDir.getURL());
  }

  @Test(expected = ConfigurationException.class)
  public void getHadoopConfigurationProvider_uninitialized() throws ConfigurationException {
    new HadoopConfigurationBootstrap().getHadoopConfigurationProvider();
  }

  @Test
  public void getHadoopConfigurationProvider() throws Exception {
    final FileObject ramRoot = VFS.getManager().resolveFile("ram://");
    final String CONFIGS_PATH = "hadoop-configs-go-here";
    ramRoot.resolveFile(CONFIGS_PATH).createFolder();

    HadoopConfiguration c = new HadoopConfiguration("test", "test", new MockHadoopShim());
    final HadoopConfigurationProvider provider = new MockHadoopConfigurationProvider(Arrays.asList(c), "test");

    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap() {
      public FileObject locatePluginDirectory() throws ConfigurationException {
        return ramRoot;
      };

      @Override
      protected Properties getPluginProperties() throws ConfigurationException {
        Properties p = new Properties();
        p.setProperty(HadoopConfigurationBootstrap.PROPERTY_HADOOP_CONFIGURATIONS_PATH, CONFIGS_PATH);
        p.setProperty(HadoopConfigurationBootstrap.PROPERTY_ACTIVE_HADOOP_CONFIGURATION, "test");
        return p;
      }

      @Override
      protected HadoopConfigurationProvider initializeHadoopConfigurationProvider(FileObject hadoopConfigurationsDir)
          throws ConfigurationException {
        return provider;
      }
    };

    b.onEnvironmentInit();

    assertEquals(provider, b.getHadoopConfigurationProvider());
  }

  @Test
  public void getHadoopConfigurationProvider_active_invalid() throws Exception {
    final FileObject ramRoot = VFS.getManager().resolveFile("ram://");
    final String CONFIGS_PATH = "hadoop-configs-go-here";
    ramRoot.resolveFile(CONFIGS_PATH).createFolder();

    HadoopConfiguration c = new HadoopConfiguration("test", "test", new MockHadoopShim());
    final HadoopConfigurationProvider provider = new MockHadoopConfigurationProvider(Arrays.asList(c), "invalid");

    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap() {
      public FileObject locatePluginDirectory() throws ConfigurationException {
        return ramRoot;
      };

      @Override
      protected Properties getPluginProperties() throws ConfigurationException {
        Properties p = new Properties();
        p.setProperty(HadoopConfigurationBootstrap.PROPERTY_HADOOP_CONFIGURATIONS_PATH, CONFIGS_PATH);
        p.setProperty(HadoopConfigurationBootstrap.PROPERTY_ACTIVE_HADOOP_CONFIGURATION, "invalid");
        return p;
      }

      @Override
      protected HadoopConfigurationProvider initializeHadoopConfigurationProvider(FileObject hadoopConfigurationsDir)
          throws ConfigurationException {
        return provider;
      }
    };

    try {
      b.onEnvironmentInit();
      fail("Expected exception");
    } catch (LifecycleException ex) {
      assertEquals("Error initializing Hadoop configurations. Aborting initialization.", ex.getMessage());
      assertNotNull(ex.getCause());
      assertEquals("Invalid active Hadoop configuration: \"invalid\".", ex.getCause().getMessage());
    }
  }

  @Test
  public void getHadoopConfigurationProvider_getActiveException() throws Exception {
    final FileObject ramRoot = VFS.getManager().resolveFile("ram://");
    final String CONFIGS_PATH = "hadoop-configs-go-here";
    ramRoot.resolveFile(CONFIGS_PATH).createFolder();

    HadoopConfiguration c = new HadoopConfiguration("test", "test", new MockHadoopShim());
    final HadoopConfigurationProvider provider = new MockHadoopConfigurationProvider(Arrays.asList(c), "test") {
      public HadoopConfiguration getActiveConfiguration() throws ConfigurationException {
        throw new NullPointerException();
      };
    };

    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap() {
      public FileObject locatePluginDirectory() throws ConfigurationException {
        return ramRoot;
      };

      @Override
      protected Properties getPluginProperties() throws ConfigurationException {
        Properties p = new Properties();
        p.setProperty(HadoopConfigurationBootstrap.PROPERTY_HADOOP_CONFIGURATIONS_PATH, CONFIGS_PATH);
        p.setProperty(HadoopConfigurationBootstrap.PROPERTY_ACTIVE_HADOOP_CONFIGURATION, "test");
        return p;
      }

      @Override
      protected HadoopConfigurationProvider initializeHadoopConfigurationProvider(FileObject hadoopConfigurationsDir)
          throws ConfigurationException {
        return provider;
      }
    };

    try {
      b.onEnvironmentInit();
      fail("Expected exception");
    } catch (LifecycleException ex) {
      assertEquals("Error initializing Hadoop configurations. Aborting initialization.", ex.getMessage());
      assertNotNull(ex.getCause());
      assertEquals("Invalid active Hadoop configuration: \"test\".", ex.getCause().getMessage());
    }
  }

  @Test
  public void initializeHadoopConfigurationProvider() throws Exception {
    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap();

    HadoopConfigurationProvider provider = b.initializeHadoopConfigurationProvider(VFS.getManager().resolveFile(
        "ram://"));
    assertNotNull(provider);
  }

  @Test
  public void locatePluginDirectory() throws Exception {
    FileObject ramRoot = VFS.getManager().resolveFile("ram:///");
    final URL folderURL = ramRoot.getURL();
    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap() {
      protected PluginInterface getPluginInterface() throws KettleException {
        return new Plugin(new String[] { "id" }, KettleLifecyclePluginType.class, null, null, null, null, null, false,
            false, null, null, null, folderURL);
      };
    };

    assertEquals(ramRoot.getURL(), b.locatePluginDirectory().getURL());
  }

  @Test
  public void locatePluginDirectory_null_plugin_folder() throws Exception {
    FileObject ramRoot = VFS.getManager().resolveFile("ram:///");
    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap() {
      protected PluginInterface getPluginInterface() throws KettleException {
        return new Plugin(new String[] { "id" }, KettleLifecyclePluginType.class, null, null, null, null, null, false,
            false, null, null, null, null);
      };
    };

    try {
      b.locatePluginDirectory();
      fail("Expected exception");
    } catch (Exception ex) {
      assertEquals("Hadoop configuration directory could not be located. Hadoop functionality will not work.",
          ex.getMessage());
    }
  }

  @Test
  public void locatePluginDirectory_invalid_path() throws Exception {
    FileObject ramRoot = VFS.getManager().resolveFile("ram:///does-not-exist");
    final URL folderURL = ramRoot.getURL();
    HadoopConfigurationBootstrap b = new HadoopConfigurationBootstrap() {
      protected PluginInterface getPluginInterface() throws KettleException {
        return new Plugin(new String[] { "id" }, KettleLifecyclePluginType.class, null, null, null, null, null, false,
            false, null, null, null, folderURL);
      };
    };

    try {
      b.locatePluginDirectory();
      fail("Expected exception");
    } catch (Exception ex) {
      assertEquals("Hadoop configuration directory could not be located. Hadoop functionality will not work.",
          ex.getMessage());
    }
  }
}
