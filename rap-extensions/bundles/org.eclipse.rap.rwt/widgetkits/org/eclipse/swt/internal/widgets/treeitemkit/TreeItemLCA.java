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
package org.eclipse.swt.internal.widgets.treeitemkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.ITreeAdapter;
import org.eclipse.swt.internal.widgets.ITreeItemAdapter;
import org.eclipse.swt.internal.widgets.IWidgetColorAdapter;
import org.eclipse.swt.internal.widgets.IWidgetFontAdapter;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;


public final class TreeItemLCA extends WidgetLCA<TreeItem> {

  public static final TreeItemLCA INSTANCE = new TreeItemLCA();

  private static final String TYPE = "rwt.widgets.GridItem";

  static final String PROP_INDEX = "index";
  static final String PROP_ITEM_COUNT = "itemCount";
  static final String PROP_TEXTS = "texts";
  static final String PROP_IMAGES = "images";
  static final String PROP_CELL_BACKGROUNDS = "cellBackgrounds";
  static final String PROP_CELL_FOREGROUNDS = "cellForegrounds";
  static final String PROP_CELL_FONTS = "cellFonts";
  static final String PROP_EXPANDED = "expanded";
  static final String PROP_CHECKED = "checked";
  static final String PROP_GRAYED = "grayed";
  private static final String PROP_CACHED = "cached";

  private static final int DEFAULT_ITEM_COUNT = 0;

  @Override
  public void preserveValues( TreeItem item ) {
    preserveProperty( item, PROP_INDEX, getIndex( item ) );
    preserveProperty( item, PROP_CACHED, isCached( item ) );
    if( isCached( item ) ) {
      preserveProperty( item, PROP_ITEM_COUNT, item.getItemCount() );
      preserveProperty( item, PROP_TEXTS, getTexts( item ) );
      preserveProperty( item, PROP_IMAGES, getImages( item ) );
      WidgetLCAUtil.preserveBackground( item, getUserBackground( item ) );
      WidgetLCAUtil.preserveForeground( item, getUserForeground( item ) );
      WidgetLCAUtil.preserveFont( item, getUserFont( item ) );
      preserveProperty( item, PROP_CELL_BACKGROUNDS, getCellBackgrounds( item ) );
      preserveProperty( item, PROP_CELL_FOREGROUNDS, getCellForegrounds( item ) );
      preserveProperty( item, PROP_CELL_FONTS, getCellFonts( item ) );
      preserveProperty( item, PROP_EXPANDED, item.getExpanded() );
      preserveProperty( item, PROP_CHECKED, item.getChecked() );
      preserveProperty( item, PROP_GRAYED, item.getGrayed() );
    }
  }

  @Override
  public void renderInitialization( TreeItem item ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( item, TYPE );
    remoteObject.setHandler( new TreeItemOperationHandler( item ) );
    Widget parent = item.getParentItem() == null ? item.getParent() : item.getParentItem();
    remoteObject.set( "parent", WidgetUtil.getId( parent ) );
  }

  @Override
  public void renderChanges( final TreeItem item ) throws IOException {
    renderProperty( item, PROP_INDEX, getIndex( item ), -1 );
    if( wasCleared( item ) ) {
      renderClear( item );
    } else if( isCached( item ) ) {
      preservingInitialized( item, new Runnable() {
        @Override
        public void run() {
          // items that were uncached and are now cached (materialized) are handled as if they were
          // just created (initialized = false)
          if( !wasCached( item ) ) {
            setInitialized( item, false );
          }
          renderProperties( item );
        }
      } );
    }
  }

  private static void renderClear( TreeItem item ) {
    getRemoteObject( item ).call( "clear", null );
  }

  private static void renderProperties( TreeItem item ) {
    renderProperty( item, PROP_ITEM_COUNT, item.getItemCount(), DEFAULT_ITEM_COUNT );
    renderProperty( item, PROP_TEXTS, getTexts( item ), null );
    renderProperty( item, PROP_IMAGES, getImages( item ), null );
    WidgetLCAUtil.renderBackground( item, getUserBackground( item ) );
    WidgetLCAUtil.renderForeground( item, getUserForeground( item ) );
    WidgetLCAUtil.renderFont( item, getUserFont( item ) );
    WidgetLCAUtil.renderCustomVariant( item );
    WidgetLCAUtil.renderData( item );
    renderProperty( item, PROP_CELL_BACKGROUNDS, getCellBackgrounds( item ), null );
    renderProperty( item, PROP_CELL_FOREGROUNDS, getCellForegrounds( item ), null );
    renderProperty( item, PROP_CELL_FONTS, getCellFonts( item ), null );
    renderProperty( item, PROP_EXPANDED, item.getExpanded(), false );
    renderProperty( item, PROP_CHECKED, item.getChecked(), false );
    renderProperty( item, PROP_GRAYED, item.getGrayed(), false );
  }

  @Override
  public void renderDispose( TreeItem item ) throws IOException {
    RemoteObject remoteObject = getRemoteObject( item );
    // The parent by the clients logic is the parent-item, not the tree (except for root layer)
    if( !getTreeItemAdapter( item ).isParentDisposed() ) {
      remoteObject.destroy();
    } else {
      ( ( RemoteObjectImpl )remoteObject ).markDestroyed();
    }
  }

  private static int getIndex( TreeItem item ) {
    int result;
    if( item.getParentItem() == null ) {
      result = item.getParent().indexOf( item );
    } else {
      result = item.getParentItem().indexOf( item );
    }
    return result;
  }

  private static boolean wasCleared( TreeItem item ) {
    return !isCached( item ) && wasCached( item );
  }

  private static boolean isCached( TreeItem item ) {
    return item.getParent().getAdapter( ITreeAdapter.class ).isCached( item );
  }

  private static boolean wasCached( TreeItem item ) {
    RemoteAdapter adapter = WidgetUtil.getAdapter( item );
    if( adapter.isInitialized() ) {
      return Boolean.TRUE.equals( adapter.getPreserved( PROP_CACHED ) );
    }
    return false;
  }

  private static String[] getTexts( TreeItem item ) {
    return getTreeItemAdapter( item ).getTexts();
  }

  private static Image[] getImages( TreeItem item ) {
    return getTreeItemAdapter( item ).getImages();
  }

  private static Color getUserBackground( TreeItem item ) {
    return item.getAdapter( IWidgetColorAdapter.class ).getUserBackground();
  }

  private static Color getUserForeground( TreeItem item ) {
    return item.getAdapter( IWidgetColorAdapter.class ).getUserForeground();
  }

  private static Font getUserFont( TreeItem item ) {
    return item.getAdapter( IWidgetFontAdapter.class ).getUserFont();
  }

  private static Color[] getCellBackgrounds( TreeItem item ) {
    return getTreeItemAdapter( item ).getCellBackgrounds();
  }

  private static Color[] getCellForegrounds( TreeItem item ) {
    return getTreeItemAdapter( item ).getCellForegrounds();
  }

  private static Font[] getCellFonts( TreeItem item ) {
    return getTreeItemAdapter( item ).getCellFonts();
  }

  private static ITreeItemAdapter getTreeItemAdapter( TreeItem item ) {
    return item.getAdapter( ITreeItemAdapter.class );
  }

  private static void preservingInitialized( TreeItem item, Runnable runnable ) {
    boolean initialized = WidgetUtil.getAdapter( item ).isInitialized();
    runnable.run();
    setInitialized( item, initialized );
  }

  private static void setInitialized( TreeItem item, boolean initialized ) {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )WidgetUtil.getAdapter( item );
    adapter.setInitialized( initialized );
  }

  private TreeItemLCA() {
    // prevent instantiation
  }

}
