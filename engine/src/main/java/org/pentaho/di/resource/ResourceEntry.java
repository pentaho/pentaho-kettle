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

package org.pentaho.di.resource;

import org.pentaho.di.core.util.StringUtil;

public class ResourceEntry {
  public enum ResourceType {
    FILE, CONNECTION, SERVER, URL, DATABASENAME, ACTIONFILE, OTHER
  }

  private String resource;
  private ResourceType resourcetype;

  /**
   * @param resource
   * @param resourcetype
   */
  public ResourceEntry( String resource, ResourceType resourcetype ) {
    super();
    this.resource = resource;
    this.resourcetype = resourcetype;
  }

  /**
   * @return the resource
   */
  public String getResource() {
    return resource;
  }

  /**
   * @param resource
   *          the resource to set
   */
  public void setResource( String resource ) {
    this.resource = resource;
  }

  /**
   * @return the resourcetype
   */
  public ResourceType getResourcetype() {
    return resourcetype;
  }

  /**
   * @param resourcetype
   *          the resourcetype to set
   */
  public void setResourcetype( ResourceType resourcetype ) {
    this.resourcetype = resourcetype;
  }

  public String toXml( int indentLevel ) {
    StringBuilder buff = new StringBuilder( 30 );
    buff
      .append( StringUtil.getIndent( indentLevel ) )
      .append( "<Resource type='" )
      .append( this.getResourcetype() )
      .append( "'><![CDATA[" ).append( this.getResource() ).append( "]]>" ).append( "</Resource>" ).append(
        StringUtil.CRLF );
    return buff.toString();
  }

}
