/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

package org.eclipse.jface.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

public abstract class ToolTip extends org.eclipse.swt.widgets.ToolTip {

  public static final int RECREATE = 1;
  public static final int NO_RECREATE = 1 << 1;

  public ToolTip( Control control ) {
    super( control.getShell(), SWT.ICON_INFORMATION );
  }

  public ToolTip( Control control, int style, boolean manualActivation ) {
    super( control.getShell(), SWT.ICON_INFORMATION );
  }

  public void setRespectMonitorBounds(boolean b) {
    // TODO Auto-generated method stub
    
  }

  public void setRespectDisplayBounds(boolean b) {
    // TODO Auto-generated method stub
    
  }

  public void setHideDelay(int i) {
    // TODO Auto-generated method stub
    
  }

  public void setPopupDelay(int i) {
    // TODO Auto-generated method stub
    
  }

  public void setHideOnMouseDown(boolean b) {
    // TODO Auto-generated method stub
    
  }

  public void setShift(Point point) {
    // TODO Auto-generated method stub
    
  }

  public void show(Point location) {
    super.setLocation( location );
    super.setVisible( true );
  }

  public void hide() {
    super.setVisible( false );
  }

  protected void afterHideToolTip(Event event) {
    // TODO Auto-generated method stub
    
  }
}
