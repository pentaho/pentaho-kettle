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

rwt.qx.Class.define( "rwt.widgets.Spinner", {
  extend : rwt.widgets.base.Spinner,

  construct : function() {
    this.base( arguments );
    this.setWrap( false );
    // Hack to prevent the spinner text field to request the focus
    this._textfield.setFocused = function() {};
    this._textfield.addEventListener( "changeValue", this._onChangeValue, this );
    this._textfield.addEventListener( "keypress", this._onChangeValue, this );
    this._textfield.addEventListener( "blur", this._onChangeValue, this );
    this._textfield.addEventListener( "keydown", this._onKeyDown, this );
    this._textfield.setTabIndex( null );
    this.addEventListener( "changeEnabled", this._onChangeEnabled, this );
    this.addEventListener( "focusout", this._onFocusOut, this );
    this._checkValue = this.__checkValueWithDigits;
  },

  destruct : function() {
    this._textfield.removeEventListener( "changeValue", this._onChangeValue, this );
    this._textfield.removeEventListener( "keypress", this._onChangeValue, this );
    this._textfield.removeEventListener( "blur", this._onChangeValue, this );
    this._textfield.removeEventListener( "keydown", this._onKeyDown, this );
    this.removeEventListener( "changeEnabled", this._onChangeEnabled, this );
    this.removeEventListener( "focusout", this._onFocusOut, this );
  },

  properties : {

    digits : {
      check : "Integer",
      init : 0,
      apply : "_applyDigits"
    },

    decimalSeparator : {
      check : "String",
      init : ".",
      apply : "_applyDecimalSeparator"
    }

  },

  members : {

    _applyTextColor : function( value, old ) {
      this.base( arguments, value, old );
      this._textfield.setTextColor( value );
    },

    setFont : function( value ) {
      this._textfield.setFont( value );
    },

    setMaxLength : function( value ) {
      this._textfield.setMaxLength( value );
    },

    // [if] Spinner#setValues allows minimum, maximum and selection to be set in
    // one hop. In case of not crossed ranges ( for example new min > old max ),
    // a javascript error appears if we set them one by one.
    setMinMaxSelection : function( min, max, value ) {
      this.setMin( Math.min( min, this.getMin() ) );
      this.setMax( Math.max( max, this.getMax() ) );
      this.setValue( value );
      this.setMin( min );
      this.setMax( max );
    },

    addState : function( state ) {
      this.base( arguments, state );
      if( state === "rwt_RIGHT_TO_LEFT" ) {
        this._upbutton.addState( state );
        this._downbutton.addState( state );
        this._textfield.addState( state );
      }
    },

    removeState : function( state ) {
      this.base( arguments, state );
      if( state === "rwt_RIGHT_TO_LEFT" ) {
        this._upbutton.removeState( state );
        this._downbutton.removeState( state );
        this._textfield.removeState( state );
      }
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.setReverseChildrenOrder( value === "rtl" );
      this.setHorizontalChildrenAlign( value === "rtl" ? "right" : "left" );
    },

    _applyCursor : function( value, old ) {
      this.base( arguments, value, old );
      if( value ) {
        this._upbutton.setCursor( value );
        this._downbutton.setCursor( value );
        this._textfield.setCursor( value );
      } else {
        this._upbutton.resetCursor();
        this._downbutton.resetCursor();
        this._textfield.resetCursor();
      }
    },

    _visualizeFocus : function() {
      this._textfield._visualizeFocus();
      if( this._textfield.isCreated() ) {
        this._textfield.selectAll();
      }
      this.addState( "focused" );
    },

    _visualizeBlur : function() {
      // setSelectionLength( 0 ) for TextField - needed for IE
      this._textfield._setSelectionLength( 0 );
      this._textfield._visualizeBlur();
      this.removeState( "focused" );
    },

    // [if] Override original qooxdoo Spinner method. Fix for bug 209476
    _oninput : function() {
      this._suspendTextFieldUpdate = true;
      this._checkValue( true, false );
      this._suspendTextFieldUpdate = false;
    },

    _onChangeValue : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var connection = rwt.remote.Connection.getInstance();
        var remoteObject = connection.getRemoteObject( this );
        remoteObject.set( "selection", this.getManager().getValue() );
        if( remoteObject.isListening( "Selection" ) || remoteObject.isListening( "Modify" ) ) {
          connection.onNextSend( this._onSend, this );
          connection.sendDelayed( 500 );
        }
      }
    },

    // TODO [rst] workaround: setting enabled to false still leaves the buttons enabled
    _onChangeEnabled : function( evt ) {
      var enabled = evt.getValue();
      this._upbutton.setEnabled( enabled && this.getValue() < this.getMax() );
      this._downbutton.setEnabled( enabled && this.getValue() > this.getMin() );
    },

    _onKeyDown : function( event ) {
      if(    event.getKeyIdentifier() == "Enter"
          && !event.isShiftPressed()
          && !event.isAltPressed()
          && !event.isCtrlPressed()
          && !event.isMetaPressed() )
      {
        event.stopPropagation();
        rwt.remote.EventUtil.notifyDefaultSelected( this );
      }
    },

    _onmousewheel : function( evt ) {
      if( this.getFocused() ) {
        this.base( arguments, evt );
      }
    },

    _onSend : function() {
      rwt.remote.EventUtil.notifySelected( this );
      if( !this.isDisposed() ) {
        rwt.remote.Connection.getInstance().getRemoteObject( this ).notify( "Modify", null, true );
      }
    },

    /////////////////
    // Digits support

    _applyDigits : function() {
      var spinnerValue = this.getManager().getValue();
      if( this.getDigits() > 0 ) {
        this._textfield.setValue( this._format( spinnerValue ) );
      } else {
        this._textfield.setValue( String( spinnerValue ) );
      }
    },

    _applyDecimalSeparator : function() {
      var spinnerValue = this.getManager().getValue();
      if( this.getDigits() > 0 ) {
        this._textfield.setValue( this._format( spinnerValue ) );
      }
    },

    _format : function( value ) {
      var digits = this.getDigits();
      var floatValue = value / Math.pow( 10, digits );
      var result = floatValue.toFixed( digits );
      var separator = this.getDecimalSeparator();
      if( separator != "." ) {
        var dot = rwt.util.Encoding.escapeRegexpChars( "." );
        result = result.replace( new RegExp( dot ), separator );
      }
      return result;
    },

    _limit : function( value ) {
      var result = value;
      var digits = this.getDigits();
      if( digits > 0 ) {
        result = result * Math.pow( 10, digits );
      }
      result = Math.round( result );
      if( result > this.getMax() ) {
        result = this.getMax();
      }
      if( result < this.getMin() ) {
        result = this.getMin();
      }
      return result;
    },

    _onFocusOut : function() {
      this._checkValue( true, false );
    },

    _onkeypress : function( evt ) {
      var identifier = evt.getKeyIdentifier();
      var separator = this.getDecimalSeparator();
      if( !( this.getDigits() > 0 && identifier == separator ) ) {
        this.base( arguments, evt );
      }
    },

    _onchange : function() {
      var value = this.getManager().getValue();
      if( !this._suspendTextFieldUpdate ) {
        if( this.getDigits() > 0 ) {
          this._textfield.setValue( this._format( value ) );
        } else {
          this._textfield.setValue( String( value ) );
        }
      }
      if( value == this.getMin() && !this.getWrap() ) {
        this._downbutton.removeState( "pressed" );
        this._downbutton.setEnabled( false );
        this._timer.stop();
      } else {
        this._downbutton.resetEnabled();
      }
      if( value == this.getMax() && !this.getWrap() ) {
        this._upbutton.removeState( "pressed" );
        this._upbutton.setEnabled( false );
        this._timer.stop();
      } else {
        this._upbutton.resetEnabled();
      }
      this.createDispatchDataEvent( "change", value );
    },

    __checkValueWithDigits : function( acceptEmpty, acceptEdit ) {
      var inputElement = this._textfield.getInputElement();
      if( inputElement ) {
        if( inputElement.value === "" && !acceptEmpty ) {
          this.resetValue();
        } else {
          var strValue = inputElement.value;
          var parseValue = strValue;
          var separator = this.getDecimalSeparator();
          if( this.getDigits() > 0 && separator != "." ) {
            separator = rwt.util.Encoding.escapeRegexpChars( separator );
            parseValue = strValue.replace( new RegExp( separator ), "." );
          }
          var value = parseFloat( parseValue );
          var limitedValue = this._limit( value );
          var oldValue = this.getManager().getValue();
          var fixedValue = limitedValue;
          if( isNaN( value ) || value != limitedValue || value != parseValue ) {
            if( acceptEdit ) {
              this._textfield.setValue( this._last_value );
            } else if( isNaN( limitedValue ) ) {
              fixedValue = oldValue;
            }
          }
          if( !acceptEdit ) {
            var formattedValue = String( fixedValue );
            if( this.getDigits() > 0 ) {
              formattedValue = this._format( fixedValue );
            }
            if(    fixedValue === oldValue
                && strValue !== formattedValue
                && !this._suspendTextFieldUpdate )
            {
              this._textfield.setValue( formattedValue );
            }
            this.getManager().setValue( fixedValue );
          }
        }
      }
    }

  }

} );
