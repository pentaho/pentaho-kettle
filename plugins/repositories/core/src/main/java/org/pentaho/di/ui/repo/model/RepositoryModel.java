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

package org.pentaho.di.ui.repo.model;

/**
 * Created by bmorrise on 10/24/16.
 */
public class RepositoryModel {

  private String id;
  private String originalName;
  private String displayName;
  private String url;
  private String description;
  private String location;
  private Boolean isDefault = false;
  private Boolean doNotModify = false;
  private Boolean showHiddenFolders = false;
  private String databaseConnection;
  private Boolean edit;
  private Boolean modify;
  private Boolean connected;

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName( String displayName ) {
    this.displayName = displayName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl( String url ) {
    this.url = url;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public Boolean getIsDefault() {
    return isDefault;
  }

  public void setIsDefault( Boolean isDefault ) {
    this.isDefault = isDefault;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation( String location ) {
    this.location = location;
  }

  public Boolean getDoNotModify() {
    return doNotModify;
  }

  public void setDoNotModify( Boolean doNotModify ) {
    this.doNotModify = doNotModify;
  }

  public Boolean getShowHiddenFolders() {
    return showHiddenFolders;
  }

  public void setShowHiddenFolders( Boolean showHiddenFolders ) {
    this.showHiddenFolders = showHiddenFolders;
  }

  public String getDatabaseConnection() {
    return databaseConnection;
  }

  public void setDatabaseConnection( String databaseConnection ) {
    this.databaseConnection = databaseConnection;
  }

  public Boolean getEdit() {
    return edit;
  }

  public void setEdit( Boolean edit ) {
    this.edit = edit;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName( String originalName ) {
    this.originalName = originalName;
  }

  public Boolean getDefault() {
    return isDefault;
  }

  public void setDefault( Boolean aDefault ) {
    isDefault = aDefault;
  }

  public Boolean getModify() {
    return modify;
  }

  public void setModify( Boolean modify ) {
    this.modify = modify;
  }

  public Boolean getConnected() {
    return connected;
  }

  public void setConnected( Boolean connected ) {
    this.connected = connected;
  }
}
