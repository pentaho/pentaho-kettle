/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

public class AttributesUtil {

  public static final String XML_TAG = "attributes";
  public static final String XML_TAG_GROUP = "group";
  public static final String XML_TAG_ATTRIBUTE = "attribute";

  /**
   * Serialize an attributes group map to XML
   *
   * @param attributesMap
   *          The attribute groups to serialize
   * @return The XML serialized attribute groups
   */
  public static String getAttributesXml( Map<String, Map<String, String>> attributesMap ) {
    StringBuilder xml = new StringBuilder();

    if ( attributesMap != null && !attributesMap.isEmpty() ) {
      xml.append( XMLHandler.openTag( XML_TAG ) );

      List<String> groupNames = new ArrayList<String>( attributesMap.keySet() );
      Collections.sort( groupNames );

      for ( String groupName : groupNames ) {

        xml.append( XMLHandler.openTag( XML_TAG_GROUP ) );
        xml.append( XMLHandler.addTagValue( "name", groupName ) );

        Map<String, String> attributes = attributesMap.get( groupName );
        List<String> keys = new ArrayList<String>( attributes.keySet() );
        for ( String key : keys ) {
          xml.append( XMLHandler.openTag( XML_TAG_ATTRIBUTE ) );
          xml.append( XMLHandler.addTagValue( "key", key ) );
          xml.append( XMLHandler.addTagValue( "value", attributes.get( key ) ) );
          xml.append( XMLHandler.closeTag( XML_TAG_ATTRIBUTE ) );
        }

        xml.append( XMLHandler.closeTag( XML_TAG_GROUP ) );
      }

      xml.append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );
    }

    return xml.toString();
  }

  /**
   * Load the attribute groups from an XML DOM Node
   *
   * @param attributesNode
   *          The attributes node to read from ( <attributes> )
   * @return the map with the attribute groups.
   */
  public static Map<String, Map<String, String>> loadAttributes( Node attributesNode ) {
    Map<String, Map<String, String>> attributesMap = new HashMap<String, Map<String, String>>();

    if ( attributesNode != null ) {
      List<Node> groupNodes = XMLHandler.getNodes( attributesNode, XML_TAG_GROUP );
      for ( Node groupNode : groupNodes ) {
        String groupName = XMLHandler.getTagValue( groupNode, "name" );
        Map<String, String> attributes = new HashMap<String, String>();
        attributesMap.put( groupName, attributes );
        List<Node> attributeNodes = XMLHandler.getNodes( groupNode, XML_TAG_ATTRIBUTE );
        for ( Node attributeNode : attributeNodes ) {
          String key = XMLHandler.getTagValue( attributeNode, "key" );
          String value = XMLHandler.getTagValue( attributeNode, "value" );
          if ( key != null && value != null ) {
            attributes.put( key, value );
          }
        }
      }
    }

    return attributesMap;
  }
}
