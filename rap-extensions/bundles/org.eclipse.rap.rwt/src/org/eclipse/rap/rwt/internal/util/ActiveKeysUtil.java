/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.util;

import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.swt.internal.widgets.ControlRemoteAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;


public final class ActiveKeysUtil {

  private static final Map<String,Integer> KEY_MAP = new HashMap<>();
  static {
    KEY_MAP.put( "BACKSPACE", Integer.valueOf( 8 ) );
    KEY_MAP.put( "BS", Integer.valueOf( 8 ) );
    KEY_MAP.put( "TAB", Integer.valueOf( 9 ) );
    KEY_MAP.put( "RETURN", Integer.valueOf( 13 ) );
    KEY_MAP.put( "ENTER", Integer.valueOf( 13 ) );
    KEY_MAP.put( "CR", Integer.valueOf( 13 ) );
    KEY_MAP.put( "PAUSE", Integer.valueOf( 19 ) );
    KEY_MAP.put( "BREAK", Integer.valueOf( 19 ) );
    KEY_MAP.put( "CAPS_LOCK", Integer.valueOf( 20 ) );
    KEY_MAP.put( "ESCAPE", Integer.valueOf( 27 ) );
    KEY_MAP.put( "ESC", Integer.valueOf( 27 ) );
    KEY_MAP.put( "SPACE", Integer.valueOf( 32 ) );
    KEY_MAP.put( "PAGE_UP", Integer.valueOf( 33 ) );
    KEY_MAP.put( "PAGE_DOWN", Integer.valueOf( 34 ) );
    KEY_MAP.put( "END", Integer.valueOf( 35 ) );
    KEY_MAP.put( "HOME", Integer.valueOf( 36 ) );
    KEY_MAP.put( "ARROW_LEFT", Integer.valueOf( 37 ) );
    KEY_MAP.put( "ARROW_UP", Integer.valueOf( 38 ) );
    KEY_MAP.put( "ARROW_RIGHT", Integer.valueOf( 39 ) );
    KEY_MAP.put( "ARROW_DOWN", Integer.valueOf( 40 ) );
    KEY_MAP.put( "PRINT_SCREEN", Integer.valueOf( 44 ) );
    KEY_MAP.put( "INSERT", Integer.valueOf( 45 ) );
    KEY_MAP.put( "DEL", Integer.valueOf( 46 ) );
    KEY_MAP.put( "DELETE", Integer.valueOf( 46 ) );
    KEY_MAP.put( "F1", Integer.valueOf( 112 ) );
    KEY_MAP.put( "F2", Integer.valueOf( 113 ) );
    KEY_MAP.put( "F3", Integer.valueOf( 114 ) );
    KEY_MAP.put( "F4", Integer.valueOf( 115 ) );
    KEY_MAP.put( "F5", Integer.valueOf( 116 ) );
    KEY_MAP.put( "F6", Integer.valueOf( 117 ) );
    KEY_MAP.put( "F7", Integer.valueOf( 118 ) );
    KEY_MAP.put( "F8", Integer.valueOf( 119 ) );
    KEY_MAP.put( "F9", Integer.valueOf( 120 ) );
    KEY_MAP.put( "F10", Integer.valueOf( 121 ) );
    KEY_MAP.put( "F11", Integer.valueOf( 122 ) );
    KEY_MAP.put( "F12", Integer.valueOf( 123 ) );
    KEY_MAP.put( "NUMPAD_0", Integer.valueOf( 96 ) );
    KEY_MAP.put( "NUMPAD_1", Integer.valueOf( 97 ) );
    KEY_MAP.put( "NUMPAD_2", Integer.valueOf( 98 ) );
    KEY_MAP.put( "NUMPAD_3", Integer.valueOf( 99 ) );
    KEY_MAP.put( "NUMPAD_4", Integer.valueOf( 100 ) );
    KEY_MAP.put( "NUMPAD_5", Integer.valueOf( 101 ) );
    KEY_MAP.put( "NUMPAD_6", Integer.valueOf( 102 ) );
    KEY_MAP.put( "NUMPAD_7", Integer.valueOf( 103 ) );
    KEY_MAP.put( "NUMPAD_8", Integer.valueOf( 104 ) );
    KEY_MAP.put( "NUMPAD_9", Integer.valueOf( 105 ) );
    KEY_MAP.put( "NUMPAD_MULTIPLY", Integer.valueOf( 106 ) );
    KEY_MAP.put( "NUMPAD_ADD", Integer.valueOf( 107 ) );
    KEY_MAP.put( "NUMPAD_SUBTRACT", Integer.valueOf( 109 ) );
    KEY_MAP.put( "NUMPAD_DECIMAL", Integer.valueOf( 110 ) );
    KEY_MAP.put( "NUMPAD_DIVIDE", Integer.valueOf( 111 ) );
    KEY_MAP.put( "NUM_LOCK", Integer.valueOf( 144 ) );
    KEY_MAP.put( "SCROLL_LOCK", Integer.valueOf( 145 ) );
  }
  private final static String ALT = "ALT+";
  private final static String CTRL = "CTRL+";
  private final static String SHIFT = "SHIFT+";

  final static String PROP_ACTIVE_KEYS = "activeKeys";
  final static String PROP_CANCEL_KEYS = "cancelKeys";
  final static String PROP_MNEMONIC_ACTIVATOR = "mnemonicActivator";


  private ActiveKeysUtil() {
    // prevent instantiation
  }

  public static void preserveActiveKeys( Display display ) {
    RemoteAdapter adapter = DisplayUtil.getAdapter( display );
    adapter.preserve( PROP_ACTIVE_KEYS, getActiveKeys( display ) );
  }

  public static void preserveActiveKeys( Control control ) {
    ControlRemoteAdapter adapter = ( ControlRemoteAdapter )WidgetUtil.getAdapter( control );
    if( !adapter.hasPreservedActiveKeys() ) {
      adapter.preserveActiveKeys( getActiveKeys( control ) );
    }
  }

  public static void preserveCancelKeys( Display display ) {
    RemoteAdapter adapter = DisplayUtil.getAdapter( display );
    adapter.preserve( PROP_CANCEL_KEYS, getCancelKeys( display ) );
  }

  public static void preserveCancelKeys( Control control ) {
    ControlRemoteAdapter adapter = ( ControlRemoteAdapter )WidgetUtil.getAdapter( control );
    if( !adapter.hasPreservedCancelKeys() ) {
      adapter.preserveCancelKeys( getCancelKeys( control ) );
    }
  }

  public static void renderActiveKeys( Display display ) {
    if( !display.isDisposed() ) {
      RemoteAdapter adapter = DisplayUtil.getAdapter( display );
      String[] actual = getActiveKeys( display );
      String[] preserved = ( String[] )adapter.getPreserved( PROP_ACTIVE_KEYS );
      if( !Arrays.equals( actual, preserved ) ) {
        getRemoteObject( display ).set( PROP_ACTIVE_KEYS, translateKeySequences( actual ) );
      }
    }
  }

  public static void renderActiveKeys( Control control ) {
    if( !control.isDisposed() ) {
      ControlRemoteAdapter adapter = ( ControlRemoteAdapter )WidgetUtil.getAdapter( control );
      if( adapter.hasPreservedActiveKeys() ) {
        String[] actual = getActiveKeys( control );
        String[] preserved = adapter.getPreservedActiveKeys();
        if( !Arrays.equals( actual, preserved ) ) {
          getRemoteObject( control ).set( PROP_ACTIVE_KEYS, translateKeySequences( actual ) );
        }
      }
    }
  }

  public static void renderCancelKeys( Display display ) {
    if( !display.isDisposed() ) {
      RemoteAdapter adapter = DisplayUtil.getAdapter( display );
      String[] actual = getCancelKeys( display );
      String[] preserved = ( String[] )adapter.getPreserved( PROP_CANCEL_KEYS );
      if( !Arrays.equals( actual, preserved ) ) {
        getRemoteObject( display ).set( PROP_CANCEL_KEYS, translateKeySequences( actual ) );
      }
    }
  }

  public static void renderCancelKeys( Control control ) {
    if( !control.isDisposed() ) {
      ControlRemoteAdapter adapter = ( ControlRemoteAdapter )WidgetUtil.getAdapter( control );
      if( adapter.hasPreservedCancelKeys() ) {
        String[] actual = getCancelKeys( control );
        String[] preserved = adapter.getPreservedCancelKeys();
        if( !Arrays.equals( actual, preserved ) ) {
          getRemoteObject( control ).set( PROP_CANCEL_KEYS, translateKeySequences( actual ) );
        }
      }
    }
  }

  public static void preserveMnemonicActivator( Display display ) {
    RemoteAdapter adapter = DisplayUtil.getAdapter( display );
    adapter.preserve( PROP_MNEMONIC_ACTIVATOR, getMnemonicActivator( display ) );
  }

  public static void renderMnemonicActivator( Display display ) {
    if( !display.isDisposed() ) {
      RemoteAdapter adapter = DisplayUtil.getAdapter( display );
      String actual = getMnemonicActivator( display );
      String preserved = ( String )adapter.getPreserved( PROP_MNEMONIC_ACTIVATOR );
      if( !equals( actual, preserved ) ) {
        getRemoteObject( display ).set( PROP_MNEMONIC_ACTIVATOR, getModifierKeys( actual ) );
      }
    }
  }

  private static String[] getActiveKeys( Display display ) {
    Object data = display.getData( RWT.ACTIVE_KEYS );
    String[] result = null;
    if( data != null ) {
      if( data instanceof String[] ) {
        result = getArrayCopy( ( String[] )data );
      } else {
        String mesg = "Illegal value for RWT.ACTIVE_KEYS in display data, must be a string array";
        throw new IllegalArgumentException( mesg );
      }
    }
    return result;
  }

  private static String[] getActiveKeys( Control control ) {
    Object data = control.getData( RWT.ACTIVE_KEYS );
    return data != null ? getArrayCopy( ( String[] )data ) : null;
  }

  private static String[] getCancelKeys( Display display ) {
    String[] result = null;
    Object data = display.getData( RWT.CANCEL_KEYS );
    if( data != null ) {
      if( data instanceof String[] ) {
        result = getArrayCopy( ( String[] )data );
      } else {
        String mesg = "Illegal value for RWT.CANCEL_KEYS in display data, must be a string array";
        throw new IllegalArgumentException( mesg );
      }
    }
    return result;
  }

  private static String[] getCancelKeys( Control control ) {
    Object data = control.getData( RWT.CANCEL_KEYS );
    return data != null ? getArrayCopy( ( String[] )data ) : null;
  }

  private static String getMnemonicActivator( Display display ) {
    String result = null;
    Object data = display.getData( RWT.MNEMONIC_ACTIVATOR );
    if( data != null ) {
      if( data instanceof String ) {
        result = ( String )data;
        if( !result.endsWith( "+" ) ) {
          result += "+";
        }
      } else {
        String mesg = "Illegal value for RWT.MNEMONIC_ACTIVATOR in display data, must be a string";
        throw new IllegalArgumentException( mesg );
      }
    }
    return result;
  }

  private static JsonArray translateKeySequences( String[] activeKeys ) {
    JsonArray result = new JsonArray();
    if( activeKeys != null ) {
      for( int i = 0; i < activeKeys.length; i++ ) {
        result.add( translateKeySequence( activeKeys[ i ] ) );
      }
    }
    return result;
  }

  private static String translateKeySequence( String keySequence ) {
    if( keySequence == null ) {
      throw new NullPointerException( "Null argument" );
    }
    if( keySequence.trim().length() == 0 ) {
      throw new IllegalArgumentException( "Empty key sequence definition found" );
    }
    String modifierPart = "";
    String keyPart = "";
    int lastPlusIndex = keySequence.lastIndexOf( "+", keySequence.length() - 2  );
    if( lastPlusIndex != -1 ) {
      modifierPart = keySequence.substring( 0, lastPlusIndex + 1 );
      keyPart = keySequence.substring( lastPlusIndex + 1 );
    } else {
      keyPart = keySequence;
    }
    return getModifierKeys( modifierPart ) + formatKey( keyPart );
  }

  private static String formatKey( String key ) {
    int keyCode = getKeyCode( key );
    // TODO [tb] : use identifier instead of keycode
    return keyCode == -1 ? key : "#" + keyCode;
  }

  private static String getModifierKeys( String modifier ) {
    StringBuilder result = new StringBuilder();
    // order modifiers
    if( modifier.indexOf( ALT ) != -1 ) {
      result.append( ALT );
    }
    if( modifier.indexOf( CTRL ) != -1 ) {
      result.append( CTRL );
    }
    if( modifier.indexOf( SHIFT ) != -1 ) {
      result.append( SHIFT );
    }
    if( modifier.length() != result.length() ) {
      throw new IllegalArgumentException( "Unrecognized modifier found in key sequence: " + modifier );
    }
    return result.toString();
  }

  private static int getKeyCode( String key ) {
    int result = -1;
    Object value = KEY_MAP.get( key );
    if( value instanceof Integer ) {
      result = ( ( Integer )value ).intValue();
    } else if( key.length() == 1 ) {
      if( Character.isLetterOrDigit( key.charAt( 0 ) ) ) {
        // NOTE: This works only for A-Z and 0-9 where keycode matches charcode
        result = key.toUpperCase().charAt( 0 );
      }
    } else {
      throw new IllegalArgumentException( "Unrecognized key: " + key );
    }
    return result;
  }

  private static String[] getArrayCopy( String[] data ) {
    return Arrays.copyOf( data, data.length );
  }

  private static boolean equals( Object object1, Object object2 ) {
    boolean result;
    if( object1 == object2 ) {
      result = true;
    } else if( object1 == null ) {
      result = false;
    } else {
      result = object1.equals( object2 );
    }
    return result;
  }

}
