/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.events;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;


public final class EventUtil {

  private static final String ATTR_LAST_EVENT_TIME = EventUtil.class.getName() + "#lastEventTime";

  public static int getLastEventTime() {
    Long eventTime;
    if( ContextProvider.hasContext() ) {
      ServiceStore serviceStore = ContextProvider.getContext().getServiceStore();
      eventTime = ( Long )serviceStore.getAttribute( ATTR_LAST_EVENT_TIME );
      if( eventTime == null ) {
        eventTime = Long.valueOf( System.currentTimeMillis() );
      } else {
        eventTime = Long.valueOf( eventTime.longValue() + 1 );
      }
      serviceStore.setAttribute( ATTR_LAST_EVENT_TIME, eventTime );
    } else {
      eventTime = Long.valueOf( System.currentTimeMillis() );
    }
    return ( int )( eventTime.longValue() % Integer.MAX_VALUE );
  }

  public static boolean allowProcessing( Event event ) {
    boolean result;
    if( event.widget.isDisposed() ) {
      result = false;
    } else {
      switch( event.type ) {
        case SWT.Activate:
        case SWT.Deactivate:
        case SWT.Move:
        case SWT.Resize:
        case SWT.Paint:
        case SWT.Modify:
        case SWT.Verify:
        case SWT.SetData:
        case EventTypes.PROGRESS_CHANGED:
        case EventTypes.PROGRESS_COMPLETED:
          result = true;
        break;
        default:
          result = isAccessible( event.widget );
        break;
      }
    }
    return result;
  }

  public static boolean isAccessible( Widget widget ) {
    boolean result;
    result = !widget.isDisposed();
    if( result ) {
      if( widget instanceof Control ) {
        result = isAccessible( ( Control )widget );
      } else if( widget instanceof MenuItem ) {
        MenuItem menuItem = ( MenuItem )widget;
        result = isAccessible( menuItem );
      } else if( widget instanceof ToolItem ) {
        ToolItem toolItem = ( ToolItem )widget;
        result = isAccessible( toolItem );
      } else if( widget instanceof Menu ) {
        Menu menu = ( Menu )widget;
        result = isAccessible( menu );
      }
    }
    return result;
  }

  private static boolean isAccessible( Control control ) {
    return control.getEnabled() && control.getVisible() && isShellAccessible( control.getShell() );
  }

  private static boolean isAccessible( Menu menu ) {
    return menu.getEnabled() && isShellAccessible( menu.getShell() );
  }

  private static boolean isAccessible( MenuItem menuItem ) {
    return menuItem.getEnabled() && isShellAccessible( menuItem.getParent().getShell() );
  }

  private static boolean isAccessible( ToolItem toolItem ) {
    return toolItem.getEnabled() && isShellAccessible( toolItem.getParent().getShell() );
  }

  private static boolean isShellAccessible( Shell shell ) {
    Shell modalShell = null;
    Shell activeShell = shell.getDisplay().getActiveShell();
    if( activeShell != null && activeShell.isVisible() && isModal( activeShell ) ) {
      modalShell = activeShell;
    }
    return modalShell == null || shell == modalShell;
  }

  private static boolean isModal( Shell shell ) {
    return ( shell.getStyle() & SWT.APPLICATION_MODAL ) != 0;
  }

  private EventUtil() {
    // prevent instantiation
  }
}
