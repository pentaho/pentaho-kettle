/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput.json.node;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Represents an object node to be deduplicated
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

  /**
   * This merge implementation has the main intent to merge json sample dato to make the data date of sample compatible with all records.
   * @param currentNode
   */
  public void mergeValue( Node currentNode ) {

    Node storedNode = getByKey( currentNode.getKey() );

    if ( !( currentNode instanceof ValueNode ) || !( storedNode instanceof ValueNode ) ) {
      return;
    }

    ValueNode<Object> currentValueNode = (ValueNode) currentNode;
    ValueNode<Object> storedValueNode = (ValueNode) storedNode;

    Object currentValue = currentValueNode.getValue();
    Object storedValue = storedValueNode.getValue();
    Object value = currentValue;

    if ( value == null ) {
      return;
    }

    if ( currentValue instanceof String || storedValue instanceof String ) {
      // If anyone is string everyone need to be a string
      value = longestString( currentValue.toString(), storedValue.toString() );
    } else if ( currentValue.getClass().equals( Object.class ) || storedValue.getClass().equals( Object.class ) ) {
      // If anyone is object type, we need to convert all into String and store the big one
      value = longestString( currentValue.toString(), storedValue.toString() );
    } else if ( currentValue instanceof BigDecimal || storedValue instanceof BigDecimal ) {
      // If anyone is BigDecimal, we need to convert into BigDecimal, except if we are dealing with objets that are not Number
      if ( !( currentValue instanceof Number ) || !( storedValue instanceof Number ) ) {
        // Convert it into a generic string
        value = longestString( currentValue.toString(), storedValue.toString() );
      } else {
        //We are dealing with Numbers, so we can convert it into BigDecimal
        value = biggestNumber( new BigDecimal( currentValue.toString() ), new BigDecimal( storedValue.toString() ) );
      }
    } else if ( currentValue instanceof Double || storedValue instanceof Double ) {
      // If anyone is Double, we need to convert into Double
      if ( !( currentValue instanceof Number ) || !( storedValue instanceof Number ) ) {
        // Convert it into a generic string
        value = longestString( currentValue.toString(), storedValue.toString() );
      } else {
        //We are dealing with Numbers, so we can convert it into Double, because we already know it is not a DigDecimal
        value = biggestNumber( Double.valueOf( currentValue.toString() ), Double.valueOf( storedValue.toString() ) );
      }
    } else if ( currentValue instanceof BigInteger || storedValue instanceof BigInteger ) {
      // If anyone is BigInteger, we need to convert into BigInteger
      if ( !( currentValue instanceof Number ) || !( storedValue instanceof Number ) ) {
        // Convert it into a generic string
        value = longestString( currentValue.toString(), storedValue.toString() );
      } else {
        //We are dealing with Numbers, so we can convert it into BigInteger, because we already know it is not a Double
        value = biggestNumber( new BigInteger( currentValue.toString() ), new BigInteger( storedValue.toString() ) );
      }
    } else if ( currentValue instanceof Number || storedValue instanceof Number ) {
      // If anyone is a generic Number, we need to convert into Integer
      if ( !( currentValue instanceof Number ) || !( storedValue instanceof Number ) ) {
        // Convert it into a generic string
        value = longestString( currentValue.toString(), storedValue.toString() );
      } else {
        //We are dealing with Numbers, so we can convert it into Long, because we already know it is not a BigInteger
        value = biggestNumber( Long.valueOf( currentValue.toString() ), Long.valueOf( storedValue.toString() ) );
      }
    } else if ( currentValue instanceof Boolean && !( storedValue instanceof Boolean ) ) {
      // Convert it into a generic string if the stored value is not a Boolean
      value = longestString( currentValue.toString(), storedValue.toString() );
    } else if ( currentValue instanceof Date && !( storedValue instanceof Date ) ) {
      // Convert it into a generic string if the stored value is not a Date
      value = longestString( currentValue.toString(), storedValue.toString() );
    } else if ( currentValue instanceof Timestamp && !( storedValue instanceof Timestamp ) ) {
      // Convert it into a generic string if the stored value is not a Timestamp
      value = longestString( currentValue.toString(), storedValue.toString() );
    } else if ( currentValue instanceof InetAddress && !( storedValue instanceof InetAddress ) ) {
      // Convert it into a generic string if the stored value is not a InetAddress
      value = longestString( currentValue.toString(), storedValue.toString() );
    }
    // Update value on stored node
    updateStoredNode( storedValueNode, value );
  }


  private void updateStoredNode( ValueNode<Object> valueNode, Object value ) {
    valueNode.setValue( value );
  }

  private String longestString( String s1, String s2 ) {
    return ( s1.length() > s2.length() ) ? s1 : s2;
  }

  private <T extends Comparable<T>> T biggestNumber( T n1, T n2 ) {
    return n1.compareTo( n2 ) >= 0 ? n1 : n2;
  }

  public List<Node> getChildren() {
    return children;
  }

  public boolean hasChildren() {
    return !children.isEmpty();
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
      } else {
        mergeValue( node );
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
    StringBuilder output = new StringBuilder();
    if ( Objects.nonNull( getKey() ) ) {
      output.append( getKey() ).append( ": {\n" );
    } else {
      output.append( "{\n" );
    }
    for ( Node node : children ) {
      output.append( node.toString() );
    }
    return output.append( "},\n" ).toString();
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
