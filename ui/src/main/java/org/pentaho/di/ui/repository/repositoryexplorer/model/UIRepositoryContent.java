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

import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;

public abstract class UIRepositoryContent extends UIRepositoryObject {

  private static final long serialVersionUID = -1376494760112305976L;

  protected RepositoryElementMetaInterface rc;
  protected UIRepositoryDirectory uiParent;

  public UIRepositoryContent() {
    super();
  }

  public UIRepositoryContent( RepositoryElementMetaInterface rc, UIRepositoryDirectory parent, Repository rep ) {
    super( rc, rep );
    this.rc = rc;
    this.uiParent = parent;
  }

  @Override
  public String getDescription() {
    return rc.getDescription();
  }

  @Override
  public Date getModifiedDate() {
    return rc.getModifiedDate();
  }

  @Override
  public String getModifiedUser() {
    return rc.getModifiedUser();
  }

  @Override
  public String getType() {
    return rc.getObjectType().name();
  }

  @Override
  public String getFormatModifiedDate() {
    Date date = rc.getModifiedDate();
    String str = null;
    if ( date != null ) {
      SimpleDateFormat sdf = new SimpleDateFormat( "d MMM yyyy HH:mm:ss z" );
      str = sdf.format( date );
    }
    return str;
  }

  // TODO: Remove references to the Kettle object RepositoryDirectory
  public RepositoryDirectory getRepositoryDirectory() {
    return uiParent.getDirectory();
  }

  public RepositoryObjectType getRepositoryElementType() {
    return rc.getObjectType();
  }

  public void setName( String name ) throws Exception {
    if ( rc.getName().equalsIgnoreCase( name ) ) {
      return;
    }
    rc.setName( name );
  }

  @Override
  public String getImage() {
    // TODO: a generic image for unknown content?
    return "";
  }

  @Override
  public void delete() throws Exception {

  }

  @Override
  public void move( UIRepositoryDirectory newParentDir ) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getCategory() {
    return 20;
  }

  public UIRepositoryDirectory getParent() {
    return uiParent;
  }
}
