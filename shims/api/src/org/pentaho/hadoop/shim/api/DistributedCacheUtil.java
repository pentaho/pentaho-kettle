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

import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.hadoop.shim.api.fs.FileSystem;
import org.pentaho.hadoop.shim.api.fs.Path;

/**
 * A collection of methods for working with Hadoop's Distributed Cache mechanism.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public interface DistributedCacheUtil {

  /**
   * Does a Kettle environment exist in the given file system at the provided path?
   * 
   * @param fs File system to look in
   * @param kettleEnvInstallDir Path to check for a Kettle environment
   * @return {@code true} if there is a valid kettle environment in the file system at the path provided.
   * @throws IOException Error communicating with the file system.
   */
  boolean isKettleEnvironmentInstalledAt(FileSystem fs, Path kettleEnvInstallDir) throws IOException;

  /**
   * Configure the configuration object to use the Kettle environment staged in the 
   * file system and path provided.
   *  
   * @param conf Configuration to update
   * @param fs File system to look in
   * @param kettleEnvInstallDir Path to the Kettle environment to use when executing a MapReduce job with the configuration provided
   * @throws Exception Error locating the Kettle environment or configuring
   */
  void configureWithKettleEnvironment(Configuration conf, FileSystem fs, Path kettleEnvInstallDir) throws Exception;

  /**
   * Installs the contents of a pre-configured Kettle environment into a Hadoop
   * file system.
   * 
   * @param pmrLibArchive Archive to stage into the file system
   * @param fs File system to write to
   * @param destination Directory to stage environment into
   * @param bigDataPluginFolder Location of the big data plugin to stage into the Kettle environment
   * @param additionalPlugins Comma-separated list of directory paths relative to a root Kettle plugin 
   *                          folder representing directories that should be copied into the installation
   * @throws Exception Error staging the Kettle environment
   */
  void installKettleEnvironment(FileObject pmrLibArchive, FileSystem fs, Path destination,
      FileObject bigDataPluginFolder, String additionalPlugins) throws Exception;

}
