/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.ael.websocket;

import org.pentaho.di.engine.api.events.DataEvent;
import org.pentaho.di.engine.api.events.ErrorEvent;
import org.pentaho.di.engine.api.events.LogEvent;
import org.pentaho.di.engine.api.events.MetricsEvent;
import org.pentaho.di.engine.api.events.StatusEvent;
import org.pentaho.di.engine.api.model.ModelType;
import org.pentaho.di.engine.api.remote.RemoteSource;
import org.pentaho.di.engine.api.remote.StopMessage;

public class Util {

  private static final LogEvent TRANSFORMATION_LOG    = new LogEvent( new RemoteSource( ModelType.TRANSFORMATION ), null );
  private static final StatusEvent TRANSFORMATION_STATUS = new StatusEvent( new RemoteSource( ModelType.TRANSFORMATION ), null );
  private static final ErrorEvent TRANSFORMATION_ERROR = new ErrorEvent( new RemoteSource( ModelType.TRANSFORMATION ), null );
  private static final StopMessage STOP_MESSAGE = new StopMessage( "" );

  public static LogEvent getTransformationLogEvent() {
    return TRANSFORMATION_LOG;
  }

  public static StatusEvent getTransformationStatusEvent() {
    return TRANSFORMATION_STATUS;
  }

  public static ErrorEvent getTransformationErrorEvent() {
    return TRANSFORMATION_ERROR;
  }

  public static StopMessage getStopMessage() {
    return STOP_MESSAGE;
  }

  public static LogEvent getOperationLogEvent( String operationID ) {
    return new LogEvent( new RemoteSource( ModelType.OPERATION, operationID ), null );
  }

  public static StatusEvent getOperationStatusEvent( String operationID ) {
    return new StatusEvent( new RemoteSource( ModelType.OPERATION, operationID ), null );
  }

  public static DataEvent getOperationRowEvent( String operationID ) {
    return new DataEvent( new RemoteSource( ModelType.OPERATION, operationID ), null );
  }

  public static MetricsEvent getMetricEvents( String operationID ) {
    return new MetricsEvent( new RemoteSource( ModelType.OPERATION, operationID ), null );
  }
}
