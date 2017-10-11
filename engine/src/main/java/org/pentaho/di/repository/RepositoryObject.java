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

package org.pentaho.di.repository;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Contains some common object details, extracted from a repository
 *
 * @author Matt
 *
 */
public class RepositoryObject implements RepositoryElementMetaInterface {

  @Deprecated
  public static final String STRING_OBJECT_TYPE_TRANSFORMATION = "Transformation";
  @Deprecated
  public static final String STRING_OBJECT_TYPE_JOB = "Job";

  private String name;
  private RepositoryDirectoryInterface repositoryDirectory;
  private String modifiedUser;
  private Date modifiedDate;
  private RepositoryObjectType objectType;
  private String description;
  private boolean deleted;
  private ObjectId objectId;

  public RepositoryObject() {
  }

  /**
   * @param name
   * @param modifiedUser
   * @param modifiedDate
   */
  public RepositoryObject( ObjectId objectId, String name, RepositoryDirectoryInterface repositoryDirectory,
    String modifiedUser, Date modifiedDate, RepositoryObjectType objectType, String description, boolean deleted ) {
    this();
    this.objectId = objectId;
    this.name = name;
    this.repositoryDirectory = repositoryDirectory;
    this.modifiedUser = modifiedUser;
    this.modifiedDate = modifiedDate;
    this.objectType = objectType;
    this.description = description;
    this.deleted = deleted;
  }

  /**
   * @return the modifiedDate
   */
  public Date getModifiedDate() {
    return modifiedDate;
  }

  /**
   * @param modifiedDate
   *          the modifiedDate to set
   */
  public void setModifiedDate( Date modifiedDate ) {
    this.modifiedDate = modifiedDate;
  }

  /**
   * @return the modifiedUser
   */
  public String getModifiedUser() {
    return modifiedUser;
  }

  /**
   * @param modifiedUser
   *          the modifiedUser to set
   */
  public void setModifiedUser( String modifiedUser ) {
    this.modifiedUser = modifiedUser;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  public static final int compareStrings( String one, String two ) {
    if ( one == null && two == null ) {
      return 0;
    }
    if ( one == null && two != null ) {
      return -1;
    }
    if ( one != null && two == null ) {
      return 1;
    }
    return one.compareToIgnoreCase( two );
  }

  public static final int compareDates( Date one, Date two ) {
    if ( one == null && two == null ) {
      return 0;
    }
    if ( one == null && two != null ) {
      return -1;
    }
    if ( one != null && two == null ) {
      return 1;
    }
    return one.compareTo( two );
  }

  public static final void sortRepositoryObjects( List<RepositoryElementMetaInterface> objects,
    final int sortPosition, final boolean ascending ) {
    Collections.sort( objects, new Comparator<RepositoryElementMetaInterface>() {
      public int compare( RepositoryElementMetaInterface r1, RepositoryElementMetaInterface r2 ) {
        int result = 0;

        switch ( sortPosition ) {
          case 0:
            result = compareStrings( r1.getName(), r2.getName() );
            break;
          case 1:
            result =
              compareStrings( r1.getObjectType().getTypeDescription(), r2.getObjectType().getTypeDescription() );
            break;
          case 2:
            result = compareStrings( r1.getModifiedUser(), r2.getModifiedUser() );
            break;
          case 3:
            result = compareDates( r1.getModifiedDate(), r2.getModifiedDate() );
            break;
          case 4:
            result = compareStrings( r1.getDescription(), r2.getDescription() );
            break;
          default:
            break;
        }

        if ( !ascending ) {
          result *= -1;
        }

        return result;
      }
    } );
  }

  /**
   * @return the objectType
   */
  public RepositoryObjectType getObjectType() {
    return objectType;
  }

  /**
   * @param objectType
   *          the objectType to set
   */
  public void setObjectType( RepositoryObjectType objectType ) {
    this.objectType = objectType;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * @return the deleted
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * @param deleted
   *          the deleted to set
   */
  public void setDeleted( boolean deleted ) {
    this.deleted = deleted;
  }

  /**
   * @return the repositoryDirectory
   */
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    return repositoryDirectory;
  }

  /**
   * @param repositoryDirectory
   *          the repositoryDirectory to set
   */
  public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory ) {
    this.repositoryDirectory = repositoryDirectory;
  }

  /**
   * @return the objectId
   */
  public ObjectId getObjectId() {
    return objectId;
  }

  /**
   * @param objectId
   *          the objectId to set
   */
  public void setObjectId( ObjectId objectId ) {
    this.objectId = objectId;
  }

}
