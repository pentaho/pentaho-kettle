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
