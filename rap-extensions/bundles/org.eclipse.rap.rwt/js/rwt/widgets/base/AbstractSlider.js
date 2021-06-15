/*******************************************************************************
 * Copyright (c) 2008, 2019 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.base.AbstractSlider", {

  extend : rwt.widgets.base.Parent,

  construct : function( horizontal ) {
    this.base( arguments );
    this.setOverflow( "hidden" );
    this._horizontal = horizontal;
    // properties (using virtual units):
    this._selection = 0;
    this._minimum = 0;
    this._maximum = 100;
    this._increment = 1;
    this._pageIncrement = 10;
    this._thumbLength = 10;
    // state:
    this._minThumbSize = 0;
    this._thumbLengthPx = 0;
    this._thumbDragOffset = 0;
    this._autoRepeat = ""; // string indicating to auto-repeat an action
    this._mouseOffset = 0; // horizontal or vertical offset to slider start
    this._delayTimer = new rwt.client.Timer( 250 ); // delay auto-repeated actions
    this._repeatTimer = new rwt.client.Timer( 100 ); // for auto-repeated actions
    // subwidgets:
    this._thumb = new rwt.widgets.base.BasicButton( "push", true );
    this._minButton = new rwt.widgets.base.BasicButton( "push", true );
    this._maxButton = new rwt.widgets.base.BasicButton( "push", true );
    this.add( this._thumb );
    this.add( this._minButton );
    this.add( this._maxButton );
    this._minButton.setTabIndex( null );
    this._maxButton.setTabIndex( null );
    this._thumb.setTabIndex( null );
    this._configureAppearance();
    this._setStates();
    this._registerListeners();
    this._setDisplayFor( this._minButton );
    this._setDisplayFor( this._maxButton );
  },

  destruct : function() {
    this._delayTimer.stop();
    this._delayTimer.dispose();
    this._delayTimer = null;
    this._repeatTimer.stop();
    this._repeatTimer.dispose();
    this._repeatTimer = null;
    this._thumb = null;
    this._minButton = null;
    this._maxButton = null;
  },

  members : {

    setThumb : function( value ) {
      if( value <= 0 ) {
        throw new Error( "Scrollbar thumb must be positive" );
      }
      this._thumbLength = value;
      this._renderThumb();
    },

    getThumb : function() {
      return this._thumbLength;
    },

    setMaximum : function( value ) {
      if( value < 0 ) {
        throw new Error( "Scrollbar maximum must be positive" );
      }
      this._maximum = value;
      this._renderThumb();
      this.dispatchSimpleEvent( "changeMaximum" );
    },

    getMaximum : function() {
      return this._maximum;
    },

    setMinimum : function( value ) {
      this._minimum = value;
      this._renderThumb();
      this.dispatchSimpleEvent( "changeMinimum" );
    },

    getMinimum : function() {
      return this._minimum;
    },

    setMinThumbSize : function( value ) {
      this._minThumbSize =  value;
    },

    ////////////
    // protected

    _setSelection : function( value ) {
      var newSelection = this._limitSelection( Math.round( value ) );
      if( newSelection !== this._selection ) {
        this._selection = newSelection;
        this._selectionChanged();
      }
    },

    _setIncrement : function( value ) {
      this._increment = value;
    },

    _setPageIncrement : function( value ) {
      this._pageIncrement = value;
    },

    ////////////////
    // Eventhandlers

    _registerListeners : function() {
      this._repeatTimer.addEventListener( "interval", this._onRepeatTimerInterval, this );
      this._delayTimer.addEventListener( "interval", this._repeatTimerStart, this );
      this.addEventListener( "changeWidth", this._onChangeSize, this );
      this.addEventListener( "changeHeight", this._onChangeSize, this );
      this.addEventListener( "changeEnabled", this._onChangeEnabled, this );
      this.addEventListener( "mousedown", this._onMouseDown, this );
      this.addEventListener( "mouseup", this._onMouseUp, this );
      this.addEventListener( "mouseout",  this._onMouseOut, this );
      this.addEventListener( "mouseover",  this._onMouseOver, this );
      this.addEventListener( "mousemove", this._onMouseMove, this );
      this.addEventListener( "mousewheel", this._onMouseWheel, this );
      this._thumb.addEventListener( "mousedown", this._onThumbMouseDown, this );
      this._thumb.addEventListener( "mousemove", this._onThumbMouseMove, this );
      this._thumb.addEventListener( "mouseup", this._onThumbMouseUp, this );
      this._minButton.addEventListener( "mousedown", this._onMinButtonMouseEvent, this );
      this._maxButton.addEventListener( "mousedown", this._onMaxButtonMouseEvent, this );
      this._minButton.addEventListener( "stateOverChanged", this._onMinButtonMouseEvent, this );
      this._maxButton.addEventListener( "stateOverChanged", this._onMaxButtonMouseEvent, this );

    },

    _selectionChanged : function() {
      this._renderThumb();
      if( this._autoRepeat !== "" && !this._repeatTimer.isEnabled() ) {
        this._delayTimer.start();
      }
      this.dispatchSimpleEvent( "selectionChanged" );
    },

    _onChangeSize : function() {
      this._renderThumb();
    },

    _onChangeEnabled : function( event ) {
      this._thumb.setVisibility( event.getValue() );
    },

    _onMouseWheel : function( event ) {
      if ( event.getTarget() === this ) {
        event.preventDefault();
        event.stopPropagation();
        var data = event.getWheelDelta();
        var change = ( data / Math.abs( data ) ) * this._increment;
        var sel = this._selection - change;
        if( sel < this._minimum ) {
          sel = this._minimum;
        }
        if( sel > ( this._maximum - this._thumbLength ) ) {
          sel = this._maximum - this._thumbLength;
        }
        this._setSelection( sel );
      }
    },

    _onMouseDown : function( event ) {
      if( event.isLeftButtonPressed() ) {
        this._mouseOffset = this._getMouseOffset( event );
        this._handleLineMouseDown();
      }
    },

    _onMouseUp : function() {
      this.setCapture( false );
      this._autoRepeat = "";
      this._delayTimer.stop();
      this._repeatTimer.stop();
    },

    _onMouseOver : function( event ) {
      var target = event.getOriginalTarget();
      if ( target === this && this._autoRepeat.slice( 0, 4 ) === "line" ) {
        this.setCapture( false );
        this._repeatTimerStart();
      }
    },

    _onMouseOut : function( event ) {
      var target = event.getRelatedTarget();
      var outOfSlider = target !== this && !this.contains( target );
      if( outOfSlider && this._autoRepeat.slice( 0, 4 ) === "line" ) {
        this.setCapture( true );
        this._delayTimer.stop();
        this._repeatTimer.stop();
      }
    },

    _onMouseMove : function( event ) {
      this._mouseOffset = this._getMouseOffset( event );
    },

    _onMinButtonMouseEvent : function( event ) {
      event.stopPropagation();
      if( this._minButton.hasState( "pressed" ) ) {
        this._autoRepeat = "minButton";
        this._setSelection( this._selection - this._increment );
      } else {
        this._autoRepeat = "";
      }
    },

    _onMaxButtonMouseEvent : function( event ) {
      event.stopPropagation();
      if( this._maxButton.hasState( "pressed" ) ) {
        this._autoRepeat = "maxButton";
        this._setSelection( this._selection + this._increment );
      } else {
        this._autoRepeat = "";
      }
    },

    _onThumbMouseDown : function( event ) {
      event.stopPropagation();
      this._thumb.addState( "pressed" );
      if( event.isLeftButtonPressed() ) {
        var mousePos = this._getMouseOffset( event );
        this._thumbDragOffset = mousePos - this._getThumbPosition();
        this._thumb.setCapture( true );
      }
    },

    _onThumbMouseMove : function( event ) {
      event.stopPropagation();
      if( this._thumb.getCapture() ) {
        var mousePos = this._getMouseOffset( event );
        var newSelection = this._pxToVirtual( mousePos - this._thumbDragOffset );
        this._setSelection( newSelection );
      }
    },

    _onThumbMouseUp : function( event ) {
      if( this._thumb.hasState( "pressed" ) ) {
        event.stopPropagation();
        this._repeatTimer.stop();
        this._thumb.setCapture( false );
        this._thumb.removeState( "pressed" );
      }
    },

    ////////////
    // Internals

    _layoutX : function() {
      if( this._horizontal ) {
        if( this.getDirection() === "rtl" ) {
          this._minButton.setLeft( null );
          this._minButton.setRight( 0 );
          this._maxButton.setRight( null );
          this._maxButton.setLeft( 0 );
        } else {
          this._minButton.setLeft( 0 );
          this._minButton.setRight( null );
          this._maxButton.setRight( 0 );
          this._maxButton.setLeft( null );
        }
        this._renderThumbPosition();
      } else {
        this._thumb.setWidth( "100%" );
        this._minButton.setWidth( "100%" );
        this._maxButton.setWidth( "100%" );
      }
    },

    _layoutY : function() {
      if( this._horizontal ) {
        this._thumb.setHeight( "100%" );
        this._minButton.setHeight( "100%" );
        this._maxButton.setHeight( "100%" );
      } else {
        this._maxButton.setBottom( 0 );
      }
    },

    addState : function( state ) {
      this.base( arguments, state );
      if( state === "rwt_RIGHT_TO_LEFT" ) {
        this._minButton.addState( state );
        this._maxButton.addState( state );
      }
    },

    removeState : function( state ) {
      this.base( arguments, state );
      if( state === "rwt_RIGHT_TO_LEFT" ) {
        this._minButton.removeState( state );
        this._maxButton.removeState( state );
      }
    },

    _setStates : function() {
      var style = this._horizontal ? "rwt_HORIZONTAL" : "rwt_VERTICAL";
      var state = this._horizontal ? "horizontal" : "vertical";
      this.addState( style );
      this._minButton.addState( style );
      this._minButton.addState( state );
      this._maxButton.addState( style );
      this._maxButton.addState( state );
      this._thumb.addState( style );
      // We need to render appearance now because valid layout values
      // (i.e. a number) might be needed by the constructor
      this._renderAppearance();
      this._minButton._renderAppearance();
      this._maxButton._renderAppearance();
      this._thumb._renderAppearance();
    },

    // overwritten:
    _visualizeFocus : function() {
      this.base( arguments );
      this._thumb.addState( "focused" );
    },

    // overwritten:
    _visualizeBlur : function() {
      this.base( arguments );
      this._thumb.removeState( "focused" );
    },

    _repeatTimerStart : function() {
      this._delayTimer.stop();
      if( this._autoRepeat !== "" ) {
        this._repeatTimer.start();
      }
    },

    _onRepeatTimerInterval : function() {
      switch( this._autoRepeat ) {
        case "minButton":
          this._setSelection( this._selection - this._increment );
        break;
        case "maxButton":
          this._setSelection( this._selection + this._increment );
        break;
        case "linePlus":
        case "lineMinus":
          this._handleLineMouseDown();
        break;
      }
    },

    _handleLineMouseDown : function() {
      var mode;
      var thumbHalf = this._thumbLengthPx / 2;
      var pxSel = this._getThumbPosition() + thumbHalf;
      var newSelection;
      if( this._mouseOffset > pxSel ) {
        newSelection = this._selection + this._pageIncrement;
        mode = "linePlus";
      } else {
        mode = "lineMinus";
        newSelection = this._selection - this._pageIncrement;
      }
      if( this._autoRepeat === "" || this._autoRepeat === mode ) {
        this._autoRepeat = mode;
        this._setSelection( newSelection );
      }
    },

    _renderThumb : function() {
      this._renderThumbSize(); // Size first since it's evaluated by _virtualToPx
      this._renderThumbPosition();
    },

    _renderThumbPosition : function() {
      this._setThumbPositionPx( this._virtualToPx( this._selection ) );
      this.dispatchSimpleEvent( "updateToolTip", this );
    },

    _renderThumbSize : function() {
      var success = false;
      var lineSize = this._getLineSize();
      var diff = this._maximum - this._minimum;
      if( lineSize > 0 && diff > 0 ) {
        var newSize = this._thumbLength * lineSize / diff;
        this._setThumbLengthPx( Math.max( this._minThumbSize, Math.round( newSize ) ) );
        success = true;
      }
      return success;
    },

    _setThumbLengthPx : function( value ) {
      this._thumbLengthPx = value;
      if( this._horizontal ) {
        this._thumb.setWidth( this._thumbLengthPx );
      } else {
        this._thumb.setHeight( this._thumbLengthPx );
      }
    },

    _setThumbPositionPx : function( value ) {
      if( this._horizontal ) {
        if( this.getDirection() === "rtl" ) {
          this._thumb.setLeft( null );
          this._thumb.setRight( value );
        } else {
          this._thumb.setLeft( value );
          this._thumb.setRight( null );
        }
      } else {
        this._thumb.setTop( value );
      }
    },

    _pxToVirtual : function( px ) {
      var buttonSize = this._getMinButtonWidth();
      var result = ( px - buttonSize ) / this._getVirtualToPxRatio() + this._minimum;
      return this._limitSelection( Math.round( result ) );
    },

    _virtualToPx : function( virtual ) {
      return   this._getMinButtonWidth()
             + this._getVirtualToPxRatio() * ( virtual - this._minimum );
    },

    _getVirtualToPxRatio : function() {
      var numPixels = Math.max( 0, this._getLineSize() - this._thumbLengthPx );
      var numVirtual = this._maximum - this._minimum - this._thumbLength;
      return numVirtual === 0 ? 0 : numPixels / numVirtual;
    },

    //////////
    // Helpers

    _limitSelection : function( value ) {
      var result = value;
      if( value >= ( this._maximum - this._thumbLength ) ) {
        result = this._maximum - this._thumbLength;
      }
      if( result <= this._minimum ) {
        result = this._minimum;
      }
      return result;
    },

    _getMouseOffset : function( mouseEvent ) {
      var location = rwt.html.Location;
      if( this._horizontal ) {
        var relativeLeft = mouseEvent.getPageX() - location.getLeft( this.getElement() );
        if( this.getDirection() === "rtl" ) {
          return this._getSliderSize() - relativeLeft;
        } else {
          return relativeLeft;
        }
      } else {
        return mouseEvent.getPageY() - location.getTop( this.getElement() );
      }
    },

    _getThumbPosition : function() {
      if( this._horizontal ) {
        if( this.getDirection() === "rtl" ) {
          return this._thumb.getRight();
        } else {
          return this._thumb.getLeft();
        }
      } else {
        return this._thumb.getTop();
      }
    },

    _getLineSize : function() {
      var buttonSize = this._getMinButtonWidth() + this._getMaxButtonWidth();
      return this._getSliderSize() - this.getFrameWidth() - buttonSize;
    },

    _getSliderSize : function() {
      var result;
      if( this._horizontal ) {
        result = this.getWidth();
      } else {
        result = this.getHeight();
      }
      return result;
    },

    _getMinButtonWidth : function() {
      var result;
      if( this._horizontal ) {
        result = this._minButton.getWidth();
      } else {
        result = this._minButton.getHeight();
      }
      return result;
    },

    _getMaxButtonWidth : function() {
      var result;
      if( this._horizontal ) {
        result = this._maxButton.getWidth();
      } else {
        result = this._maxButton.getHeight();
      }
      return result;
    },

    _setDisplayFor : function( button ) {
      if( button.getImage()[ 0 ] === null ) {
        if( this._horizontal ) {
          button.setWidth( 0 );
        } else {
          button.setHeight( 0 );
        }
        button.setDisplay( false );
      }
    }

  }
} );
