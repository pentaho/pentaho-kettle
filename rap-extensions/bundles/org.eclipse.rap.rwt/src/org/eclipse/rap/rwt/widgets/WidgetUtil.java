/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.widgets;

import org.eclipse.swt.widgets.Widget;


/**
 * Helper class to access RWT specific properties of widgets.
 *
 * @since 2.3
 */
public final class WidgetUtil {

  private WidgetUtil() {
    // prevent instantiation
  }

  /**
   * Returns the protocol ID of the given widget.
   *
   * @param widget the widget to obtain the id for, must not be <code>null</code>
   * @return the id for the given <code>widget</code>
   */
  public static String getId( Widget widget ) {
    return org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId( widget );
  }

  /**
   * Adds keys to the list of widget data keys to be synchronized with the client. It is save to add
   * the same key twice, there are no side-effects. The method has to be called from the UI thread
   * and affects the entire UI-session. The data is only transferred from server to client, not
   * back.
   *
   * @see org.eclipse.swt.widgets.Widget#setData(String, Object)
   * @param keys The keys to add to the list.
   */
  public static void registerDataKeys( String... keys ) {
    org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.registerDataKeys( keys );
  }

}
