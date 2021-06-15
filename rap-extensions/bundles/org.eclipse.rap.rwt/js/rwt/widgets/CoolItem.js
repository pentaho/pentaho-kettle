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

/**
 * The parameter orientation must be one of "vertical" or "horizontal".
 * Note that updateHandleBounds must be called after each size manipulation.
 */
rwt.qx.Class.define( "rwt.widgets.CoolItem", {

  extend : rwt.widgets.base.Parent,

  construct : function( orientation ) {
    this.base( arguments );
    this.setOverflow( "hidden" );
    this.setAppearance( "coolitem" );
    this._orientation = orientation;
    // Create handle to drag this CoolItem around
    this._handle = new rwt.widgets.base.Terminator();
    this._handle.addState( orientation );
    this._handle.setAppearance( "coolitem-handle" );
    //this._handle.setHeight( "100%" );
    this._handle.addEventListener( "mousedown", this._onHandleMouseDown, this );
    this._handle.addEventListener( "mousemove", this._onHandleMouseMove, this );
    this._handle.addEventListener( "mouseup", this._onHandleMouseUp, this );
    this.add( this._handle );
    // buffers zIndex and background during drag to be restored when dropped
    this._bufferedZIndex = null;
    this._bufferedControlZIndex = null;
    this._control = null;
  },

  destruct : function() {
    if( this._handle != null ) {
      this._handle.removeEventListener( "mousedown", this._onHandleMouseDown, this );
      this._handle.removeEventListener( "mousemove", this._onHandleMouseMove, this );
      this._handle.removeEventListener( "mouseup", this._onHandleMouseUp, this );
      this._handle.dispose();
    }
  },

  statics : {
    DRAG_CURSOR : "col-resize",
    CONTROL_OFFSET : 6
  },

  members : {

    setLocked : function( value )  {
      this._handle.setDisplay( !value );
    },

    // reparenting to enable coolitem dragging
    setControl : function( control ) {
      if( control != null ) {
        // TODO [tb] : Control positioning is already handled by server
        control.setLeft( this.getLeft() + rwt.widgets.CoolItem.CONTROL_OFFSET );
        control.setDisplay( true );
      }
      if( this._control != null ) {
        this._control.setDisplay( false );
      }
      this._control = control;
    },

    updateHandleBounds : function() {
      if( this._orientation == "vertical" ) {
        this._handle.setWidth( this.getWidth() );
      } else {
        this._handle.setHeight( this.getHeight() );
      }
    },

    _applyParent : function( value, oldValue ) {
      this.base( arguments, value, oldValue );
      if( value != null ) {
        this.setLocked( value.getLocked() );
        this.setDirection( value.getDirection() );
      }
    },

    _onHandleMouseDown : function( evt ) {
      this._handle.setCapture( true );
      this.getTopLevelWidget().setGlobalCursor( rwt.widgets.CoolItem.DRAG_CURSOR );
      this._initialLeft = this.getLeft();
      this._offsetX = evt.getPageX();
      this._bufferedZIndex = this.getZIndex();
      this.setZIndex( 1e7 - 1 );
      if( this._control != null ) {
        this._bufferedControlZIndex = this._control.getZIndex();
        this._control.setZIndex( 1e7 );
      }
      // In some cases the coolItem appeare transparent when dragged around
      // To fix this, walk along the parent hierarchy and use the first explicitly
      // set background color.
      this.setBackgroundColor( this._findBackground() );
    },

    _applyLeft : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      if( this._control != null ) {
        var left = newValue + rwt.widgets.CoolItem.CONTROL_OFFSET;
        this._control.setLeft( left );
      }
    },

    _applyWidth : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      if( this._control != null ) {
        var width = newValue - rwt.widgets.CoolItem.CONTROL_OFFSET;
        this._control.setWidth( width );
      }
    },

    _onHandleMouseMove : function( evt ) {
      if( this._handle.getCapture() ) {
        this.setLeft( this._initialLeft + this._getMouseOffset( evt ) );
      }
    },

    _onHandleMouseUp : function() {
      this._handle.setCapture( false );
      this.setZIndex( this._bufferedZIndex );
      if( this._control != null ) {
        this._control.setZIndex( this._bufferedControlZIndex );
      }
      this.resetBackgroundColor();
      this.getTopLevelWidget().setGlobalCursor( null );
      // Send request that informs about dragged CoolItem
      if( !rwt.remote.EventUtil.getSuspended() ) {
        rwt.remote.Connection.getInstance().getRemoteObject( this ).call( "move", {
          "left" : this.getLeft()
        } );
      }
    },

    _getMouseOffset : function( mouseEvent ) {
      if( this.getDirection() === "rtl" ) {
        return this._offsetX - mouseEvent.getPageX();
      }
      return mouseEvent.getPageX() - this._offsetX;
    },

    _findBackground : function() {
      var hasParent = true;
      var result = null;
      var parent = this.getParent();
      while( hasParent && parent != null && result == null ) {
        if( parent.getBackgroundColor ) {
          result = parent.getBackgroundColor();
        }
        if( parent.getParent ) {
          parent = parent.getParent();
        } else {
          hasParent = false;
        }
      }
      return result;
    }
  }

});
