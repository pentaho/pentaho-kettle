/*******************************************************************************
 * Copyright (c) 2011 Rüdiger Herrmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rüdiger Herrmann - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.swt.widgets.Widget;


public final class CellToolTipUtil {

  public static boolean isEnabledFor( Widget widget ) {
    boolean result = false;
    Object data = widget.getData( ICellToolTipProvider.ENABLE_CELL_TOOLTIP );
    if( Boolean.TRUE.equals( data ) ) {
      result = true;
    }
    return result;
  }

  public static ICellToolTipAdapter getAdapter( Widget widget ) {
    Object adapter = widget.getAdapter( ICellToolTipAdapter.class );
    return ( ICellToolTipAdapter )adapter;
  }
  
  private CellToolTipUtil() {
    // prevent instantiation
  }
}
