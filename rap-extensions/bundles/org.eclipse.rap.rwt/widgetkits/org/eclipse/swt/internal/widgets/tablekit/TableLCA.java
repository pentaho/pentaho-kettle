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
package org.eclipse.swt.internal.widgets.tablekit;

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
import org.eclipse.swt.internal.widgets.ITableAdapter;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


public final class TableLCA extends WidgetLCA<Table> {

  public static final TableLCA INSTANCE = new TableLCA();

  private static final String TYPE = "rwt.widgets.Grid";
  private static final String[] ALLOWED_STYLES = {
    "SINGLE",
    "MULTI",
    "CHECK",
    "FULL_SELECTION",
    "HIDE_SELECTION",
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
  private static final String PROP_TREE_COLUMN = "treeColumn";
  private static final String PROP_FIXED_COLUMNS = "fixedColumns";
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
  private static final String PROP_ALWAYS_HIDE_SELECTION = "alwaysHideSelection";
  private static final String PROP_ENABLE_CELL_TOOLTIP = "enableCellToolTip";
  private static final String PROP_CELL_TOOLTIP_TEXT = "cellToolTipText";
  private static final String PROP_MARKUP_ENABLED = "markupEnabled";

  private static final int ZERO = 0 ;
  private static final String[] DEFAULT_SELECTION = new String[ 0 ];
  private static final String[] DEFAULT_COLUMN_ORDER = new String[ 0 ];
  private static final String DEFAULT_SORT_DIRECTION = "none";

  @Override
  public void preserveValues( Table table ) {
    preserveProperty( table, PROP_ITEM_COUNT, table.getItemCount() );
    preserveProperty( table, PROP_ITEM_HEIGHT, table.getItemHeight() );
    preserveProperty( table, PROP_ITEM_METRICS, getItemMetrics( table ) );
    preserveProperty( table, PROP_COLUMN_COUNT, table.getColumnCount() );
    preserveProperty( table, PROP_COLUMN_ORDER, getColumnOrder( table ) );
    preserveProperty( table, PROP_FIXED_COLUMNS, getFixedColumns( table ) );
    preserveProperty( table, PROP_HEADER_HEIGHT, table.getHeaderHeight() );
    preserveProperty( table, PROP_HEADER_VISIBLE, table.getHeaderVisible() );
    preserveProperty( table, PROP_HEADER_FOREGROUND, table.getHeaderForeground() );
    preserveProperty( table, PROP_HEADER_BACKGROUND, table.getHeaderBackground() );
    preserveProperty( table, PROP_LINES_VISIBLE, table.getLinesVisible() );
    preserveProperty( table, PROP_TOP_ITEM_INDEX, table.getTopIndex() );
    preserveProperty( table, PROP_FOCUS_ITEM, getFocusItem( table ) );
    preserveProperty( table, PROP_SCROLL_LEFT, getScrollLeft( table ) );
    preserveProperty( table, PROP_SELECTION, getSelection( table ) );
    preserveProperty( table, PROP_SORT_DIRECTION, getSortDirection( table ) );
    preserveProperty( table, PROP_SORT_COLUMN, table.getSortColumn() );
    preserveProperty( table, PROP_ALWAYS_HIDE_SELECTION, hasAlwaysHideSelection( table ) );
    preserveProperty( table, PROP_ENABLE_CELL_TOOLTIP, CellToolTipUtil.isEnabledFor( table ) );
    preserveProperty( table, PROP_CELL_TOOLTIP_TEXT, null );
  }

  @Override
  public void renderInitialization( Table table ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( table, TYPE );
    remoteObject.setHandler( new TableOperationHandler( table ) );
    remoteObject.set( "parent", getId( table.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( table, ALLOWED_STYLES ) ) );
    remoteObject.set( "appearance", "table" );
    ITableAdapter adapter = getTableAdapter( table );
    if( ( table.getStyle() & SWT.CHECK ) != 0 ) {
      JsonArray metrics = new JsonArray()
        .add( adapter.getCheckLeft() )
        .add( adapter.getCheckWidth() );
      remoteObject.set( "checkBoxMetrics", metrics );
    }
    if( getFixedColumns( table ) >= 0 ) {
      remoteObject.set( "splitContainer", true );
    }
    remoteObject.set( "indentionWidth", 0 );
    remoteObject.set( PROP_TREE_COLUMN, -1 );
    remoteObject.set( PROP_MARKUP_ENABLED, isMarkupEnabledFor( table ) );
    TemplateLCAUtil.renderRowTemplate( table );
    remoteObject.listen( PROP_SETDATA_LISTENER, isVirtual( table ) );
  }

  @Override
  public void renderChanges( final Table table ) throws IOException {
    ControlLCAUtil.renderChanges( table );
    WidgetLCAUtil.renderCustomVariant( table );
    renderProperty( table, PROP_ITEM_COUNT, table.getItemCount(), ZERO );
    renderProperty( table, PROP_ITEM_HEIGHT, table.getItemHeight(), ZERO );
    renderItemMetrics( table );
    renderProperty( table, PROP_COLUMN_COUNT, table.getColumnCount(), ZERO );
    renderProperty( table, PROP_COLUMN_ORDER, getColumnOrder( table ), DEFAULT_COLUMN_ORDER );
    renderProperty( table, PROP_FIXED_COLUMNS, getFixedColumns( table ), -1 );
    renderProperty( table, PROP_HEADER_HEIGHT, table.getHeaderHeight(), ZERO );
    renderProperty( table, PROP_HEADER_VISIBLE, table.getHeaderVisible(), false );
    renderProperty( table, PROP_HEADER_FOREGROUND, table.getHeaderForeground(), null );
    renderProperty( table, PROP_HEADER_BACKGROUND, table.getHeaderBackground(), null );
    renderProperty( table, PROP_LINES_VISIBLE, table.getLinesVisible(), false );
    renderProperty( table, PROP_SORT_DIRECTION, getSortDirection( table ), DEFAULT_SORT_DIRECTION );
    renderAfterItems( table, new Runnable() {
      @Override
      public void run() {
        renderProperty( table, PROP_TOP_ITEM_INDEX, table.getTopIndex(), ZERO );
        renderProperty( table, PROP_SCROLL_LEFT, getScrollLeft( table ), ZERO );
        renderProperty( table, PROP_FOCUS_ITEM, getFocusItem( table ), null );
        renderProperty( table, PROP_SELECTION, getSelection( table ), DEFAULT_SELECTION );
        renderProperty( table, PROP_SORT_COLUMN, table.getSortColumn(), null );
      }
    } );
    renderListenSelection( table );
    renderListenDefaultSelection( table );
    renderProperty( table, PROP_ALWAYS_HIDE_SELECTION, hasAlwaysHideSelection( table ), false );
    renderProperty( table, PROP_ENABLE_CELL_TOOLTIP, CellToolTipUtil.isEnabledFor( table ), false );
    renderProperty( table, PROP_CELL_TOOLTIP_TEXT, getAndResetCellToolTipText( table ), null );
  }

  @Override
  public void doRedrawFake( Control control ) {
    Table table = ( Table )control;
    getTableAdapter( table ).checkData();
  }

  private static String getAndResetCellToolTipText( Table table ) {
    ICellToolTipAdapter adapter = CellToolTipUtil.getAdapter( table );
    String toolTipText = adapter.getCellToolTipText();
    adapter.setCellToolTipText( null );
    return toolTipText;
  }

  private static boolean isVirtual( Table table ) {
    return ( table.getStyle() & SWT.VIRTUAL ) != 0;
  }

  private static String[] getSelection( Table table ) {
    TableItem[] selection = table.getSelection();
    String[] result = new String[ selection.length ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = getId( selection[ i ] );
    }
    return result;
  }

  private static String[] getColumnOrder( Table table ) {
    int[] order = table.getColumnOrder();
    String[] result = new String[ order.length ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = getId( table.getColumn( order[ i ] ) );
    }
    return result;
  }

  private static int getFixedColumns( Table table ) {
    return getTableAdapter( table ).getFixedColumns();
  }

  private static int getScrollLeft( Table table ) {
    return getTableAdapter( table ).getLeftOffset();
  }

  private static TableItem getFocusItem( Table table ) {
    TableItem result = null;
    int focusIndex = getTableAdapter( table ).getFocusIndex();
    if( focusIndex != -1 ) {
      // TODO [rh] do something about when index points to unresolved item!
      result = table.getItem( focusIndex );
    }
    return result;
  }

  private static String getSortDirection( Table table ) {
    String result = "none";
    if( table.getSortDirection() == SWT.UP ) {
      result = "up";
    } else if( table.getSortDirection() == SWT.DOWN ) {
      result = "down";
    }
    return result;
  }

  static boolean hasAlwaysHideSelection( Table table ) {
    Object data = table.getData( Table.ALWAYS_HIDE_SELECTION );
    return Boolean.TRUE.equals( data );
  }

  private static ITableAdapter getTableAdapter( Table table ) {
    return table.getAdapter( ITableAdapter.class );
  }

  private static void renderAfterItems( Table table, Runnable runnable ) {
    Item[] items = table.getAdapter( IItemHolderAdapter.class ).getItems();
    if( items.length > 0 ) {
      Item lastItem = items[ items.length - 1 ];
      WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( lastItem );
      adapter.addRenderRunnable( runnable );
    } else {
      runnable.run();
    }
  }

  private static void renderItemMetrics( Table table ) {
    ItemMetrics[] itemMetrics = getItemMetrics( table );
    if( hasChanged( table, PROP_ITEM_METRICS, itemMetrics ) ) {
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
      getRemoteObject( table ).set( PROP_ITEM_METRICS, metrics );
    }
  }

  static ItemMetrics[] getItemMetrics( Table table ) {
    int columnCount = Math.max( 1, table.getColumnCount() );
    ItemMetrics[] result = new ItemMetrics[ columnCount ];
    for( int i = 0; i < columnCount; i++ ) {
      result[ i ] = new ItemMetrics();
    }
    ITableAdapter tableAdapter = getTableAdapter( table );
    TableItem measureItem = tableAdapter.getMeasureItem();
    if( measureItem != null ) {
      for( int i = 0; i < columnCount; i++ ) {
        int leftOffset = tableAdapter.getColumnLeftOffset( i );
        Rectangle bounds = measureItem.getBounds( i );
        Rectangle imageBounds = measureItem.getImageBounds( i );
        Rectangle textBounds = measureItem.getTextBounds( i );
        // If in column mode, cut image width if image exceeds right cell border
        int imageWidth = tableAdapter.getItemImageWidth( i );
        if( table.getColumnCount() > 0 ) {
          TableColumn column = table.getColumn( i );
          int columnLeft = tableAdapter.getColumnLeft( column );
          int columnWidth = column.getWidth();
          int maxImageWidth = columnWidth - ( imageBounds.x - columnLeft + leftOffset );
          if( imageWidth > maxImageWidth ) {
            imageWidth = Math.max( 0, maxImageWidth );
          }
        }
        result[ i ].left = bounds.x + leftOffset;
        result[ i ].width = bounds.width;
        result[ i ].imageLeft = imageBounds.x + leftOffset;
        result[ i ].imageWidth = imageWidth;
        result[ i ].textLeft = textBounds.x + leftOffset;
        result[ i ].textWidth = textBounds.width;
      }
    } else if( table.getColumnCount() > 0 ) {
      for( int i = 0; i < columnCount; i++ ) {
        TableColumn column = table.getColumn( i );
        int columnLeft = tableAdapter.getColumnLeft( column );
        int columnWidth = column.getWidth();
        result[ i ].left = columnLeft;
        result[ i ].width = columnWidth;
      }
    }
    return result;
  }

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

  private TableLCA() {
    // prevent instantiation
  }

}
