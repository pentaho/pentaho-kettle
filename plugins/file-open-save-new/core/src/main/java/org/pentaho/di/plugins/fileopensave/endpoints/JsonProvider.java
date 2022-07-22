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

package org.pentaho.di.plugins.fileopensave.endpoints;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.InvalidFileProviderException;
import org.pentaho.di.plugins.fileopensave.providers.ProviderService;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Produces( MediaType.APPLICATION_JSON )
public class JsonProvider extends JacksonJaxbJsonProvider {

  public static final String PROVIDER = "provider";

  public JsonProvider( ProviderService providerService ) {
    super();

    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer( File.class, new FileDeserializer( providerService ) );

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule( simpleModule );

    setMapper( objectMapper );
  }

  public static class FileDeserializer extends StdDeserializer<File> {

    private transient ProviderService providerService;

    public FileDeserializer( ProviderService providerService ) {
      this( File.class );
      this.providerService = providerService;
    }

    public FileDeserializer( Class<?> vc ) {
      super( vc );
    }

    @Override
    public File deserialize( JsonParser jsonParser, DeserializationContext deserializationContext )
      throws IOException {
      JsonNode jsonNode = jsonParser.getCodec().readTree( jsonParser );

      String type = jsonNode.get( PROVIDER ).asText();
      try {
        Class<File> clazz = providerService.get( type ).getFileClass();
        if ( clazz != null ) {
          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
          return objectMapper.readValue( objectMapper.treeAsTokens( jsonNode ), clazz );
        }
      } catch ( InvalidFileProviderException e ) {
        return null;
      }
      return null;
    }
  }

}
