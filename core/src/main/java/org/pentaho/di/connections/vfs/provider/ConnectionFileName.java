/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;

import java.util.Objects;

public class ConnectionFileName extends AbstractFileName {

  private String connection;

  public ConnectionFileName( String scheme, String connection, String absPath, FileType type ) {
    super( scheme, absPath, type );
    this.connection = connection;
  }

  public FileName createName( String absPath, FileType type ) {
    return new ConnectionFileName( getScheme(), connection, absPath, type );
  }

  protected void appendRootUri( StringBuilder buffer, boolean addPassword ) {
    buffer.append( getScheme() );
    buffer.append( "://" );
    buffer.append( connection );
  }

  public String getConnection() {
    return connection;
  }

  public void setConnection( String connection ) {
    this.connection = connection;
  }

  @Override public boolean equals( Object o ) {
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

  @Override public int hashCode() {
    return Objects.hash( super.hashCode(), connection );
  }
}
