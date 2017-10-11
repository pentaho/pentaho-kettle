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

/**
 * The logging hierarchy of a transformation or job
 *
 * @author matt
 *
 */
public class LoggingHierarchy {
  private String rootChannelId; // from the xform or job
  private long batchId; // from the xform or job
  private LoggingObjectInterface loggingObject;

  /**
   * @return the rootChannelId
   */
  public String getRootChannelId() {
    return rootChannelId;
  }

  /**
   * @param rootChannelId
   *          the rootChannelId to set
   */
  public void setRootChannelId( String rootChannelId ) {
    this.rootChannelId = rootChannelId;
  }

  /**
   * @return the batchId
   */
  public long getBatchId() {
    return batchId;
  }

  /**
   * @param batchId
   *          the batchId to set
   */
  public void setBatchId( long batchId ) {
    this.batchId = batchId;
  }

  /**
   * @return the loggingObject
   */
  public LoggingObjectInterface getLoggingObject() {
    return loggingObject;
  }

  /**
   * @param loggingObject
   *          the loggingObject to set
   */
  public void setLoggingObject( LoggingObjectInterface loggingObject ) {
    this.loggingObject = loggingObject;
  }

  /**
   * @param rootChannelId
   * @param batchId
   * @param loggingObject
   */
  public LoggingHierarchy( String rootChannelId, long batchId, LoggingObjectInterface loggingObject ) {
    this.rootChannelId = rootChannelId;
    this.batchId = batchId;
    this.loggingObject = loggingObject;
  }

}
