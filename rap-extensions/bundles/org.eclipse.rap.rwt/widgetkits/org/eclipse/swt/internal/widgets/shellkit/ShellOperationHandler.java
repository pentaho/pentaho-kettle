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
package org.eclipse.swt.internal.widgets.shellkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_CLOSE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MOVE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_RESIZE;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.find;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.events.EventUtil;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.internal.widgets.IShellAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


public class ShellOperationHandler extends ControlOperationHandler<Shell> {

  private static final String PROP_MODE = "mode";
  private static final String PROP_BOUNDS = "bounds";
  private static final String PROP_ACTIVE_CONTROL = "activeControl";

  public ShellOperationHandler( Shell shell ) {
    super( shell );
  }

  @Override
  public void handleSet( Shell shell, JsonObject properties ) {
    super.handleSet( shell, properties );
    handleSetMode( shell, properties );
    handleSetBounds( shell, properties );
    handleSetActiveControl( shell, properties );
  }

  @Override
  public void handleNotify( Shell shell, String eventName, JsonObject properties ) {
    if( EVENT_CLOSE.equals( eventName ) ) {
      handleNotifyClose( shell );
    } else if( EVENT_RESIZE.equals( eventName ) ) {
      handleNotifyResize();
    } else if( EVENT_MOVE.equals( eventName ) ) {
      handleNotifyMove();
    } else {
      super.handleNotify( shell, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET mode
   * Note: "mode" need to be set before "bounds"
   *
   * @param mode (String) the shell mode, could be "maximized", "minimized" or "normal"
   */
  public void handleSetMode( Shell shell, JsonObject properties ) {
    JsonValue value = properties.get( PROP_MODE );
    if( value != null ) {
      String mode = value.asString();
      if( "maximized".equals( mode ) ) {
        shell.setMaximized( true );
      } else if( "minimized".equals( mode ) ) {
        shell.setMinimized( true );
      } else {
        shell.setMinimized( false );
        shell.setMaximized( false );
      }
    }
  }

  /*
   * PROTOCOL SET bounds
   * Note: "bounds" need to be set after "mode"
   *
   * @param bounds ([int]) the shell bounds
   */
  public void handleSetBounds( Shell shell, JsonObject properties ) {
    JsonValue value = properties.get( PROP_BOUNDS );
    if( value != null ) {
      JsonArray arrayValue = value.asArray();
      Rectangle bounds = new Rectangle( arrayValue.get( 0 ).asInt(),
                                        arrayValue.get( 1 ).asInt(),
                                        arrayValue.get( 2 ).asInt(),
                                        arrayValue.get( 3 ).asInt() );
      getAdapter( shell ).setBounds( bounds );
    }
  }

  /*
   * PROTOCOL SET activeControl
   *
   * @param activeControl (string) the id of the new active control
   */
  public void handleSetActiveControl( Shell shell, JsonObject properties ) {
    JsonValue value = properties.get( PROP_ACTIVE_CONTROL );
    if( value != null ) {
      String activeControlId = value.asString();
      Widget widget = find( shell, activeControlId );
      if( widget != null ) {
        setActiveControl( shell, widget );
      }
    }
  }

  /*
   * PROTOCOL NOTIFY Close
   */
  public void handleNotifyClose( Shell shell ) {
    shell.close();
  }

  /*
   * PROTOCOL NOTIFY Resize
   *
   * ignored, Resize event is fired when set bounds
   */
  public void handleNotifyResize() {
  }

  /*
   * PROTOCOL NOTIFY Move
   *
   * ignored, Move event is fired when set bounds
   */
  public void handleNotifyMove() {
  }

  /*
   * PROTOCOL NOTIFY Activate
   */
  @Override
  public void handleNotifyActivate( Shell shell, JsonObject properties ) {
    IDisplayAdapter displayAdapter = shell.getDisplay().getAdapter( IDisplayAdapter.class );
    displayAdapter.setActiveShell( shell );
    displayAdapter.invalidateFocus();
  }

  private static void setActiveControl( Shell shell, Widget widget ) {
    if( EventUtil.isAccessible( widget ) ) {
      getAdapter( shell ).setActiveControl( ( Control )widget );
    }
  }

  @Override
  protected boolean allowMouseEvent( Shell shell, int x, int y ) {
    return super.allowMouseEvent( shell, x, y ) && y > getAdapter( shell ).getTopTrim();
  }

  private static IShellAdapter getAdapter( Shell shell ) {
    return shell.getAdapter( IShellAdapter.class );
  }

}
