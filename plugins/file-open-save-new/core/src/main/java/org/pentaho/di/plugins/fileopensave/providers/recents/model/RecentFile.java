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


package org.pentaho.di.plugins.fileopensave.providers.recents.model;

import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseEntity;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.recents.RecentFileProvider;

public class RecentFile extends BaseEntity implements File {

  private String type;
  private String repository;
  private String username;

  @Override public String getProvider() {
    return RecentFileProvider.TYPE;
  }

  public static RecentFile create( LastUsedFile lastUsedFile ) {
    RecentFile recentFile = new RecentFile();
    recentFile.setType( lastUsedFile.isTransformation() ? TRANSFORMATION : JOB );
    recentFile.setDate( lastUsedFile.getLastOpened() );
    recentFile.setRoot( RecentFileProvider.NAME );
    RecentUtils.setPaths( lastUsedFile, recentFile );
    return recentFile;
  }

  @Override public String getType() {
    return type;
  }

  @Override public void setType( String type ) {
    this.type = type;
  }

  public String getRepository() {
    return repository;
  }

  public void setRepository( String repository ) {
    this.repository = repository;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public EntityType getEntityType(){
    return EntityType.RECENT_FILE;
  }
}
