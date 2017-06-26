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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.pentaho.di.engine.api.events.LogEvent;
import org.pentaho.di.engine.api.remote.RemoteSource;
import org.pentaho.di.engine.api.reporting.LogEntry;
import org.pentaho.di.engine.api.reporting.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nbaker on 3/23/17.
 */
public class LogEventSerializer extends BaseSerializer<LogEvent> {

  public static final DateFormat DATE_TIME_INSTANCE = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );

  private Logger logger = LoggerFactory.getLogger( getClass() );

  public LogEventSerializer() {
    super( LogEvent.class );


    SimpleModule module = new SimpleModule();
    module.addSerializer( LogEvent.class, new JsonSerializer<LogEvent>() {
      @Override
      public void serialize( LogEvent logEvent, JsonGenerator jsonGenerator,
                             SerializerProvider serializerProvider )
        throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        LogEntry data = (LogEntry) logEvent.getData();

        jsonGenerator.writeStringField( "message", data.getMessage() );

        if( data.getThrowable() != null ) {
          StringWriter stackTrace = new StringWriter();
          data.getThrowable().printStackTrace( new PrintWriter( stackTrace ) );
          jsonGenerator.writeStringField( "stacktrace", stackTrace.toString() );
        }

        Map<String, String> extras = data.getExtras();
        if( extras.size() > 0 ) {
          jsonGenerator.writeArrayFieldStart("extras" );
          for ( Map.Entry<String, String> entry : extras.entrySet() ) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField( "key", entry.getKey() );
            jsonGenerator.writeStringField( "value", entry.getValue() );
            jsonGenerator.writeEndObject();
          }
          jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeStringField( "timestamp", DATE_TIME_INSTANCE.format( data.getTimestamp() ) );
        jsonGenerator.writeStringField( "level", data.getLogLogLevel().toString() );
        jsonGenerator.writeStringField( "model-id", logEvent.getSource().getId() );

        jsonGenerator.writeEndObject();
      }
    } );
    module.addDeserializer( LogEvent.class, new StdNodeBasedDeserializer<LogEvent>( LogEvent.class ) {
      @Override public LogEvent convert( JsonNode jsonNode, DeserializationContext deserializationContext )
        throws IOException {
        LogEntry.LogEntryBuilder builder = new LogEntry.LogEntryBuilder();

        builder.withLogLevel( LogLevel.valueOf( jsonNode.get( "level" ).asText() ) );
        try {
          builder.withTimestamp( DATE_TIME_INSTANCE.parse( jsonNode.get( "timestamp" ).asText() ) );
        } catch ( ParseException e ) {
          logger.error( "Error parsing Log timestamp: " + jsonNode.get( "timestamp" ) );
          builder.withTimestamp( new Date() );
        }
        if( jsonNode.has( "stacktrace" ) ) {
          // We cannot recreate the exception so it's added to the message
          builder.withMessage( jsonNode.get( "message" ).asText() + "\n\nStackTrace: \n" +jsonNode.get( "stacktrace" ).asText() );
        } else {

          builder.withMessage( jsonNode.get( "message" ).asText() );
        }

        if( jsonNode.has( "extras" ) ) {
          Map<String, String> extras = new HashMap<>();

          for ( JsonNode extra : jsonNode.get( "extras" ) ) {
            extras.put( extra.get( "key" ).asText(), extra.get( "value" ).asText() );
          }
          builder.withExtras( extras );
        }

        LogEntry entry = builder.build();

        return new LogEvent( new RemoteSource( jsonNode.get( "model-id" ).asText() ), entry );

      }
    } );
    mapper.registerModule( module );

  }


}
