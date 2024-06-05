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

package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;

import java.util.Objects;

public class ConnectionFileName extends AbstractFileName {

  @Nullable
  private String connection;

  public ConnectionFileName( @Nullable String connection ) {
    this( connection, SEPARATOR, FileType.FOLDER );
  }

  public ConnectionFileName( @Nullable String connection, @Nullable String absPath, @NonNull FileType type ) {
    super( ConnectionFileProvider.SCHEME, absPath, type );
    this.connection = StringUtils.isEmpty( connection ) ? null : connection;

    if ( this.connection == null && !SEPARATOR.equals( getPath() ) ) {
      throw new IllegalArgumentException( "The PVFS root must have a path of '/'." );
    }
  }

  @Override
  public ConnectionFileName createName( String absPath, FileType type ) {
    return new ConnectionFileName( connection, absPath, type );
  }

  public ConnectionFileName createChildName( String name, FileType type ) {
    String childAbsPath = getConnectionFileNameUtils().ensureTrailingSeparator( getPath() ) + name;
    return new ConnectionFileName( connection, childAbsPath, type );
  }

  @NonNull
  protected ConnectionFileNameUtils getConnectionFileNameUtils() {
    // Seems overkill to store in an instance field solely for being unit test injection.
    return ConnectionFileNameUtils.getInstance();
  }

  @Override
  protected void appendRootUri( StringBuilder buffer, boolean addPassword ) {
    buffer.append( getScheme() );
    buffer.append( "://" );

    // pvfs:// has no connection
    if ( connection != null ) {
      buffer.append( connection );
    }
  }

  @Override
  public String getRootURI() {
    // Ensure the PVFS root gets a URI of `pvfs://`, and not `pvfs:///`.
    if ( isPvfsRoot() ) {
      StringBuilder buffer = new StringBuilder();
      appendRootUri( buffer, false );
      return buffer.toString();
    }

    return super.getRootURI();
  }

  @Override
  protected String createURI() {
    // Ensure the root gets a URI of `pvfs://`, and not `pvfs:///`.
    if ( isPvfsRoot() ) {
      return getRootURI();
    }

    return super.createURI();
  }

  @Override
  public String getFriendlyURI() {
    // Ensure the root gets a URI of `pvfs://`, and not `pvfs:///`..
    return getURI();
  }

  /**
   * Gets the connection name.
   *
   * @return The connection name.
   */
  @Nullable
  public String getConnection() {
    return connection;
  }

  /**
   * Indicates if the file name is that of the PVFS root, "pvfs://".
   *
   * @return {@code true} if so; {@code false}, otherwise.
   */
  public boolean isPvfsRoot() {
    return connection == null;
  }

  /**
   * Indicates if the file name is that of a connection root, e.g. "pvfs://connection-name/".
   *
   * @return {@code true} if so; {@code false}, otherwise.
   */
  public boolean isConnectionRoot() {
    return !isPvfsRoot() && SEPARATOR.equals( getPath() );
  }

  /**
   * Sets the connection name.
   *
   * @param connection The connection name.
   */
  public void setConnection( @Nullable String connection ) {
    this.connection = connection;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }

    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    if ( !super.equals( o ) ) {
      return false;
    }

    ConnectionFileName that = (ConnectionFileName) o;
    return Objects.equals( connection, that.connection );
  }

  @Override
  public int hashCode() {
    return Objects.hash( super.hashCode(), connection );
  }
}
