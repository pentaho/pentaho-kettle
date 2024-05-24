/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.di.connections.ConnectionProvider;
import org.pentaho.di.connections.utils.VFSConnectionTestOptions;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;

import java.util.List;

/**
 * Created by bmorrise on 2/3/19.
 */
public interface VFSConnectionProvider<T extends VFSConnectionDetails> extends ConnectionProvider<T> {
  FileSystemOptions getOpts( T vfsConnectionDetails );
  List<VFSRoot> getLocations( T vfsConnectionDetails );
  String getProtocol( T vfsConnectionDetails );
  String sanitizeName( String string );

  /**
   * If the provided ConnectionDetails are the correct type, return a FileObject for the provided path. This method and
   * the returning FileObject must not depend on a ConnectionManager or MetaStore.
   *
   * @param connectionDetails for the connection
   * @param path path relative to the connection
   *
   * @return FileSystem or null if the provided connection details are not the matching type
   */
  default FileObject getDirectFile( T connectionDetails, String path ) throws KettleFileException {
    String pvfsUrl = connectionDetails.getType() + "://" + path;
    // use an empty Variables to prevent other "connection" values from causing StackOverflowErrors
    return KettleVFS.getFileObject( pvfsUrl, new Variables(), getOpts( connectionDetails ) );
  }

  /**
   * Tests if a given VFS connection is valid, optionally, with certain testing options.
   * <p>
   * This method should first delegate to {@link ConnectionProvider#test(ConnectionDetails)} to perform basic
   * validation, independent of the connection's root path, {@link VFSConnectionDetails#getRootPath}, if any,
   * immediately returning {@code false}, when unsuccessful.
   * <p>
   * When base validation is successful, if {@code options} has a {@code true}
   * {@link VFSConnectionTestOptions#isIgnoreRootPath()}, this method should immediately return {@code true}.
   * <p>
   * Otherwise, the method should validate that the connection's root folder path is valid, taking into account the
   * values of {@link VFSConnectionDetails#isSupportsRootPath()}, {@link VFSConnectionDetails#isRootPathRequired()} and
   * {@link VFSConnectionDetails#getRootPath()}.
   * <p>
   * The default implementation exists for backward compatibility reasons and simply delegates to
   * {@link ConnectionProvider#test(ConnectionDetails)}.
   * @param connectionDetails The VFS connection.
   * @param options The testing options, or {@code null}. When {@code null}, a default instance of
   * {@link VFSConnectionTestOptions} is constructed and used.
   * @return {@code true} if the provided rootPath is valid; {@code false} otherwise.
   */
  default boolean test( @NonNull T connectionDetails, @NonNull VFSConnectionTestOptions options ) throws KettleException {
    return test( connectionDetails );
  }

  /**
   * Gets the resolved root path of a given connection.
   *
   * @param connectionDetails The VFS connection.
   * @return The non-empty resolved root path, if any; {@code null}, if. none.
   */
  default String getResolvedRootPath( @NonNull T connectionDetails ) {
    return connectionDetails.getRootPath();
  }

  /**
   * Checks if a given connection uses buckets in its current configuration.
   * <p>
   * A connection is using buckets if it can have buckets, as determined by {@link ConnectionDetails#hasBuckets()}, and if its {@link #getResolvedRootPath() resolved root path} is empty.
   * @param connectionDetails The VFS connection.
   * @return {@code true} if a connection has buckets; {@code false} otherwise.
   */
  default boolean usesBuckets( @NonNull T connectionDetails ) {
    return connectionDetails.hasBuckets() && getResolvedRootPath( connectionDetails ) == null;
  }
}
