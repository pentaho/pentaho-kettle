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

package org.pentaho.di.trans.steps.jsoninput.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.steps.jsoninput.json.node.ArrayNode;
import org.pentaho.di.trans.steps.jsoninput.json.node.Node;
import org.pentaho.di.trans.steps.jsoninput.json.node.ObjectNode;
import org.pentaho.di.trans.steps.jsoninput.json.node.ValueNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
   * @param tree
   * @return The sampled Node
   * @throws IOException
   */
  private Node sample( JsonParser jsonParser, Tree tree ) throws IOException {
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
    convertToSwtTree( node, null, tree );
    return node;
  }

  private void convertToSwtTree( Node node, TreeItem treeItem, Tree tree ) {
    TreeItem item = Objects.isNull( treeItem ) ? new TreeItem( tree, 0 ) : new TreeItem( treeItem, 0 );
    if ( Objects.nonNull( node ) ) {
      item.setData( "Key", node.getKey() );
      item.setData( "Type", convertJsonTypeToPentahoTypes( node.getType() ) );
      if ( "Object".equals( node.getType() ) ) {
        processObject( node, tree, item );
      } else if ( "Array".equals( node.getType() ) ) {
        processArray( node, tree, item );
      } else {
        processValues( node, item );
      }
    }
  }

  /**
   * This method converts DataTypes returned from json sampler into compatible Pentaho DataType
   *
   * @param type
   * @return
   */
  private String convertJsonTypeToPentahoTypes( String type ) {
    if ( type == null ) {
      return null;
    }
    switch ( type ) {
      case "BigDecimal":
        return "BigNumber";
      case "BigInteger":
        return "Integer";
      case "Double":
        return "Number";
      case "Object":
      case "Array":
        return "String";
      default:
        return type;
    }
  }

  private void processValues( Node node, TreeItem item ) {
    Object value = ( (ValueNode) node ).getValue();
    if ( Objects.nonNull( value ) && value instanceof String ) {
      item.setText( node.getKey() + ": " + "\"" + value + "\"" );
    } else {
      item.setText( node.getKey() + ": " + value );
    }
  }

  private void processArray( Node node, Tree tree, TreeItem item ) {
    item.setText( node.getKey() + " : [" );
    for ( Node child : node.getChildren() ) {
      convertToSwtTree( child, item, tree );
    }
  }

  private void processObject( Node node, Tree tree, TreeItem item ) {
    item.setText( Objects.isNull( node.getKey() ) ? "{" : node.getKey() + " : {" );
    for ( Node child : node.getChildren() ) {
      convertToSwtTree( child, item, tree );
    }
  }

  /**
   * Sample a json file by InputStream
   *
   * @param inputStream - a File input stream
   * @param tree
   * @return The sampled Node
   * @throws IOException
   */
  public Node sample( InputStream inputStream, Tree tree ) throws IOException {
    try ( JsonParser jsonParser = jsonFactory.createParser( inputStream ) ) {
      return sample( jsonParser, tree );
    }
  }

  /**
   * Sample a json file by name
   *
   * @param file - a file name
   * @param tree
   * @return The sampled Node
   * @throws IOException
   */
  public Node sample( String file, Tree tree ) throws IOException, KettleFileException {
    return sample( KettleVFS.getInputStream( file ), tree );
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
          return new ValueNode<>( key, jsonParser.getBigIntegerValue() );
        default:
          return null;
      }
    } catch ( IOException ioe ) {
      return null;
    }
  }

  public void selectByPath( String path, TreeItem node ) {
    List<String> expressions = getExpressions( path );
    if ( !expressions.isEmpty() ) {
      String expression = expressions.remove( 0 );
      while ( StringUtils.isNotBlank( expression ) ) {
        if ( "$".equals( expression ) ) {
          expression = stepOne( node, expressions );
        } else if ( ".".equals( expression ) ) {
          expression = stepTwo( node, expressions );
        } else if ( "..".equals( expression ) ) {
          expression = stepThree( node, expressions );
        } else if ( "[*]".equals( expression ) && !expressions.isEmpty() ) {
          expression = stepFour( node, expressions );
        } else {
          expression = stepFive( expressions );
        }
      }
    }
  }

  private static String stepFive( List<String> expressions ) {
    String expression;
    if ( expressions.isEmpty() ) {
      expression = null;
    } else {
      expression = expressions.remove( 0 );
    }
    return expression;
  }

  private static String stepFour( TreeItem node, List<String> expressions ) {
    TreeItem item = node.getItem( 0 );
    item.setChecked( true );
    return expressions.remove( 0 );
  }

  private String stepThree( TreeItem node, List<String> expressions ) {
    String expression = expressions.remove( 0 );
    TreeItem item = findAny( node, expression );
    if ( Objects.nonNull( item ) ) {
      item.setChecked( true );
    }
    return expression;
  }

  private String stepTwo( TreeItem node, List<String> expressions ) {
    String expression = expressions.remove( 0 );
    TreeItem item = findChild( node, expression );
    if ( Objects.nonNull( item ) ) {
      item.setChecked( true );
    }
    return expression;
  }

  private static String stepOne( TreeItem node, List<String> expressions ) {
    String expression = expressions.remove( 0 );
    if ( ".".equals( expression ) ) {
      node.setChecked( true );
    }
    return expression;
  }

  private List<String> getExpressions( String path ) {
    Matcher matcher = Pattern.compile( "\\w+|\\[[\\s\\S]*?]|\\$|\\.\\.|\\." ).matcher( path );
    List<String> expressions = new ArrayList<>();
    while ( matcher.find() ) {
      expressions.add( matcher.group() );
    }
    return expressions;
  }

  private TreeItem findAny( TreeItem node, String value ) {
    if ( Objects.nonNull( node.getItems() ) && node.getItems().length > 0 ) {
      for ( int i = 0; i < node.getItems().length; i++ ) {
        if ( value.equals( node.getItem( i ).getData( "Key" ) ) ) {
          return node.getItem( i );
        }
        if ( node.getItem( i ).getItems().length > 0 ) {
          TreeItem found = findAny( node.getItem( i ), value );
          if ( Objects.nonNull( found ) ) {
            return found;
          }
        }
      }
    }
    return null;
  }

  private TreeItem findChild( TreeItem node, String value ) {
    if ( Objects.nonNull( node.getItems() ) && node.getItems().length > 0 ) {
      for ( int i = 0; i < node.getItems().length; i++ ) {
        if ( value.equals( node.getItem( i ).getData( "Key" ) ) ) {
          return node.getItem( i );
        }
      }
    }
    return null;
  }

  public List<String> getChecked( final Tree tree ) {
    final List<String> checked = new ArrayList<>();

    final TreeItem[] topItems = tree.getItems();

    for ( final TreeItem item : topItems ) {
      if ( item.getChecked() ) {
        String path = getPath( item );
        if ( StringUtils.isNotBlank( path ) ) {
          checked.add( path );
        }
      }

      addChecked( checked, item );
    }

    return checked;
  }

  private void addChecked( final List<String> checked, final TreeItem treeItem ) {
    final TreeItem[] items = treeItem.getItems();

    for ( final TreeItem item : items ) {
      if ( item.getChecked() ) {
        String path = getPath( item );
        if ( StringUtils.isNotBlank( path ) ) {
          checked.add( path );
        }
      }

      addChecked( checked, item );
    }
  }

  private String getPath( TreeItem item ) {
    if ( !hasCheckedChildren( item ) ) {
      String data = generatePath( item );
      Object key = item.getData( "Key" );
      if ( Objects.isNull( key ) ) {
        if ( Objects.nonNull( item.getParent() ) ) {
          key = item.getParent().getData( "Key" );
        } else {
          key = "root";
        }
      }
      return key + ":" + data + ":" + item.getData( "Type" );
    }
    return null;
  }

  private String generatePath( TreeItem item ) {
    StringBuilder path = new StringBuilder( (String) getNodePath( item ) );
    TreeItem parent = item.getParentItem();
    while ( Objects.nonNull( parent ) ) {
      if ( parent.getChecked() ) {
        path.insert( 0, getNodePath( parent ) );
      }
      parent = parent.getParentItem();
    }
    return "$" + path;
  }

  private Object getNodePath( TreeItem item ) {
    Object key = item.getData( "Key" );
    if ( Objects.isNull( key ) ) {
      if ( Objects.nonNull( item.getParentItem() ) && item.getParentItem().getText().contains( "[" ) ) {
        if ( item.getParentItem().getChecked() ) {
          key = "[*]";
        } else {
          key = "..[*]";
        }
      } else {
        key = "";
      }
    } else {
      if ( Objects.nonNull( item.getParentItem() ) && item.getParentItem().getChecked() ) {
        key = "." + key;
      } else {
        key = ".." + key;
      }
    }
    return key;
  }

  private boolean hasCheckedChildren( TreeItem item ) {
    if ( Objects.nonNull( item.getItems() ) && item.getItems().length > 0 ) {
      for ( int i = 0; i < item.getItems().length; i++ ) {
        TreeItem child = item.getItems()[i];
        if ( child.getChecked() ) {
          return true;
        }
        if ( Objects.nonNull( child.getItems() ) && child.getItems().length > 0 && hasCheckedChildren( child ) ) {
          return true;
        }
      }
    }
    return false;
  }
}
