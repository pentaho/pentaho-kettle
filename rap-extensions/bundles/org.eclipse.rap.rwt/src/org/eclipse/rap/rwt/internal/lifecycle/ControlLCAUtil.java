/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderData;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenKey;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderToolTipMarkupEnabled;
import static org.eclipse.swt.internal.widgets.ControlUtil.getControlAdapter;

import org.eclipse.rap.rwt.internal.util.ActiveKeysUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.ControlRemoteAdapter;
import org.eclipse.swt.internal.widgets.IControlAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;


public class ControlLCAUtil {

  private static final String PROP_ACTIVATE_LISTENER = "Activate";
  private static final String PROP_DEACTIVATE_LISTENER = "Deactivate";
  private static final String PROP_FOCUS_IN_LISTENER = "FocusIn";
  private static final String PROP_FOCUS_OUT_LISTENER = "FocusOut";
  private static final String PROP_MOUSE_DOWN_LISTENER = "MouseDown";
  private static final String PROP_MOUSE_DOUBLE_CLICK_LISTENER = "MouseDoubleClick";
  private static final String PROP_MOUSE_UP_LISTENER = "MouseUp";
  private static final String PROP_TRAVERSE_LISTENER = "Traverse";
  private static final String PROP_MENU_DETECT_LISTENER = "MenuDetect";
  private static final String PROP_HELP_LISTENER = "Help";

  private ControlLCAUtil() {
    // prevent instance creation
  }

  public static void renderChanges( Control control ) {
    ControlRemoteAdapter remoteAdapter = getRemoteAdapter( control );
    IControlAdapter controlAdapter = getControlAdapter( control );
    if( control instanceof Shell ) {
      recalculateTabIndex( ( Shell ) control );
    }
    if( control instanceof Composite ) {
      remoteAdapter.renderChildren( ( Composite )control );
    }
    remoteAdapter.renderBounds( controlAdapter );
    remoteAdapter.renderTabIndex( control );
    renderToolTipMarkupEnabled( control );
    remoteAdapter.renderToolTipText( control );
    remoteAdapter.renderMenu( control );
    remoteAdapter.renderVisible( control );
    remoteAdapter.renderEnabled( control );
    remoteAdapter.renderOrientation( control );
    remoteAdapter.renderForeground( controlAdapter );
    remoteAdapter.renderBackground( controlAdapter );
    remoteAdapter.renderBackgroundImage( controlAdapter );
    remoteAdapter.renderFont( controlAdapter );
    remoteAdapter.renderCursor( control );
    renderData( control );
    ActiveKeysUtil.renderActiveKeys( control );
    ActiveKeysUtil.renderCancelKeys( control );
    renderListenActivate( control );
    renderListenMouse( control );
    renderListenFocus( control );
    renderListenKey( control );
    renderListenTraverse( control );
    renderListenMenuDetect( control );
    renderListenHelp( control );
  }

  private static void recalculateTabIndex( Shell shell ) {
    resetTabIndices( shell );
    // tabIndex must be a positive value
    computeTabIndices( shell, 1 );
  }

  private static void resetTabIndices( Composite composite ) {
    for( Control control : composite.getChildren() ) {
      getControlAdapter( control ).setTabIndex( -1 );
      if( control instanceof Composite ) {
        resetTabIndices( ( Composite )control );
      }
    }
  }

  private static int computeTabIndices( Composite composite, int startIndex ) {
    int result = startIndex;
    for( Control control : composite.getTabList() ) {
      getControlAdapter( control ).setTabIndex( result );
      // for Links, leave a range out to be assigned to hrefs on the client
      result += control instanceof Link ? 300 : 1;
      if( control instanceof Composite ) {
        result = computeTabIndices( ( Composite )control, result );
      }
    }
    return result;
  }

  private static void renderListenActivate( Control control ) {
    // Note: Shell "Activate" event is handled by ShellLCA
    if( !( control instanceof Shell ) ) {
      renderListener( control, SWT.Activate, PROP_ACTIVATE_LISTENER );
      renderListener( control, SWT.Deactivate, PROP_DEACTIVATE_LISTENER );
    }
  }

  private static void renderListenMouse( Control control ) {
    renderListener( control, SWT.MouseDown, PROP_MOUSE_DOWN_LISTENER );
    renderListener( control, SWT.MouseUp, PROP_MOUSE_UP_LISTENER );
    renderListener( control, SWT.MouseDoubleClick, PROP_MOUSE_DOUBLE_CLICK_LISTENER );
  }

  private static void renderListenFocus( Control control ) {
    if( ( control.getStyle() & SWT.NO_FOCUS ) == 0 ) {
      renderListener( control, SWT.FocusIn, PROP_FOCUS_IN_LISTENER );
      renderListener( control, SWT.FocusOut, PROP_FOCUS_OUT_LISTENER );
    }
  }

  private static void renderListenTraverse( Control control ) {
    renderListener( control, SWT.Traverse, PROP_TRAVERSE_LISTENER );
  }

  private static void renderListenMenuDetect( Control control ) {
    renderListener( control, SWT.MenuDetect, PROP_MENU_DETECT_LISTENER );
  }

  private static void renderListenHelp( Control control ) {
    renderListener( control, SWT.Help, PROP_HELP_LISTENER );
  }

  private static ControlRemoteAdapter getRemoteAdapter( Control control ) {
    return ( ControlRemoteAdapter )control.getAdapter( RemoteAdapter.class );
  }

}
