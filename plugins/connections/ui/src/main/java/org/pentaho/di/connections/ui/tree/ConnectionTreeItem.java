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

package org.pentaho.di.connections.ui.tree;

/**
 * Created by bmorrise on 2/4/19.
 */
public class ConnectionTreeItem {
  private String label;

  public ConnectionTreeItem( String label ) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel( String label ) {
    this.label = label;
  }
}
