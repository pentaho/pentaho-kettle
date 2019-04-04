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

public interface LogChannelInterface {

  /**
   * @return the id of the logging channel
   */
  String getLogChannelId();

  LogLevel getLogLevel();

  void setLogLevel( LogLevel logLevel );

  /**
   * @return the containerObjectId
   */
  String getContainerObjectId();

  /**
   * @param containerObjectId
   *          the containerObjectId to set
   */
  void setContainerObjectId( String containerObjectId );

  String getFilter();

  void setFilter( String filter );

  boolean isBasic();

  boolean isDetailed();

  boolean isDebug();

  boolean isRowLevel();

  boolean isError();

  void logMinimal( String message );

  void logMinimal( String message, Object... arguments );

  void logBasic( String message );

  void logBasic( String message, Object... arguments );

  void logDetailed( String message );

  void logDetailed( String message, Object... arguments );

  void logDebug( String message );

  void logDebug( String message, Object... arguments );

  void logRowlevel( String message );

  void logRowlevel( String message, Object... arguments );

  void logError( String message );

  void logError( String message, Throwable e );

  void logError( String message, Object... arguments );

  boolean isGatheringMetrics();

  void setGatheringMetrics( boolean gatheringMetrics );

  /**
   * This option will force the create of a separate logging channel even if the logging concerns identical objects with
   * identical names.
   *
   * @param forcingSeparateLogging
   *          Set to true to force separate logging
   */
  void setForcingSeparateLogging( boolean forcingSeparateLogging );

  /**
   * @return True if the logging is forcibly separated out from even identical objects.
   */
  boolean isForcingSeparateLogging();

  /**
   * Add a snapshot to the metrics system for this log channel at the time of invocation. This will process the value
   * depending on the type of metric specified. For example, for MetricsInterface.look up the maximum value in the
   * metrics and replace it if the new value is higher. The snapshot date will be retained in that case.
   *
   * @param metric
   *          The metric to use (ex. connect to a database)
   * @param value
   *          the value to store
   */
  void snap( MetricsInterface metric, long... value );

  /**
   * Add a maximum snapshot to the metrics system for this log channel at the time of invocation. This will look up the
   * maximum value in the metrics and replace it if the new value is higher. The snapshot date will be retained in that
   * case.
   *
   * @param metric
   *          The metric to use (ex. connect to a database)
   * @param subject
   *          The subject (ex. the name of the database)
   * @param value
   *          the value to store
   */
  void snap( MetricsInterface metric, String subject, long... value );
}
