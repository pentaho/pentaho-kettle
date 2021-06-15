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
package org.eclipse.swt.internal.dnd.droptargetkit;

import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.swt.internal.dnd.DNDUtil.convertOperations;
import static org.eclipse.swt.internal.dnd.DNDUtil.convertTransferTypes;
import static org.eclipse.swt.internal.dnd.DNDUtil.getDataTypeChangedControl;
import static org.eclipse.swt.internal.dnd.DNDUtil.getDataTypeChangedValue;
import static org.eclipse.swt.internal.dnd.DNDUtil.getDetailChangedControl;
import static org.eclipse.swt.internal.dnd.DNDUtil.getDetailChangedValue;
import static org.eclipse.swt.internal.dnd.DNDUtil.getFeedbackChangedControl;
import static org.eclipse.swt.internal.dnd.DNDUtil.getFeedbackChangedValue;
import static org.eclipse.swt.internal.dnd.DNDUtil.hasDataTypeChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.hasDetailChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.hasFeedbackChanged;
import static org.eclipse.swt.internal.events.EventLCAUtil.isListening;

import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;


public final class DropTargetLCA extends WidgetLCA<DropTarget> {

  public static final DropTargetLCA INSTANCE = new DropTargetLCA();

  private static final String TYPE = "rwt.widgets.DropTarget";
  private static final String PROP_TRANSFER = "transfer";
  private static final String PROP_DRAG_ENTER_LISTENER = "DragEnter";
  private static final String PROP_DRAG_OVER_LISTENER = "DragOver";
  private static final String PROP_DRAG_LEAVE_LISTENER = "DragLeave";
  private static final String PROP_DRAG_OPERATION_CHANGED_LISTENER = "DragOperationChanged";
  private static final String PROP_DROP_ACCEPT_LISTENER = "DropAccept";
  private static final String PROP_FILE_DROP_ENABLED = "fileDropEnabled";

  private static final Transfer[] DEFAULT_TRANSFER = new Transfer[ 0 ];

  @Override
  public void preserveValues( DropTarget dropTarget ) {
    preserveProperty( dropTarget, PROP_TRANSFER, dropTarget.getTransfer() );
    preserveProperty( dropTarget, PROP_FILE_DROP_ENABLED, isFileDropEnabled( dropTarget ) );
    preserveListener( dropTarget,
                      PROP_DRAG_ENTER_LISTENER,
                      isListening( dropTarget, DND.DragEnter ) );
    preserveListener( dropTarget,
                      PROP_DRAG_OVER_LISTENER,
                      isListening( dropTarget, DND.DragOver ) );
    preserveListener( dropTarget,
                      PROP_DRAG_LEAVE_LISTENER,
                      isListening( dropTarget, DND.DragLeave ) );
    preserveListener( dropTarget,
                      PROP_DRAG_OPERATION_CHANGED_LISTENER,
                      isListening( dropTarget, DND.DragOperationChanged ) );
    preserveListener( dropTarget,
                      PROP_DROP_ACCEPT_LISTENER,
                      isListening( dropTarget, DND.DropAccept ) );
  }

  @Override
  public void readData( DropTarget dropTarget ) {
  }

  @Override
  public void renderInitialization( DropTarget dropTarget ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( dropTarget, TYPE );
    remoteObject.setHandler( new DropTargetOperationHandler( dropTarget ) );
    remoteObject.set( "control", getId( dropTarget.getControl() ) );
    remoteObject.set( "style", convertOperations( dropTarget.getStyle() ) );
  }

  @Override
  public void renderChanges( DropTarget dropTarget ) throws IOException {
    renderTransfer( dropTarget );
    renderDetail( dropTarget );
    renderFeedback( dropTarget );
    renderDataType( dropTarget );
    renderFileDropEnabled( dropTarget );
    renderListener( dropTarget,
                    PROP_DRAG_ENTER_LISTENER,
                    isListening( dropTarget, DND.DragEnter ),
                    false );
    renderListener( dropTarget,
                    PROP_DRAG_OVER_LISTENER,
                    isListening( dropTarget, DND.DragOver ),
                    false );
    renderListener( dropTarget,
                    PROP_DRAG_LEAVE_LISTENER,
                    isListening( dropTarget, DND.DragLeave ),
                    false );
    renderListener( dropTarget,
                    PROP_DRAG_OPERATION_CHANGED_LISTENER,
                    isListening( dropTarget, DND.DragOperationChanged ),
                    false );
    renderListener( dropTarget,
                    PROP_DROP_ACCEPT_LISTENER,
                    isListening( dropTarget, DND.DropAccept ),
                    false );
  }

  private static void renderTransfer( DropTarget dropTarget ) {
    Transfer[] newValue = dropTarget.getTransfer();
    if( hasChanged( dropTarget, PROP_TRANSFER, newValue, DEFAULT_TRANSFER ) ) {
      JsonValue renderValue = convertTransferTypes( newValue );
      getRemoteObject( dropTarget ).set( "transfer", renderValue );
    }
  }

  private static void renderDetail( DropTarget dropTarget ) {
    if( hasDetailChanged() && dropTarget.getControl() == getDetailChangedControl() ) {
      JsonArray operations = convertOperations( getDetailChangedValue() );
      JsonValue detail = JsonValue.valueOf( "DROP_NONE" );
      if( !operations.isEmpty() ) {
        detail = operations.get( 0 );
      }
      JsonObject parameters = new JsonObject().add( "detail", detail );
      getRemoteObject( dropTarget ).call( "changeDetail", parameters );
    }
  }

  private static void renderFeedback( DropTarget dropTarget ) {
    if( hasFeedbackChanged() && dropTarget.getControl() == getFeedbackChangedControl() ) {
      JsonObject parameters = new JsonObject()
        .add( "flags", getFeedbackChangedValue() )
        .add( "feedback", convertFeedback( getFeedbackChangedValue() ) );
      getRemoteObject( dropTarget ).call( "changeFeedback", parameters );
    }
  }

  private static void renderDataType( DropTarget dropTarget ) {
    if( hasDataTypeChanged() && dropTarget.getControl() == getDataTypeChangedControl() ) {
      JsonObject parameters = new JsonObject().add( "dataType", getDataTypeChangedValue().type );
      getRemoteObject( dropTarget ).call( "changeDataType", parameters );
    }
  }

  private static void renderFileDropEnabled( DropTarget dropTarget ) {
    boolean value = isFileDropEnabled( dropTarget );
    renderProperty( dropTarget, PROP_FILE_DROP_ENABLED, value, false );
  }

  private static JsonArray convertFeedback( int feedback ) {
    JsonArray feedbackNames = new JsonArray();
    if( ( feedback & DND.FEEDBACK_EXPAND ) != 0 ) {
      feedbackNames.add( "FEEDBACK_EXPAND" );
    }
    if( ( feedback & DND.FEEDBACK_INSERT_AFTER ) != 0 ) {
      feedbackNames.add( "FEEDBACK_INSERT_AFTER" );
    }
    if( ( feedback & DND.FEEDBACK_INSERT_BEFORE ) != 0 ) {
      feedbackNames.add( "FEEDBACK_INSERT_BEFORE" );
    }
    if( ( feedback & DND.FEEDBACK_SCROLL ) != 0 ) {
      feedbackNames.add( "FEEDBACK_SCROLL" );
    }
    if( ( feedback & DND.FEEDBACK_SELECT ) != 0 ) {
      feedbackNames.add( "FEEDBACK_SELECT" );
    }
    return feedbackNames;
  }

  private static boolean isFileDropEnabled( DropTarget target ) {
    Transfer[] transfers = target.getTransfer();
    for( Transfer transfer : transfers ) {
      if( transfer instanceof ClientFileTransfer ) {
        return true;
      }
    }
    return false;
  }

  private DropTargetLCA() {
    // prevent instantiation
  }

}
