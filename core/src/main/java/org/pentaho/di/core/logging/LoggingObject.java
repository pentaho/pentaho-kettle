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

package org.pentaho.di.core.logging;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

public class LoggingObject implements LoggingObjectInterface {

  private String logChannelId;
  private LoggingObjectType objectType;
  private String objectName;
  private String objectCopy;
  private RepositoryDirectoryInterface repositoryDirectory;
  private String filename;
  private ObjectId objectId;
  private ObjectRevision objectRevision;
  private LogLevel logLevel = DefaultLogLevel.getLogLevel();

  private String containerObjectId;

  private LoggingObjectInterface parent;

  private Date registrationDate;

  private boolean gatheringMetrics;
  private boolean forcingSeparateLogging;

  public LoggingObject( Object object ) {
    if ( object instanceof LoggingObjectInterface ) {
      grabLoggingObjectInformation( (LoggingObjectInterface) object );
    } else {
      grabObjectInformation( object );
    }
  }

  @Override
  public boolean equals( Object obj ) {
    if ( !( obj instanceof LoggingObject ) ) {
      return false;
    }
    if ( obj == this ) {
      return true;
    }

    try {
      LoggingObject loggingObject = (LoggingObject) obj;

      // No carte object id specified on either side OR the same carte object id means: the same family.
      //
      boolean sameCarteFamily =
        ( getContainerObjectId() == null && loggingObject.getContainerObjectId() == null )
          || ( getContainerObjectId() != null && loggingObject.getContainerObjectId() != null && getContainerObjectId()
            .equals( loggingObject.getContainerObjectId() ) );

      // See if we recognize the repository ID, this is an absolute match
      //
      if ( sameCarteFamily
        && loggingObject.getObjectId() != null && loggingObject.getObjectId().equals( getObjectId() ) ) {
        return true;
      }

      // Check if objects have the same parent
      boolean sameParents =
          loggingObject.getParent() == null && this.getParent() == null || loggingObject.getParent() != null
              && this.getParent() != null && loggingObject.getParent().equals( this.getParent() );

      // If the filename is the same and parent is the same, it's the same object...
      if ( sameCarteFamily && !Utils.isEmpty( loggingObject.getFilename() )
          && loggingObject.getFilename().equals( getFilename() ) && sameParents
              && StringUtils.equals( loggingObject.getObjectName(), getObjectName() ) ) {
        return true;
      }

      // See if the carte family, the name & type and parent name & type is the same.
      // This will catch most matches except for the most exceptional use-case.
      //
      if ( !sameCarteFamily
        || ( loggingObject.getObjectName() == null && getObjectName() != null )
        || ( loggingObject.getObjectName() != null && getObjectName() == null ) ) {
        return false;
      }

      if ( sameCarteFamily
        && ( ( loggingObject.getObjectName() == null && getObjectName() == null ) || ( loggingObject
          .getObjectName().equals( getObjectName() ) ) )
        && loggingObject.getObjectType().equals( getObjectType() ) ) {

        // If there are multiple copies of this object, they both need their own channel
        //
        if ( !Utils.isEmpty( getObjectCopy() ) && !getObjectCopy().equals( loggingObject.getObjectCopy() ) ) {
          return false;
        }

        LoggingObjectInterface parent1 = loggingObject.getParent();
        LoggingObjectInterface parent2 = getParent();

        if ( ( parent1 != null && parent2 == null ) || ( parent1 == null && parent2 != null ) ) {
          return false;
        }
        if ( parent1 == null && parent2 == null ) {
          return true;
        }

        // This goes to the parent recursively...
        //
        if ( parent1.equals( parent2 ) ) {
          return true;
        }
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    return false;
  }

  private void grabLoggingObjectInformation( LoggingObjectInterface loggingObject ) {
    objectType = loggingObject.getObjectType();
    objectName = loggingObject.getObjectName();
    repositoryDirectory = loggingObject.getRepositoryDirectory();
    filename = loggingObject.getFilename();
    objectId = loggingObject.getObjectId();
    objectRevision = loggingObject.getObjectRevision();
    objectCopy = loggingObject.getObjectCopy();
    logLevel = loggingObject.getLogLevel();
    containerObjectId = loggingObject.getContainerObjectId();
    forcingSeparateLogging = loggingObject.isForcingSeparateLogging();
    gatheringMetrics = loggingObject.isGatheringMetrics();

    if ( loggingObject.getParent() != null ) {
      getParentLoggingObject( loggingObject.getParent() );
      // inherit the containerObjectId from parent
      containerObjectId = loggingObject.getParent().getContainerObjectId();
    }
  }

  private void grabObjectInformation( Object object ) {
    objectType = LoggingObjectType.GENERAL;
    objectName = object.toString(); // name of class or name of object..

    parent = null;
  }

  private void getParentLoggingObject( Object parentObject ) {

    if ( parentObject == null ) {
      return;
    }

    if ( parentObject instanceof LoggingObjectInterface ) {

      parent = (LoggingObjectInterface) parentObject;

      // See if the parent is already in the logging registry.
      // This prevents the logging registry from hanging onto Trans and Job objects that would continue to consume
      // memory
      //
      if ( parent.getLogChannelId() != null ) {
        LoggingObjectInterface parentLoggingObject =
          LoggingRegistry.getInstance().getLoggingObject( parent.getLogChannelId() );
        if ( parentLoggingObject != null ) {
          parent = parentLoggingObject;
        }
      }
      return;
    }

    LoggingRegistry registry = LoggingRegistry.getInstance();

    // Extract the hierarchy information from the parentObject...
    //
    LoggingObject check = new LoggingObject( parentObject );
    LoggingObjectInterface loggingObject = registry.findExistingLoggingSource( check );
    if ( loggingObject == null ) {
      String logChannelId = registry.registerLoggingSource( check );
      loggingObject = check;
      check.setLogChannelId( logChannelId );
    }

    parent = loggingObject;
  }

  /**
   * @return the name
   */
  @Override
  public String getObjectName() {
    return objectName;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setObjectName( String name ) {
    this.objectName = name;
  }

  /**
   * @return the repositoryDirectory
   */
  @Override
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    return repositoryDirectory;
  }

  /**
   * @param repositoryDirectory
   *          the repositoryDirectory to set
   */
  public void setRepositoryDirectory( RepositoryDirectory repositoryDirectory ) {
    this.repositoryDirectory = repositoryDirectory;
  }

  /**
   * @return the filename
   */
  @Override
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
   * @return the objectId
   */
  @Override
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

  /**
   * @return the objectRevision
   */
  @Override
  public ObjectRevision getObjectRevision() {
    return objectRevision;
  }

  /**
   * @param objectRevision
   *          the objectRevision to set
   */
  public void setObjectRevision( ObjectRevision objectRevision ) {
    this.objectRevision = objectRevision;
  }

  /**
   * @return the id
   */
  @Override
  public String getLogChannelId() {
    return logChannelId;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setLogChannelId( String logChannelId ) {
    this.logChannelId = logChannelId;
  }

  /**
   * @return the parent
   */
  @Override
  public LoggingObjectInterface getParent() {
    return parent;
  }

  /**
   * @param parent
   *          the parent to set
   */
  public void setParent( LoggingObjectInterface parent ) {
    this.parent = parent;
  }

  /**
   * @return the objectType
   */
  @Override
  public LoggingObjectType getObjectType() {
    return objectType;
  }

  /**
   * @param objectType
   *          the objectType to set
   */
  public void setObjectType( LoggingObjectType objectType ) {
    this.objectType = objectType;
  }

  /**
   * @return the copy
   */
  @Override
  public String getObjectCopy() {
    return objectCopy;
  }

  /**
   * @param copy
   *          the copy to set
   */
  public void setObjectCopy( String objectCopy ) {
    this.objectCopy = objectCopy;
  }

  @Override
  public LogLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel( LogLevel logLevel ) {
    this.logLevel = logLevel;
  }

  /**
   * @return the carteObjectId
   */
  @Override
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * @param carteObjectId
   *          the carteObjectId to set
   */
  public void setCarteObjectId( String carteObjectId ) {
    this.containerObjectId = carteObjectId;
  }

  /**
   * @return the registrationDate
   */
  @Override
  public Date getRegistrationDate() {
    return registrationDate;
  }

  /**
   * @param registrationDate
   *          the registrationDate to set
   */
  public void setRegistrationDate( Date registrationDate ) {
    this.registrationDate = registrationDate;
  }

  /**
   * @return the gatheringMetrics
   */
  @Override
  public boolean isGatheringMetrics() {
    return gatheringMetrics;
  }

  /**
   * @param gatheringMetrics
   *          the gatheringMetrics to set
   */
  @Override
  public void setGatheringMetrics( boolean gatheringMetrics ) {
    this.gatheringMetrics = gatheringMetrics;
  }

  /**
   * @return the forcingSeparateLogging
   */
  @Override
  public boolean isForcingSeparateLogging() {
    return forcingSeparateLogging;
  }

  /**
   * @param forcingSeparateLogging
   *          the forcingSeparateLogging to set
   */
  @Override
  public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {
    this.forcingSeparateLogging = forcingSeparateLogging;
  }
}
