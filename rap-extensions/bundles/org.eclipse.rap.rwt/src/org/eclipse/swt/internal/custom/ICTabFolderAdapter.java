/*******************************************************************************
 * Copyright (c) 2002, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.custom;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IWidgetGraphicsAdapter;


public interface ICTabFolderAdapter {

  boolean getChevronVisible();
  Rectangle getChevronRect();

  Rectangle getMinimizeRect();
  Rectangle getMaximizeRect();

  void showListMenu();

  boolean showItemImage( CTabItem item );
  boolean showItemClose( CTabItem item );
  String getShortenedItemText( CTabItem item );

  public Color getUserSelectionForeground();

  public Color getUserSelectionBackground();

  public Image getUserSelectionBackgroundImage();

  // TODO [rst] This method should either return a suitable data structure or it
  //            should be replaced by separate methods for colors and percents.
  public IWidgetGraphicsAdapter getUserSelectionBackgroundGradient();

  void doRedraw();

}
