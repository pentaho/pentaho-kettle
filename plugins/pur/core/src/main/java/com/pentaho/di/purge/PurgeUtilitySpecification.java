/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.di.purge;

import java.util.Date;

import org.apache.log4j.Level;

/**
 * @author tkafalas
 */
public class PurgeUtilitySpecification {
  String path;
  boolean purgeFiles; // If set, remove files in total rather then revisions
  boolean purgeRevisions; // If set, purge all revisions for a file. Ignored if purgeFiles is set.
  boolean sharedObjects; // If set, purge shared objects as well
  int versionCount = -1; // if not equal to -1, keep only the newest versionCount versions of a file
  Date beforeDate; // if not null, delete all revisions dated before beforeDate
  String fileFilter = "*"; // File filter used by Tree call
  Level logLevel = Level.INFO;

  public PurgeUtilitySpecification() {
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public boolean isPurgeFiles() {
    return purgeFiles;
  }

  public void setPurgeFiles( boolean purgeFiles ) {
    this.purgeFiles = purgeFiles;
  }

  public boolean isPurgeRevisions() {
    return purgeRevisions;
  }

  public void setPurgeRevisions( boolean purgeRevisions ) {
    this.purgeRevisions = purgeRevisions;
  }

  public int getVersionCount() {
    return versionCount;
  }

  public void setVersionCount( int versionCount ) {
    this.versionCount = versionCount;
  }

  public Date getBeforeDate() {
    return beforeDate;
  }

  public void setBeforeDate( Date beforeDate ) {
    this.beforeDate = beforeDate;
  }

  public String getFileFilter() {
    return fileFilter;
  }

  public void setFileFilter( String fileFilter ) {
    this.fileFilter = fileFilter;
  }

  public boolean isSharedObjects() {
    return sharedObjects;
  }

  public void setSharedObjects( boolean sharedObjects ) {
    this.sharedObjects = sharedObjects;
  }

  public Level getLogLevel() {
    return logLevel;
  }

  public void setLogLevel( Level logLevel ) {
    this.logLevel = logLevel;
  }

}
