/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.database;

/**
 * Contains the information that's stored in a single catalog.
 *
 * @author Matt
 * @since 7-apr-2005
 */
public class Catalog {
  private String catalogName;
  private String[] items;

  public Catalog( String catalogName, String[] items ) {
    this.catalogName = catalogName;
    this.items = items;
  }

  public Catalog( String catalogName ) {
    this( catalogName, new String[] {} );
  }

  /**
   * @return Returns the catalogName.
   */
  public String getCatalogName() {
    return catalogName;
  }

  /**
   * @param catalogName
   *          The catalogName to set.
   */
  public void setCatalogName( String catalogName ) {
    this.catalogName = catalogName;
  }

  /**
   * @return Returns the items.
   */
  public String[] getItems() {
    return items;
  }

  /**
   * @param items
   *          The items to set.
   */
  public void setItems( String[] items ) {
    this.items = items;
  }
}
