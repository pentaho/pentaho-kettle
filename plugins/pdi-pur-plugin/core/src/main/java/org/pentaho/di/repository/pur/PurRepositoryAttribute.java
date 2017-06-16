/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import java.util.List;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;

public class PurRepositoryAttribute implements RepositoryAttributeInterface, java.io.Serializable {

  private static final long serialVersionUID = -5787096049770518000L; /* EESOURCE: UPDATE SERIALVERUID */

  private DataNode dataNode;
  private List<DatabaseMeta> databases;

  public PurRepositoryAttribute( DataNode dataNode, List<DatabaseMeta> databases ) {
    this.dataNode = dataNode;
    this.databases = databases;
  }

  public void setAttribute( String code, String value ) {
    dataNode.setProperty( code, value );
  }

  public String getAttributeString( String code ) {
    DataProperty property = dataNode.getProperty( code );
    if ( property != null ) {
      return property.getString();
    }
    return null;
  }

  public void setAttribute( String code, boolean value ) {
    dataNode.setProperty( code, value );
  }

  public boolean getAttributeBoolean( String code ) {
    DataProperty property = dataNode.getProperty( code );
    if ( property != null ) {
      return property.getBoolean();
    }
    return false;
  }

  public void setAttribute( String code, long value ) {
    dataNode.setProperty( code, value );
  }

  public long getAttributeInteger( String code ) {
    DataProperty property = dataNode.getProperty( code );
    if ( property != null ) {
      return property.getLong();
    }
    return 0L;
  }

  public void setAttribute( String code, DatabaseMeta databaseMeta ) {
    dataNode.setProperty( code, databaseMeta.getObjectId().getId() );
  }

  public DatabaseMeta getAttributeDatabaseMeta( String code ) {
    DataProperty property = dataNode.getProperty( code );
    if ( property == null || Utils.isEmpty( property.getString() ) ) {
      return null;
    }
    ObjectId id = new StringObjectId( property.getString() );
    return DatabaseMeta.findDatabase( databases, id );
  }
}
