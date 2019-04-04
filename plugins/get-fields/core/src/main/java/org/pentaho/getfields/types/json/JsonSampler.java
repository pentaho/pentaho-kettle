/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.getfields.types.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.getfields.types.json.node.ArrayNode;
import org.pentaho.getfields.types.json.node.Node;
import org.pentaho.getfields.types.json.node.ObjectNode;
import org.pentaho.getfields.types.json.node.ValueNode;

import java.io.IOException;
import java.io.InputStream;

/**
 * Samples a set value of a JSON file and allows for deduplication
 *
 * Created by bmorrise on 7/27/18.
 */
public class JsonSampler {

  private int start = 0;
  private Configuration configuration;
  private JsonFactory jsonFactory = new MappingJsonFactory();

  /**
   * The constructor that takes are configuration object as a parameter
   *
   * @param configuration
   */
  public JsonSampler( Configuration configuration ) {
    this.configuration = configuration;
  }

  public JsonSampler() {
    this.configuration = new Configuration();
  }

  /**
   * Samples a json file by parser
   *
   * @param jsonParser - The json parser
   * @return The sampled Node
   * @throws IOException
   */
  private Node sample( JsonParser jsonParser ) throws IOException {
    jsonParser.enable( JsonParser.Feature.ALLOW_COMMENTS );
    Node node = null;
    while ( jsonParser.nextToken() != null ) {
      if ( jsonParser.currentToken() == JsonToken.START_ARRAY ) {
        node = new ArrayNode( null );
        sampleArray( jsonParser, (ArrayNode) node );
      }
      if ( jsonParser.currentToken() == JsonToken.START_OBJECT ) {
        node = new ObjectNode( null );
        sampleObject( jsonParser, (ObjectNode) node );
      }
      if ( start > configuration.getLines() ) {
        break;
      }
    }
    if ( node != null && configuration.isDedupe() ) {
      node.dedupe();
    }
    return node;
  }

  /**
   * Sample a json file by InputStream
   *
   * @param inputStream - a File input stream
   * @return The sampled Node
   * @throws IOException
   */
  public Node sample( InputStream inputStream ) throws IOException {
    try ( JsonParser jsonParser = jsonFactory.createParser( inputStream ) ) {
      return sample( jsonParser );
    }
  }

  /**
   * Sample a json file by name
   *
   * @param file - a file name
   * @return The sampled Node
   * @throws IOException
   */
  public Node sample( String file ) throws IOException, KettleFileException {
    try {
      return sample( KettleVFS.getInputStream( file ) );
    } catch ( KettleFileException kfe ) {
      throw kfe;
    }
  }

  /**
   * * Sample a json array recursively
   *
   * @param jsonParser
   * @param arrayNode
   * @throws IOException
   */
  private void sampleArray( JsonParser jsonParser, ArrayNode arrayNode ) throws IOException {
    start++;
    if ( start > configuration.getLines() ) {
      return;
    }
    while ( jsonParser.nextToken() != JsonToken.END_ARRAY ) {
      if ( start > configuration.getLines() ) {
        return;
      }
      Object node = getValue( jsonParser, null );
      arrayNode.addChild( (Node) node );
      if ( node instanceof ObjectNode ) {
        sampleObject( jsonParser, (ObjectNode) node );
      }
      if ( node instanceof ArrayNode ) {
        sampleArray( jsonParser, (ArrayNode) node );
      }
    }
  }

  /**
   * Sample a json object recursively
   *
   * @param jsonParser
   * @param objectNode
   * @throws IOException
   */
  private void sampleObject( JsonParser jsonParser, ObjectNode objectNode ) throws IOException {
    start++;
    if ( start > configuration.getLines() ) {
      return;
    }
    while ( jsonParser.nextToken() != JsonToken.END_OBJECT ) {
      if ( start > configuration.getLines() ) {
        return;
      }
      if ( jsonParser.currentToken() == JsonToken.FIELD_NAME ) {
        String name = jsonParser.getCurrentName();
        jsonParser.nextToken();
        Object node = getValue( jsonParser, name );
        if ( node instanceof ObjectNode ) {
          sampleObject( jsonParser, (ObjectNode) node );
        }
        if ( node instanceof ArrayNode ) {
          sampleArray( jsonParser, (ArrayNode) node );
        }
        objectNode.addValue( (Node) node );
      }
    }
  }

  /**
   * Get Node type from the parser
   *
   * @param jsonParser
   * @return Node - return Node type based on json token
   */
  private Node getValue( JsonParser jsonParser, String key ) {
    try {
      switch ( jsonParser.currentToken() ) {
        case START_OBJECT:
          return new ObjectNode( key );
        case START_ARRAY:
          return new ArrayNode( key );
        case VALUE_STRING:
          return new ValueNode<>( key, jsonParser.getValueAsString() );
        case VALUE_TRUE:
        case VALUE_FALSE:
          return new ValueNode<>( key, jsonParser.getValueAsBoolean() );
        case VALUE_NULL:
          return new ValueNode<>( key, null );
        case VALUE_NUMBER_FLOAT:
          return new ValueNode<>( key, jsonParser.getValueAsDouble() );
        case VALUE_NUMBER_INT:
          return new ValueNode<>( key, jsonParser.getValueAsInt() );
      }
    } catch ( IOException ioe ) {
      return null;
    }
    return null;
  }
}
