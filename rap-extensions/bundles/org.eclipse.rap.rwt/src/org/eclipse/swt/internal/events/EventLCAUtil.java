/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.events;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.readEventPropertyValue;

import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;


public final class EventLCAUtil {

  public static int readStateMask( Widget widget, String eventName ) {
    JsonValue altKey = readEventPropertyValue( getId( widget ), eventName, "altKey" );
    JsonValue ctrlKey = readEventPropertyValue( getId( widget ), eventName, "ctrlKey" );
    JsonValue shiftKey = readEventPropertyValue( getId( widget ), eventName, "shiftKey" );
    return translateModifier( JsonValue.TRUE.equals( altKey ),
                              JsonValue.TRUE.equals( ctrlKey ),
                              JsonValue.TRUE.equals( shiftKey ) );
  }

  static int translateModifier( boolean hasAltKey, boolean hasCtrlKey, boolean hasShiftKey ) {
    int result = 0;
    if( hasCtrlKey ) {
      result |= SWT.CTRL;
    }
    if( hasAltKey ) {
      result |= SWT.ALT;
    }
    if( hasShiftKey ) {
      result |= SWT.SHIFT;
    }
    return result;
  }

  public static int translateButton( int value ) {
    int result = 0;
    switch( value ) {
      case 1:
        result = SWT.BUTTON1;
      break;
      case 2:
        result = SWT.BUTTON2;
      break;
      case 3:
        result = SWT.BUTTON3;
      break;
      case 4:
        result = SWT.BUTTON4;
      break;
      case 5:
        result = SWT.BUTTON5;
      break;
    }
    return result;
  }

  public static boolean isListening( Widget widget, int eventType ) {
    for( Listener listener : widget.getListeners( eventType ) ) {
      if( !( listener instanceof ClientListener ) ) {
        return true;
      }
    }
    return false;
  }

  private EventLCAUtil() {
    // prevent instantiation
  }

  public static long getEventMask( int eventType ) {
    if( eventType <= 0 || eventType > 64 ) {
      return 0;
    }
    return 1L << (eventType - 1);
  }

  public static boolean containsEvent( long events, int event ) {
    return ( events & getEventMask( event ) ) != 0;
  }

}
