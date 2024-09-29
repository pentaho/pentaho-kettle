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

import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.trans.step.StepMeta;

public class TransPainterFlyoutTooltipExtension {

  private AreaOwner areaOwner;
  private TransGraph transGraph;
  private Point point;

  public static final String DET_RUN = "DET_RUN";
  public static final String DET_INSPECT = "DET_INSPECT";
  public static final String DET_LABEL = "DET_LABEL";

  public TransPainterFlyoutTooltipExtension( AreaOwner areaOwner, TransGraph transGraph, Point point ) {
    super();
    this.areaOwner = areaOwner;
    this.transGraph = transGraph;
    this.point = point;
  }

  public String getExtensionAreaType() {
    return (String) this.areaOwner.getExtensionAreaType();
  }

  public StepMeta getStepMeta() {
    return (StepMeta) this.areaOwner.getParent();
  }

  public TransGraph getTransGraph() {
    return transGraph;
  }

  public Point getPoint() {
    return point;
  }
}
