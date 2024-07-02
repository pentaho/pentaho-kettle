/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.pentaho.di.core.vfs.AliasedFileObject;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * The abstract base {@link FileObject} implementation for the PVFS (Pentaho VFS) VFS provider.
 * <p>
 * The purpose of {@code ConnectionFileObject} is to represent a named connection {@code FileObject}.
 * {@code ConnectionFileObject} holds a reference to the resolved {@code FileObject} and delegates metadata calls to
 * that reference object, but for other calls, such as getChildren, {@code ConnectionFileObject} resolves the child file
 * objects, but converts them to PVFS file objects to maintain named connection information.
 * <p>
 * The various subclasses clarify the cases for which connection file objects and {@link ConnectionFileName} are used,
 * as well as allow providing more precise error messages to the user
 * (see {@link ConnectionFileObject#requireResolvedFileObject()}).
 * The subclasses are:
 * <ul>
 *   <li>{@link PvfsRootFileObject}</li>
 *   <li>{@link UndefinedConnectionFileObject}</li>
 *   <li>{@link ConnectionWithBucketsRootFileObject}</li>
 *   <li>{@link ResolvedConnectionFileObject}</li>
 * </ul>
 * The de facto factory of connection file objects is the method
 * {@link ConnectionFileSystem#createFile(AbstractFileName)}.
 */
@SuppressWarnings( "resource" )
public abstract class ConnectionFileObject extends AbstractFileObject<ConnectionFileSystem>
  implements AliasedFileObject {

  @NonNull
  public static final String DELIMITER = FileName.SEPARATOR;

  protected ConnectionFileObject( @NonNull ConnectionFileName name, @NonNull ConnectionFileSystem fs ) {
    super( name, fs );
  }

  @Override
  public ConnectionFileName getName() {
    return (ConnectionFileName) super.getName();
  }

  @Override
  protected long doGetContentSize() throws Exception {
    return 0;
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    return null;
  }

  @Override
  protected FileType doGetType() throws Exception {
    return null;
  }

  @Override
  protected String[] doListChildren() throws Exception {
    return new String[ 0 ];
  }

  @Override
  public void close() throws FileSystemException {
    requireResolvedFileObject().close();
  }

  @Override
  public void copyFrom( FileObject file, FileSelector selector ) throws FileSystemException {
    requireResolvedFileObject().copyFrom( file, selector );
  }

  @Override
  public void createFile() throws FileSystemException {
    requireResolvedFileObject().createFile();
  }

  @Override
  public void createFolder() throws FileSystemException {
    requireResolvedFileObject().createFolder();
  }

  @Override
  public boolean delete() throws FileSystemException {
    return requireResolvedFileObject().delete();
  }

  @Override
  public int delete( FileSelector selector ) throws FileSystemException {
    return requireResolvedFileObject().delete( selector );
  }

  @Override
  public int deleteAll() throws FileSystemException {
    return requireResolvedFileObject().deleteAll();
  }

  @Nullable
  @Override
  public FileObject getChild( String name ) throws FileSystemException {
    // Returns null if not found.
    @Nullable
    FileObject resolvedChildFileObject = requireResolvedFileObject().getChild( name );

    return resolvedChildFileObject != null ? createChild( resolvedChildFileObject ) : null;
  }

  /**
   * Resolve children from the delegated vfs provider then convert to PVFS
   *
   * @return File objects with PVFS scheme
   * @throws FileSystemException File doesn't exist
   */
  @Override
  public FileObject[] getChildren() throws FileSystemException {
    FileObject[] providerChildren = requireResolvedFileObject().getChildren();
    FileObject[] children = new FileObject[ providerChildren.length ];
    for ( int i = 0; i < providerChildren.length; i++ ) {
      children[ i ] = createChild( providerChildren[ i ] );
    }

    return children;
  }

  /**
   * Convert child provider file object to a child PVFS file object.
   *
   * @param childProviderFileObject The child provider FileObject.
   * @return The child PVFS FileObject.
   * @throws FileSystemException File doesn't exist
   */
  @NonNull
  protected FileObject createChild( @NonNull FileObject childProviderFileObject ) throws FileSystemException {
    return getAbstractFileSystem().createChild( this, childProviderFileObject );
  }

  @Override
  public FileContent getContent() throws FileSystemException {
    return requireResolvedFileObject().getContent();
  }

  @Override
  public FileOperations getFileOperations() throws FileSystemException {
    return requireResolvedFileObject().getFileOperations();
  }

  @Override
  public InputStream getInputStream() throws FileSystemException {
    return requireResolvedFileObject().getInputStream();
  }

  @Override
  public InputStream getInputStream( int bufferSize ) throws FileSystemException {
    return requireResolvedFileObject().getInputStream( bufferSize );
  }

  @Override
  public OutputStream getOutputStream() throws FileSystemException {
    return requireResolvedFileObject().getOutputStream();
  }

  @Override
  public OutputStream getOutputStream( boolean bAppend ) throws FileSystemException {
    return requireResolvedFileObject().getOutputStream( bAppend );
  }

  @Override
  public RandomAccessContent getRandomAccessContent( RandomAccessMode mode ) throws FileSystemException {
    return requireResolvedFileObject().getRandomAccessContent( mode );
  }

  @Override
  public void moveTo( FileObject destFile ) throws FileSystemException {
    requireResolvedFileObject().moveTo( destFile );
  }

  @Override
  public void refresh() throws FileSystemException {
    requireResolvedFileObject().refresh();
  }

  /**
   * Gets the resolved file object.
   * @return The resolved file object, if one is available; {@code null}, otherwise.
   */
  @Nullable
  public abstract FileObject getResolvedFileObject();

  // see also BACKLOG-40732
  /**
   * Gets the resolved file object, throwing if it is not available.
   * <p>
   * Implementers of this method should throw an exception that makes it clear the reason for a resolved file object
   * not being available, such as being the PVFS root or an undefined connection.
   *
   * @return The resolved file object.
   */
  @NonNull
  protected abstract AbstractFileObject<?> requireResolvedFileObject() throws FileSystemException;

  @Override
  public String getOriginalURIString() {
    return this.getName().toString();
  }

  @Override
  public String getAELSafeURIString() {
    throw new UnsupportedOperationException( String.format(
      "This connection file object does not support this operation: '%s'",
      this.getOriginalURIString() ) );
  }
}
