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

package org.pentaho.di.trans;

import java.util.List;

import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.trans.step.StepMeta;

public class TransPainterFlyoutExtension {

  public GCInterface gc;
  public boolean shadow;
  public List<AreaOwner> areaOwners;
  public TransMeta transMeta;
  public StepMeta stepMeta;
  public Point offset;
  public Point area;
  public float translationX;
  public float translationY;
  public float magnification;

  public TransPainterFlyoutExtension( GCInterface gc, List<AreaOwner> areaOwners, TransMeta transMeta,
      StepMeta stepMeta, float translationX, float translationY, float magnification, Point area, Point offset ) {
    super();
    this.gc = gc;
    this.areaOwners = areaOwners;
    this.transMeta = transMeta;
    this.stepMeta = stepMeta;
    this.translationX = translationX;
    this.translationY = translationY;
    this.magnification = magnification;
    this.area = area;
    this.offset = offset;
  }
}
