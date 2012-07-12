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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.hadoop.shim.spi.HadoopShim;
import org.pentaho.hadoop.shim.spi.MockHadoopShim;

public class HadoopConfigurationLocatorTest {

  private static String HADOOP_CONFIGURATIONS_PATH = System.getProperty("java.io.tmpdir") + "/hadoop-configurations";

  @BeforeClass
  public static void setup() throws Exception {
    // Create a test hadoop configuration "a"
    FileObject ramRoot = VFS.getManager().resolveFile(HADOOP_CONFIGURATIONS_PATH);
    FileObject aConfigFolder = ramRoot.resolveFile("a");
    if (aConfigFolder.exists()) {
      aConfigFolder.delete(new AllFileSelector());
    }
    aConfigFolder.createFolder();
    
    assertEquals(FileType.FOLDER, aConfigFolder.getType());

    // Create the properties file for the configuration as hadoop-configurations/a/config.properties
    FileObject configFile = aConfigFolder.resolveFile("config.properties");
    Properties p = new Properties();
    p.setProperty("name", "Test Configuration A");
    p.setProperty("classpath", "");
    p.setProperty("library.path", "");
    p.store(configFile.getContent().getOutputStream(), "Test Configuration A");

    // Create the implementation jar
    FileObject implJar = aConfigFolder.resolveFile("a-config.jar");
    implJar.createFile();

    // Use ShrinkWrap to create the jar and write it out to VFS
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "a-configuration.jar").addAsServiceProvider(
        HadoopShim.class, MockHadoopShim.class)
        .addClass(MockHadoopShim.class);        
    archive.as(ZipExporter.class).exportTo(implJar.getContent().getOutputStream());
  }

  @Test(expected = ConfigurationException.class)
  public void init_invalidDirectory() throws FileSystemException, ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.init(VFS.getManager().resolveFile("ram://bogus-path"), new MockActiveHadoopConfigurationLocator(),
        new DefaultFileSystemManager());
  }

  @Test(expected = NullPointerException.class)
  public void init_null_basedir() throws FileSystemException, ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.init(null, new MockActiveHadoopConfigurationLocator(),
        new DefaultFileSystemManager());
  }

  @Test(expected = NullPointerException.class)
  public void init_null_activeLocator() throws FileSystemException, ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.init(VFS.getManager().resolveFile(HADOOP_CONFIGURATIONS_PATH), null,
        new DefaultFileSystemManager());
  }
  
  @Test(expected = NullPointerException.class)
  public void init_null_fsm() throws FileSystemException, ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.init(VFS.getManager().resolveFile(HADOOP_CONFIGURATIONS_PATH), new MockActiveHadoopConfigurationLocator(),
        null);
  }

  @Test
  public void init() throws FileSystemException, ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.init(VFS.getManager().resolveFile(HADOOP_CONFIGURATIONS_PATH), new MockActiveHadoopConfigurationLocator(),
        new DefaultFileSystemManager());

    assertEquals(1, locator.getConfigurations().size());
  }

  @Test(expected = RuntimeException.class)
  public void hasConfiguration_not_intialized() {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.hasConfiguration(null);
  }

  @Test(expected = RuntimeException.class)
  public void getConfiguration_not_intialized() throws ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.getConfiguration(null);
  }

  @Test(expected = RuntimeException.class)
  public void getConfigurations_not_intialized() {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.getConfigurations();
  }

  @Test(expected = RuntimeException.class)
  public void getActiveConfiguration_not_intialized() throws ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.getActiveConfiguration();
  }

  @Test(expected = NullPointerException.class)
  public void registerNativeLibraryPath_null_path() throws SecurityException, NoSuchFieldException,
      IllegalArgumentException, IllegalAccessException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.registerNativeLibraryPath(null);
  }

  @Test
  public void registerNativeLibraryPaths() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
      IllegalAccessException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.registerNativeLibraryPaths("test,ing");

    Field f = ClassLoader.class.getDeclaredField("usr_paths");
    boolean accessible = f.isAccessible();
    f.setAccessible(true);
    try {
      String[] usrPaths = (String[]) f.get(null);
      assertEquals("test", usrPaths[usrPaths.length - 2]);
      assertEquals("ing", usrPaths[usrPaths.length - 1]);
    } finally {
      f.setAccessible(accessible);
    }
  }

  @Test
  public void registerNativeLibraryPaths_no_duplicates() throws SecurityException, NoSuchFieldException,
      IllegalArgumentException, IllegalAccessException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.registerNativeLibraryPaths("test,ing");
    locator.registerNativeLibraryPaths("ing");

    Field f = ClassLoader.class.getDeclaredField("usr_paths");
    boolean accessible = f.isAccessible();
    f.setAccessible(true);
    try {
      String[] usrPaths = (String[]) f.get(null);
      assertEquals("test", usrPaths[usrPaths.length - 2]);
      assertEquals("ing", usrPaths[usrPaths.length - 1]);
    } finally {
      f.setAccessible(accessible);
    }
  }

  @Test(expected = ConfigurationException.class)
  public void createConfigurationLoader_null_root() throws ConfigurationException {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    locator.createConfigurationLoader(null, null, null);
  }

  @Test(expected = ConfigurationException.class)
  public void createConfigurationLoader_root_not_a_folder() throws Exception {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();
    // Try to create a configuration based on a file, not a folder
    FileObject buildProperties = VFS.getManager().resolveFile("ram:///test.file");
    buildProperties.createFile();
    assertEquals(FileType.FILE, buildProperties.getType());
    locator.createConfigurationLoader(buildProperties, null, null);
  }

  @Test
  public void createConfigurationLoader() throws Exception {
    HadoopConfigurationLocator locator = new HadoopConfigurationLocator();

    FileObject root = VFS.getManager().resolveFile(HADOOP_CONFIGURATIONS_PATH + "/a");
    ClassLoader cl = locator.createConfigurationLoader(root, getClass().getClassLoader(), null);
    
    assertNotNull(cl.getResource("config.properties"));
  }
}
