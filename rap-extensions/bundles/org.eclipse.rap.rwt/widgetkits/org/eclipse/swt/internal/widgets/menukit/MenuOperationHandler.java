/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.menukit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_HELP;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_HIDE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SHOW;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;


public class MenuOperationHandler extends WidgetOperationHandler<Menu> {

  public MenuOperationHandler( Menu menu ) {
    super( menu );
  }

  @Override
  public void handleNotify( Menu menu, String eventName, JsonObject properties ) {
    if( EVENT_SHOW.equals( eventName ) ) {
      handleNotifyShow( menu );
    } else if( EVENT_HIDE.equals( eventName ) ) {
      handleNotifyHide( menu );
    } else if( EVENT_HELP.equals( eventName ) ) {
      handleNotifyHelp( menu );
    } else {
      super.handleNotify( menu, eventName, properties );
    }
  }

  /*
   * PROTOCOL NOTIFY Show
   */
  public void handleNotifyShow( Menu menu ) {
    menu.notifyListeners( SWT.Show, new Event() );
    for( MenuItem item : menu.getItems() ) {
      if( isArmingMenuItem( item ) ) {
        item.notifyListeners( SWT.Arm, new Event() );
      }
    }
  }

  /*
   * PROTOCOL NOTIFY Hide
   */
  public void handleNotifyHide( Menu menu ) {
    menu.notifyListeners( SWT.Hide, new Event() );
  }

  /*
   * PROTOCOL NOTIFY Help
   */
  public void handleNotifyHelp( Menu menu ) {
    menu.notifyListeners( SWT.Help, new Event() );
  }

  private static boolean isArmingMenuItem( MenuItem item ) {
    return ( item.getStyle() & ( SWT.PUSH | SWT.CASCADE | SWT.CHECK | SWT.RADIO ) ) != 0;
  }

}
