/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.expandbarkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.find;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_COLLAPSE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_EXPAND;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_ITEM;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;


public class ExpandBarOperationHandler extends ControlOperationHandler<ExpandBar> {

  public ExpandBarOperationHandler( ExpandBar expandBar ) {
    super( expandBar );
  }

  @Override
  public void handleNotify( ExpandBar expandBar, String eventName, JsonObject properties ) {
    if( EVENT_EXPAND.equals( eventName ) ) {
      handleNotifyExpand( expandBar, properties );
    } else if( EVENT_COLLAPSE.equals( eventName ) ) {
      handleNotifyCollapse( expandBar, properties );
    } else {
      super.handleNotify( expandBar, eventName, properties );
    }
  }

  /*
   * PROTOCOL NOTIFY Expand
   *
   * @param item (string) id of expanded item
   */
  public void handleNotifyExpand( ExpandBar expandBar, JsonObject properties ) {
    Event event = new Event();
    event.item = getItem( expandBar, properties.get( EVENT_PARAM_ITEM ).asString() );
    expandBar.notifyListeners( SWT.Expand, event );
  }

  /*
   * PROTOCOL NOTIFY Collapse
   *
   * @param item (string) id of collapsed item
   */
  public void handleNotifyCollapse( ExpandBar expandBar, JsonObject properties ) {
    Event event = new Event();
    event.item = getItem( expandBar, properties.get( EVENT_PARAM_ITEM ).asString() );
    expandBar.notifyListeners( SWT.Collapse, event );
  }

  private static ExpandItem getItem( ExpandBar bar, String itemId ) {
    return ( ExpandItem )find( bar, itemId );
  }

}
