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


  default void setHooks( LoggingObjectLifecycleInterface loggingObjectLifecycleInterface ) {
    logBasic( "This implementation was not overridden, please implement in your own implementation" );
  }

  default LoggingObjectLifecycleInterface getHooks() {
    logBasic( "This implementation was not overridden, please implement in your own implementation" );
    return null;
  }
}
