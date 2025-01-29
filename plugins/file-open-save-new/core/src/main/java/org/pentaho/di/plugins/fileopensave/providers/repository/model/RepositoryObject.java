/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.plugins.fileopensave.providers.repository.model;

import org.pentaho.di.plugins.fileopensave.api.providers.BaseEntity;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 5/16/17.
 */
public abstract class RepositoryObject extends BaseEntity implements File {
  private String objectId;
  private boolean hasChildren = false;
  private String extension;
  private String repository;
  private boolean hidden = false;
  private List<RepositoryFile> children = new ArrayList<>();

  @Override public String getProvider() {
    return RepositoryFileProvider.TYPE;
  }

  public void addChild( RepositoryFile repositoryFile ) {
    this.children.add( repositoryFile );
  }

  public List<RepositoryFile> getChildren() {
    return children;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId( String objectId ) {
    this.objectId = objectId;
  }

  public boolean isHasChildren() {
    return hasChildren;
  }

  public void setHasChildren( boolean hasChildren ) {
    this.hasChildren = hasChildren;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension( String extension ) {
    this.extension = extension;
  }

  public String getRepository() {
    return repository;
  }

  public void setRepository( String repository ) {
    this.repository = repository;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  public EntityType getEntityType(){
    return EntityType.REPOSITORY_OBJECT;
  }
}
