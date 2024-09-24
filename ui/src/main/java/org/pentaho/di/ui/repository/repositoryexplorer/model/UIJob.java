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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementMetaInterface;

public class UIJob extends UIRepositoryContent {

  private static final long serialVersionUID = -7673439096628166542L;

  private static final String REPOSITORY_PKG = "org.pentaho.di.ui.repository";

  public UIJob() {
  }

  public UIJob( RepositoryElementMetaInterface rc, UIRepositoryDirectory parent, Repository rep ) {
    super( rc, parent, rep );
  }

  @Override
  public void setName( String name ) throws Exception {
    renameJob( this.getObjectId(), getRepositoryDirectory(), name );
    super.setName( name );
    uiParent.fireCollectionChanged();
  }

  protected ObjectId renameJob( ObjectId objectId, RepositoryDirectory directory, String name ) throws Exception {
    String comment = BaseMessages.getString( REPOSITORY_PKG, "Repository.Rename", super.getName(), name );
    return rep.renameJob( this.getObjectId(), comment, getRepositoryDirectory(), name );
  }

  public void delete() throws Exception {
    rep.deleteJob( this.getObjectId() );
    if ( uiParent.getRepositoryObjects().contains( this ) ) {
      uiParent.getRepositoryObjects().remove( this );
    }
  }

  public void move( UIRepositoryDirectory newParentDir ) throws KettleException {
    if ( newParentDir != null ) {
      rep.renameJob( obj.getObjectId(), newParentDir.getDirectory(), null );
      newParentDir.refresh();
    }
  }

  @Override
  public String getImage() {
    return "ui/images/jobrepo.svg";
  }
}
