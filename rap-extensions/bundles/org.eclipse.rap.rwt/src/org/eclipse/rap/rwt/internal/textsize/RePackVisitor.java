/*******************************************************************************
 * Copyright (c) 2011, 2015 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import org.eclipse.swt.internal.widgets.ControlUtil;
import org.eclipse.swt.internal.widgets.IColumnAdapter;
import org.eclipse.swt.internal.widgets.IControlAdapter;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;


public class RePackVisitor implements WidgetTreeVisitor {

  @Override
  public boolean visit( Widget widget ) {
    if( widget instanceof Control ) {
      Control control = ( Control )widget;
      IControlAdapter adapter = ControlUtil.getControlAdapter( control );
      if( adapter.isPacked() ) {
        control.pack();
        adapter.clearPacked();
      }
    } else if( widget instanceof TableColumn ) {
      TableColumn column = ( TableColumn )widget;
      IColumnAdapter adapter = getAdapter( column );
      if( adapter.isPacked() ) {
        column.pack();
        adapter.clearPacked();
      }
    } else if( widget instanceof TreeColumn ) {
      TreeColumn column = ( TreeColumn )widget;
      IColumnAdapter adapter = getAdapter( column );
      if( adapter.isPacked() ) {
        column.pack();
        adapter.clearPacked();
      }
    }
    return true;
  }

  private static IColumnAdapter getAdapter( Item column ) {
    return column.getAdapter( IColumnAdapter.class );
  }

}
