/*******************************************************************************
 * Copyright (c) 2013, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_ACTIVATE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEACTIVATE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_FOCUS_IN;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_FOCUS_OUT;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_HELP;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_KEY_DOWN;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MENU_DETECT;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MOUSE_DOUBLE_CLICK;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MOUSE_DOWN;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MOUSE_UP;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_BUTTON;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_CHAR_CODE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_KEY_CODE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_TIME;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_X;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_Y;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_TRAVERSE;
import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.wasEventSent;
import static org.eclipse.swt.internal.events.EventLCAUtil.translateButton;
import static org.eclipse.swt.internal.widgets.ControlUtil.getControlAdapter;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;


public abstract class ControlOperationHandler<T extends Control> extends WidgetOperationHandler<T> {

  private static final String PROP_FOREGROUND = "foreground";
  private static final String PROP_BACKGROUND = "background";
  private static final String PROP_VISIBILITY = "visibility";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_TOOL_TIP = "toolTip";
  private static final String PROP_CURSOR = "cursor";

  public ControlOperationHandler( T control ) {
    super( control );
  }

  @Override
  public void handleSet( T control, JsonObject properties ) {
    handleSetForeground( control, properties );
    handleSetBackground( control, properties );
    handleSetVisibility( control, properties );
    handleSetEnabled( control, properties );
    handleSetToolTip( control, properties );
    handleSetCursor( control, properties );
  }

  @Override
  public void handleNotify( T control, String eventName, JsonObject properties ) {
    if( EVENT_FOCUS_IN.equals( eventName ) ) {
      handleNotifyFocusIn( control, properties );
    } else if( EVENT_FOCUS_OUT.equals( eventName ) ) {
      handleNotifyFocusOut( control, properties );
    } else if( EVENT_MOUSE_DOWN.equals( eventName ) ) {
      handleNotifyMouseDown( control, properties );
    } else if( EVENT_MOUSE_DOUBLE_CLICK.equals( eventName ) ) {
      handleNotifyMouseDoubleClick( control, properties );
    } else if( EVENT_MOUSE_UP.equals( eventName ) ) {
      handleNotifyMouseUp( control, properties );
    } else if( EVENT_TRAVERSE.equals( eventName ) ) {
      handleNotifyTraverse( control, properties );
    } else if( EVENT_KEY_DOWN.equals( eventName ) ) {
      handleNotifyKeyDown( control, properties );
    } else if( EVENT_MENU_DETECT.equals( eventName ) ) {
      handleNotifyMenuDetect( control, properties );
    } else if( EVENT_HELP.equals( eventName ) ) {
      handleNotifyHelp( control, properties );
    } else if( EVENT_ACTIVATE.equals( eventName ) ) {
      handleNotifyActivate( control, properties );
    } else if( EVENT_DEACTIVATE.equals( eventName ) ) {
      handleNotifyDeactivate( control, properties );
    } else {
      super.handleNotify( control, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET foreground
   *
   * @param foreground ([int]) the foreground color of the control as RGB array or null
   */
  public void handleSetForeground( T control, JsonObject properties ) {
    JsonValue value = properties.get( PROP_FOREGROUND );
    if( value != null ) {
      Color foreground = null;
      if( !value.isNull() ) {
        JsonArray arrayValue = value.asArray();
        foreground = new Color( control.getDisplay(),
                                arrayValue.get( 0 ).asInt(),
                                arrayValue.get( 1 ).asInt(),
                                arrayValue.get( 2 ).asInt() );
      }
      getControlAdapter( control ).setForeground( foreground );
    }
  }

  /*
   * PROTOCOL SET background
   *
   * @param foreground ([int]) the background color of the control as RGB array or null
   */
  public void handleSetBackground( T control, JsonObject properties ) {
    JsonValue value = properties.get( PROP_BACKGROUND );
    if( value != null ) {
      Color background = null;
      if( !value.isNull() ) {
        JsonArray arrayValue = value.asArray();
        background = new Color( control.getDisplay(),
                                arrayValue.get( 0 ).asInt(),
                                arrayValue.get( 1 ).asInt(),
                                arrayValue.get( 2 ).asInt() );
      }
      getControlAdapter( control ).setBackground( background );
    }
  }

  /*
   * PROTOCOL SET visibility
   *
   * @param visibility (boolean) true if control is visible, false otherwise
   */
  public void handleSetVisibility( T control, JsonObject properties ) {
    JsonValue value = properties.get( PROP_VISIBILITY );
    if( value != null ) {
      getControlAdapter( control ).setVisible( value.asBoolean() );
    }
  }

  /*
   * PROTOCOL SET enabled
   *
   * @param enabled (boolean) true if control is enabled, false otherwise
   */
  public void handleSetEnabled( T control, JsonObject properties ) {
    JsonValue value = properties.get( PROP_ENABLED );
    if( value != null ) {
      getControlAdapter( control ).setEnabled( value.asBoolean() );
    }
  }

  /*
   * PROTOCOL SET toolTip
   *
   * @param toolTip (String) the new toolTip text
   */
  public void handleSetToolTip( T control, JsonObject properties ) {
    JsonValue value = properties.get( PROP_TOOL_TIP );
    if( value != null ) {
      String toolTipText = value.isNull() ? null : value.asString();
      getControlAdapter( control ).setToolTipText( toolTipText );
    }
  }

  /*
   * PROTOCOL SET cursor
   *
   * @param cursor (String) the new cursor as defined in CSS specification
   */
  public void handleSetCursor( T control, JsonObject properties ) {
    JsonValue value = properties.get( PROP_CURSOR );
    if( value != null ) {
      Cursor cursor = null;
      if( !value.isNull() ) {
        cursor = new Cursor( control.getDisplay(), translateCursor( value.asString() ) );
      }
      getControlAdapter( control ).setCursor( cursor );
    }
  }

  /*
   * PROTOCOL NOTIFY FocusIn
   */
  @SuppressWarnings( "unused" )
  public void handleNotifyFocusIn( T control, JsonObject properties ) {
    control.notifyListeners( SWT.FocusIn, new Event() );
  }

  /*
   * PROTOCOL NOTIFY FocusOut
   */
  @SuppressWarnings( "unused" )
  public void handleNotifyFocusOut( T control, JsonObject properties ) {
    control.notifyListeners( SWT.FocusOut, new Event() );
  }

  /*
   * PROTOCOL NOTIFY MouseDown
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param button (int) the number of the mouse button as in Event.button
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   */
  public void handleNotifyMouseDown( T control, JsonObject properties ) {
    Event event = createMouseEvent( SWT.MouseDown, control, properties );
    if( allowMouseEvent( control, event.x, event.y ) ) {
      control.notifyListeners( event.type, event );
    }
  }

  /*
   * PROTOCOL NOTIFY MouseDoubleClick
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param button (int) the number of the mouse button as in Event.button
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   */
  public void handleNotifyMouseDoubleClick( T control, JsonObject properties ) {
    Event event = createMouseEvent( SWT.MouseDoubleClick, control, properties );
    if( allowMouseEvent( control, event.x, event.y ) ) {
      control.notifyListeners( event.type, event );
    }
  }

  /*
   * PROTOCOL NOTIFY MouseUp
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param button (int) the number of the mouse button as in Event.button
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   */
  public void handleNotifyMouseUp( T control, JsonObject properties ) {
    Event event = createMouseEvent( SWT.MouseUp, control, properties );
    if( allowMouseEvent( control, event.x, event.y ) ) {
      control.notifyListeners( event.type, event );
    }
  }

  /*
   * PROTOCOL NOTIFY Traverse
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param keyCode (int) the key code of the key that was typed
   * @param charCode (int) the char code of the key that was typed
   */
  public void handleNotifyTraverse( T control, JsonObject properties ) {
    processTraverseEvent( control, properties );
  }

  /*
   * PROTOCOL NOTIFY KeyDown
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param keyCode (int) the key code of the key that was typed
   * @param charCode (int) the char code of the key that was typed
   */
  public void handleNotifyKeyDown( T control, JsonObject properties ) {
    control.notifyListeners( SWT.KeyDown, createKeyEvent( properties ) );
    control.notifyListeners( SWT.KeyUp, createKeyEvent( properties ) );
  }

  /*
   * PROTOCOL NOTIFY MenuDetect
   *
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   */
  public void handleNotifyMenuDetect( T control, JsonObject properties ) {
    control.notifyListeners( SWT.MenuDetect, createMenuDetectEvent( properties ) );
  }

  /*
   * PROTOCOL NOTIFY Help
   */
  @SuppressWarnings( "unused" )
  public void handleNotifyHelp( T control, JsonObject properties ) {
    control.notifyListeners( SWT.Help, new Event() );
  }

  /*
   * PROTOCOL NOTIFY Activate
   *
   * ignored, Activate event is fired when set activeControl
   */
  @SuppressWarnings( "unused" )
  public void handleNotifyActivate( T control, JsonObject properties ) {
  }

  /*
   * PROTOCOL NOTIFY Deactivate
   *
   * ignored, Deactivate event is fired when set activeControl
   */
  @SuppressWarnings( "unused" )
  public void handleNotifyDeactivate( T control, JsonObject properties ) {
  }

  static Event createMouseEvent( int eventType, Control control, JsonObject properties ) {
    Event event = new Event();
    event.type = eventType;
    event.widget = control;
    event.button = properties.get( EVENT_PARAM_BUTTON ).asInt();
    int x = properties.get( EVENT_PARAM_X ).asInt();
    int y = properties.get( EVENT_PARAM_Y ).asInt();
    Point point = control.getDisplay().map( null, control, x, y );
    event.x = point.x;
    event.y = point.y;
    event.time = properties.get( EVENT_PARAM_TIME ).asInt();
    event.stateMask = readStateMask( properties ) | translateButton( event.button );
    // TODO: send count by the client
    event.count = determineCount( eventType, control );
    return event;
  }

  protected boolean allowMouseEvent( T control, int x, int y ) {
    Point size = control.getSize();
    int borderWidth = control.getBorderWidth();
    Rectangle outerBounds =  new Rectangle( - borderWidth, - borderWidth, size.x, size.y );
    Rectangle innerBounds = new Rectangle( 0, 0, size.x - 2 * borderWidth, size.y - 2 * borderWidth );
    return !outerBounds.contains( x, y ) || innerBounds.contains( x, y );
  }

  private static int determineCount( int eventType, Control control ) {
    if(    eventType == SWT.MouseDoubleClick
        || wasEventSent( getId( control ), EVENT_MOUSE_DOUBLE_CLICK ) )
    {
      return 2;
    }
    return 1;
  }

  private static void processTraverseEvent( Control control, JsonObject properties ) {
    int keyCode = properties.get( EVENT_PARAM_KEY_CODE ).asInt();
    int charCode = properties.get( EVENT_PARAM_CHAR_CODE ).asInt();
    int stateMask = readStateMask( properties );
    int traverseKey = getTraverseKey( keyCode, stateMask );
    if( traverseKey != SWT.TRAVERSE_NONE ) {
      Event event = createKeyEvent( keyCode, charCode, stateMask );
      event.detail = traverseKey;
      control.notifyListeners( SWT.Traverse, event );
    }
  }

  static Event createKeyEvent( JsonObject properties ) {
    int keyCode = properties.get( EVENT_PARAM_KEY_CODE ).asInt();
    int charCode = properties.get( EVENT_PARAM_CHAR_CODE ).asInt();
    int stateMask = readStateMask( properties );
    return createKeyEvent( keyCode, charCode, stateMask );
  }

  static Event createMenuDetectEvent( JsonObject properties ) {
    Event event = new Event();
    event.x = properties.get( EVENT_PARAM_X ).asInt();
    event.y = properties.get( EVENT_PARAM_Y ).asInt();
    return event;
  }

  static Event createKeyEvent( int keyCode, int charCode, int stateMask ) {
    Event event = new Event();
    event.keyCode = translateKeyCode( keyCode );
    if( charCode == 0 ) {
      if( ( event.keyCode & SWT.KEYCODE_BIT ) == 0 ) {
        event.character = translateCharacter( event.keyCode );
      }
    } else {
      event.character = translateCharacter( charCode );
      if( Character.isLetter( charCode ) ) {
        // NOTE : keycodes from browser are the upper-case character, in SWT it is the lower-case
        event.keyCode = Character.toLowerCase( charCode );
      }
    }
    event.stateMask = stateMask;
    return event;
  }

  static int getTraverseKey( int keyCode, int stateMask ) {
    switch( keyCode ) {
      case 27:
        return SWT.TRAVERSE_ESCAPE;
      case 13:
        return SWT.TRAVERSE_RETURN;
      case 9:
        if( ( stateMask & SWT.MODIFIER_MASK ) == 0 ) {
          return SWT.TRAVERSE_TAB_NEXT;
        }
        if( stateMask == SWT.SHIFT ) {
          return SWT.TRAVERSE_TAB_PREVIOUS;
        }
        return SWT.TRAVERSE_NONE;
      default:
        return SWT.TRAVERSE_NONE;
    }
  }

  static int translateKeyCode( int keyCode ) {
    switch( keyCode ) {
      case 16:
        return SWT.SHIFT;
      case 17:
        return SWT.CONTROL;
      case 18:
        return SWT.ALT;
      case 20:
        return SWT.CAPS_LOCK;
      case 38:
        return SWT.ARROW_UP;
      case 37:
        return SWT.ARROW_LEFT;
      case 39:
        return SWT.ARROW_RIGHT;
      case 40:
        return SWT.ARROW_DOWN;
      case 33:
        return SWT.PAGE_UP;
      case 34:
        return SWT.PAGE_DOWN;
      case 35:
        return SWT.END;
      case 36:
        return SWT.HOME;
      case 45:
        return SWT.INSERT;
      case 46:
        return SWT.DEL;
      case 112:
        return SWT.F1;
      case 113:
        return SWT.F2;
      case 114:
        return SWT.F3;
      case 115:
        return SWT.F4;
      case 116:
        return SWT.F5;
      case 117:
        return SWT.F6;
      case 118:
        return SWT.F7;
      case 119:
        return SWT.F8;
      case 120:
        return SWT.F9;
      case 121:
        return SWT.F10;
      case 122:
        return SWT.F11;
      case 123:
        return SWT.F12;
      case 144:
        return SWT.NUM_LOCK;
      case 44:
        return SWT.PRINT_SCREEN;
      case 145:
        return SWT.SCROLL_LOCK;
      case 19:
        return SWT.PAUSE;
      default:
        return keyCode;
    }
  }

  private static char translateCharacter( int keyCode ) {
    char result = ( char )0;
    if( Character.isDefined( ( char )keyCode ) ) {
      result = ( char )keyCode;
    }
    return result;
  }

  private static int translateCursor( String cursor ) {
    int result;
    if( "default".equals( cursor ) ) {
      result = SWT.CURSOR_ARROW;
    } else if( "wait".equals( cursor ) ) {
      result = SWT.CURSOR_WAIT;
    } else if( "progress".equals( cursor ) ) {
      result = SWT.CURSOR_APPSTARTING;
    } else if( "crosshair".equals( cursor ) ) {
      result = SWT.CURSOR_CROSS;
    } else if( "help".equals( cursor ) ) {
      result = SWT.CURSOR_HELP;
    } else if( "move".equals( cursor ) ) {
      result = SWT.CURSOR_SIZEALL;
    } else if( "row-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZENS;
    } else if( "col-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZEWE;
    } else if( "n-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZEN;
    } else if( "s-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZES;
    } else if( "e-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZEE;
    } else if( "w-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZEW;
    } else if( "ne-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZENE;
    } else if( "se-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZESE;
    } else if( "sw-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZESW;
    } else if( "nw-resize".equals( cursor ) ) {
      result = SWT.CURSOR_SIZENW;
    } else if( "text".equals( cursor ) ) {
      result = SWT.CURSOR_IBEAM;
    } else if( "pointer".equals( cursor ) ) {
      result = SWT.CURSOR_HAND;
    } else if( "not-allowed".equals( cursor ) ) {
      result = SWT.CURSOR_NO;
    } else {
      throw new IllegalArgumentException( "Unsupported cursor: " + cursor );
    }
    return result;
  }

}
