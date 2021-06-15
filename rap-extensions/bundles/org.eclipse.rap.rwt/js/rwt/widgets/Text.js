/*******************************************************************************
 * Copyright (c) 2010, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function() {

rwt.qx.Class.define( "rwt.widgets.Text", {

  extend : rwt.widgets.base.BasicText,

  construct : function( isTextarea ) {
    this.base( arguments );
    if( isTextarea ) {
      this._inputTag = "textarea";
      this._inputType = null;
      this._inputOverflow = "auto";
      this.setAppearance( "text-area" );
      this.setAllowStretchY( true );
      this.__oninput = rwt.util.Functions.bind( this._oninputDomTextarea, this );
    }
    this._message = null;
    this._messageElement = null;
    this._searchIconElement = null;
    this._cancelIconElement = null;
  },

  destruct : function() {
    this._messageElement = null;
    this._searchIconElement = null;
    this._cancelIconElement = null;
    this.__oninput = null;
  },

  properties : {

    wrap : {
      check : "Boolean",
      init : true,
      apply : "_applyWrap"
    }

  },

  members : {

    //////
    // API

    setMessage : function( value ) {
      this._message = value ? rwt.util.Encoding.escapeText( value, false ) : null;
      this._updateMessage();
    },

    getMessage : function() {
      return this._message;
    },

    setPasswordMode : function( value ) {
      var type = value ? "password" : "text";
      if( !this._isTextArea() && this._inputType != type ) {
        this._inputType = type;
        if( this._isCreated ) {
          this._inputElement.type = this._inputType;
        }
      }
    },

    ////////////////
    // event handler

    _ontabfocus : function() {
      if( this._isTextArea() ) {
        this._renderSelection();
      } else {
        this.selectAll();
      }
    },

    _onkeydown : function( event ) {
      this.base( arguments, event );
      if(    event.getKeyIdentifier() == "Enter"
          && !event.isShiftPressed()
          && !event.isAltPressed()
          && !event.isCtrlPressed()
          && !event.isMetaPressed() )
      {
        if( this._isTextArea() ) {
          event.stopPropagation();
        }
        if( this._shouldNotifyDefaultSelection() ) {
          rwt.remote.EventUtil.notifyDefaultSelected( this );
        }
      }
    },

    _shouldNotifyDefaultSelection : function() {
      // Emulate SWT (on Windows) where a default button takes precedence over
      // a SelectionListener on a text field when both are on the same shell.
      var shell = rwt.widgets.util.WidgetUtil.getShell( this );
      var defButton = shell ? shell.getDefaultButton() : null;
      var hasDefaultButton = defButton != null && defButton.isSeeable() && defButton.getEnabled();
      return !hasDefaultButton && !this._isTextArea();
    },

    _onMouseDownUp : function( event ) {
      this.base( arguments, event );
      if( event.getType() === "mousedown" ) {
        var target = event.getDomTarget();
        var detail = null;
        if( target === this._searchIconElement ) {
          detail = "search";
        } else if( target === this._cancelIconElement ) {
          this.setValue( "" );
          detail = "cancel";
        }
        if( detail != null ) {
          rwt.remote.EventUtil.notifyDefaultSelected( this, 0, 0, 0, 0, detail );
        }
      }
    },

    ///////////////
    // send changes

    _handleSelectionChange : function( start, length ) {
      this.base( arguments, start, length );
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
        remoteObject.set( "selection", [ start, start + length ] );
      }
    },

    _handleModification : function() {
      var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
      remoteObject.set( "text", this.getComputedValue() );
      this._notifyModify();
      this._detectSelectionChange();
    },

    _notifyModify : function() {
      var connection = rwt.remote.Connection.getInstance();
      if( connection.getRemoteObject( this ).isListening( "Modify" ) ) {
        connection.onNextSend( this._onSend, this );
        connection.sendDelayed( 500 );
      }
    },

    _onSend : function() {
      if( !this.isDisposed() ) {
        rwt.remote.Connection.getInstance().getRemoteObject( this ).notify( "Modify", null, true );
      }
    },

    ///////////////////
    // textarea support

    _applyElement : function( value, oldValue ) {
      this.base( arguments, value, oldValue );
      if( this._isTextArea() ) {
        this._styleWrap();
      }
      var client = rwt.client.Client;
      if( client.isTrident() && client.getVersion() === 9 && this._isTextArea() ) {
        // Bug 427828 - [Text] Loses focus on click in IE8
        // Bug 422974 - [Text] Multi-Line Text with border-radius not focusable by mouse in IE9
        var blank = rwt.remote.Connection.RESOURCE_PATH + "static/image/blank.gif";
        rwt.html.Style.setBackgroundImage( this._inputElement, blank );
      }
      // Fix for bug 306354
      this._inputElement.style.paddingRight = "1px";
      this._updateAllIcons();
      this._updateMessage();
    },

    _webkitMultilineFix : function() {
      if( !this._isTextArea() ) {
        this.base( arguments );
      }
    },

    _applyWrap : function() {
      if( this._isTextArea() ) {
        this._styleWrap();
      }
    },

    _styleWrap : rwt.util.Variant.select( "qx.client", {
      "gecko" : function() {
        if( this._inputElement ) {
          var wrapValue = this.getWrap() ? "soft" : "off";
          var styleValue = this.getWrap() ? "" : "auto";
          this._inputElement.setAttribute( 'wrap', wrapValue );
          this._inputElement.style.overflow = styleValue;
        }
      },
      "default" : function() {
        if( this._inputElement ) {
          var wrapValue = this.getWrap() ? "soft" : "off";
          this._inputElement.setAttribute( 'wrap', wrapValue );
        }
      }
    } ),

    _applyMaxLength : function( value, oldValue ) {
      if( !this._isTextArea() ) {
        this.base( arguments, value, oldValue );
      }
    },

    _oninputDomTextarea : function( event ) {
      try {
        var maxLength = this.getMaxLength();
        var fireEvents = true;
        if( maxLength != null ) {
          var value = this._inputElement.value;
          if( value.length > this.getMaxLength() ) {
            var oldValue = this.getValue();
            // NOTE [tb] : When pasting strings, this might not always
            //             behave like SWT. There is no reliable fix for that.
            var position = this._getSelectionStart();
            if( oldValue.length == ( value.length - 1 ) ) {
              // The user added one character, undo.
              this._inputElement.value = oldValue;
              this._setSelectionStart( position - 1 );
              this._setSelectionLength( 0 );
            } else if( value.length >= oldValue.length && value != oldValue) {
              // The user pasted a string, shorten:
              this._inputElement.value = value.slice( 0, this.getMaxLength() );
              this._setSelectionStart( Math.min( position, this.getMaxLength() ) );
              this._setSelectionLength( 0 );
            }
            if( this._inputElement.value == oldValue ) {
              fireEvents = false;
            }
          }
        }
        if( fireEvents ) {
          this._oninput( event );
        }
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },

    _isTextArea : function() {
      return this._inputTag === "textarea";
    },

    ////////////////
    // icons support

    // overrided
    _syncFieldWidth : function() {
      if( this._inputElement ) {
        var width =   this.getInnerWidth()
                    - this._getIconOuterWidth( "search" )
                    - this._getIconOuterWidth( "cancel" );
        this._inputElement.style.width = Math.max( 2, width ) + "px";
      }
    },

    _syncFieldLeft : function() {
      if( this._inputElement ) {
        var style = this._inputElement.style;
        if( this.getDirection() === "rtl" ) {
          style.marginLeft = "0";
          style.marginRight = this._getIconOuterWidth( "search" ) + "px";
        } else {
          style.marginLeft = this._getIconOuterWidth( "search" ) + "px";
          style.marginRight = "0";
        }
      }
    },

    _updateAllIcons : function() {
      if( this._isCreated ) {
        this._updateIcon( "search" );
        this._updateIcon( "cancel" );
      }
    },

    _updateIcon : function( iconId ) {
      var element = this._getIconElement( iconId );
      if( this._hasIcon( iconId ) && element == null ) {
        element = document.createElement( "div" );
        element.style.position = "absolute";
        element.style.cursor = "pointer";
        this._getTargetNode().insertBefore( element, this._inputElement );
        this._setIconElement( iconId, element );
      }
      if( element ) {
        var image = this._getIconImage( iconId );
        rwt.html.Style.setBackgroundImage( element, image ? image[ 0 ] : null );
      }
      this._layoutIcon( iconId );
    },

    _layoutAllIcons : function() {
      this._layoutIcon( "search" );
      this._layoutIcon( "cancel" );
    },

    _layoutIcon : function( iconId ) {
      var element = this._getIconElement( iconId );
      if( element ) {
        var style = element.style;
        var image = this._getIconImage( iconId );
        var iconWidth = image ? image[ 1 ] : 0;
        var iconHeight = image ? image[ 2 ] : 0;
        var styleMap = this._getMessageStyle();
        style.width = iconWidth + "px";
        style.height = iconHeight + "px";
        style.top = Math.round( this.getInnerHeight() / 2 - iconHeight / 2 ) + "px";
        if( this._getIconPosition( iconId ) === "right" ) {
          style.left = "";
          style.right = styleMap.paddingRight + "px";
        } else {
          style.left = styleMap.paddingLeft + "px";
          style.right = "";
        }
      }
    },

    _getIconElement : function( iconId ) {
      return iconId === "search" ? this._searchIconElement : this._cancelIconElement;
    },

    _setIconElement : function( iconId, element ) {
      if( iconId === "search" ) {
        this._searchIconElement = element;
      } else {
        this._cancelIconElement = element;
      }
    },

    _getIconOuterWidth : function( iconId ) {
      var image = this._getIconImage( iconId );
      if( this._hasIcon( iconId ) && image != null ) {
        return image[ 1 ] + this._getIconSpacing( iconId );
      }
      return 0;
    },

    _hasIcon : function( iconId ) {
      return this.hasState( iconId === "search" ? "rwt_ICON_SEARCH" : "rwt_ICON_CANCEL" );
    },

    _getIconImage : function( iconId ) {
      return this._hasIcon( iconId ) ? this._getIconStyle( iconId ).icon : null;
    },

    _getIconPosition : function( iconId ) {
      var rtl = this.getDirection() === "rtl";
      var searchPos = rtl ? "right" : "left";
      var cancelPos = rtl ? "left" : "right";
      return iconId === "search" ? searchPos : cancelPos;
    },

    _getIconSpacing : function( iconId ) {
      return this._hasIcon( iconId ) ? this._getIconStyle( iconId ).spacing : 0;
    },

    _getIconStyle : function( iconId ) {
      var manager = rwt.theme.AppearanceManager.getInstance();
      var states = {};
      if( iconId === "search" ) {
        states[ "search" ] = true;
      }
      if( this._customVariant !== null ) {
        states[ this._customVariant ] = true;
      }
      return manager.styleFrom( "text-field-icon", states );
    },

    ///////////////////
    // password support

    _reCreateInputField : function() {
      var selectionStart = this._getSelectionStart();
      var selectionLength = this._getSelectionLength();
      this._inputElement.parentNode.removeChild( this._inputElement );
      this._inputElement.onpropertychange = null;
      this._inputElement = null;
      this._firstInputFixApplied = false;
      this._textColor = null;
      this._applyElement( this.getElement(), null );
      this._afterAppear();
      this._postApply();
      this._applyFocused( this.getFocused() );
      this._setSelectionStart( selectionStart );
      this._setSelectionLength( selectionLength );
    },

    //////////////////
    // message support

    _postApply : function() {
      this.base( arguments );
      this._syncFieldLeft();
      this._layoutAllIcons();
      this._layoutMessageX();
    },

    _applyValue : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._updateMessageVisibility();
      if( !rwt.remote.EventUtil.getSuspended() ) {
        this._handleModification();
      }
    },

    _applyFocused : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._updateMessageVisibility();
      if( newValue && ( this.getValue() === "" || this.getValue() == null ) ) {
        this._forceFocus();
      }
    },

    _forceFocus : rwt.util.Variant.select( "qx.client", {
      "webkit|blink" : function() {
        rwt.client.Timer.once( function() {
          if( this._inputElement ) {
            this._inputElement.focus();
          }
        }, this, 1 );
      },
      "default" : function() {
        // nothing to do
      }
    } ),

    _applyCursor : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._updateMessageCursor();
    },

    _applyFont : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      this._updateMessageFont();
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this._applyTextAlign();
      this._syncFieldLeft();
      this._layoutAllIcons();
      this._layoutMessageX();
    },

    // Overwritten
    _preventEnter : function( event ) {
      if( !this._isTextArea() ) {
        this.base( arguments, event );
      }
    },

    _updateMessage : function() {
      if( this._isCreated ) {
        if( this._message != null && this._message !== "" && this._messageElement == null ) {
          this._messageElement = document.createElement( "div" );
          var style = this._messageElement.style;
          style.position = "absolute";
          style.outline = "none";
          style.overflow = "hidden";
          style.whiteSpace = "nowrap";
          var styleMap = this._getMessageStyle();
          style.color = styleMap.textColor || "";
          rwt.html.Style.setTextShadow( this._messageElement, styleMap.textShadow );
          this._getTargetNode().insertBefore( this._messageElement, this._inputElement );
        }
        if( this._messageElement ) {
          this._messageElement.innerHTML = this._message ? this._message : "";
        }
        this._updateMessageCursor();
        this._updateMessageVisibility();
        this._updateMessageFont();
        this._layoutMessageX();
      }
    },

    _centerFieldVertically : function() {
      this.base( arguments );
      this._layoutMessageY();
    },

    _layoutMessageX : function() {
      if( this._messageElement ) {
        var styleMap = this._getMessageStyle();
        var style = this._messageElement.style;
        var width = this.getBoxWidth()
                    - this._cachedBorderLeft
                    - this._cachedBorderRight
                    - styleMap.paddingLeft
                    - styleMap.paddingRight
                    - this._getIconOuterWidth( "search" )
                    - this._getIconOuterWidth( "cancel" );
        style.width = Math.max( 0, width ) + "px";
        if( this._isTextArea() ) {
          // The text-area padding is hard codded in the appearances
          style.left = "3px";
        } else {
          var leftIcon = this.getDirection() === "rtl" ? "cancel" : "search";
          style.left = ( this._getIconOuterWidth( leftIcon ) + styleMap.paddingLeft ) + "px";
        }
      }
    },

    _layoutMessageY : function() {
      if( this._messageElement ) {
        if( this._isTextArea() ) {
          this._messageElement.style.top = "0px";
        } else {
          this._messageElement.style.top = this.getInputElement().style.top;
        }
      }
    },

    _getMessageStyle : function() {
      var manager = rwt.theme.AppearanceManager.getInstance();
      return manager.styleFrom( "text-field-message", {} );
    },

    _updateMessageVisibility : function() {
      if( this._messageElement ) {
        var visible = ( this.getValue() == null || this.getValue() === "" ) && !this.getFocused();
        this._messageElement.style.display = visible ? "" : "none";
      }
    },

    _updateMessageFont : function() {
      if( this._messageElement ) {
        var font = this.getFont();
        font.renderElement( this._messageElement );
      }
    },

    _updateMessageCursor : function() {
      if( this._messageElement ) {
        var cursor = this._inputElement.style.cursor;
        if( cursor == null || cursor === "" ) {
          cursor = "text";
        }
        this._messageElement.style.cursor = cursor;
      }
    }

  }

} );

}() );

