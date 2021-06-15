/*******************************************************************************
 * Copyright (c) 2014, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.displaykit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_RESIZE;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.events.EventUtil;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


public class DisplayOperationHandler extends AbstractOperationHandler {

  private static final String PROP_BOUNDS = "bounds";
  private static final String PROP_CURSOR_LOCATION = "cursorLocation";
  private static final String PROP_FOCUS_CONTROL = "focusControl";

  private final Display display;

  public DisplayOperationHandler( Display display ) {
    this.display = display;
  }

  @Override
  public void handleSet( JsonObject properties ) {
    handleSetBounds( display, properties );
    handleSetCursorLocation( display, properties );
    handleSetFocusControl( display, properties );
  }

  @Override
  public void handleNotify( String eventName, JsonObject properties ) {
    if( EVENT_RESIZE.equals( eventName ) ) {
      handleNotifyResize( display );
    }
  }

  /*
   * PROTOCOL SET bounds
   *
   * @param bounds ([int]) the display bounds
   */
  public void handleSetBounds( Display display, JsonObject properties ) {
    JsonValue value = properties.get( PROP_BOUNDS );
    if( value != null ) {
      JsonArray arrayValue = value.asArray();
      Rectangle bounds = new Rectangle( arrayValue.get( 0 ).asInt(),
                                        arrayValue.get( 1 ).asInt(),
                                        arrayValue.get( 2 ).asInt(),
                                        arrayValue.get( 3 ).asInt() );
      getDisplayAdapter( display ).setBounds( bounds );
    }
  }

  /*
   * PROTOCOL SET cursorLocation
   *
   * @param cursorLocation ([int]) the cursor location
   */
  public void handleSetCursorLocation( Display display, JsonObject properties ) {
    JsonValue value = properties.get( PROP_CURSOR_LOCATION );
    if( value != null ) {
      JsonArray arrayValue = value.asArray();
      Point location = new Point( arrayValue.get( 0 ).asInt(), arrayValue.get( 1 ).asInt() );
      getDisplayAdapter( display ).setCursorLocation( location.x, location.y );
    }
  }

  /*
   * PROTOCOL SET focusControl
   *
   * @param focusControl (string) the id of focused control
   */
  public void handleSetFocusControl( Display display, JsonObject properties ) {
    JsonValue value = properties.get( PROP_FOCUS_CONTROL );
    // Even though the loop below would anyway result in focusControl == null
    // the client may send 'null' to indicate that no control on the active
    // shell currently has the input focus.
    if( value != null && !value.isNull() ) {
      String id = value.asString();
      Control focusControl = null;
      // TODO [rh] revise this: traversing the widget tree once more only to find
      //      out which control is focused. Could that be optimized?
      Shell[] shells = getDisplayAdapter( display ).getShells();
      for( int i = 0; focusControl == null && i < shells.length; i++ ) {
        Widget widget = WidgetUtil.find( shells[ i ], id );
        if( widget instanceof Control ) {
          focusControl = ( Control )widget;
        }
      }
      if( focusControl != null && EventUtil.isAccessible( focusControl ) ) {
        getDisplayAdapter( display ).setFocusControl( focusControl, false );
      }
    }
  }

  /*
   * PROTOCOL NOTIFY Resize
   */
  public void handleNotifyResize( Display display ) {
    Event event = new Event();
    event.display = display;
    event.type = SWT.Resize;
    event.time = EventUtil.getLastEventTime();
    event.setBounds( display.getBounds() );
    getDisplayAdapter( display ).notifyListeners( SWT.Resize, event );
  }

  private static IDisplayAdapter getDisplayAdapter( Display display ) {
    return display.getAdapter( IDisplayAdapter.class );
  }

}
