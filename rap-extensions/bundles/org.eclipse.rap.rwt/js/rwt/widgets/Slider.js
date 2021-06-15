/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
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
 * This class provides the client-side counterpart for
 * rwt.widgets.Slider.
 */
rwt.qx.Class.define( "rwt.widgets.Slider", {

  extend : rwt.widgets.base.AbstractSlider,

  construct : function( isHorizontal ) {
    this.base( arguments, isHorizontal );
    this._requestScheduled = false;
    this.addEventListener( "contextmenu", this._onContextMenu, this );
    this.addEventListener( "keypress", this._onKeyPress, this );
  },

  statics : {

    SEND_DELAY : 50,

    _isNoModifierPressed : function( evt ) {
      return    !evt.isCtrlPressed()
             && !evt.isShiftPressed()
             && !evt.isAltPressed()
             && !evt.isMetaPressed();
    }

  },

  members : {

    _configureAppearance : function() {
      this.setAppearance( "slider" );
      this._thumb.setAppearance( "slider-thumb" );
      this._minButton.setAppearance( "slider-min-button" );
      this._maxButton.setAppearance( "slider-max-button" );
    },

    setSelection : function( value ) {
      this._setSelection( value );
    },

    setIncrement : function( value ) {
      this._setIncrement( value );
    },

    setPageIncrement : function( value ) {
      this._setPageIncrement( value );
    },

    getToolTipTargetBounds : function() {
      var xOffset = this._horizontal && this.getDirection() === "rtl"
                  ? this.getWidth() - this._thumb.getRight() - this._thumb.getWidth()
                  : this._thumb.getLeft();
      return {
        "left" : this._cachedBorderLeft + ( xOffset || 0 ),
        "top" : this._cachedBorderLeft + ( this._thumb.getTop() || 0 ),
        "width" : this._thumb.getBoxWidth(),
        "height" : this._thumb.getBoxHeight()
      };
    },

    //////////////
    // Overwritten

    _setSelection : function( value ) {
      this.base( arguments, value );
      this._scheduleSendChanges();
    },

    ////////////
    // Internals

    _onContextMenu : function( evt ) {
      var menu = this.getContextMenu();
      if( menu != null ) {
        menu.setLocation( evt.getPageX(), evt.getPageY() );
        menu.setOpener( this );
        menu.show();
        evt.stopPropagation();
      }
    },

    _onKeyPress : function( evt ) {
      var keyIdentifier = evt.getKeyIdentifier();
      var sel = null;
      if( rwt.widgets.Slider._isNoModifierPressed( evt ) ) {
        switch( keyIdentifier ) {
          case "Left":
            sel = this._selection - this._increment;
            evt.preventDefault();
            evt.stopPropagation();
            break;
          case "Down":
            if( this._horizontal ) {
              sel = this._selection - this._increment;
            } else {
              sel = this._selection + this._increment;
            }
            evt.preventDefault();
            evt.stopPropagation();
            break;
          case "Right":
            sel = this._selection + this._increment;
            evt.preventDefault();
            evt.stopPropagation();
            break;
          case "Up":
            if( this._horizontal ) {
              sel = this._selection + this._increment;
            } else {
              sel = this._selection - this._increment;
            }
            evt.preventDefault();
            evt.stopPropagation();
            break;
          case "Home":
            sel = this._minimum;
            evt.preventDefault();
            evt.stopPropagation();
            break;
          case "End":
            sel = this._maximum;
            evt.preventDefault();
            evt.stopPropagation();
            break;
          case "PageDown":
            if( this._horizontal ) {
              sel = this._selection - this._pageIncrement;
            } else {
              sel = this._selection + this._pageIncrement;
            }
            evt.preventDefault();
            evt.stopPropagation();
            break;
          case "PageUp":
            if( this._horizontal ) {
              sel = this._selection + this._pageIncrement;
            } else {
              sel = this._selection - this._pageIncrement;
            }
            evt.preventDefault();
            evt.stopPropagation();
            break;
        }
        if( sel != null ) {
          if( sel < this._minimum ) {
            sel = this._minimum;
          }
          if( sel > this._maximum ) {
            sel = this._maximum;
          }
          this.setSelection( sel );
          if( this._readyToSendChanges ) {
            this._readyToSendChanges = false;
            // Send changes
            rwt.client.Timer.once( this._sendChanges, this, rwt.widgets.Slider.SEND_DELAY );
          }
        }
      }
    },

    _onMouseWheel : function( evt ) {
      if( this.getFocused() ) {
        this.base( arguments, evt );
        if( this._readyToSendChanges ) {
          this._readyToSendChanges = false;
          // Send changes
          rwt.client.Timer.once( this._sendChanges, this, 500 );
        }
      }
    },

    // TODO [tb] : refactor to use only this for scheduling
    _scheduleSendChanges : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        if( !this._requestScheduled ) {
          this._requestScheduled = true;
          // Send changes
          rwt.client.Timer.once( this._sendChanges, this, rwt.widgets.Slider.SEND_DELAY );

        }
      }
    },

    _sendChanges : function() {
      var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
      remoteObject.set( "selection", this._selection );
      rwt.remote.EventUtil.notifySelected( this );
      this._requestScheduled = false;
    }

  }

} );
