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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.util.StringUtil;

public class ResourceReference {
  private ResourceHolderInterface resourceReferenceHolder;
  private List<ResourceEntry> entries;

  /**
   * @param resourceReferenceHolder
   *          Where to put the resource references
   * @param entries
   *          the resource entries list
   */
  public ResourceReference( ResourceHolderInterface resourceReferenceHolder, List<ResourceEntry> entries ) {
    super();
    this.resourceReferenceHolder = resourceReferenceHolder;
    this.entries = entries;
  }

  public ResourceReference( ResourceHolderInterface resourceReferenceHolder ) {
    this.resourceReferenceHolder = resourceReferenceHolder;
    this.entries = new ArrayList<ResourceEntry>();
  }

  /**
   * @return the resource reference holder
   */
  public ResourceHolderInterface getReferenceHolder() {
    return resourceReferenceHolder;
  }

  /**
   * @param resourceReferenceHolder
   *          the resource reference holder to set
   */
  public void setReferenceHolder( ResourceHolderInterface resourceReferenceHolder ) {
    this.resourceReferenceHolder = resourceReferenceHolder;
  }

  /**
   * @return the entries
   */
  public List<ResourceEntry> getEntries() {
    return entries;
  }

  /**
   * @param entries
   *          the entries to set
   */
  public void setEntries( List<ResourceEntry> entries ) {
    this.entries = entries;
  }

  public String toXml() {
    return toXml( null, 0 );
  }

  public String toXml( ResourceXmlPropertyEmitterInterface injector ) {
    return toXml( injector, 0 );
  }

  public String toXml( int indentLevel ) {
    return toXml( null, indentLevel );
  }

  public String toXml( ResourceXmlPropertyEmitterInterface injector, int indentLevel ) {
    StringBuilder buff = new StringBuilder();
    addXmlElementWithAttribute( buff, indentLevel, "ActionComponent", "type", resourceReferenceHolder
      .getHolderType() );
    indentLevel++;
    addXmlElement( buff, indentLevel, "ComponentName", resourceReferenceHolder.getName() );
    addXmlElement( buff, indentLevel, "ComponentId", resourceReferenceHolder.getTypeId() );
    addXmlElement( buff, indentLevel, "ComponentResources" );
    indentLevel++;
    for ( ResourceEntry entry : this.getEntries() ) {
      buff.append( entry.toXml( indentLevel ) );
    }
    indentLevel--;
    addXmlCloseElement( buff, indentLevel, "ComponentResources" );
    if ( injector != null ) {
      addXmlElement( buff, indentLevel, "ComponentProperties" );
      indentLevel++;
      buff.append( injector.getExtraResourceProperties( resourceReferenceHolder, indentLevel ) );
      indentLevel--;
      addXmlCloseElement( buff, indentLevel, "ComponentProperties" );
    }
    indentLevel--;
    addXmlCloseElement( buff, indentLevel, "ActionComponent" );
    return buff.toString();
  }

  public void addXmlElementWithAttribute( StringBuilder buff, int indentLevel, String elementName, String attrName,
    String attrValue ) {
    buff
      .append( StringUtil.getIndent( indentLevel ) )
      .append( "<" ).append( elementName ).append( " " ).append( attrName ).append( "='" );
    buff.append( attrValue ).append( "'>" ).append( StringUtil.CRLF );
  }

  public void addXmlCloseElement( StringBuilder buff, int indentLevel, String elementName ) {
    buff.append( StringUtil.getIndent( indentLevel ) ).append( "</" ).append( elementName ).append( ">" ).append(
      StringUtil.CRLF );
  }

  public void addXmlElement( StringBuilder buff, int indentLevel, String elementName ) {
    buff.append( StringUtil.getIndent( indentLevel ) ).append( "<" ).append( elementName ).append( ">" ).append(
      StringUtil.CRLF );
  }

  public void addXmlElement( StringBuilder buff, int indentLevel, String elementName, String elementValue ) {
    buff
      .append( StringUtil.getIndent( indentLevel ) )
      .append( "<" ).append( elementName ).append( "><![CDATA[" ).append( elementValue ).append( "]]></" ).append(
        elementName ).append( ">" ).append( StringUtil.CRLF );
  }

}
