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

package org.pentaho.di.ui.spoon.trans;

import org.eclipse.swt.events.MouseEvent;
import org.pentaho.di.core.gui.Point;

public class TransGraphExtension {

  private TransGraph transGraph;
  private MouseEvent event;
  private Point point;
  private boolean preventDefault;

  public TransGraphExtension( TransGraph transGraph, MouseEvent event, Point point ) {
    this.transGraph = transGraph;
    this.event = event;
    this.point = point;
  }

  public TransGraph getTransGraph() {
    return transGraph;
  }

  public void setTransGraph( TransGraph transGraph ) {
    this.transGraph = transGraph;
  }

  public MouseEvent getEvent() {
    return event;
  }

  public void setEvent( MouseEvent event ) {
    this.event = event;
  }

  public Point getPoint() {
    return point;
  }

  public void setPoint( Point point ) {
    this.point = point;
  }

  public boolean isPreventDefault() {
    return preventDefault;
  }

  public void setPreventDefault( boolean preventDefault ) {
    this.preventDefault = preventDefault;
  }
}
