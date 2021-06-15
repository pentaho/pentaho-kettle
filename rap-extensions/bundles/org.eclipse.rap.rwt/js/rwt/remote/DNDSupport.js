/*******************************************************************************
 * Copyright (c) 2009, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

namespace( "rwt.remote" );

rwt.remote.DNDSupport = function() {
  this._dragSources = {};
  this._dropTargets = {};
  this._eventQueue = {};
  this._requestScheduled = false;
  this._currentDragControl = null;
  this._currentDropControl = null;
  this._currentTargetElement = null;
  this._currentMousePosition = { x : 0, y : 0 };
  this._actionOverwrite = null;
  this._dataTypeOverwrite = null;
  this._dropFeedbackRenderer = null;
  this._dropFeedbackFlags = 0;
  this._dragFeedbackWidget = null;
  this._blockDrag = false;
};

rwt.remote.DNDSupport.getInstance = function() {
  return rwt.runtime.Singletons.get( rwt.remote.DNDSupport );
};

rwt.remote.DNDSupport.prototype = {

  /////////////
  // dragSource

  registerDragSource : function( dragSource ) {
    var control = dragSource.control;
    control.addEventListener( "dragstart", this._dragStartHandler, this );
    control.addEventListener( "dragend", this._dragEndHandler, this );
    this._dragSources[ control.toHashCode() ] = dragSource;
  },

  setDragSourceTransferTypes : function( widget, transferTypes ) {
    this._getDragSource( widget ).dataTypes = transferTypes;
  },

  deregisterDragSource : function( dragSource ) {
    var control = dragSource.control;
    control.removeEventListener( "dragstart", this._dragStartHandler, this );
    control.removeEventListener( "dragend", this._dragEndHandler, this );
    delete this._dragSources[ control.toHashCode() ];
  },

  isDragSource : function( widget ) {
    return typeof this._getDragSource( widget ) != "undefined";
  },

  isDropTarget : function( widget ) {
    return typeof this._getDropTarget( widget ) != "undefined";
  },

  _dragStartHandler : function( event ) {
    var wm = rwt.remote.WidgetManager.getInstance();
    var target = event.getCurrentTarget();
    var control = wm.findControl( event.getTarget() );
    if( control == target && !this._blockDrag ) {
      var dataTypes = this._getDragSource( target ).dataTypes;
      if( dataTypes.length > 0 ) {
        for( var i = 0; i < dataTypes.length; i++ ) {
          event.addData( dataTypes[ i ], true );
        }
        this._actionOverwrite = null;
        this._currentDragControl = target;
        var dndHandler = rwt.event.DragAndDropHandler.getInstance();
        dndHandler.clearActions();
        var doc = rwt.widgets.base.ClientDocument.getInstance();
        doc.addEventListener( "elementOver", this._onMouseOver, this );
        doc.addEventListener( "keydown", this._onKeyEvent, this );
        doc.addEventListener( "keyup", this._onKeyEvent, this );
        this.setCurrentTargetElement( event.getOriginalTarget() );
        // fix for bug 296348
        rwt.widgets.util.WidgetUtil._fakeMouseEvent( this._currentTargetElement, "mouseout" );
        var sourceElement = dndHandler.__dragCache.sourceElement;
        var feedbackWidget = this._getFeedbackWidget( control, sourceElement );
        // Note: Unlike SWT, the feedbackWidget can not be rendered behind
        // the cursor, i.e. with a negative offset, as the widget would
        // get all the mouse-events instead of a potential drop-target.
        dndHandler.setFeedbackWidget( feedbackWidget, 10, 20 );
        event.startDrag();
        event.stopPropagation();
      }
      this._sendDragSourceEvent( target, "DragStart", event.getMouseEvent() );
    }
  },

  _dragEndHandler : function( event ) {
    var target = event.getCurrentTarget();
    var mouseEvent = event.getMouseEvent();
    // fix for Bug 301544: block new dragStarts until request is send
    this._blockDrag = true;
    this._sendDragSourceEvent( target, "DragEnd", mouseEvent );
    this._cleanUp();
    event.stopPropagation();
  },

  _sendDragSourceEvent : function( widget, type, qxDomEvent ) {
    var x = 0;
    var y = 0;
    if( qxDomEvent instanceof rwt.event.MouseEvent ) {
      x = qxDomEvent.getPageX();
      y = qxDomEvent.getPageY();
    }
    var event = {};
    event[ "widget" ] = this._getDragSource( widget );
    event[ "eventName" ] = type;
    event[ "param" ] = {
      "x" : Math.round( x ),
      "y" : Math.round( y ),
      "time" : rwt.remote.EventUtil.eventTimestamp()
    };
    this._eventQueue[ type ] = event;
    var connection = rwt.remote.Connection.getInstance();
    if( !this._requestScheduled ) {
      connection.addEventListener( "send", this._onSend, this );
      this._requestScheduled = true;
    }
    connection.send();
  },

  /////////////
  // dropTarget

  registerDropTarget : function( dropTarget ) {
    var control = dropTarget.control;
    control.addEventListener( "dragover", this._dragOverHandler, this );
    control.addEventListener( "dragmove", this._dragMoveHandler, this );
    control.addEventListener( "dragout", this._dragOutHandler, this );
    control.addEventListener( "dragdrop", this._dragDropHandler, this );
    this._dropTargets[ control.toHashCode() ] = dropTarget;
    control.setSupportsDropMethod( rwt.util.Functions.returnTrue );
  },

  setDropTargetTransferTypes : function( widget, transferTypes ) {
    widget.setDropDataTypes( transferTypes );
  },

  deregisterDropTarget : function( dropTarget ) {
    var control = dropTarget.control;
    control.setDropDataTypes( [] );
    control.removeEventListener( "dragover", this._dragOverHandler, this );
    control.removeEventListener( "dragmove", this._dragMoveHandler, this );
    control.removeEventListener( "dragout", this._dragOutHandler, this );
    control.removeEventListener( "dragdrop", this._dragDropHandler, this );
    delete this._dropTargets[ control.toHashCode() ];
    control.setSupportsDropMethod( null );
  },

  _dragOverHandler : function( event ) {
    var target = event.getCurrentTarget();
    var mouseEvent = event.getMouseEvent();
    this._currentDropControl = target;
    var action = this._computeCurrentAction( mouseEvent, target );
    this._setAction( action, null );
    this._sendDropTargetEvent( target, "DragEnter", mouseEvent, action );
    event.stopPropagation();
  },

  _dragMoveHandler : function( event ) {
    var target = event.getCurrentTarget();
    var mouseEvent = event.getMouseEvent();
    this._currentMousePosition.x = mouseEvent.getPageX();
    this._currentMousePosition.y = mouseEvent.getPageY();
    var action = this._computeCurrentAction( mouseEvent, target );
    this._setAction( action, mouseEvent );
    this._sendDropTargetEvent( target, "DragOver", mouseEvent, action );
    event.stopPropagation();
  },

  _dragOutHandler : function( event ) {
    var target = event.getCurrentTarget();
    var mouseEvent = event.getMouseEvent();
    if( this._currentTargetElement !== mouseEvent.getDomTarget() ) {
      this._onMouseOver( mouseEvent );
    }
    var dndHandler = rwt.event.DragAndDropHandler.getInstance();
    dndHandler.clearActions();
    this.setFeedback( target, null, 0 );
    this._currentDropControl = null;
    this._actionOverwrite = null;
    this._dataTypeOverwrite = null;
    if( this._isEventScheduled( "DragEnter" ) ) {
      this._cancelEvent( "DragEnter" );
      this._cancelEvent( "DragOver" );
    } else {
      this._sendDropTargetEvent( target, "DragLeave", mouseEvent, "none" );
    }
    event.stopPropagation();
  },

  _dragDropHandler : function( event ) {
    var target = event.getCurrentTarget();
    var mouseEvent = event.getMouseEvent();
    var action = this._computeCurrentAction( mouseEvent, target );
    this._sendDropTargetEvent( target, "DropAccept", mouseEvent, action );
    event.stopPropagation();
  },

  _sendDropTargetEvent : function( widget, type, qxDomEvent, action ) {
    var item = this._getCurrentItemTarget();
    var itemId = item != null ? rwt.remote.ObjectRegistry.getId( item ) : null;
    var x = 0;
    var y = 0;
    if( qxDomEvent instanceof rwt.event.MouseEvent ) {
      x = qxDomEvent.getPageX();
      y = qxDomEvent.getPageY();
    } else {
      x = this._currentMousePosition.x;
      y = this._currentMousePosition.y;
    }
    var source = rwt.remote.ObjectRegistry.getId( this._currentDragControl );
    var time = rwt.remote.EventUtil.eventTimestamp();
    var operation = action == "alias" ? "link" : action;
    var event = {};
    event[ "widget" ] = this._getDropTarget( widget );
    event[ "eventName" ] = type;
    event[ "param" ] = {
      "x" : Math.round( x ),
      "y" : Math.round( y ),
      "item" : itemId,
      "operation" : operation,
      "feedback" : this._dropFeedbackFlags,
      "dataType" : this._dataTypeOverwrite,
      "source" : source,
      "time" : time
    };
    this._eventQueue[ type ] = event;
    if( !this._requestScheduled ) {
      var connection = rwt.remote.Connection.getInstance();
      connection.addEventListener( "send", this._onSend, this );
      this._requestScheduled = true;
      rwt.client.Timer.once( connection.send, connection, 200 );
    }
  },

  _isEventScheduled : function( type ) {
    return typeof this._eventQueue[ type ] != "undefined";
  },

  _cancelEvent : function( type ) {
    delete this._eventQueue[ type ];
  },

  _setPropertyRetroactively : function( widget, property, value ) {
    for( var type in this._eventQueue ) {
      var event = this._eventQueue[ type ];
      if( event[ "widget" ].control === widget ) {
        event[ "param" ][ property ] = value;
      }
    }
  },

  _attachEvents : function() {
    var connection = rwt.remote.Connection.getInstance();
    var order = this._getEventOrder( this._eventQueue );
    for( var i = 0; i < order.length; i++ ) {
      var event = this._eventQueue[ order[ i ] ];
      if( event ) {
        connection.getRemoteObject( event.widget ).notify( event.eventName, event.param );
      }
    }
    this._eventQueue = {};
  },

  _getEventOrder : function( events ) {
    var order;
    if( this._isLeaveBeforeEnter( events ) ) {
      order = [ "DragStart", "DragLeave", "DragEnter", "DragOperationChanged", "DragOver",
                "DropAccept", "DragEnd" ];
    } else {
      order = [ "DragStart", "DragEnter", "DragOperationChanged", "DragOver", "DragLeave",
                "DropAccept", "DragEnd" ];
    }
    return order;
  },

  _isLeaveBeforeEnter : function( events ) {
    var leave = events[ "DragLeave" ];
    var enter = events[ "DragEnter" ];
    return leave && enter && leave[ "param" ].time <= enter[ "param" ].time;
  },

  _getCurrentItemTarget : function() {
    var result = null;
    var target = this._getCurrentFeedbackTarget();
    if( target instanceof rwt.widgets.base.GridRow ) {
      var tree = this._currentDropControl;
      result = tree._rowContainer.findItemByRow( target );
    } else {
      result = target;
    }
    return result;
  },

  //////////
  // actions

  _setAction : function( newAction, sourceEvent ) {
    // NOTE: using setCurrentAction would conflict with key events
    var dndHandler = rwt.event.DragAndDropHandler.getInstance();
    var oldAction = dndHandler.getCurrentAction();
    if( oldAction != newAction ) {
      dndHandler.clearActions();
      dndHandler.setAction( newAction );
      if( sourceEvent != null ) {
        this._sendDropTargetEvent( this._currentDropControl,
                                   "DragOperationChanged",
                                   sourceEvent,
                                   newAction );
      }
    }
  },

  _operationsToActions : function( operations ) {
    var result = {};
    for( var i = 0; i < operations.length; i++ ) {
      var action = this._toAction( operations[ i ] );
      result[ action ] = action != null;
    }
    return result;
  },

  _toAction : function( operation ) {
    var result;
    switch( operation ) {
      case "DROP_MOVE":
        result = "move";
      break;
      case "DROP_COPY":
        result = "copy";
      break;
      case "DROP_LINK":
        result = "alias";
      break;
      default:
        result = operation;
      break;
    }
    return result;
  },

  _computeCurrentAction : function( domEvent, target ) {
    var result;
    if( this._actionOverwrite != null ) {
      result = this._actionOverwrite;
    } else {
      result = "move";
      var shift = domEvent.isShiftPressed();
      var ctrl = domEvent.isCtrlPressed();
      var alt = domEvent.isAltPressed();
      if( ctrl && !shift && !alt ) {
        result = "copy";
      } else if( alt && !shift && !ctrl ) {
        result = "alias";
      } else if( !alt && shift && ctrl ) {
        result = "alias";
      }
      var dropActions = this._getDropTarget( target ).actions;
      var dragActions = this._getDragSource( this._currentDragControl ).actions;
      if( !dragActions[ result ] || !dropActions[ result ] ) {
        result = "none";
      }
    }
    return result;
  },

  ///////////
  // feedback

  // TODO [tb] : allow overwrite using DropTarget.setDropTargetEffect?
  /*
   * Creates a feedback-renderer matching the given widget,
   * "implementing" the following interface:
   *  setFeedback : function( feedbackMap )
   *  renderFeedback : function( target )
   *  isFeedbackNode : function( node )
   */
  _createFeedback : function( widget ) {
    if( this._dropFeedbackRenderer == null ) {
      if( widget instanceof rwt.widgets.Grid ) {
        this._dropFeedbackRenderer = new rwt.widgets.util.GridDNDFeedback( widget );
      }
    }
  },

  _renderFeedback : function() {
    if( this._dropFeedbackRenderer != null ) {
      var target = this._getCurrentFeedbackTarget();
      this._dropFeedbackRenderer.renderFeedback( target );
    }
  },

  _getCurrentFeedbackTarget : function() {
    var result = null;
    if( this._currentDropControl instanceof rwt.widgets.Grid ) {
      var element = this._currentTargetElement;
      result = this._currentDropControl.getRowContainer().findRowByElement( element );
    }
    return result;
  },

  // TODO [tb] : allow overwrite using DragSourceEvent.image?
  _getFeedbackWidget : function( sourceControl, sourceElement ) {
    if( this._dragFeedbackWidget == null ) {
      this._dragFeedbackWidget = new rwt.widgets.base.MultiCellWidget( [ "image", "label" ] );
      this._dragFeedbackWidget.setOpacity( 0.7 );
      this._dragFeedbackWidget.setEnabled( false );
      this._dragFeedbackWidget.setPadding( 2 );
    }
    if( sourceControl instanceof rwt.widgets.Grid ) {
      var row = sourceControl.getRowContainer().findRowByElement( sourceElement );
      if( row ) {
        this._configureTreeRowFeedback( row );
        return this._dragFeedbackWidget;
      }
    }
    return null;
  },

  _configureTreeRowFeedback : function( row ) {
    var widget = this._dragFeedbackWidget;
    var tree = this._currentDragControl;
    var item = tree._rowContainer.findItemByRow( row );
    if( item != null ) {
      var config = tree.getRenderConfig();
      var image = item.getImage( config.treeColumn );
      if( image != null ) {
        widget.setCellContent( 0, image[ 0 ] );
        var imageWidth = config.itemImageWidth[ config.treeColumn ];
        widget.setCellDimension( 0, imageWidth, row.getHeight() );
      }
      var backgroundColor = item.getCellBackground( config.treeColumn );
      var textColor = item.getCellForeground( config.treeColumn );
      widget.setBackgroundColor( backgroundColor );
      widget.setTextColor( textColor );
      var text = item.getText( config.treeColumn );
      widget.setCellContent( 1, rwt.util.Encoding.escapeText( text, false ) );
      widget.setFont( config.font );
    }
  },

  _resetFeedbackWidget : function() {
    if( this._dragFeedbackWidget != null ) {
      this._dragFeedbackWidget.setParent( null );
      this._dragFeedbackWidget.setFont( null );
      this._dragFeedbackWidget.setCellContent( 0, null );
      this._dragFeedbackWidget.setCellDimension( 0, null, null );
      this._dragFeedbackWidget.setCellContent( 1, null );
      this._dragFeedbackWidget.setBackgroundColor( null );
    }
  },

  ///////////////
  // eventhandler

  _onSend : function() {
    this._attachEvents();
    this._requestScheduled = false;
    this._blockDrag = false;
    rwt.remote.Connection.getInstance().removeEventListener( "send", this._onSend, this );
  },

  _onMouseOver : function( event ) {
    var target = event.getDomTarget();
    if( this._dropFeedbackRenderer != null ) {
      if( !this._dropFeedbackRenderer.isFeedbackNode( target ) ) {
        this.setCurrentTargetElement( target );
      }
    } else {
      this.setCurrentTargetElement( target );
    }
  },

  setCurrentTargetElement : function( target ) {
    this._currentTargetElement = target;
    this._renderFeedback();
  },

  _onKeyEvent : function( event ) {
    if( event.getType() == "keyup" && event.getKeyIdentifier() == "Alt" ) {
      // NOTE: This combination causes problems with future dom events,
      // so instead we cancel the operation.
      this._sendDragSourceEvent( this._currentDragControl, "DragEnd", event );
      this.cancel();
    } else if( this._currentDropControl != null ) {
      var dndHandler = rwt.event.DragAndDropHandler.getInstance();
      var action = this._computeCurrentAction( event, this._currentDropControl );
      this._setAction( action, event );
      dndHandler._renderCursor();
    }
  },

  /////////
  // helper

  _cleanUp : function() {
    // fix for bug 296348
    var widgetUtil = rwt.widgets.util.WidgetUtil;
    widgetUtil._fakeMouseEvent( this._currentTargetElement, "elementOver" );
    widgetUtil._fakeMouseEvent( this._currentTargetElement, "mouseover" );
    this.setCurrentTargetElement( null );
    if( this._currentDropControl != null) {
      this.setFeedback( this._currentDropControl, null, 0 );
      this._currentDropControl = null;
    }
    var dndHandler = rwt.event.DragAndDropHandler.getInstance();
    dndHandler.setFeedbackWidget( null );
    this._resetFeedbackWidget();
    this._currentDragControl = null;
    this._dataTypeOverwrite = null;
    this._currentMousePosition.x = 0;
    this._currentMousePosition.y = 0;
    var doc = rwt.widgets.base.ClientDocument.getInstance();
    doc.removeEventListener( "elementOver", this._onMouseOver, this );
    doc.removeEventListener( "keydown", this._onKeyEvent, this );
    doc.removeEventListener( "keyup", this._onKeyEvent, this );
  },

  _getDragSource : function( control ) {
    return this._dragSources[ control.toHashCode() ];
  },

  _getDropTarget : function( control ) {
    return this._dropTargets[ control.toHashCode() ];
  },

  //////////////////
  // server response

  cancel : function() {
    if( this._currentDragControl != null ) {
      var dndHandler = rwt.event.DragAndDropHandler.getInstance();
      dndHandler.globalCancelDrag();
      this._cleanUp();
    }
  },

  setOperationOverwrite : function( widget, operation ) {
    if( widget == this._currentDropControl ) {
      var action = this._toAction( operation );
      var dndHandler = rwt.event.DragAndDropHandler.getInstance();
      this._actionOverwrite = action;
      this._setAction( action, null );
      dndHandler._renderCursor();
    }
    this._setPropertyRetroactively( widget, "operation", operation );
  },

  /*
   * feedback is an array of strings with possible values
   * "select", "before", "after", "expand" and "scroll", while
   * flags is the "feedback"-field of SWTs dropTargetEvent,
   * representing the same information as an integer.
   */
  setFeedback : function( widget, feedback, flags ) {
    if( widget == this._currentDropControl ) {
      if( feedback != null ) {
        this._createFeedback( widget );
        if( this._dropFeedbackRenderer != null ) {
          var feedbackMap = {};
          for( var i = 0; i < feedback.length; i++ ) {
            feedbackMap[ feedback[ i ] ] = true;
          }
          this._dropFeedbackRenderer.setFeedback( feedbackMap );
          this._renderFeedback();
        }
      } else if( this._dropFeedbackRenderer != null ) {
        this._dropFeedbackRenderer.dispose();
        this._dropFeedbackRenderer = null;
      }
      this._dropFeedbackFlags = flags;
    }
  },

  setDataType : function( widget, type ) {
    if( widget == this._currentDropControl ) {
      this._dataTypeOverwrite = type;
    }
    this._setPropertyRetroactively( widget, "dataType", type );
  }

};
