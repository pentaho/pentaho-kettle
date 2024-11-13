/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.jsoninput.json.node;

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
    return !children.isEmpty();
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
    if ( !objectNode.getChildren().isEmpty() ) {
      children.add( objectNode );
    }
    if ( !arrayNode.getChildren().isEmpty() ) {
      arrayNode.dedupe();
      children.add( arrayNode );
    }
  }

  @Override
  public String toString() {
    StringBuilder output = new StringBuilder();
    if ( key != null ) {
      output.append( key ).append(  ": [\n" );
    } else {
      output.append( "[\n" );
    }
    for ( Node child : children ) {
      output.append( child.toString() );
    }
    return output.append( "],\n" ).toString();
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
