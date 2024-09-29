/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.attributes;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributesUtil {

  public static final String XML_TAG = "attributes";
  public static final String XML_TAG_GROUP = "group";
  public static final String XML_TAG_ATTRIBUTE = "attribute";

  /**
   * <p>Serialize an attributes group map to XML.</p>
   * <p>The information will be encapsulated in the default tag: {@link #XML_TAG}.</p>
   * <p>If a null or empty Map is given, the generated XML will have the default tag (with no content).</p>
   * <p>Equivalent to:</p>
   * <pre>  <code>getAttributesXml( attributesMap, AttributesUtil.XML_TAG )</code></pre>
   *
   * @param attributesMap the attribute groups to serialize
   * @return the XML serialized attribute groups
   * @see #getAttributesXml(Map, String)
   */
  public static String getAttributesXml( Map<String, Map<String, String>> attributesMap ) {
    return getAttributesXml( attributesMap, XML_TAG );
  }

  /**
   * <p>Serialize an attributes group map to XML.</p>
   * <p>The information will be encapsulated in the specified tag.</p>
   * <p>If a null or empty Map is given, the generated XML will have the provided tag (with no content).</p>
   *
   * @param attributesMap the attribute groups to serialize
   * @param xmlTag        the xml tag to use for the generated xml
   * @return the XML serialized attribute groups
   * @see #getAttributesXml(Map)
   */
  public static String getAttributesXml( Map<String, Map<String, String>> attributesMap, String xmlTag ) {
    StringBuilder xml = new StringBuilder();

    xml.append( XMLHandler.openTag( xmlTag ) );

    if ( attributesMap != null && !attributesMap.isEmpty() ) {
      List<String> groupNames = new ArrayList<>( attributesMap.keySet() );
      Collections.sort( groupNames );

      for ( String groupName : groupNames ) {

        xml.append( XMLHandler.openTag( XML_TAG_GROUP ) );
        xml.append( XMLHandler.addTagValue( "name", groupName ) );

        Map<String, String> attributes = attributesMap.get( groupName );
        List<String> keys = new ArrayList<>( attributes.keySet() );
        for ( String key : keys ) {
          xml.append( XMLHandler.openTag( XML_TAG_ATTRIBUTE ) );
          xml.append( XMLHandler.addTagValue( "key", key ) );
          xml.append( XMLHandler.addTagValue( "value", attributes.get( key ) ) );
          xml.append( XMLHandler.closeTag( XML_TAG_ATTRIBUTE ) );
        }

        xml.append( XMLHandler.closeTag( XML_TAG_GROUP ) );
      }
    }

    xml.append( XMLHandler.closeTag( xmlTag ) ).append( Const.CR );

    return xml.toString();
  }

  /**
   * <p>Load the attribute groups from an XML DOM Node.</p>
   * <p>An empty Map will be returned if a null or empty Node is given.</p>
   *
   * @param attributesNode the attributes node to read from
   * @return the Map with the attribute groups
   */
  public static Map<String, Map<String, String>> loadAttributes( Node attributesNode ) {
    Map<String, Map<String, String>> attributesMap = new HashMap<>();

    if ( attributesNode != null ) {
      List<Node> groupNodes = XMLHandler.getNodes( attributesNode, XML_TAG_GROUP );
      for ( Node groupNode : groupNodes ) {
        String groupName = XMLHandler.getTagValue( groupNode, "name" );
        Map<String, String> attributes = new HashMap<>();
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
