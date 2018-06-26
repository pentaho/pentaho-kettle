/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.adapters;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.MetricsInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ccaspanello on 6/26/18.
 */
public class TestLogger implements LogChannelInterface {

  private static final Logger LOG = LoggerFactory.getLogger( TestLogger.class );

  @Override
  public String getLogChannelId() {
    return null;
  }

  @Override
  public LogLevel getLogLevel() {
    return null;
  }

  @Override
  public void setLogLevel( LogLevel logLevel ) {

  }

  @Override
  public String getContainerObjectId() {
    return null;
  }

  @Override
  public void setContainerObjectId( String containerObjectId ) {

  }

  @Override
  public String getFilter() {
    return null;
  }

  @Override
  public void setFilter( String filter ) {

  }

  @Override
  public boolean isBasic() {
    return false;
  }

  @Override
  public boolean isDetailed() {
    return false;
  }

  @Override
  public boolean isDebug() {
    return false;
  }

  @Override
  public boolean isRowLevel() {
    return false;
  }

  @Override
  public boolean isError() {
    return false;
  }

  @Override
  public void logMinimal( String message ) {
    LOG.info( message );
  }

  @Override
  public void logMinimal( String message, Object... arguments ) {
    LOG.info( message, arguments );
  }

  @Override
  public void logBasic( String message ) {
    LOG.info( message );
  }

  @Override
  public void logBasic( String message, Object... arguments ) {
    LOG.info( message, arguments );
  }

  @Override
  public void logDetailed( String message ) {
    LOG.info( message, message );
  }

  @Override
  public void logDetailed( String message, Object... arguments ) {
    LOG.info( message, arguments );
  }

  @Override
  public void logDebug( String message ) {
    LOG.info( message );
  }

  @Override
  public void logDebug( String message, Object... arguments ) {
    LOG.info( message, arguments );
  }

  @Override
  public void logRowlevel( String message ) {

  }

  @Override
  public void logRowlevel( String message, Object... arguments ) {

  }

  @Override
  public void logError( String message ) {
    LOG.error( message );
  }

  @Override
  public void logError( String message, Throwable e ) {
    LOG.error( message, e );
  }

  @Override
  public void logError( String message, Object... arguments ) {
    LOG.error( message, arguments );
  }

  @Override
  public boolean isGatheringMetrics() {
    return false;
  }

  @Override
  public void setGatheringMetrics( boolean gatheringMetrics ) {

  }

  @Override
  public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {

  }

  @Override
  public boolean isForcingSeparateLogging() {
    return false;
  }

  @Override
  public void snap( MetricsInterface metric, long... value ) {

  }

  @Override
  public void snap( MetricsInterface metric, String subject, long... value ) {

  }
}
