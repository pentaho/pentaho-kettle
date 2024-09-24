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

package org.pentaho.di.ui.core.database.dialog;

import java.util.ArrayList;

import org.pentaho.ui.xul.util.AbstractModelNode;

public class DatabaseExplorerNode extends AbstractModelNode<DatabaseExplorerNode> {

  private static final long serialVersionUID = -7409853507740739091L;

  private String name;
  private String schema;
  private String image;
  private boolean isTable;
  private boolean isSchema;

  // possibly a combination of schema and table
  private String label;

  public DatabaseExplorerNode() {
    this.children = new ArrayList<DatabaseExplorerNode>();
  }

  public String getName() {
    return this.name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String toString() {
    return "Database Node: " + this.name;
  }

  public String getImage() {
    return this.image;
  }

  public void setImage( String aImage ) {
    this.image = aImage;
  }

  public void setIsSchema( boolean isSchema ) {
    this.isSchema = isSchema;
  }

  public boolean isSchema() {
    return isSchema;
  }

  public boolean isTable() {
    return this.isTable;
  }

  public void setIsTable( boolean aIsTable ) {
    this.isTable = aIsTable;
  }

  public void setSchema( String schema ) {
    this.schema = schema;
  }

  public String getSchema() {
    return schema;
  }

  public String getLabel() {
    if ( label != null ) {
      return label;
    } else {
      return name;
    }
  }

  public void setLabel( String label ) {
    this.label = label;
  }

}
