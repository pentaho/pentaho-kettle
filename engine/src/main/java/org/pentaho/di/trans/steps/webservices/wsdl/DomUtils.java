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


package org.pentaho.di.trans.steps.webservices.wsdl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Some DOM utility methods.
 */
public final class DomUtils {

  /**
   * <p>
   * Returns the first child element with the given name. Returns <code>null</code> if not found.
   * </p>
   *
   * @param parent
   *          parent element
   * @param localName
   *          name of the child element
   * @return child element, null if not found.
   */
  protected static Element getChildElementByName( Element parent, String localName ) {
    NodeList children = parent.getChildNodes();

    for ( int i = 0; i < children.getLength(); i++ ) {
      Node node = children.item( i );
      if ( node.getNodeType() == Node.ELEMENT_NODE ) {
        Element element = (Element) node;
        if ( element.getLocalName().equals( localName ) ) {
          return element;
        }
      }
    }
    return null;
  }

  /**
   * <p>
   * Returns a list of child elements with the given name. Returns an empty list if there are no such child elements.
   * </p>
   *
   * @param parent
   *          parent element
   * @param localName
   *          Local name of the child element
   * @return child elements
   */
  protected static List<Element> getChildElementsByName( Element parent, String localName ) {
    List<Element> elements = new ArrayList<Element>();

    NodeList children = parent.getChildNodes();

    for ( int i = 0; i < children.getLength(); i++ ) {
      Node node = children.item( i );
      if ( node.getNodeType() == Node.ELEMENT_NODE ) {
        Element element = (Element) node;
        if ( element.getLocalName().equals( localName ) ) {
          elements.add( element );
        }
      }
    }
    return elements;
  }
}
