/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Link", {

  extend : rwt.widgets.base.Terminator,

  construct : function() {
    this.base( arguments );
    this.setAppearance( "link" );
    this._text = "";
    this._hyperlinksHaveListeners = false;
    this._readyToSendChanges = true;
    this._focusedLinkIndex = -1;
    this._linksCount = 0;
    this.setSelectable( false );
    this.setHideFocus( true );
    this.__onMouseDown = rwt.util.Functions.bind( this._onMouseDown, this );
    this.__onMouseOver = rwt.util.Functions.bind( this._onMouseOver, this );
    this.__onMouseOut = rwt.util.Functions.bind( this._onMouseOut, this );
    this.__onKeyDown = rwt.util.Functions.bind( this._onKeyDown, this );
    this.addEventListener( "create", this._onCreate, this );
    this.addEventListener( "changeEnabled", this._onChangeEnabled, this );
    this.addEventListener( "keypress", this._onKeyPress );
    this.addEventListener( "focusout", this._onFocusOut );
  },

  destruct : function() {
    this._removeEventListeners();
    delete this.__onMouseDown;
    delete this.__onMouseOver;
    delete this.__onMouseOut;
    delete this.__onKeyDown;
  },

  members : {

    _onCreate : function() {
      this._renderText();
    },

    _applyTextColor : function( value, old ) {
      this.base( arguments, value, old );
      this._applyHyperlinksStyleProperties();
    },

    _onChangeEnabled : function() {
      this._applyHyperlinksStyleProperties();
      this._changeHyperlinksTabIndexProperty();
    },

    _getStates : function() {
      if( !this.__states ) {
        this.__states = {};
      }
      return this.__states;
    },

    addText : function( text ) {
      this._text += text;
    },

    addLink : function( text, index ) {
      var widgetManager = rwt.remote.WidgetManager.getInstance();
      var id = widgetManager.findIdByWidget( this ) + "#" + index;
      this._text += "<span tabIndex=\"1\" ";
      this._text += "id=\"" + id + "\"";
      this._text += ">";
      this._text += text;
      this._text += "</span>";
      this._linksCount++;
    },

    applyText : function() {
      this._renderText();
      if( this._linksCount === 0 ) {
        this.setTabIndex( null );
      } else {
        this.setTabIndex( 1 );
      }
    },

    clear : function() {
      this._removeEventListeners();
      this._text = "";
      this._linksCount = 0;
      this._focusedLinkIndex = -1;
    },

    _renderText : function() {
      if( this._isCreated ) {
        this._getTargetNode().innerHTML = this._text;
        this._applyHyperlinksStyleProperties();
        this._addEventListeners();
      }
    },

    _applyFont : function( value ) {
      if( value ) {
        value.render( this );
      } else {
        rwt.html.Font.reset( this );
      }
    },

    _applyHyperlinksStyleProperties : function() {
      var style = this._getHyperlinkStyle( false );
      var hyperlinks = this._getHyperlinkElements();
      for( var i = 0; i < hyperlinks.length; i++ ) {
        rwt.html.Style.setStyleProperty( hyperlinks[ i ], "color", style.textColor );
        rwt.html.Style.setTextShadow( hyperlinks[ i ], style.textShadow );
        rwt.html.Style.setStyleProperty( hyperlinks[ i ], "cursor", style.cursor );
        rwt.html.Style.setStyleProperty( hyperlinks[ i ], "textDecoration", style.textDecoration );
      }
    },

    _changeHyperlinksTabIndexProperty : function() {
      var hyperlinks = this._getHyperlinkElements();
      for( var i = 0; i < hyperlinks.length; i++ ) {
        if( this.isEnabled() ) {
          hyperlinks[ i ].tabIndex = "1";
        } else {
          hyperlinks[ i ].tabIndex = "-1";
        }
      }
    },

    // TODO [tb] : This is way more complicated than it needs to be.
    //             There is no need to work on DOM-level except when handling the event.
    _addEventListeners : function() {
      var hyperlinks = this._getHyperlinkElements();
      if( hyperlinks.length > 0 && !this._hyperlinksHaveListeners ) {
        for( var i = 0; i < hyperlinks.length; i++ ) {
          hyperlinks[ i ].addEventListener( "mousedown", this.__onMouseDown, false );
          hyperlinks[ i ].addEventListener( "mouseover", this.__onMouseOver, false );
          hyperlinks[ i ].addEventListener( "mouseout", this.__onMouseOut, false );
          hyperlinks[ i ].addEventListener( "keydown", this.__onKeyDown, false );
        }
        this._hyperlinksHaveListeners = true;
      }
    },

    _removeEventListeners : function() {
      var hyperlinks = this._getHyperlinkElements();
      if( hyperlinks.length > 0 && this._hyperlinksHaveListeners ) {
        for( var i = 0; i < hyperlinks.length; i++ ) {
          hyperlinks[ i ].removeEventListener( "mousedown", this.__onMouseDown, false );
          hyperlinks[ i ].removeEventListener( "mouseover", this.__onMouseOver, false );
          hyperlinks[ i ].removeEventListener( "mouseout", this.__onMouseOut, false );
          hyperlinks[ i ].removeEventListener( "keydown", this.__onKeyDown, false );
        }
        this._hyperlinksHaveListeners = false;
      }
    },

    _onMouseDown : function( event ) {
      try {
        if( this.isEnabled() && this._isLeftMouseButtonPressed( event ) ) {
          var index = this._getLinkIndex( event.target );
          this._setFocusedLink( index );
          if( this._readyToSendChanges ) {
            // [if] Fix for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=252559
            this._readyToSendChanges = false;
            rwt.client.Timer.once( function() {
              this._sendChanges( index );
            }, this, rwt.remote.EventUtil.DOUBLE_CLICK_TIME );
          }
        }
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },

    _onMouseOver : function( event ) {
      try {
        var style = this._getHyperlinkStyle( true );
        rwt.html.Style.setStyleProperty( event.target, "textDecoration", style.textDecoration );
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },

    _onMouseOut : function( event ) {
      try {
        var style = this._getHyperlinkStyle( false );
        rwt.html.Style.setStyleProperty( event.target, "textDecoration", style.textDecoration );
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },

    _isLeftMouseButtonPressed : function( event ) {
      var result = false;
      if( event.which ) {
        result = ( event.which === 1 );
      } else if( event.button ) {
        result = ( event.button === 0 );
      }
      return result;
    },

    _onKeyDown : function( event ) {
      try {
        if( this.isEnabled() && event.keyCode === 13 ) {
          var index = this._getLinkIndex( event.target );
          this._sendChanges( index );
        }
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },

    _getLinkIndex : function( element ) {
      var id = element.id;
      var index = id.substr( id.lastIndexOf( "#" ) + 1 );
      return parseInt( index, 10 );
    },

    _getHyperlinkStyle : function( hover ) {
      var states = this._getStates();
      if( hover ) {
        states[ "over" ] = true;
      } else {
        delete states[ "over" ];
      }
      var manager = rwt.theme.AppearanceManager.getInstance();
      return manager.styleFrom( "link-hyperlink", states );
    },

    // Override of the _ontabfocus method from rwt.widgets.base.Widget
    _ontabfocus : function() {
      if( this._focusedLinkIndex === -1 && this._linksCount > 0 ) {
        this._setFocusedLink( 0 );
      }
    },

    _onKeyPress : function( event ) {
      if( this.isFocused() && event.getKeyIdentifier() === "Tab" && this._linksCount > 0 ) {
        var index = this._focusedLinkIndex;
        if( !event.isShiftPressed() && index >= 0 && index < this._linksCount - 1 ) {
          event.stopPropagation();
          event.preventDefault();
          this._setFocusedLink( index + 1 );
        } else if( !event.isShiftPressed() && index === -1 ) {
          event.stopPropagation();
          event.preventDefault();
          this._setFocusedLink( 0 );
        } else if( event.isShiftPressed() && index > 0 && index <= this._linksCount - 1 ) {
          event.stopPropagation();
          event.preventDefault();
          this._setFocusedLink( index - 1 );
        }
      }
    },

    _onFocusOut : function() {
      this._setFocusedLink( -1 );
    },

    _setFocusedLink : function( index ) {
      var hyperlink = this._getFocusedHyperlinkElement();
      if( hyperlink !== null ) {
        hyperlink.blur();
        hyperlink.style.outline = "none";
      }
      this._focusedLinkIndex = index;
      hyperlink = this._getFocusedHyperlinkElement();
      if( hyperlink !== null ) {
        hyperlink.focus();
        hyperlink.style.outline = "1px dotted";
      }
    },

    _getFocusedHyperlinkElement : function() {
      var result = null;
      var hyperlinks = this._getHyperlinkElements();
      var index = this._focusedLinkIndex;
      if( index >= 0 && index < hyperlinks.length ) {
        result = hyperlinks[ index ];
      }
      return result;
    },

    _getHyperlinkElements : function() {
      var result;
      var linkElement = this.getElement();
      if( linkElement ) {
        result = linkElement.getElementsByTagName( "span" );
      } else {
        result = [];
      }
      return result;
    },

    _sendChanges : function( index ) {
      rwt.remote.EventUtil.notifySelected( this, { "index" : index } );
      this._readyToSendChanges = true;
    }

  }

} );
