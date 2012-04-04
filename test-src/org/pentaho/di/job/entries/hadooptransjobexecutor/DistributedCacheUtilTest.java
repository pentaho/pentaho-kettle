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

package org.pentaho.di.job.entries.hadooptransjobexecutor;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hdfs.vfs.HDFSFileSystem;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Test the DistributedCacheUtil
 */
public class DistributedCacheUtilTest {

  private FileObject createTestFolderWithContent() throws Exception {
    return createTestFolderWithContent("sample-folder");
  }

  private FileObject createTestFolderWithContent(String rootFolderName) throws Exception {
    String rootName = "bin/test/" + rootFolderName;
    FileObject root = KettleVFS.getFileObject(rootName);
    FileObject jar1 = KettleVFS.getFileObject(rootName + Const.FILE_SEPARATOR + "jar1.jar");
    FileObject jar2 = KettleVFS.getFileObject(rootName + Const.FILE_SEPARATOR + "jar2.jar");
    FileObject folder = KettleVFS.getFileObject(rootName + Const.FILE_SEPARATOR + "folder");
    FileObject file = KettleVFS.getFileObject(rootName + Const.FILE_SEPARATOR + "folder" + Const.FILE_SEPARATOR + "file.txt");

    root.createFolder();
    folder.createFolder();
    jar1.createFile();
    jar2.createFile();
    file.createFile();

    return root;
  }

  @Test
  public void deleteDirectory() throws Exception {
    FileObject test = KettleVFS.getFileObject("bin/test/deleteDirectoryTest");
    test.createFolder();

    DistributedCacheUtil ch = new DistributedCacheUtil();
    ch.deleteDirectory(test);
    try {
      assertFalse(test.exists());
    } finally {
      // Delete the directory with java.io.File if it wasn't removed
      File f = new File("bin/test/deleteDirectoryTest");
      if (f.exists() && !f.delete()) {
        throw new IOException("unable to delete test directory: " + f.getAbsolutePath());
      }
    }
  }

  @Test
  public void extract_invalid_archive() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    try {
      ch.extract(KettleVFS.getFileObject("bogus"), null);
      fail("expected exception");
    } catch (IllegalArgumentException ex) {
      assertTrue(ex.getMessage().startsWith("archive does not exist"));
    }
  }

  @Test
  public void extract_destination_exists() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    FileObject archive = KettleVFS.getFileObject("test-res/pentaho-mapreduce-sample.jar");

    try {
      ch.extract(archive, KettleVFS.getFileObject("."));
    } catch (IllegalArgumentException ex) {
      assertTrue("destination already exists".equals(ex.getMessage()));
    }
  }

  @Test
  public void extractToTemp() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    FileObject archive = KettleVFS.getFileObject("test-res/pentaho-mapreduce-sample.jar");
    FileObject extracted = ch.extractToTemp(archive);

    assertNotNull(extracted);
    assertTrue(extracted.exists());
    try {
      // There should be 3 files and 5 directories inside the root folder (which is the 9th entry)
      assertTrue(extracted.findFiles(new AllFileSelector()).length == 9);
    } finally {
      // clean up after ourself
      ch.deleteDirectory(extracted);
    }
  }

  @Test
  public void extractToTemp_missing_archive() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    try {
      ch.extractToTemp(null);
      fail("Expected exception");
    } catch (NullPointerException ex) {
      assertEquals("archive is required", ex.getMessage());
    }
  }

  @Test
  public void findFiles_vfs() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    FileObject testFolder = createTestFolderWithContent();

    try {
      // Simply test we can find the jar files in our test folder
      List<String> jars = ch.findFiles(testFolder, "jar");
      assertEquals(2, jars.size());

      // Look for all files and folders
      List<String> all = ch.findFiles(testFolder, null);
      assertEquals(5, all.size());
    } finally {
      testFolder.delete(new AllFileSelector());
    }
  }

  @Test
  public void findFiles_vfs_hdfs() throws Exception {

    // Stage files then make sure we can find them in HDFS
    DistributedCacheUtil ch = new DistributedCacheUtil();
    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);
    HDFSFileSystem.setMockHDFSFileSystem(fs);

    // Must use absolute paths so the HDFS VFS FileSystem can resolve the URL properly (can't do relative paths when
    // using KettleVFS.getFileObject() within HDFS)
    Path root = new Path(KettleVFS.getFileObject(".").getURL().getPath() + "/bin/test/findFiles_hdfs");
    Path dest = new Path(root, "org/pentaho/mapreduce/");

    FileObject hdfsDest = KettleVFS.getFileObject("hdfs://localhost/" + dest.toString());

    // Copy the contents of test folder
    FileObject source = createTestFolderWithContent();

    try {
      try {
        ch.stageForCache(source, fs, dest, true);

        List<String> files = ch.findFiles(hdfsDest, null);
        assertEquals(5, files.size());
      } finally {
        fs.delete(root, true);
      }
    } finally {
      source.delete(new AllFileSelector());
    }
  }

  @Test
  public void findFiles_hdfs_native() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    // Copy the contents of test folder
    FileObject source = createTestFolderWithContent();
    Path root = new Path("bin/test/stageArchiveForCacheTest");
    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);
    Path dest = new Path(root, "org/pentaho/mapreduce/");
    try {
      try {
        ch.stageForCache(source, fs, dest, true);

        List<Path> files = ch.findFiles(fs, dest, null);
        assertEquals(3, files.size());

        files = ch.findFiles(fs, dest, Pattern.compile(".*jar$"));
        assertEquals(2, files.size());

        files = ch.findFiles(fs, dest, Pattern.compile(".*folder$"));
        assertEquals(1, files.size());
      } finally {
        fs.delete(root, true);
      }
    } finally {
      source.delete(new AllFileSelector());
    }
  }

  /**
   * Utility to attempt to stage a file to HDFS for use with Distributed Cache.
   *
   * @param ch                Distributed Cache Helper
   * @param source            File or directory to stage
   * @param fs                FileSystem to stage to
   * @param root              Root directory to clean up when this test is complete
   * @param dest              Destination path to stage to
   * @param expectedFileCount Expected number of files to exist in the destination once staged
   * @param expectedDirCount  Expected number of directories to exist in the destiation once staged
   * @throws Exception
   */
  private void stageForCacheTester(DistributedCacheUtil ch, FileObject source, FileSystem fs, Path root, Path dest, int expectedFileCount, int expectedDirCount) throws Exception {
    try {
      ch.stageForCache(source, fs, dest, true);

      assertTrue(fs.exists(dest));
      ContentSummary cs = fs.getContentSummary(dest);
      assertEquals(expectedFileCount, cs.getFileCount());
      assertEquals(expectedDirCount, cs.getDirectoryCount());
      assertEquals(FsPermission.createImmutable((short) 0755), fs.getFileStatus(dest).getPermission());
    } finally {
      // Clean up after ourself
      if (!fs.delete(root, true)) {
        System.err.println("error deleting FileSystem temp dir " + root);
      }
    }
  }

  @Test
  public void stageForCache() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    // Copy the contents of test folder
    FileObject source = createTestFolderWithContent();

    try {
      Path root = new Path("bin/test/stageArchiveForCacheTest");
      Path dest = new Path(root, "org/pentaho/mapreduce/");

      Configuration conf = new Configuration();
      org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);

      stageForCacheTester(ch, source, fs, root, dest, 3, 2);
    } finally {
      source.delete(new AllFileSelector());
    }
  }

  @Test
  public void stageForCache_missing_source() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);

    Path dest = new Path("bin/test/bogus-destination");
    FileObject bogusSource = KettleVFS.getFileObject("bogus");
    try {
      ch.stageForCache(bogusSource, fs, dest, true);
      fail("expected exception when source does not exist");
    } catch (KettleFileException ex) {
      assertEquals(BaseMessages.getString(DistributedCacheUtil.class, "DistributedCacheUtil.SourceDoesNotExist", bogusSource), ex.getMessage().trim());
    }
  }

  @Test
  public void stageForCache_destination_no_overwrite() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);

    FileObject source = createTestFolderWithContent();
    try {
      Path root = new Path("bin/test/stageForCache_destination_exists");
      Path dest = new Path(root, "dest");

      fs.mkdirs(dest);
      assertTrue(fs.exists(dest));
      assertTrue(fs.getFileStatus(dest).isDir());
      try {
        ch.stageForCache(source, fs, dest, false);
      } catch (KettleFileException ex) {
        assertTrue(ex.getMessage().contains("Destination exists"));
      } finally {
        fs.delete(root, true);
      }
    } finally {
      source.delete(new AllFileSelector());
    }
  }

  @Test
  public void stageForCache_destination_exists() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);

    FileObject source = createTestFolderWithContent();
    try {
      Path root = new Path("bin/test/stageForCache_destination_exists");
      Path dest = new Path(root, "dest");

      fs.mkdirs(dest);
      assertTrue(fs.exists(dest));
      assertTrue(fs.getFileStatus(dest).isDir());

      stageForCacheTester(ch, source, fs, root, dest, 3, 2);
    } finally {
      source.delete(new AllFileSelector());
    }
  }

  @Test
  public void addCachedFilesToClasspath() throws IOException {
    DistributedCacheUtil ch = new DistributedCacheUtil();
    Configuration conf = new Configuration();

    List<Path> files = Arrays.asList(new Path("a"), new Path("b"), new Path("c"));

    ch.addCachedFilesToClasspath(files, conf);

    assertEquals("yes", conf.get("mapred.create.symlink"));

    for (Path file : files) {
      assertTrue(conf.get("mapred.cache.files").contains(file.toString()));
      assertTrue(conf.get("mapred.job.classpath.files").contains(file.toString()));
    }
  }

  @Test
  public void ispmrInstalledAt() throws IOException {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);

    Path root = new Path("bin/test/ispmrInstalledAt");
    Path lib = new Path(root, "lib");
    Path plugins = new Path(root, "plugins");
    Path bigDataPlugin = new Path(plugins, DistributedCacheUtil.PENTAHO_BIG_DATA_PLUGIN_FOLDER_NAME);

    Path lockFile = ch.getLockFileAt(root);
    try {
      // Create all directories (parent directories created automatically)
      fs.mkdirs(lib);
      fs.mkdirs(bigDataPlugin);

      assertTrue(ch.isKettleEnvironmentInstalledAt(fs, root));

      // If lock file is there pmr is not installed
      fs.create(lockFile);
      assertFalse(ch.isKettleEnvironmentInstalledAt(fs, root));

      // Try to create a file instead of a directory for the pentaho-big-data-plugin. This should be detected.
      fs.delete(bigDataPlugin, true);
      fs.create(bigDataPlugin);
      assertFalse(ch.isKettleEnvironmentInstalledAt(fs, root));
    } finally {
      fs.delete(root, true);
    }
  }

  @Test
  public void installKettleEnvironment_missing_arguments() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    try {
      ch.installKettleEnvironment(null, null, null, null, null);
      fail("Expected exception on missing archive");
    } catch (NullPointerException ex) {
      assertEquals("pmrArchive is required", ex.getMessage());
    }

    try {
      ch.installKettleEnvironment(KettleVFS.getFileObject("."), null, null, null, null);
      fail("Expected exception on missing archive");
    } catch (NullPointerException ex) {
      assertEquals("destination is required", ex.getMessage());
    }

    try {
      ch.installKettleEnvironment(KettleVFS.getFileObject("."), null, new Path("."), null, null);
      fail("Expected exception on missing archive");
    } catch (NullPointerException ex) {
      assertEquals("big data plugin required", ex.getMessage());
    }
  }

  @Test
  public void installKettleEnvironment() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);

    // This "empty pmr" contains a lib/ folder but with no content
    FileObject pmrArchive = KettleVFS.getFileObject("test-res/empty-pmr.zip");

    FileObject bigDataPluginDir = createTestFolderWithContent(DistributedCacheUtil.PENTAHO_BIG_DATA_PLUGIN_FOLDER_NAME);

    Path root = new Path("bin/test/installKettleEnvironment");
    try {
      ch.installKettleEnvironment(pmrArchive, fs, root, bigDataPluginDir, null);
      assertTrue(ch.isKettleEnvironmentInstalledAt(fs, root));
    } finally {
      bigDataPluginDir.delete(new AllFileSelector());
      fs.delete(root, true);
    }
  }

  @Test
  public void installKettleEnvironment_additional_plugins() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);

    // This "empty pmr" contains a lib/ folder but with no content
    FileObject pmrArchive = KettleVFS.getFileObject("test-res/empty-pmr.zip");

    FileObject bigDataPluginDir = createTestFolderWithContent(DistributedCacheUtil.PENTAHO_BIG_DATA_PLUGIN_FOLDER_NAME);
    FileObject samplePluginDir = createTestFolderWithContent("sample-plugin");

    Path root = new Path("bin/test/installKettleEnvironment");
    try {
      ch.installKettleEnvironment(pmrArchive, fs, root, bigDataPluginDir, Arrays.asList(samplePluginDir));
      assertTrue(ch.isKettleEnvironmentInstalledAt(fs, root));
      assertTrue(fs.exists(new Path(root, "plugins/sample-plugin")));
    } finally {
      bigDataPluginDir.delete(new AllFileSelector());
      samplePluginDir.delete(new AllFileSelector());
      fs.delete(root, true);
    }
  }

  @Test
  public void stagePluginsForCache() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);

    Path pluginsDir = new Path("bin/test/plugins-installation-dir");

    FileObject pluginDir = createTestFolderWithContent();

    try {
      ch.stagePluginsForCache(fs, pluginsDir, true, Arrays.asList(pluginDir));
      Path pluginInstallPath = new Path(pluginsDir, pluginDir.getURL().toURI().getPath());
      assertTrue(fs.exists(pluginInstallPath));
      ContentSummary summary = fs.getContentSummary(pluginInstallPath);
      assertEquals(3, summary.getFileCount());
      assertEquals(2, summary.getDirectoryCount());
    } finally {
      pluginDir.delete(new AllFileSelector());
      fs.delete(pluginsDir, true);
    }
  }

  @Test
  public void configureWithpmr() throws Exception {
    DistributedCacheUtil ch = new DistributedCacheUtil();

    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.getLocal(conf);

    // This "empty pmr" contains a lib/ folder and some empty kettle-*.jar files but no actual content
    FileObject pmrArchive = KettleVFS.getFileObject("test-res/empty-pmr.zip");

    FileObject bigDataPluginDir = createTestFolderWithContent(DistributedCacheUtil.PENTAHO_BIG_DATA_PLUGIN_FOLDER_NAME);

    Path root = new Path("bin/test/installKettleEnvironment");
    try {
      ch.installKettleEnvironment(pmrArchive, fs, root, bigDataPluginDir, null);
      assertTrue(ch.isKettleEnvironmentInstalledAt(fs, root));

      ch.configureWithKettleEnvironment(conf, fs, root);

      // Make sure our libraries are on the classpath
      assertTrue(conf.get("mapred.cache.files").contains("lib/kettle-core.jar"));
      assertTrue(conf.get("mapred.cache.files").contains("lib/kettle-engine.jar"));
      assertTrue(conf.get("mapred.job.classpath.files").contains("lib/kettle-core.jar"));
      assertTrue(conf.get("mapred.job.classpath.files").contains("lib/kettle-engine.jar"));

      // Make sure our plugins folder is registered
      assertTrue(conf.get("mapred.cache.files").contains("#plugins"));

      // Make sure our libraries aren't included twice
      assertFalse(conf.get("mapred.cache.files").contains("#lib"));

      // We should not have individual files registered
      assertFalse(conf.get("mapred.cache.files").contains("pentaho-big-data-plugin/jar1.jar"));
      assertFalse(conf.get("mapred.cache.files").contains("pentaho-big-data-plugin/jar2.jar"));
      assertFalse(conf.get("mapred.cache.files").contains("pentaho-big-data-plugin/folder/file.txt"));

    } finally {
      bigDataPluginDir.delete(new AllFileSelector());
      fs.delete(root, true);
    }
  }

  @Test
  public void findPluginFolder() throws Exception {
    DistributedCacheUtil util = new DistributedCacheUtil();

    // Fake out the "plugins" directory for the project's root directory
    System.setProperty(Const.PLUGIN_BASE_FOLDERS_PROP, KettleVFS.getFileObject(".").getURL().toURI().getPath());

    assertNotNull("Should have found plugin dir: src/", util.findPluginFolder("src"));
    assertNotNull("Should be able to find nested plugin dir: src/org/", util.findPluginFolder("src/org"));

    assertNull("Should not have found plugin dir: src/org/", util.findPluginFolder("org"));
  }

  @Test
  public void addFilesToClassPath() throws IOException {
    DistributedCacheUtil util = new DistributedCacheUtil();
    Path p1 = new Path("/testing1");
    Path p2 = new Path("/testing2");
    Configuration conf = new Configuration();
    util.addFileToClassPath(p1, conf);
    util.addFileToClassPath(p2, conf);
    assertEquals("/testing1:/testing2", conf.get("mapred.job.classpath.files"));
  }

  @Test
  public void addFilesToClassPath_custom_path_separator() throws IOException {
    DistributedCacheUtil util = new DistributedCacheUtil();
    Path p1 = new Path("/testing1");
    Path p2 = new Path("/testing2");
    Configuration conf = new Configuration();

    System.setProperty("hadoop.cluster.path.separator", "J");

    util.addFileToClassPath(p1, conf);
    util.addFileToClassPath(p2, conf);
    assertEquals("/testing1J/testing2", conf.get("mapred.job.classpath.files"));

  }
}
