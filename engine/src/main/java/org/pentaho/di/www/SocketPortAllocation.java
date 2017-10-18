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

package org.pentaho.di.www;

import java.util.Date;

public class SocketPortAllocation {
  private boolean allocated;
  private int port;
  private Date lastRequested;

  private String transformationName;
  private String clusterRunId;
  private String sourceSlaveName;
  private String sourceStepName;
  private String sourceStepCopy;
  private String targetSlaveName;
  private String targetStepName;
  private String targetStepCopy;

  /**
   * @param port
   * @param lastRequested
   * @param slaveName
   * @param transformationName
   * @param sourceStepName
   * @param sourceStepCopy
   */
  public SocketPortAllocation( int port, Date lastRequested, String clusterRunId, String transformationName,
    String sourceSlaveName, String sourceStepName, String sourceStepCopy, String targetSlaveName,
    String targetStepName, String targetStepCopy ) {
    this.port = port;
    this.lastRequested = lastRequested;
    this.clusterRunId = clusterRunId;
    this.transformationName = transformationName;
    this.sourceSlaveName = sourceSlaveName;
    this.sourceStepName = sourceStepName;
    this.sourceStepCopy = sourceStepCopy;

    this.targetSlaveName = targetSlaveName;
    this.targetStepName = targetStepName;
    this.targetStepCopy = targetStepCopy;
    this.allocated = true;
  }

  /**
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * @param port
   *          the port to set
   */
  public void setPort( int port ) {
    this.port = port;
  }

  public boolean equals( Object obj ) {
    if ( obj == this ) {
      return true;
    }
    if ( !( obj instanceof SocketPortAllocation ) ) {
      return false;
    }

    SocketPortAllocation allocation = (SocketPortAllocation) obj;

    return allocation.getPort() == port;
  }

  public int hashCode() {
    return Integer.valueOf( port ).hashCode();
  }

  /**
   * @return the lastRequested
   */
  public Date getLastRequested() {
    return lastRequested;
  }

  /**
   * @param lastRequested
   *          the lastRequested to set
   */
  public void setLastRequested( Date lastRequested ) {
    this.lastRequested = lastRequested;
  }

  /**
   * @return the transformationName
   */
  public String getTransformationName() {
    return transformationName;
  }

  /**
   * @param transformationName
   *          the transformationName to set
   */
  public void setTransformationName( String transformationName ) {
    this.transformationName = transformationName;
  }

  /**
   * @return the allocated
   */
  public boolean isAllocated() {
    return allocated;
  }

  /**
   * @param allocated
   *          the allocated to set
   */
  public void setAllocated( boolean allocated ) {
    this.allocated = allocated;
  }

  /**
   * @return the sourceStepName
   */
  public String getSourceStepName() {
    return sourceStepName;
  }

  /**
   * @param sourceStepName
   *          the sourceStepName to set
   */
  public void setSourceStepName( String sourceStepName ) {
    this.sourceStepName = sourceStepName;
  }

  /**
   * @return the sourceStepCopy
   */
  public String getSourceStepCopy() {
    return sourceStepCopy;
  }

  /**
   * @param sourceStepCopy
   *          the sourceStepCopy to set
   */
  public void setSourceStepCopy( String sourceStepCopy ) {
    this.sourceStepCopy = sourceStepCopy;
  }

  /**
   * @return the targetStepName
   */
  public String getTargetStepName() {
    return targetStepName;
  }

  /**
   * @param targetStepName
   *          the targetStepName to set
   */
  public void setTargetStepName( String targetStepName ) {
    this.targetStepName = targetStepName;
  }

  /**
   * @return the targetStepCopy
   */
  public String getTargetStepCopy() {
    return targetStepCopy;
  }

  /**
   * @param targetStepCopy
   *          the targetStepCopy to set
   */
  public void setTargetStepCopy( String targetStepCopy ) {
    this.targetStepCopy = targetStepCopy;
  }

  /**
   * @return the sourceSlaveName
   */
  public String getSourceSlaveName() {
    return sourceSlaveName;
  }

  /**
   * @param sourceSlaveName
   *          the sourceSlaveName to set
   */
  public void setSourceSlaveName( String sourceSlaveName ) {
    this.sourceSlaveName = sourceSlaveName;
  }

  /**
   * @return the targetSlaveName
   */
  public String getTargetSlaveName() {
    return targetSlaveName;
  }

  /**
   * @param targetSlaveName
   *          the targetSlaveName to set
   */
  public void setTargetSlaveName( String targetSlaveName ) {
    this.targetSlaveName = targetSlaveName;
  }

  /**
   * @return the carteObjectId
   */
  public String getClusterRunId() {
    return clusterRunId;
  }

  /**
   * @param clusterRunId
   *          the carteObjectId to set
   */
  public void setClusterRunId( String clusterRunId ) {
    this.clusterRunId = clusterRunId;
  }

}
