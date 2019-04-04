/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.ui.endpoints;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Produces( MediaType.APPLICATION_JSON )
public class JsonProvider extends JacksonJaxbJsonProvider {

  public static final String TYPE = "type";

  public JsonProvider() {
    super();

    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer( ConnectionDetails.class, new ConnectionDetailsDeserializer() );

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule( simpleModule );

    setMapper( objectMapper );
  }

  public static class ConnectionDetailsDeserializer extends StdDeserializer<ConnectionDetails> {

    public ConnectionDetailsDeserializer() {
      this( ConnectionDetails.class );
    }

    public ConnectionDetailsDeserializer( Class<?> vc ) {
      super( vc );
    }

    @Override
    public ConnectionDetails deserialize( JsonParser jsonParser, DeserializationContext deserializationContext )
      throws IOException {
      JsonNode jsonNode = jsonParser.getCodec().readTree( jsonParser );

      String type = jsonNode.get( TYPE ).asText();
      Class<? extends ConnectionDetails> clazz =
        ConnectionManager.getInstance().createConnectionDetails( type ).getClass();
      if ( clazz != null ) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
        return objectMapper.readValue( objectMapper.treeAsTokens( jsonNode ), clazz );
      }
      return null;
    }
  }

}
