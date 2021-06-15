/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Sash", {

  extend : rwt.widgets.base.Parent,

  include : rwt.widgets.util.OverStateMixin,

  construct : function() {
    this.base( arguments );
    this.setOverflow( null );
    this.setHtmlProperty( "unselectable", "on" );
    this.addEventListener( "changeWidth", this._onChangeSize, this );
    this.addEventListener( "changeHeight", this._onChangeSize, this );
    this._slider = new rwt.widgets.base.Parent();
    this._slider.setAppearance( "sash-slider" );
    this._slider.setVisibility( false );
    this.add( this._slider );
    this._sliderHandle = new rwt.widgets.base.Parent();
    rwt.html.Style.setBackgroundPosition( this._sliderHandle, "center center" );
    this._sliderHandle.setAppearance( "sash-handle" );
    this._sliderHandle.setVisibility( false );
    this.add( this._sliderHandle );
    this._handle = new rwt.widgets.base.Parent();
    rwt.html.Style.setBackgroundPosition( this._handle, "center center" );
    this._handle.setAppearance( "sash-handle" );
    this.add( this._handle );
    this.initOrientation();
    this._bufferZIndex = null;
  },

  destruct : function() {
    this.removeEventListener( "changeWidth", this._onChangeSize, this );
    this.removeEventListener( "changeHeight", this._onChangeSize, this );
    this._removeStyle( this.getOrientation() );
    this._disposeObjects( "_slider", "_handle", "_sliderHandle" );
  },

  properties : {

    appearance : {
      refine : true,
      init : "sash"
    },

    orientation : {
      check : [ "horizontal", "vertical" ],
      apply : "_applyOrientation",
      init : "horizontal",
      nullable : true
    }

  },

  members : {

    _onChangeSize : function() {
      this._handle.setWidth( this.getWidth() );
      this._handle.setHeight( this.getHeight() );
    },

    _onMouseDownX : function( evt ) {
      if( evt.isLeftButtonPressed() ) {
        if( this.getEnabled() ) {
          this._commonMouseDown();
          this._dragOffset = evt.getPageX();
          var rtl = this.getDirection() === "rtl";
          var leftLimit = - this.getLeft() - this._frameOffset;
          var rightLimit = this.getParent().getWidth() - this.getLeft()
                         - this.getWidth() - this._frameOffset;
          this._minMove = rtl ? - rightLimit : leftLimit;
          this._maxMove = rtl ? - leftLimit : rightLimit;
        }
      }
    },

    _onMouseDownY : function( evt ) {
      if( evt.isLeftButtonPressed() ) {
        if( this.getEnabled() ) {
          this._commonMouseDown();
          this._dragOffset = evt.getPageY();
          this._minMove = - this.getTop() - this._frameOffset;
          this._maxMove = this.getParent().getHeight() - this.getTop()
                        - this.getHeight() - this._frameOffset;
        }
      }
    },

    _commonMouseDown : function() {
      this.setCapture( true );
      this.getTopLevelWidget().setGlobalCursor( this.getCursor() );
      // Used to subtract border width
      // Note: Assumes that the Sash border has equal width on all four edges
      this._frameOffset = this.getFrameWidth() / 2;
      this._slider.setLeft( 0 - this._frameOffset );
      this._slider.setTop( 0 - this._frameOffset );
      this._slider.setWidth( this.getWidth() );
      this._slider.setHeight( this.getHeight() );
      this._sliderHandle.setLeft( 0 );
      this._sliderHandle.setTop( 0 );
      this._sliderHandle.setWidth( this.getWidth() );
      this._sliderHandle.setHeight( this.getHeight() );
      this._bufferZIndex = this.getZIndex();
      this.setZIndex( 1e7 );
      this._slider.show();
      this._sliderHandle.show();
      // notify server
      this._sendWidgetSelected();
    },

    _onMouseUpX : function() {
      if( this.getCapture() ) {
        this._commonMouseUp();
      }
    },

    _onMouseUpY : function() {
      if( this.getCapture() ) {
        this._commonMouseUp();
      }
    },

    _commonMouseUp : function() {
      this._slider.hide();
      this._sliderHandle.hide();
      this.setCapture( false );
      this.getTopLevelWidget().setGlobalCursor( null );
      if( this._bufferZIndex != null ) {
        this.setZIndex( this._bufferZIndex );
      }
      var widgetUtil = rwt.widgets.util.WidgetUtil;
      widgetUtil._fakeMouseEvent( this, "mouseout" );
      // notify server
      this._sendWidgetSelected();
    },

    _onMouseMoveX : function( evt ) {
      if( this.getCapture() ) {
        // [if] Global cursor is reset by Request#_hideWaitHint. Set it again.
        if( this.getTopLevelWidget().getGlobalCursor() != this.getCursor() ) {
          this.getTopLevelWidget().setGlobalCursor( this.getCursor() );
        }
        var toMove = this._limitMove( evt.getPageX() - this._dragOffset );
        this._slider.setLeft( toMove );
        this._sliderHandle.setLeft( toMove );
      }
    },

    _onMouseMoveY : function( evt ) {
      if( this.getCapture() ) {
        // [if] Global cursor is reset by Request#_hideWaitHint. Set it again.
        if( this.getTopLevelWidget().getGlobalCursor() != this.getCursor() ) {
          this.getTopLevelWidget().setGlobalCursor( this.getCursor() );
        }
        var toMove = this._limitMove( evt.getPageY() - this._dragOffset );
        this._slider.setTop( toMove );
        this._sliderHandle.setTop( toMove );
      }
    },

    _limitMove : function( toMove ) {
      if( toMove < this._minMove ) {
        return this._minMove;
      } else if( toMove > this._maxMove ) {
        return this._maxMove;
      }
      return toMove;
    },

    _applyOrientation : function( value, old ) {
      this._removeStyle( old );
      this._setStyle( value );
    },

    _setStyle : function( style ) {
      if( style == "horizontal" ) {
        this.addEventListener( "mousedown", this._onMouseDownY, this );
        this.addEventListener( "mousemove", this._onMouseMoveY, this );
        this.addEventListener( "mouseup", this._onMouseUpY, this );
        this.addState( "horizontal" );
        this._handle.addState( "horizontal" );
        this._sliderHandle.addState( "horizontal" );
      } else if( style == "vertical" ) {
        this.addEventListener( "mousemove", this._onMouseMoveX, this );
        this.addEventListener( "mousedown", this._onMouseDownX, this );
        this.addEventListener( "mouseup", this._onMouseUpX, this );
        this.addState( "vertical" );
        this._handle.addState( "vertical" );
        this._sliderHandle.addState( "vertical" );
      }
    },

    _removeStyle : function( style ) {
      if( style == "horizontal" ) {
        this.removeEventListener( "mousedown", this._onMouseDownY, this );
        this.removeEventListener( "mousemove", this._onMouseMoveY, this );
        this.removeEventListener( "mouseup", this._onMouseUpY, this );
        this.removeState( "horizontal" );
        this._handle.removeState( "horizontal" );
        this._sliderHandle.removeState( "horizontal" );
      } else if( style == "vertical" ) {
        this.removeEventListener( "mousedown", this._onMouseDownX, this );
        this.removeEventListener( "mousemove", this._onMouseMoveX, this );
        this.removeEventListener( "mouseup", this._onMouseUpX, this );
        this.removeState( "vertical" );
        this._handle.removeState( "vertical" );
        this._sliderHandle.removeState( "vertical" );
      }
    },

    _sendWidgetSelected : function() {
      var leftOffset = this._slider.getLeft() + this._frameOffset;
      if( this.getDirection() === "rtl" ) {
        leftOffset = -leftOffset;
      }
      var topOffset = this._slider.getTop() + this._frameOffset;
      rwt.remote.EventUtil.notifySelected(
        this,
        this.getLeft() + leftOffset,
        this.getTop() + topOffset,
        this.getWidth(),
        this.getHeight(),
        this.getCapture() ? "drag" : null
       );
    }

  }

} );
