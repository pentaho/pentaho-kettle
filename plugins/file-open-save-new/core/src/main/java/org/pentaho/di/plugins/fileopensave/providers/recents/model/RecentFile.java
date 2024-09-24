/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2023 by Hitachi Vantara : http://www.pentaho.com
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
