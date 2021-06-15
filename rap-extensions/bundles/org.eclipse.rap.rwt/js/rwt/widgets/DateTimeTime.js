/*******************************************************************************
 * Copyright (c) 2008, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.DateTimeTime", {

  extend : rwt.widgets.base.Parent,

  construct : function( style ) {
    this.base( arguments );
    this.setOverflow( "hidden" );
    this._short = rwt.util.Strings.contains( style, "short" );
    this._medium = rwt.util.Strings.contains( style, "medium" );
    this._long = rwt.util.Strings.contains( style, "long" );
    this.addEventListener( "keypress", this._onKeyPress, this );
    this.addEventListener( "keyup", this._onKeyUp, this );
    this.addEventListener( "mousewheel", this._onMouseWheel, this );
    this.addEventListener( "focus", this._onFocusIn, this );
    this.addEventListener( "blur", this._onFocusOut, this );
    this._timePane = new rwt.widgets.base.Parent();
    this._timePane.setLeft( 0 );
    this._timePane.setHeight( "100%" );
    this.add( this._timePane );
    // Hours
    this._hoursTextField = new rwt.widgets.base.Label( "00" );
    this._hoursTextField.setAppearance( "datetime-field" );
    this._hoursTextField.setUserData( "maxLength", 2 );
    this._hoursTextField.addEventListener( "mousedown",  this._onTextFieldMouseDown, this );
    this._timePane.add(this._hoursTextField);
    // Separator
    this._separator3 = new rwt.widgets.base.Label( ":" );
    this._separator3.setAppearance( "datetime-separator" );
    this._timePane.add(this._separator3);
    // Minutes
    this._minutesTextField = new rwt.widgets.base.Label( "00" );
    this._minutesTextField.setAppearance( "datetime-field" );
    this._minutesTextField.setUserData( "maxLength", 2 );
    this._minutesTextField.addEventListener( "mousedown",  this._onTextFieldMouseDown, this );
    this._timePane.add(this._minutesTextField);
    // Separator
    this._separator4 = new rwt.widgets.base.Label( ":" );
    this._separator4.setAppearance( "datetime-separator" );
    if( this._medium || this._long ) {
      this._timePane.add(this._separator4);
    }
    // Seconds
    this._secondsTextField = new rwt.widgets.base.Label( "00" );
    this._secondsTextField.setAppearance( "datetime-field" );
    this._secondsTextField.setUserData( "maxLength", 2 );
    this._secondsTextField.addEventListener( "mousedown",  this._onTextFieldMouseDown, this );
    if( this._medium || this._long ) {
      this._timePane.add(this._secondsTextField);
    }
    // Spinner
    this._spinner = this._createSpinner();
    this.add( this._spinner );
    // Set the default focused text field
    this._focusedTextField = this._hoursTextField;
    // Set the appearance after subwidgets are created
    this.setAppearance( "datetime-time" );
  },

  destruct : function() {
    this._disposeObjects( "_timePane",
                          "_hoursTextField",
                          "_minutesTextField",
                          "_secondsTextField",
                          "_focusedTextField",
                          "_spinner",
                          "_separator3",
                          "_separator4" );
  },

  members : {

    _getSubWidgets : function() {
      return [ this._hoursTextField,
               this._minutesTextField,
               this._secondsTextField,
               this._spinner,
               this._separator3,
               this._separator4 ];
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.getLayoutImpl().setMirror( value === "rtl" );
      this._spinner._upbutton.setDirection( value );
      this._spinner._downbutton.setDirection( value );
    },

    _layoutX : function() {
      this._timePane.setWidth( this.getWidth() - this._spinner.getWidth() );
    },

    _createSpinner : function() {
      var spinner = new rwt.widgets.base.Spinner();
      spinner.set( {
        wrap: true,
        border: null,
        backgroundColor: null,
        selectTextOnInteract : false
      } );
      spinner.setMin( 0 );
      spinner.setMax( 23 );
      spinner.setValue( 0 );
      spinner.addEventListener( "change",  this._onSpinnerChange, this );
      spinner.addEventListener( "changeWidth", this._layoutX, this );
      spinner._textfield.setTabIndex( null );
      // Hack to prevent the spinner text field to request the focus
      spinner._textfield.setFocused = rwt.util.Functions.returnTrue;
      // Solution for Bug 284021
      spinner._textfield.setDisplay( false );
      spinner._upbutton.setAppearance( "datetime-button-up" );
      spinner._downbutton.setAppearance( "datetime-button-down" );
      spinner.removeEventListener( "keypress", spinner._onkeypress, spinner );
      spinner.removeEventListener( "keydown", spinner._onkeydown, spinner );
      spinner.removeEventListener( "keyup", spinner._onkeyup, spinner );
      spinner.removeEventListener( "mousewheel", spinner._onmousewheel, spinner );
      return spinner;
    },

    _applyFont : function( value, old ) {
      this.base( arguments, value, old );
      this._hoursTextField.setFont( value );
      this._minutesTextField.setFont( value );
      this._secondsTextField.setFont( value );
      this._separator3.setFont( value );
      this._separator4.setFont( value );
    },

    _onFocusIn : function() {
      this._focusedTextField.addState( "selected" );
      this._initialEditing = true;
    },

    _onFocusOut : function() {
      this._focusedTextField.removeState( "selected" );
    },

    _onTextFieldMouseDown : function( event ) {
      this._setFocusedTextField( event.getTarget() );
    },

    _setFocusedTextField :  function( textField ) {
      if( this._focusedTextField !== textField ) {
        this._focusedTextField.removeState( "selected" );
        this._focusedTextField = null;
        this._applySpinnerValue( textField );
        this._focusedTextField = textField;
        this._focusedTextField.addState( "selected" );
        this._initialEditing = true;
      }
    },

    _applySpinnerValue : function( textField ) {
      if( textField === this._hoursTextField ) {
        this._applyHourSpinnerValue();
      } else if( textField === this._minutesTextField ) {
        this._applyMinuteSpinnerValue();
      } else if( textField === this._secondsTextField ) {
        this._applySecondSpinnerValue();
      }
    },

    _applySecondSpinnerValue : function() {
      this._spinner.setMin( 0 );
      this._spinner.setMax( 59 );
      var hour = parseInt( this._removeLeadingZero( this._hoursTextField.getText() ) );
      var minute = parseInt( this._removeLeadingZero( this._minutesTextField.getText() ) );
      if( this._minimum && this._minimum.getHours() === hour && this._minimum.getMinutes() === minute ) {
        this._spinner.setMin( this._minimum.getSeconds() );
      }
      if( this._maximum && this._maximum.getHours() === hour && this._maximum.getMinutes() === minute ) {
        this._spinner.setMax( this._maximum.getSeconds() );
      }
      var tmpValue = this._removeLeadingZero( this._secondsTextField.getText() );
      this._spinner.setValue( parseInt( tmpValue, 10 ) );
    },

    _applyMinuteSpinnerValue : function() {
      this._spinner.setMin( 0 );
      this._spinner.setMax( 59 );
      var hour = parseInt( this._removeLeadingZero( this._hoursTextField.getText() ) );
      if( this._minimum && this._minimum.getHours() === hour ) {
        this._spinner.setMin( this._minimum.getMinutes() );
      }
      if( this._maximum && this._maximum.getHours() === hour ) {
        this._spinner.setMax( this._maximum.getMinutes() );
      }
      var tmpValue = this._removeLeadingZero( this._minutesTextField.getText() );
      this._spinner.setValue( parseInt( tmpValue, 10 ) );
    },

    _applyHourSpinnerValue : function() {
      this._spinner.setMin( 0 );
      this._spinner.setMax( 23 );
      if( this._minimum ) {
        this._spinner.setMin( this._minimum.getHours() );
      }
      if( this._maximum ) {
        this._spinner.setMax( this._maximum.getHours() );
      }
      var tmpValue = this._removeLeadingZero( this._hoursTextField.getText() );
      this._spinner.setValue( parseInt( tmpValue, 10 ) );
    },

    _onSpinnerChange : function() {
      if( this._focusedTextField != null ) {
        var oldValue = this._focusedTextField.getText();
        var newValue = this._addLeadingZero( this._spinner.getValue() );
        this._focusedTextField.setText( newValue );
        if( oldValue != newValue ) {
          this._sendChanges();
        }
        this._applyLimitRestriction();
      }
    },

    _applyLimitRestriction : function() {
      var hour = parseInt( this._removeLeadingZero( this._hoursTextField.getText() ) );
      var minute = parseInt( this._removeLeadingZero( this._minutesTextField.getText() ) );
      var second = parseInt( this._removeLeadingZero( this._secondsTextField.getText() ) );
      var date = new Date( 1970, 0, 1, hour, minute, second );
      if ( this._minimum && date.getTime() < this._minimum.getTime() ) {
        this.setHours( this._minimum.getHours() );
        this.setMinutes( this._minimum.getMinutes() );
        this.setSeconds( this._minimum.getSeconds() );
      }
      if ( this._maximum && date.getTime() > this._maximum.getTime()) {
        this.setHours( this._maximum.getHours() );
        this.setMinutes( this._maximum.getMinutes() );
        this.setSeconds( this._maximum.getSeconds() );
      }
    },

    _onKeyPress : function( event ) {
      if( event.getModifiers() === 0 ) {
        switch( event.getKeyIdentifier() ) {
          case "Enter":
            this._handleKeyEnter( event );
            break;
          case "Left":
            this._handleKeyLeft( event );
            break;
          case "Right":
            this._handleKeyRight( event );
            break;
          case "Up":
            this._handleKeyUp( event );
            break;
          case "Down":
            this._handleKeyDown( event );
            break;
          case "PageUp":
          case "PageDown":
          case "Home":
          case "End":
            this._stopEvent( event );
            break;
        }
      }
    },

    _onKeyUp : function( event ) {
      if( event.getModifiers() === 0 ) {
        switch( event.getKeyIdentifier() ) {
          case "0": case "1": case "2": case "3": case "4":
          case "5": case "6": case "7": case "8": case "9":
            this._handleKeyNumber( event );
            break;
          case "Home":
            this._handleKeyHome( event );
            break;
          case "End":
            this._handleKeyEnd( event );
            break;
        }
      }
    },

    _handleKeyEnter : function() {
      rwt.remote.EventUtil.notifyDefaultSelected( this );
    },

    _handleKeyLeft : function( event ) {
      if( this._focusedTextField === this._hoursTextField ) {
        if( this._short ) {
          this._setFocusedTextField( this._minutesTextField );
        } else {
          this._setFocusedTextField( this._secondsTextField );
        }
      } else if( this._focusedTextField === this._minutesTextField ) {
        this._setFocusedTextField( this._hoursTextField );
      } else if( this._focusedTextField === this._secondsTextField ) {
        this._setFocusedTextField( this._minutesTextField );
      }
      this._stopEvent( event );
    },

    _handleKeyRight : function( event ) {
      if( this._focusedTextField === this._hoursTextField ) {
        this._setFocusedTextField( this._minutesTextField );
      } else if( this._focusedTextField === this._minutesTextField ) {
        if( this._short ) {
          this._setFocusedTextField( this._hoursTextField );
        } else {
          this._setFocusedTextField( this._secondsTextField );
        }
      } else if( this._focusedTextField === this._secondsTextField ) {
        this._setFocusedTextField( this._hoursTextField );
      }
      this._stopEvent( event );
    },

    _handleKeyDown : function( event ) {
      var value = this._spinner.getValue();
      if( value == this._spinner.getMin() ) {
        this._spinner.setValue( this._spinner.getMax() );
      } else {
        this._spinner.setValue( value - 1 );
      }
      this._stopEvent( event );
    },

    _handleKeyUp : function( event ) {
      var value = this._spinner.getValue();
      if( value == this._spinner.getMax() ) {
        this._spinner.setValue( this._spinner.getMin() );
      } else {
        this._spinner.setValue( value + 1 );
      }
      this._stopEvent( event );
    },

    _handleKeyNumber : function( event ) {
      var key = event.getKeyIdentifier();
      var value = this._removeLeadingZero( this._focusedTextField.getText() );
      var maxChars = this._focusedTextField.getUserData( "maxLength" );
      var newValue = value.length < maxChars && !this._initialEditing ? value + key : key;
      var intValue = parseInt( newValue, 10 );
      if( intValue >= this._spinner.getMin() && intValue <= this._spinner.getMax() ) {
        this._spinner.setValue( intValue );
      } else {
        intValue = parseInt( key, 10 );
        if( intValue >= this._spinner.getMin() && intValue <= this._spinner.getMax() ) {
          this._spinner.setValue( intValue );
        }
      }
      this._initialEditing = false;
      this._stopEvent( event );
    },

    _handleKeyHome : function( event ) {
      this._spinner.setValue( this._spinner.getMin() );
      this._initialEditing = true;
      this._stopEvent( event );
    },

    _handleKeyEnd : function( event ) {
      this._spinner.setValue( this._spinner.getMax() );
      this._initialEditing = true;
      this._stopEvent( event );
    },

    _onMouseWheel : function( event ) {
      if( this.getFocused() ) {
        this._stopEvent( event );
        this._spinner._onmousewheel( event );
      }
    },

    _stopEvent : function( event ) {
      event.preventDefault();
      event.stopPropagation();
    },

    _addLeadingZero : function( value ) {
      return value < 10 ? "0" + value : "" + value;
    },

    _removeLeadingZero : function( value ) {
      var result = value;
      if( value.length == 2 ) {
        var firstChar = value.substring( 0, 1 );
        if( firstChar == "0" ) {
          result = value.substring( 1 );
        }
      }
      return result;
    },

    _sendChanges : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var connection = rwt.remote.Connection.getInstance();
        var remoteObject = connection.getRemoteObject( this );
        var hours = parseInt( this._removeLeadingZero( this._hoursTextField.getText() ), 10 );
        remoteObject.set( "hours", hours );
        var minutes = parseInt( this._removeLeadingZero( this._minutesTextField.getText() ), 10 );
        remoteObject.set( "minutes", minutes );
        var seconds = parseInt( this._removeLeadingZero( this._secondsTextField.getText() ), 10 );
        remoteObject.set( "seconds", seconds );
        if( remoteObject.isListening( "Selection" ) ) {
          connection.onNextSend( this._onSend, this );
          connection.sendDelayed( 200 );
        }
      }
    },

    _onSend : function() {
      rwt.remote.EventUtil.notifySelected( this );
    },

    setHours : function( value ) {
      this._hoursTextField.setText( this._addLeadingZero( value ) );
      if( this._focusedTextField === this._hoursTextField ) {
        this._spinner.setValue( value );
      }
    },

    setMinutes : function( value ) {
      this._minutesTextField.setText( this._addLeadingZero( value ) );
      if( this._focusedTextField === this._minutesTextField ) {
        this._spinner.setValue( value );
      }
    },

    setSeconds : function( value ) {
      this._secondsTextField.setText( this._addLeadingZero( value ) );
      if( this._focusedTextField === this._secondsTextField ) {
        this._spinner.setValue( value );
      }
    },

    setMinimum : function( value ) {
      this._minimum = value === null ? null : new Date( value );
      if( this._minimum ) {
        this._minimum.setYear( 1970 );
        this._minimum.setMonth( 0 );
        this._minimum.setDate( 1 );
      }
      this._applyLimitRestriction();
    },

    setMaximum : function( value ) {
      this._maximum = value === null ? null : new Date( value );
      if( this._maximum ) {
        this._maximum.setYear( 1970 );
        this._maximum.setMonth( 0 );
        this._maximum.setDate( 1 );
      }
      this._applyLimitRestriction();
    },

    setBounds : function( index, x, y, width, height ) {
      var widget;
      switch( index ) {
        case 7:
          widget = this._spinner;
        break;
        case 8:
          widget = this._hoursTextField;
        break;
        case 9:
          widget = this._minutesTextField;
        break;
        case 10:
          widget = this._secondsTextField;
        break;
        case 11:
          widget = this._separator3;
        break;
        case 12:
          widget = this._separator4;
        break;
      }
      widget.set( {
        left: x,
        top: y,
        width: width,
        height: height
      } );
    }

  }

} );
