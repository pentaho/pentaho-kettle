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

package org.pentaho.di.core.database;

/**
 * Contains the information that's stored in a single schema.
 *
 * @author Matt
 * @since 7-apr-2005
 */
public class Schema {
  private String schemaName;
  private String[] items;

  public Schema( String schemaName, String[] items ) {
    this.schemaName = schemaName;
    this.items = items;
  }

  public Schema( String schemaName ) {
    this( schemaName, new String[] {} );
  }

  /**
   * @return Returns the schemaName.
   */
  public String getSchemaName() {
    return schemaName;
  }

  /**
   * @param schemaName
   *          The catalogName to set.
   */
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
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
