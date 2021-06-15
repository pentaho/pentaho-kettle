/*******************************************************************************
 * Copyright (c) 2002, 2018 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.treekit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenDefaultSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;

import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.template.TemplateLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.CellToolTipUtil;
import org.eclipse.swt.internal.widgets.ICellToolTipAdapter;
import org.eclipse.swt.internal.widgets.IItemHolderAdapter;
import org.eclipse.swt.internal.widgets.ITreeAdapter;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public final class TreeLCA extends WidgetLCA<Tree> {

  public static final TreeLCA INSTANCE = new TreeLCA();

  private static final String TYPE = "rwt.widgets.Grid";
  private static final String[] ALLOWED_STYLES = {
    "SINGLE",
    "MULTI",
    "CHECK",
    "FULL_SELECTION",
    "VIRTUAL",
    "NO_SCROLL",
    "NO_RADIO_GROUP",
    "BORDER"
  };

  private static final String PROP_ITEM_COUNT = "itemCount";
  private static final String PROP_ITEM_HEIGHT = "itemHeight";
  private static final String PROP_ITEM_METRICS = "itemMetrics";
  private static final String PROP_COLUMN_COUNT = "columnCount";
  private static final String PROP_COLUMN_ORDER = "columnOrder";
  private static final String PROP_FIXED_COLUMNS = "fixedColumns";
  private static final String PROP_TREE_COLUMN = "treeColumn";
  private static final String PROP_HEADER_HEIGHT = "headerHeight";
  private static final String PROP_HEADER_VISIBLE = "headerVisible";
  private static final String PROP_HEADER_FOREGROUND = "headerForeground";
  private static final String PROP_HEADER_BACKGROUND = "headerBackground";
  private static final String PROP_LINES_VISIBLE = "linesVisible";
  private static final String PROP_TOP_ITEM_INDEX = "topItemIndex";
  private static final String PROP_FOCUS_ITEM = "focusItem";
  private static final String PROP_SCROLL_LEFT = "scrollLeft";
  private static final String PROP_SELECTION = "selection";
  private static final String PROP_SORT_DIRECTION = "sortDirection";
  private static final String PROP_SORT_COLUMN = "sortColumn";
  private static final String PROP_SETDATA_LISTENER = "SetData";
  private static final String PROP_EXPAND_LISTENER = "Expand";
  private static final String PROP_COLLAPSE_LISTENER = "Collapse";
  private static final String PROP_ENABLE_CELL_TOOLTIP = "enableCellToolTip";
  private static final String PROP_CELL_TOOLTIP_TEXT = "cellToolTipText";
  private static final String PROP_MARKUP_ENABLED = "markupEnabled";

  private static final int ZERO = 0 ;
  private static final String[] DEFAULT_SELECTION = new String[ 0 ];
  private static final String[] DEFAULT_COLUMN_ORDER = new String[ 0 ];
  private static final String DEFAULT_SORT_DIRECTION = "none";

  @Override
  public void preserveValues( Tree tree ) {
    preserveProperty( tree, PROP_ITEM_COUNT, tree.getItemCount() );
    preserveProperty( tree, PROP_ITEM_HEIGHT, tree.getItemHeight() );
    preserveProperty( tree, PROP_ITEM_METRICS, getItemMetrics( tree ) );
    preserveProperty( tree, PROP_COLUMN_COUNT, tree.getColumnCount() );
    preserveProperty( tree, PROP_COLUMN_ORDER, getColumnOrder( tree ) );
    preserveProperty( tree, PROP_FIXED_COLUMNS, getFixedColumns( tree ) );
    preserveProperty( tree, PROP_TREE_COLUMN, getTreeColumn( tree ) );
    preserveProperty( tree, PROP_HEADER_HEIGHT, tree.getHeaderHeight() );
    preserveProperty( tree, PROP_HEADER_VISIBLE, tree.getHeaderVisible() );
    preserveProperty( tree, PROP_HEADER_FOREGROUND, tree.getHeaderForeground() );
    preserveProperty( tree, PROP_HEADER_BACKGROUND, tree.getHeaderBackground() );
    preserveProperty( tree, PROP_LINES_VISIBLE, tree.getLinesVisible() );
    preserveProperty( tree, PROP_TOP_ITEM_INDEX, getTopItemIndex( tree ) );
    preserveProperty( tree, PROP_FOCUS_ITEM, getFocusItem( tree ) );
    preserveProperty( tree, PROP_SCROLL_LEFT, getScrollLeft( tree ) );
    preserveProperty( tree, PROP_SELECTION, getSelection( tree ) );
    preserveProperty( tree, PROP_SORT_DIRECTION, getSortDirection( tree ) );
    preserveProperty( tree, PROP_SORT_COLUMN, tree.getSortColumn() );
    preserveProperty( tree, PROP_ENABLE_CELL_TOOLTIP, CellToolTipUtil.isEnabledFor( tree ) );
    preserveProperty( tree, PROP_CELL_TOOLTIP_TEXT, null );
  }

  @Override
  public void renderInitialization( Tree tree ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( tree, TYPE );
    remoteObject.setHandler( new TreeOperationHandler( tree ) );
    remoteObject.set( "parent", getId( tree.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( tree, ALLOWED_STYLES ) ) );
    remoteObject.set( "appearance", "tree" );
    ITreeAdapter adapter = getTreeAdapter( tree );
    if( ( tree.getStyle() & SWT.CHECK ) != 0 ) {
      JsonArray metrics = new JsonArray()
        .add( adapter.getCheckLeft() )
        .add( adapter.getCheckWidth() );
      remoteObject.set( "checkBoxMetrics", metrics );
    }
    if( getFixedColumns( tree ) >= 0 ) {
      remoteObject.set( "splitContainer", true );
    }
    if( ( tree.getStyle() & SWT.FULL_SELECTION ) == 0 ) {
      Rectangle textMargin = getTreeAdapter( tree ).getTextMargin();
      JsonArray padding = new JsonArray()
        .add( textMargin.x )
        .add( textMargin.width - textMargin.x );
      remoteObject.set( "selectionPadding", padding );
    }
    remoteObject.set( "indentionWidth", adapter.getIndentionWidth() );
    remoteObject.set( PROP_MARKUP_ENABLED, isMarkupEnabledFor( tree ) );
    TemplateLCAUtil.renderRowTemplate( tree );
    remoteObject.listen( PROP_SETDATA_LISTENER, isVirtual( tree ) );
    // Always render listen for Expand and Collapse, currently required for scrollbar
    // visibility update and setData events.
    remoteObject.listen( PROP_EXPAND_LISTENER, true );
    remoteObject.listen( PROP_COLLAPSE_LISTENER, true );
  }

  @Override
  public void renderChanges( final Tree tree ) throws IOException {
    ControlLCAUtil.renderChanges( tree );
    WidgetLCAUtil.renderCustomVariant( tree );
    renderProperty( tree, PROP_ITEM_COUNT, tree.getItemCount(), ZERO );
    renderProperty( tree, PROP_ITEM_HEIGHT, tree.getItemHeight(), ZERO );
    renderItemMetrics( tree );
    renderProperty( tree, PROP_COLUMN_COUNT, tree.getColumnCount(), ZERO );
    renderProperty( tree, PROP_COLUMN_ORDER, getColumnOrder( tree ), DEFAULT_COLUMN_ORDER );
    renderProperty( tree, PROP_FIXED_COLUMNS, getFixedColumns( tree ), -1 );
    renderProperty( tree, PROP_TREE_COLUMN, getTreeColumn( tree ), ZERO );
    renderProperty( tree, PROP_HEADER_HEIGHT, tree.getHeaderHeight(), ZERO );
    renderProperty( tree, PROP_HEADER_VISIBLE, tree.getHeaderVisible(), false );
    renderProperty( tree, PROP_HEADER_FOREGROUND, tree.getHeaderForeground(), null );
    renderProperty( tree, PROP_HEADER_BACKGROUND, tree.getHeaderBackground(), null );
    renderProperty( tree, PROP_LINES_VISIBLE, tree.getLinesVisible(), false );
    renderProperty( tree, PROP_SORT_DIRECTION, getSortDirection( tree ), DEFAULT_SORT_DIRECTION );
    renderAfterItems( tree, new Runnable() {
      @Override
      public void run() {
        renderProperty( tree, PROP_TOP_ITEM_INDEX, getTopItemIndex( tree ), ZERO );
        renderProperty( tree, PROP_SCROLL_LEFT, getScrollLeft( tree ), ZERO );
        if( tree.getSelectionCount() > 0 ) {
          renderProperty( tree, PROP_FOCUS_ITEM, getFocusItem( tree ), null );
        }
        renderProperty( tree, PROP_SELECTION, getSelection( tree ), DEFAULT_SELECTION );
        renderProperty( tree, PROP_SORT_COLUMN, tree.getSortColumn(), null );
      }
    } );
    renderListenSelection( tree );
    renderListenDefaultSelection( tree );
    renderProperty( tree, PROP_ENABLE_CELL_TOOLTIP, CellToolTipUtil.isEnabledFor( tree ), false );
    renderProperty( tree, PROP_CELL_TOOLTIP_TEXT, getAndResetCellToolTipText( tree ), null );
  }

  @Override
  public void doRedrawFake( Control control ) {
    Tree tree = ( Tree )control;
    getTreeAdapter( tree ).checkData();
  }

  private static String getAndResetCellToolTipText( Tree tree ) {
    ICellToolTipAdapter adapter = CellToolTipUtil.getAdapter( tree );
    String toolTipText = adapter.getCellToolTipText();
    adapter.setCellToolTipText( null );
    return toolTipText;
  }

  private static boolean isVirtual( Tree tree ) {
    return ( tree.getStyle() & SWT.VIRTUAL ) != 0;
  }

  private static String[] getSelection( Tree tree ) {
    TreeItem[] selection = tree.getSelection();
    String[] result = new String[ selection.length ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = getId( selection[ i ] );
    }
    return result;
  }

  private static String[] getColumnOrder( Tree tree ) {
    int[] order = tree.getColumnOrder();
    String[] result = new String[ order.length ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = getId( tree.getColumn( order[ i ] ) );
    }
    return result;
  }

  private static int getFixedColumns( Tree tree ) {
    return getTreeAdapter( tree ).getFixedColumns();
  }

  private static int getScrollLeft( Tree tree ) {
    return getTreeAdapter( tree ).getScrollLeft();
  }

  private static int getTopItemIndex( Tree tree ) {
    return getTreeAdapter( tree ).getTopItemIndex();
  }

  private static TreeItem getFocusItem( Tree tree ) {
    TreeItem result = null;
    TreeItem[] selection = tree.getSelection();
    if( selection.length > 0 ) {
      result = selection[ 0 ];
    }
    return result;
  }

  private static int getTreeColumn( Tree tree ) {
    int[] values = tree.getColumnOrder();
    return values.length > 0 ? values[ 0 ] : 0;
  }

  private static String getSortDirection( Tree tree ) {
    String result = "none";
    if( tree.getSortDirection() == SWT.UP ) {
      result = "up";
    } else if( tree.getSortDirection() == SWT.DOWN ) {
      result = "down";
    }
    return result;
  }

  private static ITreeAdapter getTreeAdapter( Tree tree ) {
    return tree.getAdapter( ITreeAdapter.class );
  }

  private static void renderAfterItems( Tree tree, Runnable runnable ) {
    Item[] items = tree.getAdapter( IItemHolderAdapter.class ).getItems();
    if( items.length > 0 ) {
      Item lastItem = items[ items.length - 1 ];
      WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( lastItem );
      adapter.addRenderRunnable( runnable );
    } else {
      runnable.run();
    }
  }

  private static void renderItemMetrics( Tree tree ) {
    ItemMetrics[] itemMetrics = getItemMetrics( tree );
    if( hasChanged( tree, PROP_ITEM_METRICS, itemMetrics ) ) {
      JsonArray metrics = new JsonArray();
      for( int i = 0; i < itemMetrics.length; i++ ) {
        metrics.add( new JsonArray().add( i )
                                    .add( itemMetrics[ i ].left )
                                    .add( itemMetrics[ i ].width )
                                    .add( itemMetrics[ i ].imageLeft )
                                    .add( itemMetrics[ i ].imageWidth )
                                    .add( itemMetrics[ i ].textLeft )
                                    .add( itemMetrics[ i ].textWidth ) );
      }
      getRemoteObject( tree ).set( PROP_ITEM_METRICS, metrics );
    }
  }

  static ItemMetrics[] getItemMetrics( Tree tree ) {
    int columnCount = Math.max( 1, tree.getColumnCount() );
    ItemMetrics[] result = new ItemMetrics[ columnCount ];
    for( int i = 0; i < columnCount; i++ ) {
      result[ i ] = new ItemMetrics();
    }
    ITreeAdapter adapter = getTreeAdapter( tree );
    for( int i = 0; i < columnCount; i++ ) {
      result[ i ].left = adapter.getCellLeft( i );
      result[ i ].width = adapter.getCellWidth( i );
      result[ i ].imageLeft = result[ i ].left + adapter.getImageOffset( i );
      result[ i ].imageWidth = adapter.getItemImageSize( i ).x;
      result[ i ].textLeft = result[ i ].left + adapter.getTextOffset( i );
      result[ i ].textWidth = adapter.getTextMaxWidth( i );
    }
    return result;
  }

  // TODO: merge with Table:
  static final class ItemMetrics {
    int left;
    int width;
    int imageLeft;
    int imageWidth;
    int textLeft;
    int textWidth;

    @Override
    public boolean equals( Object obj ) {
      boolean result;
      if( obj == this ) {
        result = true;
      } else  if( obj instanceof ItemMetrics ) {
        ItemMetrics other = ( ItemMetrics )obj;
        result =  other.left == left
               && other.width == width
               && other.imageLeft == imageLeft
               && other.imageWidth == imageWidth
               && other.textLeft == textLeft
               && other.textWidth == textWidth;
      } else {
        result = false;
      }
      return result;
    }

    @Override
    public int hashCode() {
      String msg = "ItemMetrics#hashCode() not implemented";
      throw new UnsupportedOperationException( msg );
    }
  }

  private TreeLCA() {
    // prevent instantiation
  }

}
