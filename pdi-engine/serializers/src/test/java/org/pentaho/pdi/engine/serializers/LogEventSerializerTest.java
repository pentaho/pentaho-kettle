/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.pdi.engine.serializers;

import org.junit.Test;
import org.pentaho.di.engine.api.events.LogEvent;
import org.pentaho.di.engine.api.reporting.LogEntry;
import org.pentaho.di.engine.api.reporting.LogLevel;
import org.pentaho.di.engine.model.Transformation;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 3/23/17.
 */
public class LogEventSerializerTest {

  @Test
  public void testSerialization() throws Exception {
    Transformation transformation = mock( Transformation.class );
    when( transformation.getId() ).thenReturn( "foo" );

    LogEntry.LogEntryBuilder builder = new LogEntry.LogEntryBuilder();
    builder.withTimestamp( new Date() );
    builder.withMessage( "I am a Message" );
    builder.withExtras( Collections.singletonMap("foo", "bar") );
    builder.withLogLevel( LogLevel.DEBUG );
    builder.withThrowable( getException() );

    LogEntry logEntry = builder.build();
    LogEvent<Transformation> logEvent = new LogEvent<>( transformation, logEntry );
    LogEventSerializer serializer = new LogEventSerializer();

    assertTrue( serializer.getSupportedClasses().contains( LogEvent.class ) );

    String serialized = serializer.serialize( logEvent );

    LogEvent deserialized = serializer.deserialize( serialized );

    assertEquals( logEvent.getSource().getId(), deserialized.getSource().getId() );
    assertEquals( logEvent.getData(), deserialized.getData() );

  }

  public Throwable getException() {
    try {
      throw new IllegalStateException( "Error" );
    } catch ( Throwable t ) {
      return t;
    }
  }
}