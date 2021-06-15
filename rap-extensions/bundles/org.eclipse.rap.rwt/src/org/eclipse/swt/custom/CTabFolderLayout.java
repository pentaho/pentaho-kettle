/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * This class provides the layout for CTabFolder
 * 
 * @see CTabFolder
 */
class CTabFolderLayout extends Layout {
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    CTabFolder folder = (CTabFolder)composite;
//    CTabItem[] items = folder.items;
    CTabItem[] items = folder.getItems();
    // preferred width of tab area to show all tabs
    int tabW = 0;
//    GC gc = new GC(folder);
    for (int i = 0; i < items.length; i++) {
      if (folder.single) {
//        tabW = Math.max(tabW, items[i].preferredWidth(gc, true, false));
        tabW = Math.max(tabW, items[i].preferredWidth(true, false));
      } else {
//        tabW += items[i].preferredWidth(gc, i == folder.selectedIndex, false);
        tabW += items[i].preferredWidth(i == folder.selectedIndex, false);
      }
    }
//    gc.dispose();
    tabW += 3;
    if (folder.showMax) tabW += CTabFolder.BUTTON_SIZE;
    if (folder.showMin) tabW += CTabFolder.BUTTON_SIZE;
    if (folder.single) tabW += 3*CTabFolder.BUTTON_SIZE/2; //chevron
    if (folder.topRight != null) {
      Point pt = folder.topRight.computeSize(SWT.DEFAULT, folder.tabHeight, flushCache);
      tabW += 3 + pt.x;
    }
//    if (!folder.single && !folder.simple) tabW += folder.curveWidth - 2*folder.curveIndent;
    
    int controlW = 0;
    int controlH = 0;
    // preferred size of controls in tab items
    for (int i = 0; i < items.length; i++) {
      Control control = items[i].getControl();
      if (control != null && !control.isDisposed()){
        Point size = control.computeSize (wHint, hHint, flushCache);
        controlW = Math.max (controlW, size.x);
        controlH = Math.max (controlH, size.y);
      }
    }

    int minWidth = Math.max(tabW, controlW);
    int minHeight = (folder.minimized) ? 0 : controlH;
    if (minWidth == 0) minWidth = CTabFolder.DEFAULT_WIDTH;
    if (minHeight == 0) minHeight = CTabFolder.DEFAULT_HEIGHT;
    
    if (wHint != SWT.DEFAULT) minWidth  = wHint;
    if (hHint != SWT.DEFAULT) minHeight = hHint;
    
    return new Point (minWidth, minHeight);
  }
  protected boolean flushCache(Control control) {
    return true;
  }
  protected void layout(Composite composite, boolean flushCache) {
    CTabFolder folder = (CTabFolder)composite;
    // resize content
    if (folder.selectedIndex != -1) {
//      Control control = folder.items[folder.selectedIndex].getControl();
      Control control = folder.getItem( folder.selectedIndex ).getControl();
      if (control != null && !control.isDisposed()) {
        control.setBounds(folder.getClientArea());
      }
    }
  }
}
