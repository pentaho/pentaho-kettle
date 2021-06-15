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
package org.eclipse.swt.internal.widgets.combokit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderClientListeners;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;


public class ComboLCA extends WidgetLCA<Combo> {

  public static final ComboLCA INSTANCE = new ComboLCA();

  private static final String TYPE = "rwt.widgets.Combo";
  private static final String[] ALLOWED_STYLES = { "DROP_DOWN", "SIMPLE", "BORDER" };

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
  public void preserveValues( Combo combo ) {
    preserveProperty( combo, PROP_ITEMS, combo.getItems() );
    preserveProperty( combo, PROP_SELECTION_INDEX, Integer.valueOf( combo.getSelectionIndex() ) );
    preserveProperty( combo, PROP_SELECTION, combo.getSelection() );
    preserveProperty( combo, PROP_TEXT_LIMIT, getTextLimit( combo ) );
    preserveProperty( combo, PROP_VISIBLE_ITEM_COUNT, combo.getVisibleItemCount() );
    preserveProperty( combo, PROP_TEXT, combo.getText() );
    preserveProperty( combo, PROP_LIST_VISIBLE, combo.getListVisible() );
    preserveProperty( combo, PROP_EDITABLE, Boolean.valueOf( isEditable( combo ) ) );
  }

  @Override
  public void renderInitialization( Combo combo ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( combo, TYPE );
    remoteObject.setHandler( new ComboOperationHandler( combo ) );
    remoteObject.set( "parent", getId( combo.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( combo, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Combo combo ) throws IOException {
    ControlLCAUtil.renderChanges( combo );
    WidgetLCAUtil.renderCustomVariant( combo );
    renderVisibleItemCount( combo );
    renderItems( combo );
    renderListVisible( combo );
    renderSelectionIndex( combo );
    renderEditable( combo );
    renderText( combo );
    renderSelection( combo );
    renderTextLimit( combo );
    renderListenSelection( combo );
    renderListenDefaultSelection( combo );
    renderListenModifyVerify( combo );
    renderClientListeners( combo );
  }

  ///////////////////////////////////////////////////
  // Helping methods to render the changed properties

  private static void renderVisibleItemCount( Combo combo ) {
    int defValue = DEFAULT_VISIBLE_ITEM_COUNT;
    renderProperty( combo, PROP_VISIBLE_ITEM_COUNT, combo.getVisibleItemCount(), defValue );
  }

  private static void renderItems( Combo combo ) {
    renderProperty( combo, PROP_ITEMS, combo.getItems(), DEFAUT_ITEMS );
  }

  private static void renderListVisible( Combo combo ) {
    renderProperty( combo, PROP_LIST_VISIBLE, combo.getListVisible(), false );
  }

  private static void renderSelectionIndex( Combo combo ) {
    Integer newSelectionIndex = Integer.valueOf( combo.getSelectionIndex() );
    boolean selectionChanged
      = hasChanged( combo, PROP_SELECTION_INDEX, newSelectionIndex, DEFAULT_SELECTION_INDEX );
    // The 'itemsChanged' statement covers the following use case:
    // combo.add( "a" );  combo.select( 0 );
    // -- in a subsequent request --
    // combo.removeAll();  combo.add( "b" );  combo.select( 0 );
    // When only examining selectionIndex, a change cannot be determined
    boolean itemsChanged = hasChanged( combo, PROP_ITEMS, combo.getItems(), DEFAUT_ITEMS );
    boolean isInitialized = WidgetUtil.getAdapter( combo ).isInitialized();
    if( selectionChanged || ( itemsChanged && isInitialized ) ) {
      getRemoteObject( combo ).set( PROP_SELECTION_INDEX, newSelectionIndex.intValue() );
    }
  }

  private static void renderEditable( Combo combo ) {
    renderProperty( combo, PROP_EDITABLE, isEditable( combo ), true );
  }

  private static void renderText( Combo combo ) {
    if( isEditable( combo ) ) {
      renderProperty( combo, PROP_TEXT, combo.getText(), "" );
    }
  }

  private static void renderSelection( Combo combo ) {
    renderProperty( combo, PROP_SELECTION, combo.getSelection(), DEFAULT_SELECTION );
  }

  private static void renderTextLimit( Combo combo ) {
    renderProperty( combo, PROP_TEXT_LIMIT, getTextLimit( combo ), null );
  }

  private static boolean isEditable( Combo combo ) {
    return ( ( combo.getStyle() & SWT.READ_ONLY ) == 0 );
  }

  private static Integer getTextLimit( Combo combo ) {
    Integer result = Integer.valueOf( combo.getTextLimit() );
    if( result.intValue() == Combo.LIMIT  ) {
      result = null;
    }
    return result;
  }

  private ComboLCA() {
    // prevent instantiation
  }

}
