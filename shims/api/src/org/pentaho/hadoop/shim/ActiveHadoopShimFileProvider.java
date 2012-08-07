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

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.FileProvider;

/**
 * Proxies the active {@link HadoopConfiguration}'s {@link FileProvider}. This
 * is used to be able to swap out the Hadoop configuration at runtime while
 * registering multiple file providers under the same scheme.
 */
public class ActiveHadoopShimFileProvider implements FileProvider {

  private HadoopConfigurationFileSystemManager fsm;
  private String scheme;
  
  public ActiveHadoopShimFileProvider(HadoopConfigurationFileSystemManager fsm, String scheme) {
    if (fsm == null || scheme == null) {
      throw new NullPointerException();
    }
    this.fsm = fsm;
    this.scheme = scheme;
  }
  
  @Override
  public FileObject createFileSystem(String scheme, FileObject file, FileSystemOptions fileSystemOptions) throws FileSystemException {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    FileProvider p = fsm.getActiveFileProvider(scheme);
    Thread.currentThread().setContextClassLoader(p.getClass().getClassLoader());
    try {
      return p.createFileSystem(scheme, file, fileSystemOptions);
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  @Override
  public FileObject findFile(FileObject baseFile, String uri, FileSystemOptions fileSystemOptions) throws FileSystemException {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    FileProvider p = fsm.getActiveFileProvider(scheme);
    Thread.currentThread().setContextClassLoader(p.getClass().getClassLoader());
    try {
      return p.findFile(baseFile, uri, fileSystemOptions);
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Capability> getCapabilities() {
    try {
      return fsm.getActiveFileProvider(scheme).getCapabilities();
    } catch (FileSystemException e) {
      return Collections.emptyList();
    }
  }

  @Override
  public FileSystemConfigBuilder getConfigBuilder() {
    try {
      return fsm.getActiveFileProvider(scheme).getConfigBuilder();
    } catch (FileSystemException e) {
      return null;
    }
  }

  @Override
  public FileName parseUri(FileName root, String uri) throws FileSystemException {
    return fsm.getActiveFileProvider(scheme).parseUri(root, uri);
  }

}
