package org.pentaho.di.ui.spoon.trans;

import org.eclipse.swt.events.MouseEvent;
import org.pentaho.di.core.gui.Point;

public class TransGraphExtension {

  private TransGraph transGraph;
  private MouseEvent event;
  private Point point;

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

}
