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

package com.pentaho.di.purge;

import java.util.Date;

import org.apache.logging.log4j.Level;

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
