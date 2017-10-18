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

package org.pentaho.di.trans;

import java.util.List;

import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.trans.step.StepMeta;

public class TransPainterExtension {

  public GCInterface gc;
  public boolean shadow;
  public List<AreaOwner> areaOwners;
  public TransMeta transMeta;
  public StepMeta stepMeta;
  public TransHopMeta transHop;
  public int x1, y1, x2, y2, mx, my;
  public Point offset;
  public int iconsize;

  public TransPainterExtension( GCInterface gc, boolean shadow, List<AreaOwner> areaOwners, TransMeta transMeta,
    StepMeta stepMeta, TransHopMeta transHop, int x1, int y1, int x2, int y2, int mx, int my, Point offset,
    int iconsize ) {
    super();
    this.gc = gc;
    this.shadow = shadow;
    this.areaOwners = areaOwners;
    this.transMeta = transMeta;
    this.stepMeta = stepMeta;
    this.transHop = transHop;
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.mx = mx;
    this.my = my;
    this.offset = offset;
    this.iconsize = iconsize;
  }
}
