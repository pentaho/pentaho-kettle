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
