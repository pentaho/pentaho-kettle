/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;


public interface ITreeAdapter {

  void setScrollLeft( int left );
  int getScrollLeft();

  void setTopItemIndex( int topItemIndex );
  int getTopItemIndex();

  boolean isCached( TreeItem item );
  Point getItemImageSize( int index );
  int getCellLeft( int index );
  int getCellWidth( int index );
  int getTextOffset( int index );
  int getTextMaxWidth( int index );
  int getCheckWidth();
  int getImageOffset( int index );
  int getIndentionWidth();
  int getCheckLeft();
  Rectangle getTextMargin();
  int getColumnLeft( TreeColumn column );

  void checkData();

  int getFixedColumns();
  boolean isFixedColumn( TreeColumn column );
}