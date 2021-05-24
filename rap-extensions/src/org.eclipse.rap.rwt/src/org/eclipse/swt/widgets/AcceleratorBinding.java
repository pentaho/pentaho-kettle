/*******************************************************************************
 * Copyright (c) 2013, 2018 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.widgets;

import static java.lang.Character.toUpperCase;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;


class AcceleratorBinding implements Listener {

  private final MenuItem menuItem;

  private int accelerator;

  AcceleratorBinding( MenuItem menuItem ) {
    this.menuItem = menuItem;
  }

  @Override
  public void handleEvent( Event event ) {
    if( isRelevantEvent( event ) && menuItem.isEnabled() ) {
      menuItem.handleAcceleratorActivation();
      event.type = SWT.NONE;
    }
  }

  int getAccelerator() {
    return accelerator;
  }

  void setAccelerator( int accelerator ) {
    if( accelerator != this.accelerator ) {
      int oldAccelerator = this.accelerator;
      this.accelerator = accelerator;
      if( ( menuItem.style & SWT.SEPARATOR ) == 0 ) {
        updateDisplayActiveKeys( oldAccelerator, accelerator );
        updateDisplayFilter( oldAccelerator, accelerator );
      }
    }
  }

  void release() {
    setAccelerator( 0 );
  }

  private boolean isRelevantEvent( Event event ) {
    boolean result = false;
    if( event.type == SWT.KeyDown ) {
      if( ( accelerator & SWT.MODIFIER_MASK ) == event.stateMask ) {
        int key = accelerator & SWT.KEY_MASK;
        if( event.character == 0 ) {
          result = event.keyCode == key;
        } else if( key <= Character.MAX_VALUE ) {
          result = toUpperCase( event.character ) == toUpperCase( ( char )key );
        }
      }
    }
    return result;
  }

  private void updateDisplayFilter( int oldAccelerator, int newAccelerator ) {
    if( oldAccelerator == 0 && newAccelerator != 0 ) {
      menuItem.display.addFilter( SWT.KeyDown, this );
    } else if( oldAccelerator != 0 && newAccelerator == 0 ) {
      menuItem.display.removeFilter( SWT.KeyDown, this );
    }
  }

  private void updateDisplayActiveKeys( int oldAccelerator, int newAccelerator ) {
    updateDisplayActiveKeys( RWT.ACTIVE_KEYS, oldAccelerator, newAccelerator );
    updateDisplayActiveKeys( RWT.CANCEL_KEYS, oldAccelerator, newAccelerator );
  }

  private void updateDisplayActiveKeys( String keysType, int oldAccelerator, int newAccelerator ) {
    String[] oldActiveKeys = ( String[] )menuItem.display.getData( keysType );
    if( oldActiveKeys == null ) {
      oldActiveKeys = new String[ 0 ];
    }
    ArrayList<String> activeKeys = new ArrayList<>( Arrays.asList( oldActiveKeys ) );
    if( oldAccelerator != 0 ) {
      activeKeys.remove( acceleratorAsString( oldAccelerator ) );
    }
    if( newAccelerator != 0 ) {
      activeKeys.add( acceleratorAsString( newAccelerator ) );
    }
    menuItem.display.setData( keysType, activeKeys.toArray( new String[ 0 ] ) );
  }

  private static String acceleratorAsString( int accelerator ) {
    String result = "";
    if( ( accelerator & SWT.ALT ) != 0 ) {
      result += "ALT+";
    }
    if( ( accelerator & SWT.CTRL ) != 0 ) {
      result += "CTRL+";
    }
    if( ( accelerator & SWT.SHIFT ) != 0 ) {
      result += "SHIFT+";
    }
    result += keyToString( accelerator & SWT.KEY_MASK );
    return result;
  }

  private static String keyToString( int key ) {
    String result;
    switch( key ) {
      case SWT.F1:
        result = "F1";
        break;
      case SWT.F2:
        result = "F2";
        break;
      case SWT.F3:
        result = "F3";
        break;
      case SWT.F4:
        result = "F4";
        break;
      case SWT.F5:
        result = "F5";
        break;
      case SWT.F6:
        result = "F6";
        break;
      case SWT.F7:
        result = "F7";
        break;
      case SWT.F8:
        result = "F8";
        break;
      case SWT.F9:
        result = "F9";
        break;
      case SWT.F10:
        result = "F10";
        break;
      case SWT.F11:
        result = "F11";
        break;
      case SWT.F12:
        result = "F12";
        break;
      case SWT.INSERT:
        result = "INSERT";
        break;
      case SWT.DEL:
        result = "DEL";
        break;
      case SWT.HOME:
        result = "HOME";
        break;
      case SWT.END:
        result = "END";
        break;
      case SWT.ESC:
        result = "ESC";
        break;
      case SWT.ARROW_UP:
        result = "ARROW_UP";
        break;
      case SWT.ARROW_DOWN:
        result = "ARROW_DOWN";
        break;
      case SWT.ARROW_LEFT:
        result = "ARROW_LEFT";
        break;
      case SWT.ARROW_RIGHT:
        result = "ARROW_RIGHT";
        break;
      case SWT.PAGE_UP:
        result = "PAGE_UP";
        break;
      case SWT.PAGE_DOWN:
        result = "PAGE_DOWN";
        break;
      case SWT.NUM_LOCK:
        result = "NUM_LOCK";
        break;
      case SWT.SCROLL_LOCK:
        result = "SCROLL_LOCK";
        break;
      case SWT.PAUSE:
        result = "PAUSE";
        break;
      case SWT.PRINT_SCREEN:
        result = "PRINT_SCREEN";
        break;
      case SWT.CAPS_LOCK:
        result = "CAPS_LOCK";
        break;
      default:
        result = Character.toString( toUpperCase( ( char )key ) );
    }
    return result;
  }

}