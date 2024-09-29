/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2023 by Hitachi Vantara : http://www.pentaho.com
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
