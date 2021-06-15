/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.custom.ccombokit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenDefaultSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenModifyVerify;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Point;


public final class CComboLCA extends WidgetLCA<CCombo> {

  public static final CComboLCA INSTANCE = new CComboLCA();

  private static final String TYPE = "rwt.widgets.Combo";
  private static final String[] ALLOWED_STYLES = { "FLAT", "BORDER" };

  // Property names for preserve-value facility
  static final String PROP_ITEMS = "items";
  static final String PROP_TEXT = "text";
  static final String PROP_SELECTION_INDEX = "selectionIndex";
  static final String PROP_SELECTION = "selection";
  static final String PROP_TEXT_LIMIT = "textLimit";
  static final String PROP_LIST_VISIBLE = "listVisible";
  static final String PROP_EDITABLE = "editable";
  static final String PROP_VISIBLE_ITEM_COUNT = "visibleItemCount";

  // Default values
  private static final String[] DEFAUT_ITEMS = new String[ 0 ];
  private static final Integer DEFAULT_SELECTION_INDEX = Integer.valueOf( -1 );
  private static final Point DEFAULT_SELECTION = new Point( 0, 0 );
  private static final int DEFAULT_VISIBLE_ITEM_COUNT = 5;

  @Override
  public void preserveValues( CCombo ccombo ) {
    preserveProperty( ccombo, PROP_ITEMS, ccombo.getItems() );
    preserveProperty( ccombo, PROP_SELECTION_INDEX, ccombo.getSelectionIndex() );
    preserveProperty( ccombo, PROP_SELECTION, ccombo.getSelection() );
    preserveProperty( ccombo, PROP_TEXT_LIMIT, getTextLimit( ccombo ) );
    preserveProperty( ccombo, PROP_VISIBLE_ITEM_COUNT, ccombo.getVisibleItemCount() );
    preserveProperty( ccombo, PROP_TEXT, ccombo.getText() );
    preserveProperty( ccombo, PROP_LIST_VISIBLE, ccombo.getListVisible() );
    preserveProperty( ccombo, PROP_EDITABLE, Boolean.valueOf( ccombo.getEditable() ) );
  }

  @Override
  public void renderInitialization( CCombo ccombo ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( ccombo, TYPE );
    remoteObject.setHandler( new CComboOperationHandler( ccombo ) );
    remoteObject.set( "parent", getId( ccombo.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( ccombo, ALLOWED_STYLES ) ) );
    remoteObject.set( "ccombo", true );
  }

  @Override
  public void renderChanges( CCombo ccombo ) throws IOException {
    ControlLCAUtil.renderChanges( ccombo );
    WidgetLCAUtil.renderCustomVariant( ccombo );
    renderVisibleItemCount( ccombo );
    renderItems( ccombo );
    renderListVisible( ccombo );
    renderSelectionIndex( ccombo );
    renderEditable( ccombo );
    renderText( ccombo );
    renderSelection( ccombo );
    renderTextLimit( ccombo );
    renderListenSelection( ccombo );
    renderListenDefaultSelection( ccombo );
    renderListenModifyVerify( ccombo );
  }

  //////////////////////////////////////////////
  // Helping methods to write changed properties

  private static void renderVisibleItemCount( CCombo ccombo ) {
    int defValue = DEFAULT_VISIBLE_ITEM_COUNT;
    renderProperty( ccombo, PROP_VISIBLE_ITEM_COUNT, ccombo.getVisibleItemCount(), defValue );
  }

  private static void renderItems( CCombo ccombo ) {
    renderProperty( ccombo, PROP_ITEMS, ccombo.getItems(), DEFAUT_ITEMS );
  }

  private static void renderListVisible( CCombo ccombo ) {
    renderProperty( ccombo, PROP_LIST_VISIBLE, ccombo.getListVisible(), false );
  }

  private static void renderSelectionIndex( CCombo ccombo ) {
    Integer newSelectionIndex = Integer.valueOf( ccombo.getSelectionIndex() );
    boolean selectionChanged
      = hasChanged( ccombo, PROP_SELECTION_INDEX, newSelectionIndex, DEFAULT_SELECTION_INDEX );
    // The 'itemsChanged' statement covers the following use case:
    // combo.add( "a" );  combo.select( 0 );
    // -- in a subsequent request --
    // combo.removeAll();  combo.add( "b" );  combo.select( 0 );
    // When only examining selectionIndex, a change cannot be determined
    boolean itemsChanged = hasChanged( ccombo, PROP_ITEMS, ccombo.getItems(), DEFAUT_ITEMS );
    boolean isInitialized = WidgetUtil.getAdapter( ccombo ).isInitialized();
    if( selectionChanged || ( itemsChanged && isInitialized ) ) {
      getRemoteObject( ccombo ).set( PROP_SELECTION_INDEX, newSelectionIndex.intValue() );
    }
  }

  private static void renderEditable( CCombo ccombo ) {
    renderProperty( ccombo, PROP_EDITABLE, ccombo.getEditable(), true );
  }

  private static void renderText( CCombo ccombo ) {
    renderProperty( ccombo, PROP_TEXT, ccombo.getText(), "" );
  }

  private static void renderSelection( CCombo ccombo ) {
    renderProperty( ccombo, PROP_SELECTION, ccombo.getSelection(), DEFAULT_SELECTION );
  }

  private static void renderTextLimit( CCombo ccombo ) {
    renderProperty( ccombo, PROP_TEXT_LIMIT, getTextLimit( ccombo ), null );
  }

  private static Integer getTextLimit( CCombo ccombo ) {
    Integer result = Integer.valueOf( ccombo.getTextLimit() );
    if( result.intValue() == CCombo.LIMIT  ) {
      result = null;
    }
    return result;
  }

  private CComboLCA() {
    // prevent instantiation
  }

}
