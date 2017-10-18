/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryAttributeInterface;

public class KettleDatabaseRepositoryTransAttribute implements RepositoryAttributeInterface {

  private KettleDatabaseRepositoryConnectionDelegate connectionDelegate;
  private ObjectId transObjectId;

  public KettleDatabaseRepositoryTransAttribute( KettleDatabaseRepositoryConnectionDelegate connectionDelegate,
    ObjectId transObjectId ) {
    this.connectionDelegate = connectionDelegate;
    this.transObjectId = transObjectId;
  }

  public boolean getAttributeBoolean( String code ) throws KettleException {
    return connectionDelegate.getTransAttributeBoolean( transObjectId, 0, code );
  }

  public long getAttributeInteger( String code ) throws KettleException {
    return connectionDelegate.getTransAttributeInteger( transObjectId, 0, code );
  }

  public String getAttributeString( String code ) throws KettleException {
    return connectionDelegate.getTransAttributeString( transObjectId, 0, code );
  }

  public void setAttribute( String code, String value ) throws KettleException {
    connectionDelegate.insertTransAttribute( transObjectId, 0, code, 0, value );
  }

  public void setAttribute( String code, boolean value ) throws KettleException {
    connectionDelegate.insertTransAttribute( transObjectId, 0, code, 0, value ? "Y" : "N" );
  }

  public void setAttribute( String code, long value ) throws KettleException {
    connectionDelegate.insertTransAttribute( transObjectId, 0, code, value, null );
  }
}
