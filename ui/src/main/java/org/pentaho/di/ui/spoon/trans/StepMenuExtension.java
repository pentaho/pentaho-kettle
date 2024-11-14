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


package org.pentaho.di.ui.spoon.trans;

import org.pentaho.ui.xul.containers.XulMenupopup;

public class StepMenuExtension {

  private TransGraph transGraph;
  private XulMenupopup menu;

  public StepMenuExtension( TransGraph transGraph, XulMenupopup menu ) {
    this.transGraph = transGraph;
    this.menu = menu;
  }

  public TransGraph getTransGraph() {
    return transGraph;
  }

  public void setTransGraph( TransGraph transGraph ) {
    this.transGraph = transGraph;
  }

  public XulMenupopup getMenu() {
    return menu;
  }

  public void setMenu( XulMenupopup menu ) {
    this.menu = menu;
  }
}
