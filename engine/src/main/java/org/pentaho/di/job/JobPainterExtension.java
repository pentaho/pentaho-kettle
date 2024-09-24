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

package org.pentaho.di.job;

import java.util.List;

import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;

public class JobPainterExtension {

  public GCInterface gc;
  public boolean shadow;
  public List<AreaOwner> areaOwners;
  public JobMeta jobMeta;
  public JobHopMeta jobHop;
  public int x1, y1, x2, y2, mx, my;
  public Point offset;

  public JobPainterExtension( GCInterface gc, boolean shadow, List<AreaOwner> areaOwners, JobMeta jobMeta,
    JobHopMeta jobHop, int x1, int y1, int x2, int y2, int mx, int my, Point offset ) {
    super();
    this.gc = gc;
    this.shadow = shadow;
    this.areaOwners = areaOwners;
    this.jobMeta = jobMeta;
    this.jobHop = jobHop;
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.mx = mx;
    this.my = my;
    this.offset = offset;
  }
}
