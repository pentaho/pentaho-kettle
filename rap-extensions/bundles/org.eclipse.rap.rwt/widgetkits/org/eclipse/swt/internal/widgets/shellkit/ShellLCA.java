/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.shellkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.IShellAdapter;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;


public final class ShellLCA extends WidgetLCA<Shell> {

  public static final ShellLCA INSTANCE = new ShellLCA();

  private static final String TYPE = "rwt.widgets.Shell";
  private static final String[] ALLOWED_STYLES = {
    "CLOSE",
    "MIN",
    "MAX",
    "NO_TRIM",
    "RESIZE",
    "TITLE",
    "ON_TOP",
    "TOOL",
    "SHEET",
    "APPLICATION_MODAL",
    "MODELESS",
    "PRIMARY_MODAL",
    "SYSTEM_MODAL",
    "BORDER"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_ALPHA = "alpha";
  static final String PROP_ACTIVE_CONTROL = "activeControl";
  static final String PROP_ACTIVE_SHELL = "activeShell";
  static final String PROP_MODE = "mode";
  static final String PROP_FULLSCREEN = "fullScreen";
  static final String PROP_MINIMUM_SIZE = "minimumSize";
  private static final String PROP_ACTIVATE_LISTENER = "Activate";
  private static final String PROP_CLOSE_LISTENER = "Close";
  private static final String PROP_RESIZE_LISTENER = "Resize";
  private static final String PROP_MOVE_LISTENER = "Move";
  private static final String PROP_DEFAULT_BUTTON = "defaultButton";

  @Override
  public void preserveValues( Shell shell ) {
    preserveProperty( shell, PROP_ACTIVE_CONTROL, getActiveControl( shell ) );
    preserveProperty( shell, PROP_ACTIVE_SHELL, shell.getDisplay().getActiveShell() );
    preserveProperty( shell, PROP_TEXT, shell.getText() );
    preserveProperty( shell, PROP_IMAGE, shell.getImage() );
    preserveProperty( shell, PROP_ALPHA, Integer.valueOf( shell.getAlpha() ) );
    preserveProperty( shell, PROP_MODE, getMode( shell ) );
    preserveProperty( shell, PROP_FULLSCREEN, Boolean.valueOf( shell.getFullScreen() ) );
    preserveProperty( shell, PROP_MINIMUM_SIZE, shell.getMinimumSize() );
    preserveProperty( shell, PROP_DEFAULT_BUTTON, shell.getDefaultButton() );
  }

  @Override
  public void readData( Shell shell ) {
    // [if] Preserve the menu bounds before setting the new shell bounds.
    preserveMenuBounds( shell );
    super.readData( shell );
  }

  @Override
  public void renderInitialization( Shell shell ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( shell, TYPE );
    remoteObject.setHandler( new ShellOperationHandler( shell ) );
    remoteObject.set( "style", createJsonArray( getStyles( shell, ALLOWED_STYLES ) ) );
    Composite parent = shell.getParent();
    if( parent instanceof Shell ) {
      remoteObject.set( "parentShell", getId( parent ) );
    }
    // TODO [tb] : These should be rendered only when there is an actual listener attached:
    remoteObject.listen( PROP_MOVE_LISTENER, true );
    remoteObject.listen( PROP_RESIZE_LISTENER, true );
    // Always listen for "Activate" and "Close" events. Client send these events regardless
    // listeners attached
    remoteObject.listen( PROP_ACTIVATE_LISTENER, true );
    remoteObject.listen( PROP_CLOSE_LISTENER, true );
  }

  @Override
  public void renderChanges( Shell shell ) throws IOException {
    WidgetLCAUtil.renderCustomVariant( shell ); // Order matters for animation
    renderImage( shell );
    renderText( shell );
    renderAlpha( shell );
    renderActiveShell( shell );
    renderMode( shell );
    renderMinimumSize( shell );
    renderDefaultButton( shell );
    renderActiveControl( shell );
    ControlLCAUtil.renderChanges( shell );
  }

  @Override
  public void renderDispose( Shell shell ) throws IOException {
    getRemoteObject( shell ).destroy();
  }

  //////////////////
  // Helping methods

  private static void renderText( Shell shell ) {
    renderProperty( shell, PROP_TEXT, shell.getText(), "" );
  }

  private static void renderAlpha( Shell shell ) {
    renderProperty( shell, PROP_ALPHA, shell.getAlpha(), 0xFF );
  }

  private static void renderMinimumSize( Shell shell ) {
    Point newValue = shell.getMinimumSize();
    if( hasChanged( shell, PROP_MINIMUM_SIZE, newValue ) ) {
      RemoteObject remoteObject = getRemoteObject( shell );
      remoteObject.set( "minimumSize", new JsonArray().add( newValue.x ).add( newValue.y ) );
    }
  }

  private static void renderDefaultButton( Shell shell ) {
    Button defaultButton = shell.getDefaultButton();
    if( hasChanged( shell, PROP_DEFAULT_BUTTON, defaultButton, null ) ) {
      String defaultButtonId = null;
      if( defaultButton != null ) {
        defaultButtonId = WidgetUtil.getId( defaultButton );
      }
      getRemoteObject( shell ).set( "defaultButton", defaultButtonId );
    }
  }

  /////////////////////////////////////////////
  // Methods to read and write the active shell

  private static void renderActiveShell( Shell shell ) {
    Shell activeShell = shell.getDisplay().getActiveShell();
    boolean hasChanged = hasChanged( shell, PROP_ACTIVE_SHELL, activeShell, null );
    if( shell == activeShell && hasChanged ) {
      getRemoteObject( shell ).set( "active", true );
    }
  }

  private static void renderActiveControl( Shell shell ) {
    final Control activeControl = getActiveControl( shell );
    if( hasChanged( shell, PROP_ACTIVE_CONTROL, activeControl, null ) ) {
      String activeControlId = null;
      if( activeControl != null ) {
        activeControlId = getId( activeControl );
      }
      getRemoteObject( shell ).set( "activeControl", activeControlId );
    }
  }

  private static void renderImage( Shell shell ) {
    if( ( shell.getStyle() & SWT.TITLE ) != 0 ) {
      Image image = shell.getImage();
      if( image == null ) {
        Image[] defaultImages = shell.getImages();
        if( defaultImages.length > 0 ) {
          image = defaultImages[0];
        }
      }
      renderProperty( shell, PROP_IMAGE, image, null );
    }
  }

  private static void renderMode( Shell shell ) {
    renderProperty( shell, PROP_MODE, getMode( shell), null );
  }

  private static Control getActiveControl( Shell shell ) {
    return shell.getAdapter( IShellAdapter.class ).getActiveControl();
  }

  private static String getMode( Shell shell ) {
    String result = null;
    if( shell.getMinimized() ) {
      result = "minimized";
    } else if( shell.getFullScreen() ) {
      result = "fullscreen";
    } else if( shell.getMaximized() ) {
      result = "maximized";
    }
    return result;
  }

  private static void preserveMenuBounds( Shell shell ) {
    Menu menuBar = shell.getMenuBar();
    if( menuBar != null ) {
      IShellAdapter shellAdapter = shell.getAdapter( IShellAdapter.class );
      getAdapter( menuBar ).preserve( Props.BOUNDS, shellAdapter.getMenuBounds() );
    }
  }

  private ShellLCA() {
    // prevent instantiation
  }

}
