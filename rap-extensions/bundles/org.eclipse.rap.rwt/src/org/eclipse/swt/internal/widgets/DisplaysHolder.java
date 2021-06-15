/*******************************************************************************
 * Copyright (c) 2011 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - bug 348056: Eliminate compiler warnings
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import java.lang.ref.WeakReference;

import org.eclipse.swt.widgets.Display;


public class DisplaysHolder {
  private WeakReference<Display>[] displays;
  
  @SuppressWarnings("unchecked")
  public DisplaysHolder() {
    displays = new WeakReference[ 4 ];
  }
  
  public WeakReference<Display>[] getDisplays() {
    return displays;
  }
  
  public void setDisplays( WeakReference<Display>[] displays ) {
    this.displays = displays;
  }
}
