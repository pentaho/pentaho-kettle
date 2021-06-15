/*******************************************************************************
 * Copyright (c) 2011, 2018 Rüdiger Herrmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rüdiger Herrmann - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.ToolTip", {

  extend : rwt.widgets.base.Popup,
  include : rwt.animation.VisibilityAnimationMixin,

  construct : function( style ) {
    this.base( arguments );
    this._style = style;
    this.setAutoHide( false );
    this.setDisplay( true );
    this.setVisibility( false );
    this.setAppearance( "tool-tip" );
    var doc = rwt.widgets.base.ClientDocument.getInstance();
    this.setMaxWidth( Math.floor( doc.getClientWidth() * 0.9 ) );
    this.setMaxHeight( Math.floor( doc.getClientHeight() * 0.9 ) );
    this.addToDocument();
    this.addEventListener( "click", this._onClick, this );
    this.addEventListener( "appear", this._update, this );
    this._hideAfterTimeout = false;
    this._messageFont = this._getMessageFont();
    this._contentArea = null;
    this._textArea = null;
    this._image = null;
    this._text = null;
    this._message = null;
    this._markupEnabled = false;
    this._createControls();
  },

  destruct : function() {
    this.removeEventListener( "click", this._onClick, this );
    this._contentArea.dispose();
    this._textArea.dispose();
    this._image.dispose();
    this._text.dispose();
    this._message.dispose();
    this._messageFont = null;
  },

  members : {

    setText : function( value ) {
      var text = value;
      if( !this._markupEnabled ) {
        text = rwt.util.Encoding.escapeText( text, false );
      }
      this._text.setText( text );
      if( this.getVisibility() ) {
        this._update();
      }
    },

    setMessage : function( value ) {
      var message = value;
      if( !this._markupEnabled ) {
        var EncodingUtil = rwt.util.Encoding;
        message = EncodingUtil.escapeText( message, false );
        message = EncodingUtil.replaceNewLines( message, "<br/>" );
      }
      this._message.setText( message );
      if( this.getVisibility() ) {
        this._update();
      }
    },

    setMarkupEnabled : function( value ) {
      this._markupEnabled = value;
    },

    setLocation : function( x, y ) {
      this.setLeft( x );
      this.setTop( y );
    },

    setHideAfterTimeout : function( value ) {
      this._hideAfterTimeout = value;
    },

    setVisible : function( visible ) {
      this.setVisibility( visible );
      if( visible ) {
        this.bringToFront();
        if( this._hideAfterTimeout ) {
          rwt.client.Timer.once( this._hide, this, 5 * 1000 );
        }
      }
    },

    addState : function( state ) {
      this.base( arguments, state );
      this._image.addState( state );
    },

    removeState : function( state ) {
      this.base( arguments, state );
      this._image.removeState( state );
    },

    _createControls : function() {
      this._contentArea = new rwt.widgets.base.BoxLayout( "horizontal" );
      this._contentArea.setWidth( "100%" );
      this._contentArea.setHeight( "100%" );
      this._contentArea.setSpacing( 5 );
      this._contentArea.setOverflow( "hidden" );
      this._contentArea.setParent( this );
      this._image = new rwt.widgets.base.Image();
      this._image.setAppearance( "tool-tip-image" );
      this._image.setParent( this._contentArea );
      this._image.addEventListener( "changeLoaded", this._update, this );
      this._textArea = new rwt.widgets.base.BoxLayout( "vertical" );
      this._textArea.setParent( this._contentArea );
      this._textArea.setHeight( "100%" );
      this._textArea.setSpacing( 5 );
      this._text = new rwt.widgets.base.Label();
      this._text.setAppearance( "tool-tip-text" );
      this._text.setParent( this._textArea );
      this._message = new rwt.widgets.base.Label();
      this._message.setAppearance( "tool-tip-message" );
      this._message.setHeight( "auto" );
      this._message.setWrap( true );
      this._message.setParent( this._textArea );
    },

    _update : function() {
      var popupWidth = this.getWidth();
      if( popupWidth === "auto" ) {
        var message = this._message.getText();
        var textSize = this._getTextSize( this._text.getText(), -1 );
        var messageSize = this._getTextSize( message, -1 );
        if( !this._markupEnabled ) {
          var width = messageSize.x;
          while( width > 0 && !this._matchesWidthToHeightRatio( messageSize ) ) {
            width -= 10;
            messageSize = this._getTextSize( message, width );
          }
        }
        messageSize.x = this._max( messageSize.x, textSize.x );
        if( messageSize.x >= this.getMaxWidth() ) {
          messageSize = this._adjustMessageSizeToMaxWidth( message );
        }
        this._message.setWidth( messageSize.x );
        this._message.setHeight( messageSize.y );
      } else {
        var messageWidth = popupWidth - this.getPaddingLeft() - this.getPaddingRight();
        messageWidth -= this._image._computePreferredInnerWidth();
        messageWidth -= this._contentArea.getSpacing();
        this._message.setWidth( messageWidth );
        this._message.setHeight( "auto" );
      }
    },

    _adjustMessageSizeToMaxWidth : function( message ) {
      var preferredMaxWidth = Math.floor( this.getMaxWidth() * 0.9 );
      var messageWidth = preferredMaxWidth - this.getPaddingLeft() - this.getPaddingRight();
      messageWidth -= this._image._computePreferredInnerWidth();
      messageWidth -= this._contentArea.getSpacing();
      return this._getTextSize( message, messageWidth );
    },

    _matchesWidthToHeightRatio : function( size ) {
      return size.x / size.y <= 6;
    },

    _max : function( a, b ) {
      return a > b ? a : b;
    },

    _getTextSize : function( text, width ) {
      var data = [];
      data[ 0 ] = "";
      data[ 1 ] = text;
      data[ 2 ] = this._messageFont.getFamily();
      data[ 3 ] = this._messageFont.getSize();
      data[ 4 ] = this._messageFont.getBold();
      data[ 5 ] = this._messageFont.getItalic();
      data[ 6 ] = width;
      var textSize = rwt.widgets.util.FontSizeCalculation._measureItem( data );
      return {
        x : textSize[ 0 ],
        y : textSize[ 1 ]
      };
    },

    _onClick : function() {
      this._hide();
      rwt.remote.EventUtil.notifySelected( this );
    },

    _hide : function() {
      if( this.getVisibility() ) {
        this.setVisible( false );
        rwt.widgets.base.Widget.flushGlobalQueues();
        rwt.remote.Connection.getInstance().getRemoteObject( this ).set( "visible", false );
      }
    },

    _getMessageFont : function() {
      var tv = new rwt.theme.ThemeValues( {} );
      return tv.getCssFont( "ToolTip-Message", "font" );
    }

  }

} );
