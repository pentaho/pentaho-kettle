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

package org.pentaho.di.job.entries.hadoopjobexecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.BeforeClass;
import org.junit.Test;

public class JarUtilityTest {

  private static final String MANIFEST_FILE_NAME = "MANIFEST.MF";

  private static String TEST_PATH = System.getProperty("java.io.tmpdir") + "/" + JarUtilityTest.class.getSimpleName();

  public static void main(String[] args) {
    System.out.println("Test main(). This does nothing. Use JUnit to execute tests on this class.");
  }

  @BeforeClass
  public static void setup() throws Exception {
    FileObject testPath = VFS.getManager().resolveFile(TEST_PATH);
    testPath.delete(new AllFileSelector());
    testPath.createFolder();
  }

  /**
   * Create a Java archive with the provided class declared as the main class in
   * its manifest.
   * 
   * @param name Name of archive
   * @param mainClass Class to use as the main class
   * @return A Java archive with the class packaged within and manifest configured to use it as the main class.
   */
  private JavaArchive createTestJarWithMainClass(String name, Class<?> mainClass) {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar");
    StringAsset manifest = new StringAsset("Main-Class: " + getClass().getName() + "\n");
    archive.addAsManifestResource(manifest, MANIFEST_FILE_NAME);
    archive.addClass(getClass());
    return archive;
  }

  @Test(expected = NullPointerException.class)
  public void getClassesInJarWithMain_null_URL() throws Exception {
    JarUtility util = new JarUtility();
    util.getMainClassFromManifest(null, getClass().getClassLoader());
  }

  @Test(expected = NullPointerException.class)
  public void getClassesInJarWithMain_null_classloader() throws Exception {
    JarUtility util = new JarUtility();
    util.getMainClassFromManifest(new File(TEST_PATH).toURI().toURL(), null);
  }

  @Test(expected = IOException.class)
  public void getClassesInJarWithMain_bad_file() throws Exception {
    JarUtility util = new JarUtility();
    File emptyFile = new File(TEST_PATH, "empty.jar");
    emptyFile.createNewFile();
    URL url = emptyFile.toURI().toURL();
    util.getMainClassFromManifest(url, getClass().getClassLoader());
  }

  @Test(expected = IOException.class)
  public void getClassesInJarWithMain_bad_URL() throws Exception {
    JarUtility util = new JarUtility();
    URL url = new URL("file://Bogus URL");
    util.getMainClassFromManifest(url, getClass().getClassLoader());
  }

  @Test
  public void getClassesInJarWithMain() throws Exception {
    JarUtility util = new JarUtility();
    JavaArchive archive = createTestJarWithMainClass("test.jar", getClass());
    File exportFile = new File(TEST_PATH, archive.getName());
    archive.as(ZipExporter.class).exportTo(exportFile);

    assertTrue(exportFile.exists());

    Class<?> mainClass = util.getMainClassFromManifest(exportFile.toURI().toURL(), getClass().getClassLoader());
    assertEquals(getClass(), mainClass);
  }

  @Test
  public void getClassesInJarWithMain_noMainClass() throws Exception {
    JarUtility util = new JarUtility();
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
    archive.addClass(getClass());
    File exportFile = new File(TEST_PATH, archive.getName());
    archive.as(ZipExporter.class).exportTo(exportFile);

    assertTrue(exportFile.exists());

    Class<?> mainClass = util.getMainClassFromManifest(exportFile.toURI().toURL(), getClass().getClassLoader());
    assertNull(mainClass);
  }

}
