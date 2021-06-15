/*******************************************************************************
 * Copyright (c) 2002, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.custom.ctabfolderkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenDefaultSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.remote.JsonMapping.toJson;

import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.custom.ICTabFolderAdapter;
import org.eclipse.swt.internal.widgets.IWidgetGraphicsAdapter;
import org.eclipse.swt.widgets.Control;


public final class CTabFolderLCA extends WidgetLCA<CTabFolder> {

  public static final CTabFolderLCA INSTANCE = new CTabFolderLCA();

  private static final String TYPE = "rwt.widgets.CTabFolder";
  private static final String[] ALLOWED_STYLES = {
    "CLOSE", "FLAT", "SINGLE", "MULTI", "NO_RADIO_GROUP", "BORDER"
  };

  // Property names
  private static final String PROP_TOOLTIP_TEXTS = "toolTipTexts";
  private static final String PROP_TAB_POSITION = "tabPosition";
  private static final String PROP_TAB_HEIGHT = "tabHeight";
  private static final String PROP_MIN_MAX_STATE = "minMaxState";
  private static final String PROP_MINIMIZE_BOUNDS = "minimizeBounds";
  private static final String PROP_MINIMIZE_VISIBLE = "minimizeVisible";
  private static final String PROP_MAXIMIZE_BOUNDS = "maximizeBounds";
  private static final String PROP_MAXIMIZE_VISIBLE = "maximizeVisible";
  private static final String PROP_CHEVRON_BOUNDS = "chevronBounds";
  private static final String PROP_CHEVRON_VISIBLE = "chevronVisible";
  private static final String PROP_UNSELECTED_CLOSE_VISIBLE = "unselectedCloseVisible";
  private static final String PROP_SELECTION = "selection";
  private static final String PROP_SELECTION_BACKGROUND = "selectionBackground";
  private static final String PROP_SELECTION_FOREGROUND = "selectionForeground";
  private static final String PROP_SELECTION_BACKGROUND_IMAGE = "selectionBackgroundImage";
  private static final String PROP_SELECTION_BG_GRADIENT = "selectionBackgroundGradient";
  private static final String PROP_SELECTION_BG_GRADIENT_COLORS
    = "selectionBgGradientColors";
  private static final String PROP_SELECTION_BG_GRADIENT_PERCENTS
    = "selectionBgGradientPercents";
  private static final String PROP_SELECTION_BG_GRADIENT_VERTICAL
    = "selectionBgGradientVertical";
  private static final String PROP_BORDER_VISIBLE = "borderVisible";
  private static final String PROP_FOLDER_LISTENER = "Folder";
  private static final String PROP_SELECTION_LISTENER = "Selection";

  private static final String DEFAULT_TAB_POSITION = "top";
  private static final int DEFAULT_TAB_HEIGHT = 0;
  private static final String DEFAULT_MIN_MAX_STATE = "normal";
  private static final Rectangle ZERO_BOUNDS = new Rectangle( 0, 0, 0, 0 );

  @Override
  public void preserveValues( CTabFolder folder ) {
    preserveProperty( folder, PROP_TAB_POSITION, getTabPosition( folder ) );
    preserveProperty( folder, PROP_TAB_HEIGHT, folder.getTabHeight() );
    preserveProperty( folder, PROP_MIN_MAX_STATE, getMinMaxState( folder ) );
    preserveProperty( folder, PROP_MINIMIZE_BOUNDS, getMinimizeBounds( folder ) );
    preserveProperty( folder, PROP_MINIMIZE_VISIBLE, folder.getMinimizeVisible() );
    preserveProperty( folder, PROP_MAXIMIZE_BOUNDS, getMaximizeBounds( folder ) );
    preserveProperty( folder, PROP_MAXIMIZE_VISIBLE, folder.getMaximizeVisible() );
    preserveProperty( folder, PROP_CHEVRON_BOUNDS, getChevronBounds( folder ) );
    preserveProperty( folder, PROP_CHEVRON_VISIBLE, getChevronVisible( folder ) );
    preserveProperty( folder, PROP_UNSELECTED_CLOSE_VISIBLE, folder.getUnselectedCloseVisible() );
    preserveProperty( folder, PROP_SELECTION, folder.getSelection() );
    preserveProperty( folder, PROP_SELECTION_BACKGROUND, getSelectionBackground( folder ) );
    preserveProperty( folder, PROP_SELECTION_FOREGROUND, getSelectionForeground( folder ) );
    preserveProperty( folder,
                      PROP_SELECTION_BACKGROUND_IMAGE,
                      getSelectionBackgroundImage( folder ) );
    preserveSelectionBgGradient( folder );
    preserveProperty( folder, PROP_BORDER_VISIBLE, folder.getBorderVisible() );
  }

  @Override
  public void renderInitialization( CTabFolder folder ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( folder, TYPE );
    remoteObject.setHandler( new CTabFolderOperationHandler( folder ) );
    remoteObject.set( "parent", WidgetUtil.getId( folder.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( folder, ALLOWED_STYLES ) ) );
    JsonArray toolTipTexts = new JsonArray()
      .add( SWT.getMessage( "SWT_Minimize" ) )
      .add( SWT.getMessage( "SWT_Maximize" ) )
      .add( SWT.getMessage( "SWT_Restore" ) )
      .add( SWT.getMessage( "SWT_ShowList" ) )
      .add( SWT.getMessage( "SWT_Close" ) );
    remoteObject.set( PROP_TOOLTIP_TEXTS, toolTipTexts );
    // Always listen for Selection and Folder.
    // Currently required for item's control visibility and bounds update.
    remoteObject.listen( PROP_SELECTION_LISTENER, true );
    // Currently required for always sending close/showList notify operations.
    remoteObject.listen( PROP_FOLDER_LISTENER, true );
  }

  @Override
  public void renderChanges( CTabFolder folder ) throws IOException {
    ControlLCAUtil.renderChanges( folder );
    WidgetLCAUtil.renderCustomVariant( folder );
    renderProperty( folder, PROP_TAB_POSITION, getTabPosition( folder ), DEFAULT_TAB_POSITION );
    renderProperty( folder, PROP_TAB_HEIGHT, folder.getTabHeight(), DEFAULT_TAB_HEIGHT );
    renderProperty( folder, PROP_MIN_MAX_STATE, getMinMaxState( folder ), DEFAULT_MIN_MAX_STATE );
    renderProperty( folder, PROP_MINIMIZE_BOUNDS, getMinimizeBounds( folder ), ZERO_BOUNDS );
    renderProperty( folder, PROP_MINIMIZE_VISIBLE, folder.getMinimizeVisible(), false );
    renderProperty( folder, PROP_MAXIMIZE_BOUNDS, getMaximizeBounds( folder ), ZERO_BOUNDS );
    renderProperty( folder, PROP_MAXIMIZE_VISIBLE, folder.getMaximizeVisible(), false );
    renderProperty( folder, PROP_CHEVRON_BOUNDS, getChevronBounds( folder ), ZERO_BOUNDS );
    renderProperty( folder, PROP_CHEVRON_VISIBLE, getChevronVisible( folder ), false );
    renderProperty( folder,
                    PROP_UNSELECTED_CLOSE_VISIBLE,
                    folder.getUnselectedCloseVisible(),
                    true );
    renderProperty( folder, PROP_SELECTION, folder.getSelection(), null );
    renderProperty( folder, PROP_SELECTION_BACKGROUND, getSelectionBackground( folder ), null );
    renderProperty( folder, PROP_SELECTION_FOREGROUND, getSelectionForeground( folder ), null );
    renderProperty( folder,
                    PROP_SELECTION_BACKGROUND_IMAGE,
                    getSelectionBackgroundImage( folder ),
                    null);
    renderSelectionBackgroundGradient( folder );
    renderProperty( folder, PROP_BORDER_VISIBLE, folder.getBorderVisible(), false );
    renderListenDefaultSelection( folder );
  }

  @Override
  public void doRedrawFake( Control control ) {
    CTabFolder folder = ( CTabFolder )control;
    getCTabFolderAdapter( folder ).doRedraw();
  }

  /////////////////////////////////////////
  // Helping methods to preserve properties

  private static void preserveSelectionBgGradient( CTabFolder folder ) {
    ICTabFolderAdapter adapter = getCTabFolderAdapter( folder );
    IWidgetGraphicsAdapter gfxAdapter = adapter.getUserSelectionBackgroundGradient();
    Color[] bgGradientColors = gfxAdapter.getBackgroundGradientColors();
    int[] bgGradientPercents = gfxAdapter.getBackgroundGradientPercents();
    Boolean bgGradientVertical = Boolean.valueOf( gfxAdapter.isBackgroundGradientVertical() );
    preserveProperty( folder, PROP_SELECTION_BG_GRADIENT_COLORS, bgGradientColors );
    preserveProperty( folder, PROP_SELECTION_BG_GRADIENT_PERCENTS, bgGradientPercents );
    preserveProperty( folder, PROP_SELECTION_BG_GRADIENT_VERTICAL, bgGradientVertical );
  }

  //////////////////////////////////////
  // Helping methods to write properties

  private static void renderSelectionBackgroundGradient( CTabFolder folder ) {
    ICTabFolderAdapter adapter = getCTabFolderAdapter( folder );
    IWidgetGraphicsAdapter gfxAdapter = adapter.getUserSelectionBackgroundGradient();
    Color[] bgGradientColors = gfxAdapter.getBackgroundGradientColors();
    int[] bgGradientPercents = gfxAdapter.getBackgroundGradientPercents();
    Boolean bgGradientVertical = Boolean.valueOf( gfxAdapter.isBackgroundGradientVertical() );
    boolean hasChanged = WidgetLCAUtil.hasChanged( folder,
                                                   PROP_SELECTION_BG_GRADIENT_COLORS,
                                                   bgGradientColors,
                                                   null )
                      || WidgetLCAUtil.hasChanged( folder,
                                                   PROP_SELECTION_BG_GRADIENT_PERCENTS,
                                                   bgGradientPercents,
                                                   null )
                      || WidgetLCAUtil.hasChanged( folder,
                                                   PROP_SELECTION_BG_GRADIENT_VERTICAL,
                                                   bgGradientVertical,
                                                   Boolean.FALSE );
    if( hasChanged ) {
      JsonValue gradient = JsonValue.NULL;
      if( bgGradientColors != null ) {
        JsonArray colors = new JsonArray();
        for( int i = 0; i < bgGradientColors.length; i++ ) {
          colors.add( toJson( bgGradientColors[ i ] ) );
        }
        JsonValue percents = createJsonArray( bgGradientPercents );
        gradient = new JsonArray()
          .add( colors )
          .add( percents )
          .add( bgGradientVertical.booleanValue() );
      }
      getRemoteObject( folder ).set( PROP_SELECTION_BG_GRADIENT, gradient );
    }
  }

  //////////////////
  // Helping methods

  private static String getTabPosition( CTabFolder folder ) {
    return folder.getTabPosition() == SWT.TOP ? "top" : "bottom";
  }

  private static String getMinMaxState( CTabFolder folder ) {
    String result = "normal";
    if( folder.getMinimized() ) {
      result = "min";
    } else if( folder.getMaximized() ) {
      result = "max";
    }
    return result;
  }

  private static Rectangle getMinimizeBounds( CTabFolder folder ) {
    return getCTabFolderAdapter( folder ).getMinimizeRect();
  }

  private static Rectangle getMaximizeBounds( CTabFolder folder ) {
    return getCTabFolderAdapter( folder ).getMaximizeRect();
  }

  private static Rectangle getChevronBounds( CTabFolder folder ) {
    return getCTabFolderAdapter( folder ).getChevronRect();
  }

  private static boolean getChevronVisible( CTabFolder folder ) {
    return getCTabFolderAdapter( folder ).getChevronVisible();
  }

  private static Color getSelectionBackground( CTabFolder folder ) {
    return getCTabFolderAdapter( folder ).getUserSelectionBackground();
  }

  private static Color getSelectionForeground( CTabFolder folder ) {
    return getCTabFolderAdapter( folder ).getUserSelectionForeground();
  }

  private static Image getSelectionBackgroundImage( CTabFolder folder ) {
    return getCTabFolderAdapter( folder ).getUserSelectionBackgroundImage();
  }

  private static ICTabFolderAdapter getCTabFolderAdapter( CTabFolder folder ) {
    return folder.getAdapter( ICTabFolderAdapter.class );
  }

  private CTabFolderLCA() {
    // prevent instantiation
  }

}
