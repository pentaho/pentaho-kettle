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
import org.pentaho.di.engine.api.events.MetricsEvent;
import org.pentaho.di.engine.api.remote.RemoteSource;
import org.pentaho.di.engine.api.reporting.Metrics;

import java.io.IOException;

/**
 * Created by nbaker on 3/4/17.
 */
public class MetricsEventSerializer extends BaseSerializer<MetricsEvent> {

  public MetricsEventSerializer() {
    super( MetricsEvent.class );

    SimpleModule module = new SimpleModule();
    module.addSerializer( MetricsEvent.class, new JsonSerializer<MetricsEvent>() {
      @Override
      public void serialize( MetricsEvent metricsEvent, JsonGenerator jsonGenerator,
                             SerializerProvider serializerProvider )
        throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        Metrics data = (Metrics) metricsEvent.getData();
        jsonGenerator.writeStringField( "model-id", metricsEvent.getSource().getId() );
        jsonGenerator.writeNumberField( "dropped", data.getDropped() );
        jsonGenerator.writeNumberField( "in", data.getIn() );
        jsonGenerator.writeNumberField( "in-flight", data.getInFlight() );
        jsonGenerator.writeNumberField( "out", data.getOut() );
        jsonGenerator.writeEndObject();
      }
    } );
    module.addDeserializer( MetricsEvent.class, new StdNodeBasedDeserializer<MetricsEvent>( MetricsEvent.class ) {
      @Override public MetricsEvent convert( JsonNode jsonNode, DeserializationContext deserializationContext )
        throws IOException {
        Metrics metrics =
          new Metrics( jsonNode.get( "in" ).asInt(), jsonNode.get( "out" ).asInt(), jsonNode.get( "dropped" ).asInt(),
            jsonNode.get( "in-flight" ).asInt() );

        return new MetricsEvent( new RemoteSource( jsonNode.get( "model-id" ).asText() ), metrics );

      }
    } );
    mapper.registerModule( module );
  }

}
