/*******************************************************************************
 * Copyright (c) 2013, 2019 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import static org.eclipse.swt.internal.widgets.MarkupUtil.MarkupTarget.TOOLTIP;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Widget;


public class MarkupUtil {

  public enum MarkupTarget {
    TEXT,
    TOOLTIP
  }

  public interface MarkupPreconditionChecker {
    public boolean check();
  }

  public static boolean isMarkupEnabledFor( Widget widget ) {
    return Boolean.TRUE.equals( widget.getData( RWT.MARKUP_ENABLED ) );
  }

  public static boolean isToolTipMarkupEnabledFor( Widget widget ) {
    return Boolean.TRUE.equals( widget.getData( RWT.TOOLTIP_MARKUP_ENABLED ) );
  }

  public static void checkMarkupPrecondition( String key,
                                              MarkupTarget target,
                                              MarkupPreconditionChecker checker )
  {
    String dataKey = TOOLTIP.equals( target ) ? RWT.TOOLTIP_MARKUP_ENABLED : RWT.MARKUP_ENABLED;
    String dataKeyName = TOOLTIP.equals( target ) ? "RWT.TOOLTIP_MARKUP_ENABLED" : "RWT.MARKUP_ENABLED";
    if( dataKey.equals( key ) && !checker.check() ) {
      SWTException exception = new SWTException();
      String message = dataKeyName + " must be set before any widget data.";
      exception.throwable = new IllegalStateException( message );
      throw exception;
    }
  }

  private MarkupUtil() {
    // prevent instantiation
  }

}
