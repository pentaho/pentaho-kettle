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
package org.eclipse.swt.internal.widgets.textkit;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;


public class TextOperationHandler extends ControlOperationHandler<Text> {

  private static final String PROP_TEXT = "text";
  private static final String PROP_SELECTION = "selection";

  public TextOperationHandler( Text text ) {
    super( text );
  }

  @Override
  public void handleSet( Text text, JsonObject properties ) {
    super.handleSet( text, properties );
    handleSetText( text, properties );
    handleSetSelection( text, properties );
  }

  @Override
  public void handleNotify( Text text, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( text, properties );
    } else if( EVENT_DEFAULT_SELECTION.equals( eventName ) ) {
      handleNotifyDefaultSelection( text, properties );
    } else if( EVENT_MODIFY.equals( eventName ) ) {
      handleNotifyModify();
    } else {
      super.handleNotify( text, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET text
   *
   * @param text (string) the text
   */
  public void handleSetText( final Text text, JsonObject properties ) {
    JsonValue value = properties.get( PROP_TEXT );
    if( value != null ) {
      final String stringValue = value.asString();
      if( isListening( text, SWT.Verify ) ) {
        // setText needs to be executed in a ProcessAcction runnable as it may
        // fire a VerifyEvent whose fields (text and doit) need to be evaluated
        // before actually setting the new value
        ProcessActionRunner.add( new Runnable() {
          @Override
          public void run() {
            if( !text.isDisposed() ) {
              setText( text, stringValue );
              // since text is set in process action, preserved values have to be replaced
              getAdapter( text ).preserve( PROP_TEXT, stringValue );
            }
          }
        } );
      } else {
        setText( text, stringValue );
      }
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection ([start, end]) the text selection
   */
  public void handleSetSelection( final Text text, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SELECTION );
    if( value != null ) {
      final Point selection = readPoint( value );
      if( isListening( text, SWT.Verify ) ) {
        // if text is delayed, delay the selection too
        ProcessActionRunner.add( new Runnable() {
          @Override
          public void run() {
            if( !text.isDisposed() ) {
              text.setSelection( selection );
              // since selection is set in process action, preserved values have to be replaced
              getAdapter( text ).preserve( PROP_SELECTION, text.getSelection() );
            }
          }
        } );
      } else {
        text.setSelection( selection );
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
  public void handleNotifySelection( Text text, JsonObject properties ) {
    if( ( text.getStyle() & SWT.MULTI ) == 0 ) {
      Event event = createSelectionEvent( SWT.Selection, properties );
      text.notifyListeners( SWT.Selection, event );
    }
  }

  /*
   * PROTOCOL NOTIFY DefaultSelection
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param detail (String) "search" if search button was pressed, "cancel" if cancel button was
   *        pressed
   */
  public void handleNotifyDefaultSelection( Text text, JsonObject properties ) {
    if( ( text.getStyle() & SWT.MULTI ) == 0 ) {
      Event event = createSelectionEvent( SWT.DefaultSelection, properties );
      text.notifyListeners( SWT.DefaultSelection, event );
    }
  }

  /*
   * PROTOCOL NOTIFY Modify
   * ignored, Modify event is fired when set text
   */
  public void handleNotifyModify() {
  }

  private static void setText( Text text, String value ) {
    text.getAdapter( ITextAdapter.class ).setText( value );
  }

}
