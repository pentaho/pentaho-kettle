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
import org.pentaho.di.engine.api.events.StatusEvent;
import org.pentaho.di.engine.api.remote.RemoteSource;
import org.pentaho.di.engine.api.reporting.Status;

import java.io.IOException;

/**
 * Created by nbaker on 2/15/17.
 */
public class StatusEventSerializer extends BaseSerializer<StatusEvent> {

  public StatusEventSerializer() {
    super( StatusEvent.class );

    SimpleModule module = new SimpleModule();
    module.addSerializer( StatusEvent.class, new JsonSerializer<StatusEvent>() {
      @Override
      public void serialize( StatusEvent statusEvent, JsonGenerator jsonGenerator,
                             SerializerProvider serializerProvider )
        throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField( "model-id", statusEvent.getSource().getId() );
        jsonGenerator.writeStringField( "status-type", statusEvent.getData().toString() );
        jsonGenerator.writeEndObject();
      }
    } );
    module.addDeserializer( StatusEvent.class, new StdNodeBasedDeserializer<StatusEvent>( StatusEvent.class ) {
      @Override public StatusEvent convert( JsonNode jsonNode, DeserializationContext deserializationContext )
        throws IOException {
        return new StatusEvent<>( new RemoteSource( jsonNode.get( "model-id" ).asText() ),
          Status.valueOf( jsonNode.get( "status-type" ).asText() ) );

      }
    } );
    mapper.registerModule( module );
  }

}
