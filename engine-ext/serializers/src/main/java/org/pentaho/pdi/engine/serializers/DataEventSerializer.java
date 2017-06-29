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
import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.engine.api.events.DataEvent;
import org.pentaho.di.engine.api.model.Row;
import org.pentaho.di.engine.api.model.Rows;
import org.pentaho.di.engine.api.remote.RemoteSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nbaker on 3/4/17.
 */
public class DataEventSerializer extends BaseSerializer<DataEvent> {

  public static final DateFormat DATE_TIME_INSTANCE = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );

  public DataEventSerializer() {
    super( DataEvent.class );

    SimpleModule module = new SimpleModule();
    module.addSerializer( DataEvent.class, new JsonSerializer<DataEvent>() {
      @Override
      public void serialize( DataEvent dataEvent, JsonGenerator jsonGenerator,
                             SerializerProvider serializerProvider )
        throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        Rows rows = (Rows) dataEvent.getData();
        jsonGenerator.writeStringField( "model-id", dataEvent.getSource().getId() );
        jsonGenerator.writeStringField( "type", rows.getType().toString() );
        jsonGenerator.writeStringField( "state", rows.getState().toString() );

        jsonGenerator.writeArrayFieldStart( "rows" );
        for ( Row row : rows ) {
          jsonGenerator.writeStartObject();
          jsonGenerator.writeArrayFieldStart( "names" );
          for ( String name : row.getColumnNames() ) {
            jsonGenerator.writeString( name );
          }
          jsonGenerator.writeEndArray();

          jsonGenerator.writeArrayFieldStart( "objects" );
          for ( Object obj : row.getObjects() ) {
            jsonGenerator.writeStartObject();
            if ( obj == null ) {
              jsonGenerator.writeStringField( "type", "Null" );
              jsonGenerator.writeEndObject();
              continue;
            }
            switch ( obj.getClass().getSimpleName() ) {
              case "String":
                jsonGenerator.writeStringField( "type", "String" );
                jsonGenerator.writeStringField( "obj", obj.toString() );
                break;
              case "Date":
                jsonGenerator.writeStringField( "type", "Date" );
                jsonGenerator.writeStringField( "obj", DATE_TIME_INSTANCE.format( (Date) obj ) );
                break;
              case "Integer":
                jsonGenerator.writeStringField( "type", "Integer" );
                jsonGenerator.writeNumberField( "obj", (Integer) obj );
                break;
              case "Long":
                jsonGenerator.writeStringField( "type", "Long" );
                jsonGenerator.writeNumberField( "obj", (Long) obj );
                break;
              case "Double":
                jsonGenerator.writeStringField( "type", "Double" );
                jsonGenerator.writeNumberField( "obj", (Double) obj );
                break;
              case "BigDecimal":
                jsonGenerator.writeStringField( "type", "BigDecimal" );
                jsonGenerator.writeStringField( "obj", obj.toString() );
                break;
              case "Boolean":
                jsonGenerator.writeStringField( "type", "Boolean" );
                jsonGenerator.writeBooleanField( "obj", (Boolean) obj );
                break;
              case "byte[]":
                jsonGenerator.writeStringField( "type", "byte[]" );
                jsonGenerator.writeStringField( "obj", new String( ( (byte[]) obj ), "UTF-8" ) );
                break;
              default:
                if ( obj instanceof Serializable ) {
                  jsonGenerator.writeStringField( "type", "Object" );

                  ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                  ObjectOutputStream objectOutputStream = new ObjectOutputStream( outputStream );
                  objectOutputStream.writeObject( obj );
                  objectOutputStream.close();
                  outputStream.close();
                  byte[] bytes = outputStream.toByteArray();
                  jsonGenerator.writeStringField( "obj", Base64.encodeBase64String( bytes ) );
                }
            }
            jsonGenerator.writeEndObject();
          }
          jsonGenerator.writeEndArray();
          jsonGenerator.writeEndObject();

        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
      }
    } );
    module.addDeserializer( DataEvent.class, new StdNodeBasedDeserializer<DataEvent>( DataEvent.class ) {
      @Override public DataEvent convert( JsonNode jsonNode, DeserializationContext deserializationContext )
        throws IOException {

        Rows.TYPE type = Rows.TYPE.valueOf( jsonNode.get( "type" ).asText() );
        Rows.STATE state = Rows.STATE.valueOf( jsonNode.get( "state" ).asText() );

        List<Row> rows = new ArrayList<>();

        JsonNode json_rows = jsonNode.get( "rows" );
        for ( JsonNode row : json_rows ) {

          List<Class> types = new ArrayList<>();
          List<String> names = new ArrayList<>();
          for ( JsonNode name : row.get( "names" ) ) {
            names.add( name.asText() );
          }
          List<Object> objects = new ArrayList<>();
          for ( JsonNode obj : row.get( "objects" ) ) {
            JsonNode t = obj.get( "type" );
            JsonNode rawObject = obj.get( "obj" );

            Object object = null;
            String objType = t.asText();
            switch ( objType ) {
              case "Null":
                types.add( Void.class );
                break;
              case "String":
                types.add( String.class );
                object = rawObject.asText();
                break;
              case "Integer":
                types.add( Integer.class );
                object = rawObject.asInt();
                break;
              case "Long":
                types.add( Long.class );
                object = rawObject.asLong();
                break;
              case "Date":
                types.add( Date.class );
                try {
                  object = DATE_TIME_INSTANCE.parse( rawObject.asText() );
                } catch ( ParseException e ) {
                  e.printStackTrace();
                }
                break;
              case "Double":
                types.add( Double.class );
                object = rawObject.asDouble();
                break;
              case "BigDecimal":
                types.add( BigDecimal.class );
                object = new BigDecimal( rawObject.asText() );
                break;
              case "Boolean":
                types.add( Boolean.class );
                object = rawObject.asBoolean();
                break;
              case "byte[]":
                types.add( byte[].class );
                object = rawObject.asText().getBytes( "UTF-8" );
                break;
              case "Object":
                try {
                  types.add( Object.class );
                  object = new ObjectInputStream( new ByteArrayInputStream( Base64.decodeBase64( rawObject.asText() ) )
                  ).readObject();
                } catch ( ClassNotFoundException e ) {
                  e.printStackTrace();
                }
                break;
            }

            objects.add( object );
          }

          Row r = new DeserializedRow( names, types, objects );
          rows.add( r );
        }
        Rows rowsObj = new Rows( rows, type, state );

        return new DataEvent( new RemoteSource( jsonNode.get( "model-id" ).asText() ), rowsObj );

      }
    } );
    mapper.registerModule( module );
  }

}
