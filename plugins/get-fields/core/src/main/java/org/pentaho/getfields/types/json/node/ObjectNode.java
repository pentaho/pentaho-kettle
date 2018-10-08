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

package org.pentaho.getfields.types.json.node;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a object node to be deduplicated
 *
 * Created by bmorrise on 7/27/18.
 */
public class ObjectNode extends Node {

  public static final String TYPE = "Object";
  private List<Node> children = new ArrayList<>();

  public ObjectNode( String key ) {
    super( key );
  }

  public void addValue( Node value ) {
    children.add( value );
  }

  public List<Node> getChildren() {
    return children;
  }

  public boolean hasChildren() {
    return children.size() > 0;
  }

  /**
   * Removes duplicate entries
   */
  public void dedupe() {
    for ( Node node : children ) {
      node.dedupe();
    }
  }

  public boolean containsKey( String key ) {
    return children.stream().anyMatch( node -> node.getKey().equals( key ) );
  }

  public Node getByKey( String key ) {
    return children.stream().filter( node -> node.getKey().equals( key ) ).findFirst().orElse( null );
  }

  /**
   * Combines this object with another
   *
   * @param secondNode - The ObjectNode to combine
   */
  public void combine( ObjectNode secondNode ) {
    for ( Node node : secondNode.getChildren() ) {
      if ( !containsKey( node.getKey() ) ) {
        addValue( node );
      }
      if ( node instanceof ObjectNode ) {
        ObjectNode objectNode1 = (ObjectNode) getByKey( node.getKey() );
        ObjectNode objectNode2 = (ObjectNode) node;
        objectNode1.combine( objectNode2 );
      }
      if ( node instanceof ArrayNode ) {
        ArrayNode arrayNode1 = (ArrayNode) getByKey( node.getKey() );
        ArrayNode arrayNode2 = (ArrayNode) node;
        arrayNode1.combine( arrayNode2 );
        arrayNode1.dedupe();
      }
    }
  }

  @Override
  public String toString() {
    String output = "";
    if ( getKey() != null ) {
      output += getKey() + ": {\n";
    } else {
      output = "{\n";
    }
    for ( Node node : children ) {
      output += node.toString();
    }
    return output + "},\n";
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
