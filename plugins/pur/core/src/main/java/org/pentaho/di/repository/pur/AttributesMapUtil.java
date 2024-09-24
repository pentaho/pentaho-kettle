/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import org.pentaho.di.core.AttributesInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;

import java.util.HashMap;
import java.util.Map;

public class AttributesMapUtil {

  public static final String NODE_ATTRIBUTE_GROUPS = "ATTRIBUTE_GROUPS";

  /**
   * <p>Copies the provided attributes into the given data node.</p>
   * <p>The attributes will be saved in a node with the default name: {@link #NODE_ATTRIBUTE_GROUPS}.</p>
   * <p>Equivalent to:</p>
   * <pre>
   *    <code>saveAttributesMap( dataNode, attributesInterface, AttributesMapUtil.NODE_ATTRIBUTE_GROUPS)</code>
   * </pre>
   *
   * @param dataNode            the data node into which we want to copy the attributes
   * @param attributesInterface the attributes to copy
   * @throws KettleException
   * @see #saveAttributesMap(DataNode, AttributesInterface, String)
   */
  public static final void saveAttributesMap( DataNode dataNode, AttributesInterface attributesInterface )
    throws KettleException {
    saveAttributesMap( dataNode, attributesInterface, NODE_ATTRIBUTE_GROUPS );
  }

  /**
   * <p>Copies the provided attributes into the given data node.</p>
   * <p>The attributes will be saved in a node with the specified name.</p>
   *
   * @param dataNode            the data node into which we want to copy the attributes
   * @param attributesInterface the attributes to copy
   * @param attributeGroupsNode the name to be used in the data node
   * @throws KettleException
   * @see #saveAttributesMap(DataNode, AttributesInterface)
   */
  public static final void saveAttributesMap( DataNode dataNode, AttributesInterface attributesInterface,
                                              String attributeGroupsNode )
    throws KettleException {
    if ( attributesInterface != null ) {
      Map<String, Map<String, String>> attributesMap = attributesInterface.getAttributesMap();

      DataNode attributeNodes = dataNode.getNode( attributeGroupsNode );
      if ( attributeNodes == null ) {
        attributeNodes = dataNode.addNode( attributeGroupsNode );
      }
      for ( String groupName : attributesMap.keySet() ) {
        DataNode attributeNode = attributeNodes.getNode( groupName );
        if ( attributeNode == null ) {
          attributeNode = attributeNodes.addNode( groupName );
        }
        Map<String, String> attributes = attributesMap.get( groupName );
        for ( Map.Entry<String, String> entry : attributes.entrySet() ) {
          String key = entry.getKey();
          String value = entry.getValue();
          if ( key != null && value != null ) {
            attributeNode.setProperty( key, value );
          }
        }
      }
    }
  }

  /**
   * <p>Loads attributes from the provided data node.</p>
   * <p>The attributes will be loaded from a node with the default name: {@link #NODE_ATTRIBUTE_GROUPS}.</p>
   * <p>Equivalent to:</p>
   * <pre>
   *    <code>loadAttributesMap( dataNode, attributesInterface, AttributesMapUtil.NODE_ATTRIBUTE_GROUPS)</code>
   * </pre>
   *
   * @param dataNode            the data node from which to load the attributes
   * @param attributesInterface where the attributes are to be loaded
   * @throws KettleException
   * @see #loadAttributesMap(DataNode, AttributesInterface, String)
   */
  public static final void loadAttributesMap( DataNode dataNode, AttributesInterface attributesInterface )
    throws KettleException {
    loadAttributesMap( dataNode, attributesInterface, NODE_ATTRIBUTE_GROUPS );
  }

  /**
   * <p>Loads attributes from the provided data node.</p>
   * <p>Copies the provided attributes into the given data node.</p>
   * <p>The attributes will be loaded from a node with the specified name.</p>
   *
   * @param dataNode            the data node from which to load the attributes
   * @param attributesInterface where the attributes are to be loaded
   * @param attributeGroupsNode the name of the node with the attributes
   * @throws KettleException
   * @see #loadAttributesMap(DataNode, AttributesInterface)
   */
  public static final void loadAttributesMap( DataNode dataNode, AttributesInterface attributesInterface,
                                              String attributeGroupsNode )
    throws KettleException {
    Map<String, Map<String, String>> attributesMap = new HashMap<>();
    attributesInterface.setAttributesMap( attributesMap );

    if ( dataNode != null ) {
      DataNode groupsNode = dataNode.getNode( attributeGroupsNode );
      if ( groupsNode != null ) {
        for ( DataNode groupNode : groupsNode.getNodes() ) {
          HashMap<String, String> attributes = new HashMap<>();
          attributesMap.put( groupNode.getName(), attributes );
          Iterable<DataProperty> properties = groupNode.getProperties();
          for ( DataProperty dataProperty : properties ) {
            String key = dataProperty.getName();
            String value = dataProperty.getString();
            if ( key != null && value != null ) {
              attributes.put( key, value );
            }
          }
        }
      }
    }
  }
}
