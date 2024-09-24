/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
