/*******************************************************************************
 * Copyright (c) 2014, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.dnd.droptargetkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_ITEM;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_TIME;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_X;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_Y;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.find;
import static org.eclipse.swt.internal.dnd.DNDUtil.getDataTypeChangedValue;
import static org.eclipse.swt.internal.dnd.DNDUtil.getDetailChangedValue;
import static org.eclipse.swt.internal.dnd.DNDUtil.getFeedbackChangedValue;
import static org.eclipse.swt.internal.dnd.DNDUtil.hasDataTypeChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.hasDetailChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.hasFeedbackChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.setDataTypeChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.setDetailChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.setFeedbackChanged;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.rap.rwt.internal.client.ClientFileImpl;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.internal.dnd.DNDEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


public class DropTargetOperationHandler extends WidgetOperationHandler<DropTarget> {

  private static final String EVENT_DRAG_ENTER = "DragEnter";
  private static final String EVENT_DRAG_OPERATION_CHANGED = "DragOperationChanged";
  private static final String EVENT_DRAG_OVER = "DragOver";
  private static final String EVENT_DRAG_LEAVE = "DragLeave";
  private static final String EVENT_DROP_ACCEPT = "DropAccept";
  private static final String EVENT_PARAM_OPERATION = "operation";
  private static final String EVENT_PARAM_FEEDBACK = "feedback";
  private static final String EVENT_PARAM_SOURCE = "source";
  private static final String EVENT_PARAM_DATATYPE = "dataType";
  private static final String EVENT_PARAM_FILES = "files";

  public DropTargetOperationHandler( DropTarget dropTarget ) {
    super( dropTarget );
  }

  @Override
  public void handleNotify( DropTarget dropTarget, String eventName, JsonObject properties ) {
    if( EVENT_DRAG_ENTER.equals( eventName ) ) {
      handleNotifyDragEnter( dropTarget, properties );
    } else if( EVENT_DRAG_OPERATION_CHANGED.equals( eventName ) ) {
      handleNotifyDragOperationChanged( dropTarget, properties );
    } else if( EVENT_DRAG_OVER.equals( eventName ) ) {
      handleNotifyDragOver( dropTarget, properties );
    } else if( EVENT_DRAG_LEAVE.equals( eventName ) ) {
      handleNotifyDragLeave( dropTarget, properties );
    } else if( EVENT_DROP_ACCEPT.equals( eventName ) ) {
      handleNotifyDropAccept( dropTarget, properties );
    } else {
      super.handleNotify( dropTarget, eventName, properties );
    }
  }

  /*
   * PROTOCOL NOTIFY DragEnter
   *
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   * @param operation (string) the DND operation, could be "copy", "move" or "link"
   * @param feedback (int) bitwise OR of DND feedback flags as defined in DND class
   * @param item (string) the id of the item that the event occurred in
   * @param source (string) the id of the control, which is currently dragged
   */
  public void handleNotifyDragEnter( final DropTarget dropTarget, final JsonObject properties ) {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        int x = properties.get( EVENT_PARAM_X ).asInt();
        int y = properties.get( EVENT_PARAM_Y ).asInt();
        int time = properties.get( EVENT_PARAM_TIME ).asInt();
        int detail = translateOperation( properties.get( EVENT_PARAM_OPERATION ).asString() );
        int feedback = properties.get( EVENT_PARAM_FEEDBACK ).asInt();
        Item item = getWidget( properties.get( EVENT_PARAM_ITEM ) );
        Control sourceControl = getWidget( properties.get( EVENT_PARAM_SOURCE ) );
        DragSource dragSource = getDragSource( sourceControl );
        int operations = getOperations( dragSource, dropTarget );
        TransferData[] dataTypes = determineDataTypes( dragSource, dropTarget );
        DNDEvent event = createDropTargetEvent( x,
                                                y,
                                                time,
                                                detail,
                                                feedback,
                                                operations,
                                                dataTypes,
                                                dataTypes[ 0 ],
                                                item );
        dropTarget.notifyListeners( DND.DragEnter, event );
        if( event.detail != detail ) {
          changeOperation( dragSource, dropTarget, event.detail );
        }
        // no check, dataType is always changed from null to a valid value:
        changeDataType( dragSource, dropTarget, event.dataType );
        if( event.feedback != feedback ) {
          setFeedbackChanged( dropTarget.getControl(), event.feedback );
        }
      }
    } );
  }

  /*
   * PROTOCOL NOTIFY DragOperationChanged
   *
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   * @param operation (string) the DND operation, could be "copy", "move" or "link"
   * @param feedback (int) bitwise OR of DND feedback flags as defined in DND class
   * @param dataType (int) transfered data type
   * @param item (string) the id of the item that the event occurred in
   * @param source (string) the id of the control, which is currently dragged
   */
  public void handleNotifyDragOperationChanged( final DropTarget dropTarget,
                                                final JsonObject properties )
  {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        int x = properties.get( EVENT_PARAM_X ).asInt();
        int y = properties.get( EVENT_PARAM_Y ).asInt();
        int time = properties.get( EVENT_PARAM_TIME ).asInt();
        int detail = getOperation( properties.get( EVENT_PARAM_OPERATION ) );
        int feedback = getFeedback( properties.get( EVENT_PARAM_FEEDBACK ) );
        TransferData dataType = getDataType( properties.get( EVENT_PARAM_DATATYPE ) );
        Item item = getWidget( properties.get( EVENT_PARAM_ITEM ) );
        Control sourceControl = getWidget( properties.get( EVENT_PARAM_SOURCE ) );
        DragSource dragSource = getDragSource( sourceControl );
        int operations = getOperations( dragSource, dropTarget );
        TransferData[] dataTypes = determineDataTypes( dragSource, dropTarget );
        DNDEvent event = createDropTargetEvent( x,
                                                y,
                                                time,
                                                detail,
                                                feedback,
                                                operations,
                                                dataTypes,
                                                dataType,
                                                item );
        dropTarget.notifyListeners( DND.DragOperationChanged, event );
        if( event.detail != detail ) {
          changeOperation( dragSource, dropTarget, event.detail );
        }
        if( event.dataType != dataType ) {
          changeDataType( dragSource, dropTarget, event.dataType );
        }
        if( event.feedback != feedback ) {
          setFeedbackChanged( dropTarget.getControl(), event.feedback );
        }
      }
    } );
  }

  /*
   * PROTOCOL NOTIFY DragOver
   *
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   * @param operation (string) the DND operation, could be "copy", "move" or "link"
   * @param feedback (int) bitwise OR of DND feedback flags as defined in DND class
   * @param dataType (int) transfered data type
   * @param item (string) the id of the item that the event occurred in
   * @param source (string) the id of the control, which is currently dragged
   */
  public void handleNotifyDragOver( final DropTarget dropTarget, final JsonObject properties ) {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        int x = properties.get( EVENT_PARAM_X ).asInt();
        int y = properties.get( EVENT_PARAM_Y ).asInt();
        int time = properties.get( EVENT_PARAM_TIME ).asInt();
        int detail = getOperation( properties.get( EVENT_PARAM_OPERATION ) );
        int feedback = getFeedback( properties.get( EVENT_PARAM_FEEDBACK ) );
        TransferData dataType = getDataType( properties.get( EVENT_PARAM_DATATYPE ) );
        Item item = getWidget( properties.get( EVENT_PARAM_ITEM ) );
        Control sourceControl = getWidget( properties.get( EVENT_PARAM_SOURCE ) );
        DragSource dragSource = getDragSource( sourceControl );
        int operations = getOperations( dragSource, dropTarget );
        TransferData[] dataTypes = determineDataTypes( dragSource, dropTarget );
        DNDEvent event = createDropTargetEvent( x,
                                                y,
                                                time,
                                                detail,
                                                feedback,
                                                operations,
                                                dataTypes,
                                                dataType,
                                                item );
        dropTarget.notifyListeners( DND.DragOver, event );
        if( event.detail != detail ) {
          changeOperation( dragSource, dropTarget, event.detail );
        }
        if( event.dataType != dataType ) {
          changeDataType( dragSource, dropTarget, event.dataType );
        }
        if( event.feedback != feedback ) {
          setFeedbackChanged( dropTarget.getControl(), event.feedback );
        }
      }
    } );
  }

  /*
   * PROTOCOL NOTIFY DragLeave
   *
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   * @param operation (string) the DND operation, could be "copy", "move" or "link"
   */
  public void handleNotifyDragLeave( final DropTarget dropTarget, final JsonObject properties ) {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        int x = properties.get( EVENT_PARAM_X ).asInt();
        int y = properties.get( EVENT_PARAM_Y ).asInt();
        int time = properties.get( EVENT_PARAM_TIME ).asInt();
        int detail = translateOperation( properties.get( EVENT_PARAM_OPERATION ).asString() );
        DNDEvent event = createDropTargetEvent( x, y, time, detail, 0, 0, null, null, null );
        dropTarget.notifyListeners( DND.DragLeave, event );
      }
    } );
  }

  /*
   * PROTOCOL NOTIFY DropAccept
   *
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   * @param operation (string) the DND operation, could be "copy", "move" or "link"
   * @param dataType (int) transfered data type
   * @param item (string) the id of the item that the event occurred in
   * @param source (string) the id of the control, which is currently dragged
   */
  public void handleNotifyDropAccept( final DropTarget dropTarget, final JsonObject properties ) {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        DropData dropData = createDropData( dropTarget, properties );
        if( !isFileDrop( dropData ) ) { // In this case no DRAG_ENTER was fired by the client
          fireDragLeave( dropTarget, dropData ); // DRAG_LEAVE was suppressed by the client
        }
        fireDropAccept( dropTarget, dropData );
        if( dropData.detail != DND.DROP_NONE && dropData.dataType != null ) {
          if( !isFileDrop( dropData ) ) {
            fireSetData( dropTarget, dropData );
          }
          fireDrop( dropTarget, dropData );
        }
      }
    } );
  }

  private static void fireDragLeave( DropTarget dropTarget, DropData dropData ) {
    DNDEvent leaveEvent = createDropTargetEvent( dropData.x,
                                                 dropData.y,
                                                 dropData.time,
                                                 dropData.detail,
                                                 0,
                                                 0,
                                                 null,
                                                 null,
                                                 null );
    dropTarget.notifyListeners( DND.DragLeave, leaveEvent );
  }

  private static void fireDropAccept( DropTarget dropTarget, DropData dropData ) {
    DNDEvent acceptEvent = createDropTargetEvent( dropData.x,
                                                  dropData.y,
                                                  dropData.time,
                                                  dropData.detail,
                                                  0,
                                                  dropData.operations,
                                                  null,
                                                  dropData.dataType,
                                                  dropData.item );
    dropTarget.notifyListeners( DND.DropAccept, acceptEvent );
    dropData.detail = changeOperation( dropData.dragSource, dropTarget, acceptEvent.detail );
    dropData.dataTypes = determineDataTypes( dropData.dragSource, dropTarget );
    dropData.dataType = checkDataType( acceptEvent.dataType, dropData.dataTypes );
  }

  private static void fireSetData( DropTarget dropTarget, DropData dropData ) {
    DNDEvent setDataEvent = createDragSetDataEvent( dropData.x, dropData.y, dropData.dataType );
    dropData.dragSource.notifyListeners( DND.DragSetData, setDataEvent );
    dropData.data = transferData( dropTarget, dropData.dataType, setDataEvent );
  }

  private static void fireDrop( DropTarget dropTarget, DropData dropData ) {
    DNDEvent dropEvent = createDropEvent( dropData );
    dropEvent.data = dropData.data;
    dropTarget.notifyListeners( DND.Drop, dropEvent );
    changeOperation( dropData.dragSource, dropTarget, dropEvent.detail ); // needed for DRAG_END
  }

  private static boolean isFileDrop( DropData dropData ) {
    return dropData.data instanceof ClientFileImpl[];
  }

  private static DNDEvent createDropEvent( DropData dropData ) {
    return createDropTargetEvent( dropData.x,
                                  dropData.y, 0,
                                  dropData.detail,
                                  0,
                                  dropData.operations,
                                  dropData.dataTypes,
                                  dropData.dataType,
                                  dropData.item );
  }

  private static DropData createDropData( DropTarget dropTarget, JsonObject properties ) {
    DropData dropData = new DropData();
    dropData.data = getClientFiles( properties.get( EVENT_PARAM_FILES ) );
    dropData.x = properties.get( EVENT_PARAM_X ).asInt();
    dropData.y = properties.get( EVENT_PARAM_Y ).asInt();
    dropData.time = properties.get( EVENT_PARAM_TIME ).asInt();
    dropData.detail = getOperation( properties.get( EVENT_PARAM_OPERATION ) );
    dropData.dataType =   dropData.data != null
                        ? getClientFileDataType()
                        : getDataType( properties.get( EVENT_PARAM_DATATYPE ) );
    dropData.dragSource = getDragSource( properties.get( EVENT_PARAM_SOURCE ) );
    dropData.dataTypes = determineDataTypes( dropData.dragSource, dropTarget );
    dropData.item = getWidget( properties.get( EVENT_PARAM_ITEM ) );
    dropData.operations = getOperations( dropData.dragSource, dropTarget );
    return dropData;
  }

  private static ClientFile[] getClientFiles( JsonValue value ) {
    if( value != null ) {
      JsonObject fileProperties = value.asObject();
      List<String> fileIds = fileProperties.names();
      int filecount = fileIds.size();
      ClientFileImpl[] result = new ClientFileImpl[ filecount ];
      for( int i = 0; i < filecount; i++ ) {
        JsonObject file = ( JsonObject )fileProperties.get( fileIds.get( i ) );
        result[ i ] = new ClientFileImpl( fileIds.get( i ),
                                          file.get( "name" ).asString(),
                                          file.get( "type" ).asString(),
                                          file.get( "size" ).asLong() );
      }
      return result;
    }
    return null;
  }

  private static DragSource getDragSource( JsonValue sourceId ) {
    Control sourceControl = getWidget( sourceId );
    DragSource dragSource = sourceControl != null ? getDragSource( sourceControl ) : null;
    return dragSource;
  }

  private static DNDEvent createDropTargetEvent( int x,
                                                 int y,
                                                 int time,
                                                 int detail,
                                                 int feedback,
                                                 int operations,
                                                 TransferData[] dataTypes,
                                                 TransferData dataType,
                                                 Item item )
  {
    DNDEvent event = new DNDEvent();
    event.x = x;
    event.y = y;
    event.time = time;
    event.detail = detail;
    event.feedback = feedback;
    event.operations = operations;
    event.dataTypes = dataTypes;
    event.dataType = dataType;
    event.item = item;
    event.doit = true;
    return event;
  }

  private static DNDEvent createDragSetDataEvent( int x, int y, TransferData dataType ) {
    DNDEvent result = new DNDEvent();
    result.detail = DND.DROP_NONE;
    result.dataType = dataType;
    result.x = x;
    result.y = y;
    result.data = null;
    result.doit = true;
    return result;
  }

  private static int getOperation( JsonValue operation ) {
    int result;
    if( hasDetailChanged() ) {
      result = getDetailChangedValue();
    } else {
      result = translateOperation( operation.asString() );
    }
    return result;
  }

  private static int translateOperation( String operation ) {
    int result = DND.DROP_NONE;
    if( "copy".equals( operation ) ) {
      result = DND.DROP_COPY;
    } else if( "move".equals( operation ) ) {
      result = DND.DROP_MOVE;
    } else if( "link".equals( operation ) ) {
      result = DND.DROP_LINK;
    }
    return result;
  }

  private static int changeOperation( DragSource dragSource, DropTarget dropTarget, int detail ) {
    int checkedOperation = checkOperation( dragSource, dropTarget, detail );
    setDetailChanged( dropTarget.getControl(), checkedOperation );
    return checkedOperation;
  }

  private static int checkOperation( DragSource dragSource, DropTarget dropTarget, int operation ) {
    int allowedOperations = getOperations( dragSource, dropTarget );
    return ( allowedOperations & operation ) != 0 ? operation : DND.DROP_NONE;
  }

  private static int getOperations( DragSource dragSource, DropTarget dropTarget ) {
    if( dragSource == null ) {
      return dropTarget.getStyle();
    }
    return( dragSource.getStyle() & dropTarget.getStyle() );
  }

  private static TransferData getDataType( JsonValue dataType ) {
    TransferData result = null;
    if( hasDataTypeChanged() ) {
      result = getDataTypeChangedValue();
    } else if( dataType != null && !dataType.isNull() ) {
      result = new TransferData();
      result.type = dataType.asInt();
    }
    return result;
  }

  private static TransferData getClientFileDataType() {
    TransferData result = new TransferData();
    result.type = ClientFileTransfer.getInstance().getSupportedTypes()[ 0 ].type;
    return result;
  }

  private static void changeDataType( DragSource dragSource,
                                      DropTarget dropTarget,
                                      TransferData dataType )
  {

    TransferData[] validDataTypes = determineDataTypes( dragSource, dropTarget );
    TransferData value = checkDataType( dataType, validDataTypes );
    // [tb] If the value is not valid, another valid value will be set.
    // This is simplified from SWT, where null would be set.
    if( value == null ) {
      value = validDataTypes[ 0 ];
    }
    setDataTypeChanged( dropTarget.getControl(), value );
  }

  static TransferData[] determineDataTypes( DragSource dragSource, DropTarget dropTarget ) {
    List<TransferData> supportedTypes = new ArrayList<>();
    for( Transfer dropTargetTransfer : dropTarget.getTransfer() ) {
      TransferData[] dataTypes = dropTargetTransfer.getSupportedTypes();
      for( TransferData dataType : dataTypes ) {
        if( dragSource == null || transfersSupport( dragSource.getTransfer(), dataType ) ) {
          supportedTypes.add( dataType );
        }
      }
    }
    return supportedTypes.toArray( new TransferData[ 0 ] );
  }

  private static boolean transfersSupport( Transfer[] transfers, TransferData dataType ) {
    for( Transfer dropTargetTransfer : transfers ) {
      if( dropTargetTransfer.isSupportedType( dataType ) ) {
        return true;
      }
    }
    return false;
  }

  private static TransferData checkDataType( TransferData dataType, TransferData[] validTypes ) {
    for( TransferData validType : validTypes ) {
      if( TransferData.sameType( dataType, validType ) ) {
        return dataType;
      }
    }
    return null;
  }

  private static int getFeedback( JsonValue feedback ) {
    int result;
    if( hasFeedbackChanged() ) {
      result = getFeedbackChangedValue();
    } else {
      result = feedback.asInt();
    }
    return result;
  }

  @SuppressWarnings( "unchecked" )
  private static <T> T getWidget( JsonValue itemId ) {
    if( itemId != null && !itemId.isNull() ) {
      return ( T )findWidgetById( itemId.asString() );
    }
    return null;
  }

  private static Widget findWidgetById( String id ) {
    for( Shell shell : LifeCycleUtil.getSessionDisplay().getShells() ) {
      Widget widget = find( shell, id );
      if( widget != null ) {
        return widget;
      }
    }
    return null;
  }

  private static DragSource getDragSource( Control control ) {
    return ( DragSource )control.getData( DND.DRAG_SOURCE_KEY );
  }

  private static Object transferData( DropTarget dropTarget,
                                      TransferData dataType,
                                      DNDEvent setDataEvent )
  {
    if( setDataEvent.doit ) {
      Transfer transfer = findTransferByType( dataType, dropTarget );
      transfer.javaToNative( setDataEvent.data, dataType );
      return transfer.nativeToJava( dataType );
    }
    return null;
  }

  private static Transfer findTransferByType( TransferData type, DropTarget dropTarget ) {
    for( Transfer supported : dropTarget.getTransfer() ) {
      if( supported.isSupportedType( type ) ) {
        return supported;
      }
    }
    return null;
  }

  private static class DropData {
    int x;
    int y;
    int operations;
    Item item;
    DragSource dragSource;
    TransferData dataType;
    TransferData[] dataTypes;
    int detail;
    int time;
    Object data;
  }

}
