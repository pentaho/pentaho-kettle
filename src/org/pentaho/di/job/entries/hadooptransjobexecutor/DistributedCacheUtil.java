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

import org.apache.commons.vfs.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginFolderInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility to work with Hadoop's Distributed Cache
 */
public class DistributedCacheUtil {
  /**
   * Default buffer size when compressing/uncompressing files.
   */
  private static final int DEFAULT_BUFFER_SIZE = 8192;

  /**
   * Pattern to match files ending in ".jar"
   */
  private static final Pattern JAR_FILES = Pattern.compile(".*\\.jar$");
  /**
   * Pattern to match all files that are not in the lib/ directory. Matches any string that does not contain /lib
   */
  private static final Pattern NOT_LIB_FILES = Pattern.compile("^((?!/lib).)*$");

  /**
   * Default permission for cached files
   * <p/>
   * Not using FsPermission.createImmutable due to EOFExceptions when using it with Hadoop 0.20.2
   */
  private static final FsPermission CACHED_FILE_PERMISSION = new FsPermission((short) 0755);

  /**
   * Name of the Big Data Plugin folder
   */
  public static final String PENTAHO_BIG_DATA_PLUGIN_FOLDER_NAME = "pentaho-big-data-plugin";

  /**
   * Creates the path to a lock file within the provided directory
   *
   * @param dir Directory to generate lock file path within
   * @return Path to lock file within {@code dir}
   */
  public Path getLockFileAt(Path dir) {
    return new Path(dir, ".lock");
  }

  /**
   * This validates that the Kettle Environment is installed. "Installed" means the kettle engine and supporting jars/plugins exist in
   * the provided file system at the path provided.
   *
   * @param fs   File System to check for the Kettle Environment in
   * @param root Root path the Kettle Environment should reside within
   * @return True if the Kettle Environment is installed at {@code root}.
   * @throws IOException Error investigating installation
   */
  public boolean isKettleEnvironmentInstalledAt(FileSystem fs, Path root) throws IOException {
    // These directories must exist
    Path[] directories = new Path[]{
        new Path(root, "lib"),
        new Path(root, "plugins"),
        new Path(new Path(root, "plugins"), PENTAHO_BIG_DATA_PLUGIN_FOLDER_NAME)
    };
    // This file must not exist
    Path lock = getLockFileAt(root);
    // These directories must exist
    for (Path dir : directories) {
      if (!(fs.exists(dir) && fs.getFileStatus(dir).isDir())) {
        return false;
      }
    }
    // There's no lock file
    return !fs.exists(lock);
  }

  public void installKettleEnvironment(FileObject pmrArchive, FileSystem fs, Path destination, FileObject bigDataPlugin, List<FileObject> additionalPluginDirectories) throws IOException, KettleFileException {
    if (pmrArchive == null) {
      throw new NullPointerException("pmrArchive is required");
    }
    if (destination == null) {
      throw new NullPointerException("destination is required");
    }
    if (bigDataPlugin == null) {
      throw new NullPointerException("big data plugin required");
    }

    FileObject extracted = extractToTemp(pmrArchive);

    // Write lock file while we're installing
    Path lockFile = getLockFileAt(destination);
    fs.create(lockFile, true);

    stageForCache(extracted, fs, destination, true);

    List<FileObject> pluginsDirectories = new ArrayList<FileObject>();
    pluginsDirectories.add(bigDataPlugin);
    if (additionalPluginDirectories != null && !additionalPluginDirectories.isEmpty()) {
      pluginsDirectories.addAll(additionalPluginDirectories);
    }

    stagePluginsForCache(fs, new Path(destination, "plugins"), true, pluginsDirectories);

    // Delete the lock file now that we're done. It is intentional that we're not doing this in a try/finally. If the
    // staging fails for some reason we require the user to forcibly overwrite the (partial) installation
    fs.delete(lockFile, true);
  }

  public void stagePluginsForCache(FileSystem fs, Path pluginsDir, boolean overwrite, List<FileObject> pluginDirectories) throws KettleFileException, IOException {
    if (pluginDirectories == null) {
      throw new IllegalArgumentException("plugins required");
    }
    if (!fs.exists(pluginsDir)) {
      fs.mkdirs(pluginsDir);
    }
    for (FileObject localPluginDir : pluginDirectories) {
      if (!localPluginDir.exists()) {
        throw new KettleFileException(BaseMessages.getString(DistributedCacheUtil.class, "DistributedCacheUtil.PluginDirectoryNotFound", localPluginDir));
      }
      Path pluginDir = new Path(pluginsDir, localPluginDir.getName().getBaseName());
      if (!overwrite && fs.exists(pluginDir)) {
        // skip installing this plugin, it already exists
        continue;
      }
      stageForCache(localPluginDir, fs, pluginDir, true);
    }
  }

  /**
   * Configure the provided configuration to use the Distributed Cache and include all files in {@code kettleInstallDir}.
   * All jar files in lib/ will be added to the classpath.
   *
   * @param conf             Configuration to update
   * @param fs               File system to load Kettle Environment installation from
   * @param kettleInstallDir Directory that contains the Kettle installation to use in the file system provided
   * @throws KettleFileException
   * @throws IOException
   */
  public void configureWithKettleEnvironment(Configuration conf, FileSystem fs, Path kettleInstallDir) throws KettleFileException, IOException {
    Path libDir = new Path(kettleInstallDir, "lib");
    List<Path> libraryJars = findFiles(fs, libDir, JAR_FILES);
    addCachedFilesToClasspath(libraryJars, conf);

    List<Path> nonLibFiles = findFiles(fs, kettleInstallDir, NOT_LIB_FILES);
    addCachedFiles(nonLibFiles, conf);
  }

  /**
   * Register a list of files from a Hadoop file system to be available and placed on the classpath when the configuration
   * is used to submit Hadoop jobs
   *
   * @param files Paths to add to the classpath of the configuration provided
   * @param conf  Configuration to modify
   * @throws IOException
   */
  public void addCachedFilesToClasspath(List<Path> files, Configuration conf) throws IOException {
    DistributedCache.createSymlink(conf);
    for (Path file : files) {
      // We need to disqualify the path so Distributed Cache in 0.20.2 can properly add the resources to
      // the classpath: https://issues.apache.org/jira/browse/MAPREDUCE-752
      DistributedCache.addFileToClassPath(disqualifyPath(file), conf);
    }
  }

  /**
   * Register a list of paths from a Hadoop file system to be available when the configuration is used to submit Hadoop jobs
   *
   * @param paths Paths to add to the list of cached paths for the configuration provided
   * @param conf  Configuration to modify
   * @throws IOException
   */
  public void addCachedFiles(List<Path> paths, Configuration conf) throws IOException {
    DistributedCache.createSymlink(conf);
    for (Path path : paths) {
      // Build a URI and set the path's short name in the fragment so the file is copied properly
      DistributedCache.addCacheFile(URI.create(path.toUri() + "#" + path.getName()), conf);
    }
  }

  /**
   * Removes the schema, host, and authentication portion of a path in its URI.
   *
   * @param path Path to cleanse
   * @return New path relative to the root of the filesystem
   */
  public Path disqualifyPath(Path path) {
    return new Path(path.toUri().getPath());
  }

  /**
   * Stages the source file or folder to a Hadoop file system and sets their permission and replication value appropriately
   * to be used with the Distributed Cache. WARNING: This will delete the contents of dest before staging the archive.
   *
   * @param source    File or folder to copy to the file system. If it is a folder all contents will be copied into dest.
   * @param fs        Hadoop file system to store the contents of the archive in
   * @param dest      Destination to copy source into. If source is a file, the new file name will be exactly dest. If source
   *                  is a folder its contents will be copied into dest. For more info see
   *                  {@link FileSystem#copyFromLocalFile(org.apache.hadoop.fs.Path, org.apache.hadoop.fs.Path)}.
   * @param overwrite Should an existing file or folder be overwritten? If not an exception will be thrown.
   * @throws IOException         Destination exists is not a directory
   * @throws KettleFileException Source does not exist or destination exists and overwrite is false.
   */
  public void stageForCache(FileObject source, FileSystem fs, Path dest, boolean overwrite) throws IOException, KettleFileException {
    if (!source.exists()) {
      throw new KettleFileException(BaseMessages.getString(DistributedCacheUtil.class, "DistributedCacheUtil.SourceDoesNotExist", source));
    }

    if (fs.exists(dest)) {
      if (overwrite) {
        // It is a directory, clear it out
        fs.delete(dest, true);
      } else {
        throw new KettleFileException(BaseMessages.getString(DistributedCacheUtil.class, "DistributedCacheUtil.DestinationExists", dest.toUri().getPath()));
      }
    }

    // Use the same replication we'd use for submitting jobs
    short replication = (short) fs.getConf().getInt("mapred.submit.replication", 10);

    Path local = new Path(source.getURL().getPath());
    fs.copyFromLocalFile(local, dest);
    fs.setPermission(dest, CACHED_FILE_PERMISSION);
    fs.setReplication(dest, replication);
  }

  /**
   * Recursively searches for all files starting at the directory provided with the extension provided. If no extension
   * is provided all files will be returned.
   *
   * @param root      Directory to start the search for files in
   * @param extension File extension to search for. If null all files will be returned.
   * @return List of absolute path names to all files found in {@code dir} and its subdirectories.
   * @throws KettleFileException
   * @throws FileSystemException
   */
  public List<String> findFiles(FileObject root, final String extension) throws FileSystemException {
    FileObject[] files = root.findFiles(new FileSelector() {
      @Override
      public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
        return extension == null || extension.equals(fileSelectInfo.getFile().getName().getExtension());
      }

      @Override
      public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
        return FileType.FOLDER.equals(fileSelectInfo.getFile().getType());
      }
    });

    if (files == null) {
      return Collections.EMPTY_LIST;
    }

    List<String> paths = new ArrayList<String>();
    for (FileObject file : files) {
      try {
        paths.add(file.getURL().toURI().getPath());
      } catch (URISyntaxException ex) {
        throw new FileSystemException("Error getting URI of file: " + file.getURL().getPath());
      }
    }
    return paths;
  }

  /**
   * Looks for all files in the path within the given file system that match the pattern provided. Only the direct
   * descendants of {@code path} will be evaluated; this is not recursive.
   * 
   * @param fs File system to search within
   * @param path Path to search in
   * @param fileNamePattern Pattern of file name to match. If {@code null}, all files will be matched.
   * @return All {@link Path}s that match the provided pattern.
   * @throws IOException Error retrieving listing status of a path from the file system
   */
  public List<Path> findFiles(FileSystem fs, Path path, Pattern fileNamePattern) throws IOException {
    FileStatus[] files = fs.listStatus(path);
    List<Path> found = new ArrayList<Path>(files.length);
    for (FileStatus file : files) {
      if (fileNamePattern == null || fileNamePattern.matcher(file.getPath().toString()).matches()) {
        found.add(file.getPath());
      }
    }
    return found;
  }

  /**
   * Delete a directory and all of its contents
   *
   * @param dir Directory to delete
   * @return True if the directory was deleted successfully
   */
  public boolean deleteDirectory(FileObject dir) throws FileSystemException {
    dir.delete(new AllFileSelector());
    return !dir.exists();
  }

  /**
   * Extract a zip archive to a temp directory.
   *
   * @param archive Zip archive to extract
   * @return Directory the zip was extracted into
   * @throws IOException
   * @throws KettleFileException
   * @see DistributedCacheUtil#extract(org.apache.commons.vfs.FileObject, org.apache.commons.vfs.FileObject)
   */
  public FileObject extractToTemp(FileObject archive) throws IOException, KettleFileException {
    if (archive == null) {
      throw new NullPointerException("archive is required");
    }
    // Ask KettleVFS for a temporary file name without extension and use that as our temporary folder to extract into
    FileObject dest = KettleVFS.createTempFile("", "", System.getProperty("java.io.tmpdir"));
    return extract(archive, dest);
  }

  /**
   * Extract a zip archive to a directory.
   *
   * @param archive Zip archive to extract
   * @param dest    Destination directory. This must not exist!
   * @return Directory the zip was extracted into
   * @throws IllegalArgumentException when the archive file does not exist or the destination directory already exists
   * @throws IOException
   * @throws KettleFileException
   */
  public FileObject extract(FileObject archive, FileObject dest) throws IOException, KettleFileException {
    if (!archive.exists()) {
      throw new IllegalArgumentException("archive does not exist: " + archive.getURL().getPath());
    }

    if (dest.exists()) {
      throw new IllegalArgumentException("destination already exists");
    }
    dest.createFolder();

    try {
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      int len = 0;
      ZipInputStream zis = new ZipInputStream(archive.getContent().getInputStream());
      try {
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
          FileObject entry = KettleVFS.getFileObject(dest + Const.FILE_SEPARATOR + ze.getName());

          if (ze.isDirectory()) {
            entry.createFolder();
            continue;
          }

          OutputStream os = KettleVFS.getOutputStream(entry, false);
          try {
            while ((len = zis.read(buffer)) > 0) {
              os.write(buffer, 0, len);
            }
          } finally {
            if (os != null) {
              os.close();
            }
          }
        }
      } finally {
        if (zis != null) {
          zis.close();
        }
      }
    } catch (Exception ex) {
      // Try to clean up the temp directory and all files
      if (!deleteDirectory(dest)) {
        throw new KettleFileException("Could not clean up temp dir after error extracting", ex);
      }
      throw new KettleFileException("error extracting archive", ex);
    }

    return dest;
  }

  /**
   * Attempts to find a plugin's installation folder on disk within all known plugin folder locations
   *
   * @param pluginFolderName Name of plugin folder
   * @return Location of the first plugin folder found as a direct descendant of one of the known plugin folder locations
   * @throws KettleFileException Error getting plugin folders
   */
  public FileObject findPluginFolder(final String pluginFolderName) throws KettleFileException {
    List<PluginFolderInterface> pluginFolders = PluginFolder.populateFolders(null);
    if (pluginFolders != null) {
      for(PluginFolderInterface pluginFolder : pluginFolders) {
        FileObject folder = KettleVFS.getFileObject(pluginFolder.getFolder());
        
        try {
          if (folder.exists()) {
            FileObject[] files = folder.findFiles(new FileSelector() {
              @Override
              public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
                if (fileSelectInfo.getFile().equals(fileSelectInfo.getBaseFolder())) {
                  // Do not consider the base folders
                  return false;
                }
                // Determine relative name to compare
                int baseNameLength = fileSelectInfo.getBaseFolder().getName().getPath().length() + 1;
                String relativeName = fileSelectInfo.getFile().getName().getPath().substring(baseNameLength);
                // Compare plugin folder name with the relative name
                return pluginFolderName.equals(relativeName);
              }

              @Override
              public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
                return true;
              }
            });
            if (files != null && files.length > 0) {
              return files[0]; // Return the first match
            }
          }
        } catch (FileSystemException ex) {
          throw new KettleFileException("Error searching for folder '" + pluginFolderName + "'", ex);
        }
      }
    }
    return null;
  }

}
