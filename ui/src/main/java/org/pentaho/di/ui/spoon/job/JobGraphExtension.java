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

package org.pentaho.di.ui.spoon.job;

import org.eclipse.swt.events.MouseEvent;
import org.pentaho.di.core.gui.Point;

public class JobGraphExtension {

  private JobGraph jobGraph;
  private MouseEvent event;
  private Point point;

  public JobGraphExtension( JobGraph jobGraph, MouseEvent event, Point point ) {
    this.jobGraph = jobGraph;
    this.event = event;
    this.point = point;
  }

  public JobGraph getJobGraph() {
    return jobGraph;
  }

  public void setJobGraph( JobGraph jobGraph ) {
    this.jobGraph = jobGraph;
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
