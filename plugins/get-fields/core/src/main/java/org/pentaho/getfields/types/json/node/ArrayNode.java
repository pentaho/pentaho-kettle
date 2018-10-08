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
 * A node that holds children of Node type
 *
 * Created by bmorrise on 8/7/18.
 */
public class ArrayNode extends Node {

  public static final String TYPE = "Array";
  private List<Node> children = new ArrayList<>();

  public ArrayNode( String key ) {
    super( key );
  }

  public void addChild( Node child ) {
    children.add( child );
  }

  public List<Node> getChildren() {
    return children;
  }

  public boolean hasChildren() {
    return children.size() > 0;
  }

  /**
   * Combines two array nodes
   *
   * @param arrayNode - The ArrayNode to combine with this one
   */
  public void combine( ArrayNode arrayNode ) {
    ObjectNode objectNode = new ObjectNode( null );
    ArrayNode arrayNode1 = new ArrayNode( null );
    for ( int i = 0; i < arrayNode.getChildren().size(); i++ ) {
      Node node = arrayNode.getChildren().get( i );
      if ( node instanceof ObjectNode ) {
        objectNode.setKey( node.getKey() );
        objectNode.combine( (ObjectNode) node );
      }
      if ( node instanceof ArrayNode ) {
        arrayNode1.combine( (ArrayNode) node );
      }
    }
    for ( int i = 0; i < children.size(); i++ ) {
      Node node = children.get( i );
      if ( node instanceof ObjectNode ) {
        objectNode.combine( (ObjectNode) node );
      }
      if ( node instanceof ArrayNode ) {
        arrayNode1.combine( (ArrayNode) node );
      }
    }
    if ( objectNode.getKey() != null || objectNode.hasChildren() ) {
      children.add( objectNode );
    }
    if ( arrayNode1.hasChildren() ) {
      children.add( arrayNode1 );
    }
  }

  /**
   * Remove duplicate child nodes
   */
  public void dedupe() {
    ObjectNode objectNode = new ObjectNode( null );
    ArrayNode arrayNode = new ArrayNode( null );
    for ( int i = 0; i < children.size(); i++ ) {
      Node child = children.get( i );
      if ( child instanceof ObjectNode ) {
        objectNode.setKey( child.getKey() );
        objectNode.combine( (ObjectNode) child );
      }
      if ( child instanceof ArrayNode ) {
        arrayNode.combine( (ArrayNode) child );
      }
    }
    children.clear();
    if ( objectNode.getChildren().size() > 0 ) {
      children.add( objectNode );
    }
    if ( arrayNode.getChildren().size() > 0 ) {
      arrayNode.dedupe();
      children.add( arrayNode );
    }
  }

  @Override
  public String toString() {
    String output = "";
    if ( key != null ) {
      output += key + ": [\n";
    } else {
      output = "[\n";
    }
    for ( Node child : children ) {
      output += child.toString();
    }
    return output + "],\n";
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
