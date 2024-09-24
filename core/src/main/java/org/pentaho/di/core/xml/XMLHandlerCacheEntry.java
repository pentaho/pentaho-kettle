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

package org.pentaho.di.core.xml;

import org.w3c.dom.Node;

/**
 * This is an entry in an XMLHandlerCache
 *
 * @author Matt
 * @since 22-Apr-2006
 */
public class XMLHandlerCacheEntry {
  private Node parentNode;
  private String tag;

  /**
   * @param parentNode
   *          The parent node
   * @param tag
   *          The tag
   */
  public XMLHandlerCacheEntry( Node parentNode, String tag ) {
    this.parentNode = parentNode;
    this.tag = tag;
  }

  /**
   * @return Returns the parentNode.
   */
  public Node getParentNode() {
    return parentNode;
  }

  /**
   * @param parentNode
   *          The parentNode to set.
   */
  public void setParentNode( Node parentNode ) {
    this.parentNode = parentNode;
  }

  /**
   * @return Returns the tag.
   */
  public String getTag() {
    return tag;
  }

  /**
   * @param tag
   *          The tag to set.
   */
  public void setTag( String tag ) {
    this.tag = tag;
  }

  @Override
  public boolean equals( Object object ) {
    if ( this == object ) {
      return true;
    }
    if ( object == null || getClass() != object.getClass() ) {
      return false;
    }
    XMLHandlerCacheEntry entry = (XMLHandlerCacheEntry) object;

    return parentNode.equals( entry.getParentNode() ) && tag.equals( entry.getTag() );
  }

  @Override
  public int hashCode() {
    return parentNode.hashCode() ^ tag.hashCode();
  }

}
