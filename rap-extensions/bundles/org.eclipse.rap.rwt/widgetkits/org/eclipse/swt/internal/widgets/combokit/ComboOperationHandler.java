/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.combokit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_DEFAULT_SELECTION;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_MODIFY;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;
import static org.eclipse.rap.rwt.remote.JsonMapping.readPoint;
import static org.eclipse.swt.internal.events.EventLCAUtil.isListening;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.ITextAdapter;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;


public class ComboOperationHandler extends ControlOperationHandler<Combo> {

  private static final String PROP_SELECTION_INDEX = "selectionIndex";
  private static final String PROP_LIST_VISIBLE = "listVisible";
  private static final String PROP_TEXT = "text";
  private static final String PROP_SELECTION = "selection";

  public ComboOperationHandler( Combo combo ) {
    super( combo );
  }

  @Override
  public void handleSet( Combo combo, JsonObject properties ) {
    super.handleSet( combo, properties );
    handleSetSelectionIndex( combo, properties );
    handleSetListVisible( combo, properties );
    handleSetText( combo, properties );
    handleSetSelection( combo, properties );
  }

  @Override
  public void handleNotify( Combo combo, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( combo, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( combo, properties );
    } else if( EVENT_MODIFY.equals( eventName ) ) {
      handleNotifyModify( );
    } else {
      super.handleNotify( combo, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET selectionIndex
   *
   * @param selectionIndex (int) the index of the item to select
   */
  public void handleSetSelectionIndex( Combo combo, JsonObject properties ) {
    JsonValue selectionIndex = properties.get( PROP_SELECTION_INDEX );
    if( selectionIndex != null ) {
      combo.select( selectionIndex.asInt() );
    }
  }

  /*
   * PROTOCOL SET listVisible
   *
   * @param listVisible (boolean) the visibility state of the list
   */
  public void handleSetListVisible( Combo combo, JsonObject properties ) {
    JsonValue listVisible = properties.get( PROP_LIST_VISIBLE );
    if( listVisible != null ) {
      combo.setListVisible( listVisible.asBoolean() );
    }
  }

  /*
   * PROTOCOL SET text
   *
   * @param text (string) the text
   */
  public void handleSetText( final Combo combo, JsonObject properties ) {
    JsonValue value = properties.get( PROP_TEXT );
    if( value != null ) {
      final String text = value.asString();
      if( isListening( combo, SWT.Verify ) ) {
        // setText needs to be executed in a ProcessAcction runnable as it may
        // fire a VerifyEvent whose fields (text and doit) need to be evaluated
        // before actually setting the new value
        ProcessActionRunner.add( new Runnable() {
          @Override
          public void run() {
            if( !combo.isDisposed() ) {
              setText( combo, text );
              // since text is set in process action, preserved values have to be replaced
              getAdapter( combo ).preserve( PROP_TEXT, text );
            }
         }
        } );
      } else {
        setText( combo, text );
      }
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection ([start, end]) the text selection
   */
  public void handleSetSelection( final Combo combo, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SELECTION );
    if( value != null ) {
      final Point selection = readPoint( value );
      if( isListening( combo, SWT.Verify ) ) {
        // if text is delayed, delay the selection too
        ProcessActionRunner.add( new Runnable() {
          @Override
          public void run() {
            if( !combo.isDisposed() ) {
              combo.setSelection( selection );
              // since selection is set in process action, preserved values have to be replaced
              getAdapter( combo ).preserve( PROP_SELECTION, combo.getSelection() );
            }
          }
        } );
      } else {
        combo.setSelection( selection );
      }
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( Combo combo, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    combo.notifyListeners( SWT.Selection, event );
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifyDefaultSelection( Combo combo, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.DefaultSelection, properties );
    combo.notifyListeners( SWT.DefaultSelection, event );
  }

  /*
   * PROTOCOL NOTIFY Modify
   * ignored, Modify event is fired when set text
   */
  public void handleNotifyModify() {
  }

  private static void setText( Combo combo, String value ) {
    if( !combo.getText().equals( value ) ) {
      combo.getAdapter( ITextAdapter.class ).setText( value );
    }
  }

}
