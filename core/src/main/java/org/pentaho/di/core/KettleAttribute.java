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


package org.pentaho.di.core;

public class KettleAttribute implements KettleAttributeInterface {
  private String key;

  private String xmlCode;
  private String repCode;
  private String description;
  private String tooltip;
  private int type;
  private KettleAttributeInterface parent;

  /**
   * @param key
   * @param xmlCode
   * @param repCode
   * @param description
   * @param tooltip
   * @param type
   */
  public KettleAttribute( String key, String xmlCode, String repCode, String description, String tooltip,
    int type, KettleAttributeInterface parent ) {
    this.key = key;
    this.xmlCode = xmlCode;
    this.repCode = repCode;
    this.description = description;
    this.tooltip = tooltip;
    this.type = type;
    this.parent = parent;
  }

  @Override
  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  /**
   * @return the xmlCode
   */
  @Override
  public String getXmlCode() {
    return xmlCode;
  }

  /**
   * @param xmlCode
   *          the xmlCode to set
   */
  public void setXmlCode( String xmlCode ) {
    this.xmlCode = xmlCode;
  }

  /**
   * @return the repCode
   */
  @Override
  public String getRepCode() {
    return repCode;
  }

  /**
   * @param repCode
   *          the repCode to set
   */
  public void setRepCode( String repCode ) {
    this.repCode = repCode;
  }

  /**
   * @return the description
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * @return the tooltip
   */
  @Override
  public String getTooltip() {
    return tooltip;
  }

  /**
   * @param tooltip
   *          the tooltip to set
   */
  public void setTooltip( String tooltip ) {
    this.tooltip = tooltip;
  }

  /**
   * @return the type
   */
  @Override
  public int getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType( int type ) {
    this.type = type;
  }

  /**
   * @return the parent
   */
  @Override
  public KettleAttributeInterface getParent() {
    return parent;
  }

  /**
   * @param parent
   *          the parent to set
   */
  public void setParent( KettleAttributeInterface parent ) {
    this.parent = parent;
  }
}
