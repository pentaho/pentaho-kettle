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
  
  public TransPainterExtension(GCInterface gc, boolean shadow, List<AreaOwner> areaOwners, TransMeta transMeta, StepMeta stepMeta,
      TransHopMeta transHop, int x1, int y1, int x2, int y2, int mx, int my, Point offset, int iconsize) {
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
