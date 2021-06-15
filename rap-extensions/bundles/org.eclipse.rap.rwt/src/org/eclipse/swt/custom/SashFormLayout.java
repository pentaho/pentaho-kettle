/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.rap.rwt.theme.ControlThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Sash;

/**
 * This class provides the layout for SashForm
 *
 * @see SashForm
 */
class SashFormLayout extends Layout {
@Override
protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
  SashForm sashForm = (SashForm)composite;
  Control[] cArray = sashForm.getControls(true);
  int width = 0;
  int height = 0;
  if (cArray.length == 0) {
    if (wHint != SWT.DEFAULT) {
      width = wHint;
    }
    if (hHint != SWT.DEFAULT) {
      height = hHint;
    }
    return new Point(width, height);
  }
  // determine control sizes
  boolean vertical = sashForm.getOrientation() == SWT.VERTICAL;
  int maxIndex = 0;
  int maxValue = 0;
  for (int i = 0; i < cArray.length; i++) {
    if (vertical) {
      Point size = cArray[i].computeSize(wHint, SWT.DEFAULT, flushCache);
      if (size.y > maxValue) {
        maxIndex = i;
        maxValue = size.y;
      }
      width = Math.max(width, size.x);
    } else {
      Point size = cArray[i].computeSize(SWT.DEFAULT, hHint, flushCache);
      if (size.x > maxValue) {
        maxIndex = i;
        maxValue = size.x;
      }
      height = Math.max(height, size.y);
    }
  }
  // get the ratios
  long[] ratios = new long[cArray.length];
  long total = 0;
  for (int i = 0; i < cArray.length; i++) {
    Object data = cArray[i].getLayoutData();
    if (data != null && data instanceof SashFormData) {
      ratios[i] = ((SashFormData)data).weight;
    } else {
      data = new SashFormData();
      cArray[i].setLayoutData(data);
      ((SashFormData)data).weight = ratios[i] = ((200 << 16) + 999) / 1000;

    }
    total += ratios[i];
  }
  if (ratios[maxIndex] > 0) {
    int sashwidth = sashForm.SASH_WIDTH;
    if( sashForm.sashes.length > 0 ) {
      Sash first = sashForm.sashes[ 0 ];
      BoxDimensions border = first.getAdapter( ControlThemeAdapter.class ).getBorder( first );
      sashwidth += border.left + border.right;
    }
    if (vertical) {
      height += (int)(total * maxValue / ratios[maxIndex]) + (cArray.length - 1) * sashwidth;
    } else {
      width += (int)(total * maxValue / ratios[maxIndex]) + (cArray.length - 1) * sashwidth;
    }
  }
  BoxDimensions border = sashForm.getAdapter( ControlThemeAdapter.class ).getBorder( sashForm );
  width += border.left + border.right;
  height += border.top + border.bottom;
  if (wHint != SWT.DEFAULT) {
    width = wHint;
  }
  if (hHint != SWT.DEFAULT) {
    height = hHint;
  }
  return new Point(width, height);
}

@Override
protected boolean flushCache(Control control) {
  return true;
}

@Override
protected void layout(Composite composite, boolean flushCache) {
  SashForm sashForm = (SashForm)composite;
  Rectangle area = sashForm.getClientArea();
  if (area.width <= 1 || area.height <= 1) {
    return;
  }

  Control[] newControls = sashForm.getControls(true);
  if (sashForm.controls.length == 0 && newControls.length == 0) {
    return;
  }
  sashForm.controls = newControls;

  Control[] controls = sashForm.controls;

  if (sashForm.maxControl != null && !sashForm.maxControl.isDisposed()) {
    for (int i= 0; i < controls.length; i++){
      if (controls[i] != sashForm.maxControl) {
        controls[i].setBounds(-200, -200, 0, 0);
      } else {
        controls[i].setBounds(area);
      }
    }
    return;
  }

  // keep just the right number of sashes
  if (sashForm.sashes.length < controls.length - 1) {
    Sash[] newSashes = new Sash[controls.length - 1];
    System.arraycopy(sashForm.sashes, 0, newSashes, 0, sashForm.sashes.length);
    for (int i = sashForm.sashes.length; i < newSashes.length; i++) {
      newSashes[i] = new Sash(sashForm, sashForm.sashStyle);
      newSashes[i].setBackground(sashForm.background);
      newSashes[i].setForeground(sashForm.foreground);
      newSashes[i].addListener(SWT.Selection, sashForm.sashListener);
    }
    sashForm.sashes = newSashes;
  }
  if (sashForm.sashes.length > controls.length - 1) {
    if (controls.length == 0) {
      for (int i = 0; i < sashForm.sashes.length; i++) {
        sashForm.sashes[i].dispose();
      }
      sashForm.sashes = new Sash[0];
    } else {
      Sash[] newSashes = new Sash[controls.length - 1];
      System.arraycopy(sashForm.sashes, 0, newSashes, 0, newSashes.length);
      for (int i = controls.length - 1; i < sashForm.sashes.length; i++) {
        sashForm.sashes[i].dispose();
      }
      sashForm.sashes = newSashes;
    }
  }
  if (controls.length == 0) {
    return;
  }
  Sash[] sashes = sashForm.sashes;
  // get the ratios
  long[] ratios = new long[controls.length];
  long total = 0;
  for (int i = 0; i < controls.length; i++) {
    Object data = controls[i].getLayoutData();
    if (data != null && data instanceof SashFormData) {
      ratios[i] = ((SashFormData)data).weight;
    } else {
      data = new SashFormData();
      controls[i].setLayoutData(data);
      ((SashFormData)data).weight = ratios[i] = ((200 << 16) + 999) / 1000;

    }
    total += ratios[i];
  }
  int sashwidth = sashForm.SASH_WIDTH;
  if( sashes.length > 0 ) {
    Sash first = sashes[ 0 ];
    BoxDimensions border = first.getAdapter( ControlThemeAdapter.class ).getBorder( first );
    sashwidth += border.left + border.right;
  }
  if (sashForm.getOrientation() == SWT.HORIZONTAL) {
    int width = (int)(ratios[0] * (area.width - sashes.length * sashwidth) / total);
    int x = area.x;
    controls[0].setBounds(x, area.y, width, area.height);
    x += width;
    for (int i = 1; i < controls.length - 1; i++) {
      sashes[i - 1].setBounds(x, area.y, sashwidth, area.height);
      x += sashwidth;
      width = (int)(ratios[i] * (area.width - sashes.length * sashwidth) / total);
      controls[i].setBounds(x, area.y, width, area.height);
      x += width;
    }
    if (controls.length > 1) {
      sashes[sashes.length - 1].setBounds(x, area.y, sashwidth, area.height);
      x += sashwidth;
      width = area.width - x;
      controls[controls.length - 1].setBounds(x, area.y, width, area.height);
    }
  } else {
    int height = (int)(ratios[0] * (area.height - sashes.length * sashwidth) / total);
    int y = area.y;
    controls[0].setBounds(area.x, y, area.width, height);
    y += height;
    for (int i = 1; i < controls.length - 1; i++) {
      sashes[i - 1].setBounds(area.x, y, area.width, sashwidth);
      y += sashwidth;
      height = (int)(ratios[i] * (area.height - sashes.length * sashwidth) / total);
      controls[i].setBounds(area.x, y, area.width, height);
      y += height;
    }
    if (controls.length > 1) {
      sashes[sashes.length - 1].setBounds(area.x, y, area.width, sashwidth);
      y += sashwidth;
      height = area.height - y;
      controls[controls.length - 1].setBounds(area.x, y, area.width, height);
    }

  }
}
}
