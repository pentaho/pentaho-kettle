/*******************************************************************************
 * Copyright: 2004, 2017 1&1 Internet AG, Germany, http://www.1und1.de,
 *                       and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.base.BasicText", {

  extend : rwt.widgets.base.Terminator,

  construct : function( value ) {
    this.base( arguments );
    if( value != null ) {
      this.setValue( value );
    }
    this.initWidth();
    this.initHeight();
    this.initTabIndex();
    this._selectionStart = 0;
    this._selectionLength = 0;
    this.__oninput = rwt.util.Functions.bind( this._oninput, this );
    this.addEventListener( "blur", this._onblur );
    this.addEventListener( "keydown", this._onkeydown );
    this.addEventListener( "keypress", this._onkeypress );
    this.addEventListener( "keyup", this._onkeyup, this );
    this.addEventListener( "mousedown", this._onMouseDownUp, this );
    this.addEventListener( "mouseup", this._onMouseDownUp, this );
    this._typed = null;
    this._selectionNeedsUpdate = false;
    this._applyBrowserFixes();
    this._inputOverflow = "hidden";
  },

  destruct : function() {
    if( this._inputElement != null ) {
      this._inputElement.removeEventListener( "input", this.__oninput, false );
    }
    this._inputElement = null;
    this.__font = null;
    if( this._checkTimer ) {
      this._checkTimer.dispose();
      this._checkTimer = null;
    }
  },

  events: {
    "input" : "rwt.event.DataEvent"
  },

  properties : {

    allowStretchX : { refine : true, init : true },
    allowStretchY : { refine : true, init : false },
    appearance : { refine : true, init : "text-field" },
    tabIndex : { refine : true, init : 1 },
    hideFocus : { refine : true, init : true },
    width : { refine : true, init : "auto" },
    height : { refine : true, init : "auto" },
    selectable : { refine : true, init : true },

    value : {
      init : "",
      nullable : true,
      event : "changeValue",
      apply : "_applyValue",
      dispose : true // in the case we use i18n text here
    },

    textAlign : {
      check : [ "left", "center", "right", "justify" ],
      nullable : true,
      themeable : true,
      apply : "_applyTextAlign"
    },

    maxLength : {
      check : "Integer",
      apply : "_applyMaxLength",
      nullable : true
    },

    readOnly : {
      check : "Boolean",
      apply : "_applyReadOnly",
      init : false,
      event : "changeReadOnly"
    }

  },

  members : {
    _inputTag : "input",
    _inputType : "text",
    _inputElement : null,

    /////////
    // API

    setSelection : function( selection ) {
      this._selectionStart = selection[ 0 ];
      this._selectionLength = selection[ 1 ] - selection[ 0 ];
      this._renderSelection();
    },

    getSelection : function() {
      return [ this._selectionStart, this._selectionStart + this._selectionLength ];
    },

    getComputedSelection : function() {
      var start = this._getSelectionStart();
      var length = this._getSelectionLength();
      return [ start, start + length ];
    },

    getComputedValue : function() {
      var result;
      if( this.isCreated() ) {
        result = this._inputElement.value;
      } else {
        result = this.getValue();
      }
      return result;
    },

    getInputElement : function() {
      if( !this._inputElement && !this.isDisposed() ) {
        this._inputElement = document.createElement( this._inputTag );
        if( this._inputType ) {
          this._inputElement.type = this._inputType;
        }
        this._inputElement.style.position = "absolute";
        this._inputElement.autoComplete = "off";
        this._inputElement.setAttribute( "autoComplete", "off" );
      }
      return this._inputElement || null;
    },

    /////////////////////
    // selection handling

    _renderSelection : function() {
      // setting selection here might de-select all other selections, so only render if focused
      if( this.isCreated() && ( this.getFocused() || this.getParent().getFocused() ) ) {
        this._setSelectionStart( this._selectionStart );
        this._setSelectionLength( this._selectionLength );
        this._selectionNeedsUpdate = false;
      }
    },

    _detectSelectionChange : function() {
      if( this._isCreated ) {
        var start = this._getSelectionStart();
        var length = this._getSelectionLength();
        if( typeof start === "undefined" ) {
          start = 0;
        }
        if( typeof length === "undefined" ) {
          length = 0;
        }
        if( this._selectionStart !== start || this._selectionLength !== length ) {
          this._handleSelectionChange( start, length );
        }
      }
    },

    _handleSelectionChange : function( start, length ) {
      this._selectionStart = start;
      this._selectionLength = length;
      this.dispatchSimpleEvent( "selectionChanged" );
    },

    _setSelectionStart : function( vStart ) {
      this._visualPropertyCheck();
      // the try catch blocks are neccesary because FireFox raises an exception
      // if the property "selectionStart" is read while the element or one of
      // its parent elements is invisible
      // https://bugzilla.mozilla.org/show_bug.cgi?id=329354
      try {
        if( this._inputElement.selectionStart !== vStart ) {
          this._inputElement.selectionStart = vStart;
        }
      } catch(ex ) {
        // do nothing
      }
    },

    _getSelectionStart : function() {
      this._visualPropertyCheck();
      try {
        if( this.isValidString( this._inputElement.value ) ) {
          return this._inputElement.selectionStart;
        } else {
          return 0;
        }
      } catch( ex ) {
        return 0;
      }
    },

    _setSelectionLength : function( length ) {
      this._visualPropertyCheck();
      try {
        if( this.isValidString( this._inputElement.value ) ) {
          var end = this._inputElement.selectionStart + length;
          if( this._inputElement.selectionEnd != end ) {
            this._inputElement.selectionEnd = end;
          }
        }
      } catch( ex ) {
        // do nothing
      }
    },

    _getSelectionLength : function() {
      this._visualPropertyCheck();
      try {
        return this._inputElement.selectionEnd - this._inputElement.selectionStart;
      } catch( ex ) {
        return 0;
      }
    },

    selectAll : function() {
      this._visualPropertyCheck();
      if( this.getValue() != null ) {
        this._setSelectionStart( 0 );
        this._setSelectionLength( this._inputElement.value.length );
      }
      // to be sure we get the element selected
      this._inputElement.select();
      // RAP [if] focus() leads to error in IE if the _inputElement is disabled or not visible.
      // 277444: JavaScript error in IE when using setSelection on a ComboViewer with setEnabled
      // is false
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=277444
      // 280420: [Combo] JavaScript error in IE when using setSelection on an invisible Combo
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=280420
      if( this.isEnabled() && this.isSeeable() ) {
        this._inputElement.focus();
      }
      this._detectSelectionChange();
    },

    ////////////
    // rendering

    _applyElement : function( value, old ) {
      this.base( arguments, value, old );
      if( value ) {
        var inputElement = this.getInputElement();
        inputElement.disabled = this.getEnabled() === false;
        inputElement.readOnly = this.getReadOnly();
        inputElement.value = this.getValue() != null ? this.getValue().toString() : "";
        if( this.getMaxLength() != null ) {
          inputElement.maxLength = this.getMaxLength();
        }
        inputElement.style.padding = 0;
        inputElement.style.margin = 0;
        inputElement.style.border = "0 none";
        inputElement.style.background = "transparent";
        // See Bug 419676: [Text] Input element of Text field may not be vertically centered in IE
        if( rwt.client.Client.isTrident() ) {
          inputElement.style.verticalAlign = "top";
        }
        inputElement.style.overflow = this._inputOverflow;
        inputElement.style.outline = "none";
        inputElement.style.resize = "none";
        inputElement.style.WebkitAppearance = "none";
        inputElement.style.MozAppearance = "none";
        this._renderFont();
        this._renderTextColor();
        this._renderTextAlign();
        this._renderCursor();
        this._renderTextShadow();
        this._textInit();
        this._getTargetNode().appendChild( this._inputElement );
      }
    },

    _textInit : function() {
      this._inputElement.addEventListener( "input", this.__oninput, false );
      this._applyBrowserFixesOnCreate();
    },

    _postApply : function() {
      this._syncFieldWidth();
      this._syncFieldHeight();
    },

    _changeInnerWidth : function() {
      this._syncFieldWidth();
    },

    _changeInnerHeight : function() {
      this._syncFieldHeight();
      this._centerFieldVertically();
    },

    _syncFieldWidth : function() {
      this._inputElement.style.width = Math.max( 2, this.getInnerWidth() ) + "px";
    },

    _syncFieldHeight : function() {
      if( this._inputTag !== "input" ) {
        this._inputElement.style.height = this.getInnerHeight() + "px";
      }
    },

    _applyCursor : function() {
      if( this.isCreated() ) {
        this._renderCursor();
      }
    },

    _renderCursor : function() {
      var value = this.getCursor();
      if( value ) {
        this._inputElement.style.cursor = value;
      } else {
        this._inputElement.style.cursor = "";
      }
    },

    _applyTextAlign : function() {
      if( this._inputElement ) {
        this._renderTextAlign();
      }
    },

    _renderTextAlign : function() {
      var textAlign = this.getTextAlign();
      if( this.getDirection() === "rtl" ) {
        if( textAlign === "left" ) {
          textAlign = "right";
        } else if( textAlign === "right" ) {
          textAlign = "left";
        }
      }
      this._inputElement.style.textAlign = textAlign || "";
    },

    _applyEnabled : function( value, old ) {
      if( this.isCreated() ) {
        this._inputElement.disabled = value === false;
      }
      return this.base( arguments, value, old );
    },

    _applyValue : function() {
      this._renderValue();
      this._detectSelectionChange();
    },

    _renderValue : function() {
      this._inValueProperty = true;
      var value = this.getValue();
      if( this.isCreated() ) {
        if (value === null) {
          value = "";
        }
        if( this._inputElement.value !== value ) {
          this._inputElement.value = value;
        }
      }
      delete this._inValueProperty;
    },

    _applyMaxLength : function( value ) {
      if( this._inputElement ) {
        this._inputElement.maxLength = value == null ? "" : value;
      }
    },

    _applyReadOnly : function( value ) {
      if( this._inputElement ) {
        this._inputElement.readOnly = value;
      }
      this.toggleState( "readonly", value );
    },

    _renderTextColor : function() {
      if( this.isCreated() ) {
        var color = this.getEnabled() ? this.getTextColor() : this.__theme$textColor;
        if( this._textColor !== color ) {
          this._textColor = color;
          rwt.html.Style.setStyleProperty( this._inputElement, "color", color || "" );
        }
      }
    },

    _applyFont : function( value ) {
      this._styleFont( value );
    },

    _styleFont : function( value ) {
      this.__font = value;
      this._renderFont();
    },

    _renderFont : function() {
      if( this.isCreated() ) {
        if( this.__font != null ) {
          this.__font.renderElement( this._inputElement );
        } else {
          rwt.html.Font.resetElement( this._inputElement );
        }
      }
    },

    _applyTextShadow : function( value ) {
      this.__textShadow = value;
      if( this._inputElement ) {
        this._renderTextShadow();
      }
    },

    _renderTextShadow : function() {
      rwt.html.Style.setTextShadow( this._inputElement, this.__textShadow );
    },

    _visualizeFocus : function() {
      this.base( arguments );
      if( !rwt.widgets.util.FocusHandler.blockFocus ) {
        try {
          this._inputElement.focus();
        } catch( ex ) {
          // ignore
        }
      }
    },

    _visualizeBlur : function() {
      this.base( arguments );
      try {
        this._inputElement.blur();
      } catch( ex ) {
        // ignore
      }
    },

    _afterAppear : function() {
      this.base( arguments );
      this._centerFieldVertically();
      this._renderSelection();
    },


    _centerFieldVertically : function() {
      if( this._inputTag === "input" && this._inputElement ) {
        var innerHeight = this.getInnerHeight();
        var inputElementHeight = this._inputElement.offsetHeight;
        if( inputElementHeight !== 0 ) {
          var top = ( innerHeight - inputElementHeight ) / 2;
          if( top < 0 ) {
            top = 0;
          }
          this._inputElement.style.top = Math.floor( top ) + "px";
        }
      }
    },

    ////////////////
    // event handler

    _oninput : function() {
      try {
        var doit = true;
        if( this.hasEventListeners( "input" ) ) {
          doit = this.dispatchEvent( new rwt.event.DataEvent( "input", this._typed ), true );
        }
        if( doit ) {
          // at least webkit does sometiems fire "input" before the selection is updated
          rwt.client.Timer.once( this._updateValueProperty, this, 0 );
        } else if( rwt.client.Client.isWebkit() || rwt.client.Client.isBlink() ) {
          // some browser set new selection after input event, ignoring all changes before that
          rwt.client.Timer.once( this._renderSelection, this, 0 );
          this._selectionNeedsUpdate = true;
        }
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },

    _updateValueProperty : function() {
      if( !this.isDisposed() ) {
        this.setValue( this.getComputedValue().toString() );
      }
    },

    _ontabfocus : function() {
      this.selectAll();
    },

    _applyFocused : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      if( newValue && !rwt.widgets.util.FocusHandler.mouseFocus ) {
        this._renderSelection();
      }
    },

    _onblur : function() {
      // RAP workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=201080
      if( this.getParent() != null ) {
        this._setSelectionLength( 0 );
      }
    },

    // [rst] Catch backspace in readonly text fields to prevent browser default
    // action (which commonly triggers a history step back)
    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=178320
    _onkeydown : function( e ) {
      if( e.getKeyIdentifier() == "Backspace" && this.getReadOnly() ) {
        e.preventDefault();
      }
      this._detectSelectionChange();
      this._typed = null;
    },

    // [if] Stops keypress propagation
    // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=335779
    _onkeypress : function( e ) {
      if( e.getKeyIdentifier() !== "Tab" ) {
        e.stopPropagation();
      }
      if( this._selectionNeedsUpdate ) {
        this._renderSelection();
      }
      this._detectSelectionChange();
      this._typed = String.fromCharCode( e.getCharCode() );
    },

    _onkeyup : function() {
      if( this._selectionNeedsUpdate ) {
        this._renderSelection();
      }
      this._detectSelectionChange();
      this._typed = null;
    },

    _onMouseDownUp : function() {
      this._detectSelectionChange();
      this._typed = null;
    },

    /////////////////
    // browser quirks

    _applyBrowserFixes : rwt.util.Variant.select( "qx.client", {
      "default" : function() {},
      "trident" : function() {
        // See Bug 372193 - Text widget: Modify Event not fired for Backspace key in IE
        this._checkTimer = new rwt.client.Timer( 0 );
        this._checkTimer.addEventListener( "interval", this._checkValueChanged, this );
        // For delete, backspace, CTRL+X, etc:
        this.addEventListener( "keypress", this._checkTimer.start, this._checkTimer );
        this.addEventListener( "keyup", this._checkTimer.start, this._checkTimer );
        // For context menu: (might not catch the change instantly
        this.addEventListener( "mousemove", this._checkValueChanged, this );
        this.addEventListener( "mouseout", this._checkValueChanged, this );
        // Backup for all other cases (e.g. menubar):
        this.addEventListener( "blur", this._checkValueChanged, this );
      }
    } ),

    _checkValueChanged : function() {
      this._checkTimer.stop();
      var newValue = this.getComputedValue();
      var oldValue = this.getValue();
      if( newValue !== oldValue ) {
        this._oninput();
      }
    },

    _applyBrowserFixesOnCreate : rwt.util.Variant.select( "qx.client", {
      "default" : function() {},
      "webkit|blink" : function() {
        this.addEventListener( "keydown", this._preventEnter, this );
        this.addEventListener( "keypress", this._preventEnter, this );
        this.addEventListener( "keyup", this._preventEnter, this );
        this._applyIOSFixes();
      }
    } ),

    _applyIOSFixes : function() {
      if( rwt.client.Client.getPlatform() === "ios" ) {
        var onfocus = rwt.util.Functions.bind( function() {
          var control = rwt.remote.WidgetManager.getInstance().findControl( this );
          control.getFocusRoot().setFocusedChild( control );
        }, this );
        this._inputElement.addEventListener( "focus", onfocus, false );
        this.addEventListener( "dispose", function() {
          if( this._inputElement != null ) {
            this._inputElement.removeEventListener( "focus", onfocus, false );
          }
        } );
      }
    },

    _preventEnter : function( event ) {
      if( event.getKeyIdentifier() === "Enter" ) {
        event.preventDefault();
      }
    },

    /////////
    // helper

    isValidString : function( v ) {
      return typeof v === "string" && v !== "";
    }
  }

} );
