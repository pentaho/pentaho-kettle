/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIDatabaseConnection extends XulEventSourceAdapter {

  protected DatabaseMeta dbMeta;
  // inheriting classes may need access to the repository
  protected Repository rep;
  protected RepositoryElementMetaInterface repoElementMeta;

  public UIDatabaseConnection() {
    super();
  }

  public UIDatabaseConnection( DatabaseMeta databaseMeta, Repository rep ) {
    super();
    this.dbMeta = databaseMeta;
    this.rep = rep;
  }

  public String getName() {
    if ( dbMeta != null ) {
      return dbMeta.getName();
    }
    return null;
  }

  public String getDisplayName() {
    if ( dbMeta != null ) {
      return dbMeta.getDisplayName();
    }
    return null;
  }

  public String getType() {
    if ( dbMeta != null ) {
      return dbMeta.getPluginId();
    }
    return null;
  }

  public String getDateModified() {
    Date dbDate = null;
    if ( repoElementMeta != null && repoElementMeta.getModifiedDate() != null ) {
      dbDate = repoElementMeta.getModifiedDate();
    }
    if ( dbMeta != null && dbMeta.getChangedDate() != null ) {
      dbDate = dbMeta.getChangedDate();
    }
    if ( dbDate == null ) {
      return null;
    }
    SimpleDateFormat sdf = new SimpleDateFormat( "d MMM yyyy HH:mm:ss z" );
    return sdf.format( dbDate );
  }

  public void setRepositoryElementMetaInterface( RepositoryElementMetaInterface repoElementMeta ) {
    this.repoElementMeta = repoElementMeta;
  }

  public DatabaseMeta getDatabaseMeta() {
    return dbMeta;
  }

}
