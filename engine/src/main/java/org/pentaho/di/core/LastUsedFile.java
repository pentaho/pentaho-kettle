/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core;

import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.core.util.Utils;

import java.util.Date;

public class LastUsedFile {
  public static final String FILE_TYPE_TRANSFORMATION = "Trans";
  public static final String FILE_TYPE_JOB = "Job";
  public static final String FILE_TYPE_SCHEMA = "Schema";
  public static final String FILE_TYPE_CUSTOM = "Custom";

  public static final int OPENED_ITEM_TYPE_MASK_NONE = 0;
  public static final int OPENED_ITEM_TYPE_MASK_GRAPH = 1;
  public static final int OPENED_ITEM_TYPE_MASK_LOG = 2;
  public static final int OPENED_ITEM_TYPE_MASK_HISTORY = 4;

  private String fileType;
  private String filename;
  private String directory;
  private boolean sourceRepository;
  private String repositoryName;
  private Date lastOpened;
  private String username;

  private boolean opened;
  private int openItemTypes;

  /**
   * @param fileType
   *          The type of file to use (FILE_TYPE_TRANSFORMATION, FILE_TYPE_JOB, ...)
   * @param filename
   * @param directory
   * @param sourceRepository
   * @param repositoryName
   * @param opened
   * @param openItemTypes
   */
  public LastUsedFile( String fileType, String filename, String directory, boolean sourceRepository,
    String repositoryName, boolean opened, int openItemTypes ) {
    this( fileType, filename, directory, sourceRepository, repositoryName, null, opened, openItemTypes, null );
  }

  /**
   * @param fileType
   *          The type of file to use (FILE_TYPE_TRANSFORMATION, FILE_TYPE_JOB, ...)
   * @param filename
   * @param directory
   * @param sourceRepository
   * @param repositoryName
   * @param username
   * @param opened
   * @param openItemTypes
   */
  public LastUsedFile( String fileType, String filename, String directory, boolean sourceRepository,
    String repositoryName, String username, boolean opened, int openItemTypes, Date lastOpened ) {
    this.fileType = fileType;
    this.filename = filename;
    this.directory = directory;
    this.sourceRepository = sourceRepository;
    this.repositoryName = repositoryName;
    this.username = username;
    this.opened = opened;
    this.openItemTypes = openItemTypes;
    this.lastOpened = lastOpened == null ? new Date() : lastOpened;
  }

  public String toString() {
    String string = "";

    if ( sourceRepository && !Utils.isEmpty( directory ) && !Utils.isEmpty( repositoryName ) ) {
      string += "[" + repositoryName + "] ";

      if ( directory.endsWith( RepositoryDirectory.DIRECTORY_SEPARATOR ) ) {
        string += ": " + directory + filename;
      } else {
        string += ": " + directory + RepositoryDirectory.DIRECTORY_SEPARATOR + filename;
      }
    } else {
      string += filename;
    }

    return string;
  }

  public int hashCode() {
    return ( getFileType() + toString() ).hashCode();
  }

  public boolean equals( Object obj ) {
    LastUsedFile file = (LastUsedFile) obj;
    return getFileType().equals( file.getFileType() ) && toString().equals( file.toString() );
  }

  /**
   * @return the directory
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * @param directory
   *          the directory to set
   */
  public void setDirectory( String directory ) {
    this.directory = directory;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          the filename to set
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * @return the repositoryName
   */
  public String getRepositoryName() {
    return repositoryName;
  }

  /**
   * @param repositoryName
   *          the repositoryName to set
   */
  public void setRepositoryName( String repositoryName ) {
    this.repositoryName = repositoryName;
  }

  /**
   * @return the sourceRepository
   */
  public boolean isSourceRepository() {
    return sourceRepository;
  }

  /**
   * @param sourceRepository
   *          the sourceRepository to set
   */
  public void setSourceRepository( boolean sourceRepository ) {
    this.sourceRepository = sourceRepository;
  }

  /**
   * @return the fileType
   */
  public String getFileType() {
    return fileType;
  }

  /**
   * @param fileType
   *          the fileType to set
   */
  public void setFileType( String fileType ) {
    this.fileType = fileType;
  }

  public boolean isTransformation() {
    return FILE_TYPE_TRANSFORMATION.equalsIgnoreCase( fileType );
  }

  public boolean isJob() {
    return FILE_TYPE_JOB.equalsIgnoreCase( fileType );
  }

  public boolean isSchema() {
    return FILE_TYPE_SCHEMA.equalsIgnoreCase( fileType );
  }

  /**
   * @return the opened
   */
  public boolean isOpened() {
    return opened;
  }

  /**
   * @param opened
   *          the opened to set
   */
  public void setOpened( boolean opened ) {
    this.opened = opened;
  }

  /**
   * @return the openItemTypes
   */
  public int getOpenItemTypes() {
    return openItemTypes;
  }

  /**
   * @param openItemTypes
   *          the openItemTypes to set
   */
  public void setOpenItemTypes( int openItemTypes ) {
    this.openItemTypes = openItemTypes;
  }

  public Date getLastOpened() {
    return lastOpened;
  }

  public void setLastOpened( Date lastOpened ) {
    this.lastOpened = lastOpened;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }
}
