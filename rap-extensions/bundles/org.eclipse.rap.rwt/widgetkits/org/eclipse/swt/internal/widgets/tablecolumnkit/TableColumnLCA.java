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
package org.eclipse.swt.internal.widgets.tablecolumnkit;

import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.widgets.IControlAdapter;
import org.eclipse.swt.internal.widgets.ITableAdapter;
import org.eclipse.swt.internal.widgets.ItemLCAUtil;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public final class TableColumnLCA extends WidgetLCA<TableColumn> {

  public static final TableColumnLCA INSTANCE = new TableColumnLCA();

  private static final String TYPE = "rwt.widgets.GridColumn";

  static final String PROP_INDEX = "index";
  static final String PROP_LEFT = "left";
  static final String PROP_WIDTH = "width";
  static final String PROP_RESIZABLE = "resizable";
  static final String PROP_MOVEABLE = "moveable";
  static final String PROP_ALIGNMENT = "alignment";
  static final String PROP_FIXED = "fixed";

  private static final int ZERO = 0;
  private static final String DEFAULT_ALIGNMENT = "left";

  @Override
  public void preserveValues( TableColumn column ) {
    WidgetLCAUtil.preserveToolTipText( column, column.getToolTipText() );
    WidgetLCAUtil.preserveFont( column, getFont( column ) );
    ItemLCAUtil.preserve( column );
    preserveProperty( column, PROP_INDEX, getIndex( column ) );
    preserveProperty( column, PROP_LEFT, getLeft( column ) );
    preserveProperty( column, PROP_WIDTH, column.getWidth() );
    preserveProperty( column, PROP_RESIZABLE, column.getResizable() );
    preserveProperty( column, PROP_MOVEABLE, column.getMoveable() );
    preserveProperty( column, PROP_ALIGNMENT, getAlignment( column ) );
    preserveProperty( column, PROP_FIXED, isFixed( column ) );
  }

  @Override
  public void renderInitialization( TableColumn column ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( column, TYPE );
    remoteObject.setHandler( new TableColumnOperationHandler( column ) );
    remoteObject.set( "parent", getId( column.getParent() ) );
  }

  @Override
  public void renderChanges( TableColumn column ) throws IOException {
    WidgetLCAUtil.renderToolTip( column, column.getToolTipText() );
    WidgetLCAUtil.renderCustomVariant( column );
    WidgetLCAUtil.renderFont( column, getFont( column ) );
    ItemLCAUtil.renderChanges( column );
    renderProperty( column, PROP_INDEX, getIndex( column ), -1 );
    renderProperty( column, PROP_LEFT, getLeft( column ), ZERO );
    renderProperty( column, PROP_WIDTH, column.getWidth(), ZERO );
    renderProperty( column, PROP_RESIZABLE, column.getResizable(), true );
    renderProperty( column, PROP_MOVEABLE, column.getMoveable(), false );
    renderProperty( column, PROP_ALIGNMENT, getAlignment( column ), DEFAULT_ALIGNMENT );
    renderProperty( column, PROP_FIXED, isFixed( column ), false );
    renderListenSelection( column );
  }

  //////////////////////////////////////////////////
  // Helping methods to obtain calculated properties

  private static int getIndex( TableColumn column ) {
    return column.getParent().indexOf( column );
  }

  static int getLeft( TableColumn column ) {
    return getTableAdapter( column ).getColumnLeft( column );
  }

  private static String getAlignment( TableColumn column ) {
    int alignment = column.getAlignment();
    String result = "left";
    if( ( alignment & SWT.CENTER ) != 0 ) {
      result = "center";
    } else if( ( alignment & SWT.RIGHT ) != 0 ) {
      result = "right";
    }
    return result;
  }

  private static Font getFont( TableColumn column ) {
    Table table = column.getParent();
    IControlAdapter adapter = table.getAdapter( IControlAdapter.class );
    return adapter.getUserFont();
  }

  private static boolean isFixed( TableColumn column ) {
    return getTableAdapter( column ).isFixedColumn( column );
  }

  private static ITableAdapter getTableAdapter( TableColumn column ) {
    return column.getParent().getAdapter( ITableAdapter.class );
  }

  private TableColumnLCA() {
    // prevent instantiation
  }

}
