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

package org.pentaho.di.trans.steps.jsoninput.sampler.node;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a object node to be deduplicated
 *
 * Created by bmorrise on 7/27/18.
 */
public class ObjectNode implements Node {
  private Map<String, Node> values = new HashMap<>();

  public void addValue( String key, Node value ) {
    values.put( key, value );
  }

  public Map<String, Node> getValues() {
    return values;
  }

  /**
   * Removes duplicate entries
   */
  public void dedupe() {
    for ( Map.Entry<String, Node> values : values.entrySet() ) {
      if ( values.getValue() instanceof ArrayNode ) {
        ArrayNode arrayNode1 = (ArrayNode) this.values.get( values.getKey() );
        arrayNode1.dedupe();
      }
      if ( values.getValue() instanceof ObjectNode ) {
        ObjectNode objectNode1 = (ObjectNode) values.getValue();
        objectNode1.dedupe();
      }
    }
  }

  /**
   * Combines this object with another
   *
   * @param secondNode - The ObjectNode to combine
   */
  public void combine( ObjectNode secondNode ) {
    for ( Map.Entry<String, Node> values : secondNode.getValues().entrySet() ) {
      if ( !this.values.containsKey( values.getKey() ) ) {
        addValue( values.getKey(), values.getValue() );
      }
      if ( values.getValue() instanceof ObjectNode ) {
        ObjectNode objectNode1 = (ObjectNode) this.values.get( values.getKey() );
        ObjectNode objectNode2 = (ObjectNode) values.getValue();
        objectNode1.combine( objectNode2 );
      }
      if ( values.getValue() instanceof ArrayNode ) {
        ArrayNode arrayNode1 = (ArrayNode) this.values.get( values.getKey() );
        ArrayNode arrayNode2 = (ArrayNode) values.getValue();
        arrayNode1.combine( arrayNode2 );
        arrayNode1.dedupe();
      }
    }
  }

  @Override
  public String output() {
    String output = "{\n";
    for ( Map.Entry<String, Node> entry : values.entrySet() ) {
      output += " " + entry.getKey() + ":" + entry.getValue().output() + "\n";
    }
    return output + "},\n";
  }
}
