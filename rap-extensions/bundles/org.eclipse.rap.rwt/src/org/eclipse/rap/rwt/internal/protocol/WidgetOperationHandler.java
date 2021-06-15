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
package org.eclipse.rap.rwt.internal.protocol;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_BUTTON;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_DETAIL;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_HEIGHT;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_TEXT;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_WIDTH;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_X;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_Y;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;


public abstract class WidgetOperationHandler<T extends Widget> extends AbstractOperationHandler {

  private final T widget;

  public WidgetOperationHandler( T widget ) {
    this.widget = widget;
  }

  @Override
  public void handleSet( JsonObject properties ) {
    handleSet( widget, properties );
  }

  @Override
  public void handleCall( String method, JsonObject parameters ) {
    handleCall( widget, method, parameters );
  }

  @Override
  public void handleNotify( String eventName, JsonObject properties ) {
    handleNotify( widget, eventName, properties );
  }

  /**
   * Handles a set operation for the given widget. To be implemented by subclasses.
   *
   * @param widget the widget that receives the operation
   * @param properties the properties to be set on the widget
   */
  public void handleSet( T widget, JsonObject properties ) {
    throw new UnsupportedOperationException( "set operations not supported by this handler" );
  }

  /**
   * Handles a call operation for the given widget. To be implemented by subclasses.
   *
   * @param widget the widget that receives the operation
   * @param method the method that is called
   * @param parameters the named method parameters
   */
  public void handleCall( T widget, String method, JsonObject parameters ) {
    throw new UnsupportedOperationException( "call operations not supported by this handler" );
  }

  /**
   * Handles a notify operation for the given widget. To be implemented by subclasses.
   *
   * @param widget the widget that receives the operation
   * @param eventName the event that the widget is notified of
   * @param properties the named event properties
   */
  public void handleNotify( T widget, String eventName, JsonObject properties ) {
    throw new UnsupportedOperationException( "notify operations not supported by this handler" );
  }

  protected static Event createSelectionEvent( int eventType, JsonObject properties ) {
    Event event = new Event();
    event.type = eventType;
    event.stateMask = readStateMask( properties );
    event.detail = readDetail( properties );
    event.text = readText( properties );
    event.button = readButton( properties );
    event.setBounds( readBounds( properties ) );
    return event;
  }

  protected static int readStateMask( JsonObject properties ) {
    int stateMask = SWT.NONE;
    if( JsonValue.TRUE.equals( properties.get( "altKey" ) ) ) {
      stateMask |= SWT.ALT;
    }
    if( JsonValue.TRUE.equals( properties.get( "ctrlKey" ) ) ) {
      stateMask |= SWT.CTRL;
    }
    if( JsonValue.TRUE.equals( properties.get( "shiftKey" ) ) ) {
      stateMask |= SWT.SHIFT;
    }
    return stateMask;
  }

  protected static int readDetail( JsonObject properties ) {
    int detail = SWT.NONE;
    JsonValue value = properties.get( EVENT_PARAM_DETAIL );
    if( value != null && value.isString() ) {
      String stringValue = value.asString();
      if( "check".equals( stringValue ) ) {
        detail = SWT.CHECK;
      } else if( "search".equals( stringValue ) ) {
        detail = SWT.ICON_SEARCH;
      } else if( "cancel".equals( stringValue ) ) {
        detail = SWT.ICON_CANCEL;
      } else if( "drag".equals( stringValue ) ) {
        detail = SWT.DRAG;
      } else if( "arrow".equals( stringValue ) ) {
        detail = SWT.ARROW;
      } else if( "cell".equals( stringValue ) ) {
        detail = RWT.CELL;
      } else if( "hyperlink".equals( stringValue ) ) {
        detail = RWT.HYPERLINK;
      }
    }
    return detail;
  }

  protected static Rectangle readBounds( JsonObject properties ) {
    Rectangle bounds = new Rectangle( 0, 0, 0, 0 );
    JsonValue x = properties.get( EVENT_PARAM_X );
    bounds.x = x == null ? 0 : x.asInt();
    JsonValue y = properties.get( EVENT_PARAM_Y );
    bounds.y = y == null ? 0 : y.asInt();
    JsonValue width = properties.get( EVENT_PARAM_WIDTH );
    bounds.width = width == null ? 0 : width.asInt();
    JsonValue height = properties.get( EVENT_PARAM_HEIGHT );
    bounds.height = height == null ? 0 : height.asInt();
    return bounds;
  }

  private static String readText( JsonObject properties ) {
    JsonValue value = properties.get( EVENT_PARAM_TEXT );
    return value == null ? null : value.asString();
  }

  private static int readButton( JsonObject properties ) {
    JsonValue value = properties.get( EVENT_PARAM_BUTTON );
    return value == null ? 0 : value.asInt();
  }

}
