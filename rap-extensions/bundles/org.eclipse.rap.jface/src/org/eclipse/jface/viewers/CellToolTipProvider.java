/*******************************************************************************
 * Copyright (c) 2009, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.viewers;

import java.io.Serializable;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.internal.widgets.ICellToolTipAdapter;
import org.eclipse.swt.internal.widgets.ICellToolTipProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;

/**
 * Support for table/tree tool-tips in RAP
 */
final class CellToolTipProvider implements ICellToolTipProvider, Serializable {
  private ColumnViewer viewer;

  CellToolTipProvider( ColumnViewer viewer ) {
    this.viewer = viewer;
  }

  static void attach( ColumnViewer viewer, CellLabelProvider labelProvider ) {
    ICellToolTipAdapter adapter = getAdapter( viewer );
    if( labelProvider != null ) {
      CellToolTipProvider provider = new CellToolTipProvider( viewer );
      adapter.setCellToolTipProvider( provider );
    } else {
      adapter.setCellToolTipProvider( null );
    }
  }

  public void getToolTipText( final Item item, final int columnIndex ) {
    SafeRunnable.run( new SafeRunnable() {
      public void run() {
        Object element = item.getData();
        ViewerColumn column = viewer.getViewerColumn( columnIndex );
        CellLabelProvider labelProvider = column.getLabelProvider();
        if( labelProvider != null ) {
          String text = labelProvider.getToolTipText( element );
          ICellToolTipAdapter adapter = getAdapter( viewer );
          adapter.setCellToolTipText( text );
        }
      }
    } );
  }

  private static ICellToolTipAdapter getAdapter( ColumnViewer viewer ) {
    Control control = viewer.getControl();
    return control.getAdapter( ICellToolTipAdapter.class );
  }

}
