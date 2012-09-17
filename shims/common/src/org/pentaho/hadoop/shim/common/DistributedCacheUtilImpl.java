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

package org.pentaho.hadoop.shim.common;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileDepthSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileTypeSelector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.VersionInfo;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginFolderInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hadoop.shim.spi.HadoopShim;

/**
 * Utility to work with Hadoop's Distributed Cache
 */
public class DistributedCacheUtilImpl implements org.pentaho.hadoop.shim.api.DistributedCacheUtil {
  
  /**
   * Path within the installation directory to deploy libraries
   */
  private static final String PATH_LIB = "lib";

  /**
   * Pentaho MapReduce library path within a Hadoop configuration
   */
  private static final String PATH_PMR = "pmr";

  /**
   * Client-only library path within a Hadoop configuration
   */
  private static final String PATH_CLIENT = "client";

  /**
   * Path within the installation directory to deploy plugins
   */
  private static final String PATH_PLUGINS = "plugins";

  /**
   * Default buffer size when compressing/uncompressing files.
   */
  private static final int DEFAULT_BUFFER_SIZE = 8192;

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
   * The Hadoop Configuration this Distributed Cache Utility is part of
   */
  private HadoopConfiguration configuration;
  
  public DistributedCacheUtilImpl(HadoopConfiguration configuration) {
    if (configuration == null) {
      throw new NullPointerException();
    }
    this.configuration = configuration;
  }

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
        new Path(root, PATH_LIB),
        new Path(root, PATH_PLUGINS),
        new Path(new Path(root, PATH_PLUGINS), PENTAHO_BIG_DATA_PLUGIN_FOLDER_NAME)
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

  public void installKettleEnvironment(FileObject pmrArchive, FileSystem fs, Path destination, FileObject bigDataPlugin, String additionalPlugins) throws IOException, KettleFileException {
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
    
    stageBigDataPlugin(fs, destination, bigDataPlugin);

    if (!Const.isEmpty(additionalPlugins)) {
      stagePluginsForCache(fs, new Path(destination, PATH_PLUGINS), additionalPlugins);
    }

    // Delete the lock file now that we're done. It is intentional that we're not doing this in a try/finally. If the
    // staging fails for some reason we require the user to forcibly overwrite the (partial) installation
    fs.delete(lockFile, true);
  }

  /**
   * Move files from the source folder to the destination folder, overwriting any files that may already exist there.
   * 
   * @param fs File system to write to
   * @param dest Destination to move source file/folder into
   * @param pluginFolder Big Data plugin folder
   * @throws KettleFileException
   * @throws IOException
   */
  private void stageBigDataPlugin(FileSystem fs, Path dest, FileObject pluginFolder) throws KettleFileException, IOException {
    Path pluginsDir = new Path(dest, PATH_PLUGINS);
    Path libDir = new Path(dest, PATH_LIB);
    Path bigDataPluginDir = new Path(pluginsDir, pluginFolder.getName().getBaseName());

    // Stage everything except the hadoop-configurations and pmr libraries
    for (FileObject f : pluginFolder.findFiles(new FileDepthSelector(1, 1))) {
      if (   !"hadoop-configurations".equals(f.getName().getBaseName())
          && !"pentaho-mapreduce-libraries.zip".equals(f.getName().getBaseName())) {
        stageForCache(f, fs, new Path(bigDataPluginDir, f.getName().getBaseName()), true);
      }
    }

    // Stage the current Hadoop configuration without its client-only or pmr libraries (these will be copied into the lib dir)
    Path hadoopConfigDir = new Path(new Path(bigDataPluginDir, "hadoop-configurations"), configuration.getIdentifier());
    for (FileObject f : configuration.getLocation().findFiles(new FileSelector() {
      @Override
      public boolean includeFile(FileSelectInfo info) throws Exception {
        return FileType.FILE.equals(info.getFile().getType());
      }
      @Override
      public boolean traverseDescendents(FileSelectInfo info) throws Exception {
        String name = info.getFile().getName().getBaseName();
        return !((PATH_PMR.equals(name) || PATH_CLIENT.equals(name)) && 
            PATH_LIB.equals(info.getFile().getParent().getName().getBaseName()));
      }
    })) {
      // Create relative path to write to
      String relPath = configuration.getLocation().getName().getRelativeName(f.getName());
      stageForCache(f, fs, new Path(hadoopConfigDir, relPath), true);
    }

    // Stage all pmr libraries for the Hadoop configuration into the root library path for the Kettle environment
    for (FileObject f : configuration.getLocation().resolveFile(PATH_LIB).resolveFile(PATH_PMR).findFiles(new FileTypeSelector(FileType.FILE))) {
      stageForCache(f, fs, new Path(libDir, f.getName().getBaseName()), true);
    }
  }

  /**
   * Stage a comma-separated list of plugin folders into a directory in HDFS.
   * 
   * @param fs File System to write to
   * @param pluginsDir Root plugins directory in HDFS to copy folders into
   * @param pluginFolderNames Comma-separated list of plugin folders to copy. These are relative to a root Kettle plugin folder (as defined by {@link Const#PLUGIN_BASE_FOLDERS_PROP})
   * @throws KettleFileException Error locating a plugin folder
   * @throws IOException Error copying
   */
  public void stagePluginsForCache(FileSystem fs, Path pluginsDir, String pluginFolderNames) throws KettleFileException, IOException {
    if (pluginFolderNames == null) {
      throw new IllegalArgumentException("pluginFolderNames required");
    }
    if (!fs.exists(pluginsDir)) {
      fs.mkdirs(pluginsDir);
    }
    for (String localPluginPath : pluginFolderNames.split(",")) {
      localPluginPath.trim();
      Object[] localFileTuple = findPluginFolder(localPluginPath);
      if (localFileTuple == null || !((FileObject) localFileTuple[0]).exists()) {
        throw new KettleFileException(BaseMessages.getString(DistributedCacheUtilImpl.class, "DistributedCacheUtil.PluginDirectoryNotFound", localPluginPath));
      }
      FileObject localFile = (FileObject) localFileTuple[0];
      String relativePath = (String) localFileTuple[1];
      Path pluginDir = new Path(pluginsDir, relativePath);
      stageForCache(localFile, fs, pluginDir, true);
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
    Path libDir = new Path(kettleInstallDir, PATH_LIB);
    // Add all files to the classpath found in the lib directory
    List<Path> libraryJars = findFiles(fs, libDir, null);
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
      addFileToClassPath(disqualifyPath(file), conf);
    }
  }

  /**
   * Add an file path to the current set of classpath entries. It adds the file
   * to cache as well.
   *
   * This is copied from Hadoop 0.20.2 o.a.h.filecache.DistributedCache so we can inject the correct path separator
   * for the environment the cluster is executing in. See {@link #getClusterPathSeparator()}.
   *
   * @param file Path of the file to be added
   * @param conf Configuration that contains the classpath setting
   */
  public void addFileToClassPath(Path file, Configuration conf)
      throws IOException {

    // TODO Replace this with a Hadoop shim if we end up having version-specific implementations scattered around
    if (VersionInfo.getVersion().contains("0.21")) {
      DistributedCache.addFileToClassPath(file, conf);
    } else {
      String classpath = conf.get("mapred.job.classpath.files");
      conf.set("mapred.job.classpath.files", classpath == null ? file.toString()
          : classpath + getClusterPathSeparator() + file.toString());
      FileSystem fs = FileSystem.get(conf);
      URI uri = fs.makeQualified(file).toUri();

      DistributedCache.addCacheFile(uri, conf);
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
      throw new KettleFileException(BaseMessages.getString(DistributedCacheUtilImpl.class, "DistributedCacheUtil.SourceDoesNotExist", source));
    }

    if (fs.exists(dest)) {
      if (overwrite) {
        // It is a directory, clear it out
        fs.delete(dest, true);
      } else {
        throw new KettleFileException(BaseMessages.getString(DistributedCacheUtilImpl.class, "DistributedCacheUtil.DestinationExists", dest.toUri().getPath()));
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
  @SuppressWarnings("unchecked")
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
   * @see DistributedCacheUtilImpl#extract(org.apache.commons.vfs.FileObject, org.apache.commons.vfs.FileObject)
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
   * @return Tuple of [(FileObject) Location of the first plugin folder found as a direct descendant of one of the known plugin folder locations, (String) Relative path from parent]
   * @throws KettleFileException Error getting plugin folders
   */
  protected Object[] findPluginFolder(final String pluginFolderName) throws KettleFileException {
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
              return new Object[] {files[0], folder.getName().getRelativeName(files[0].getName())}; // Return the first match
            }
          }
        } catch (FileSystemException ex) {
          throw new KettleFileException("Error searching for folder '" + pluginFolderName + "'", ex);
        }
      }
    }
    return null;
  }

  /**
   * Determine the class path separator of the cluster. For now there is no way to determine the remote cluster's path
   * separator nor would we want to ultimately do that. This can be configured externally with the system property
   * "hadoop.cluster.path.separator". This will default to ":" if the system property is not set.
   *
   * This is not necessary for Hadoop 0.21.x. See https://issues.apache.org/jira/browse/HADOOP-4864.
   *
   * @return Path separator to use when building up the classpath to use for the Distributed Cache
   */
  public String getClusterPathSeparator() {
    return System.getProperty("hadoop.cluster.path.separator", ":");
  }

  // Wrapping/delegating methods
  @Override
  public boolean isKettleEnvironmentInstalledAt(org.pentaho.hadoop.shim.api.fs.FileSystem fs,
      org.pentaho.hadoop.shim.api.fs.Path kettleEnvInstallDir) throws IOException {
    return isKettleEnvironmentInstalledAt(ShimUtils.asFileSystem(fs), ShimUtils.asPath(kettleEnvInstallDir));
  }

  @Override
  public void configureWithKettleEnvironment(org.pentaho.hadoop.shim.api.Configuration conf,
      org.pentaho.hadoop.shim.api.fs.FileSystem fs, org.pentaho.hadoop.shim.api.fs.Path kettleEnvInstallDir) throws KettleFileException, IOException {
    configureWithKettleEnvironment(ShimUtils.asConfiguration(conf), ShimUtils.asFileSystem(fs), ShimUtils.asPath(kettleEnvInstallDir));
  }

  @Override
  public void installKettleEnvironment(FileObject pmrLibArchive, org.pentaho.hadoop.shim.api.fs.FileSystem fs,
      org.pentaho.hadoop.shim.api.fs.Path destination, FileObject bigDataPluginFolder,
      String additionalPlugins) throws KettleFileException, IOException {
    installKettleEnvironment(pmrLibArchive, ShimUtils.asFileSystem(fs), ShimUtils.asPath(destination), bigDataPluginFolder, additionalPlugins);
    
  }
}
