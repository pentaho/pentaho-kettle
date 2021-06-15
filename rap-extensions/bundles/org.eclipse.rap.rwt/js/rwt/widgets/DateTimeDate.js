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

rwt.qx.Class.define( "rwt.widgets.DateTimeDate", {

  extend : rwt.widgets.base.Parent,

  construct : function( style,
                        monthNames,
                        weekdayNames,
                        weekdayShortNames,
                        dateSeparator,
                        datePattern )
  {
    this.base( arguments );
    this.setOverflow( "hidden" );
    this._short = rwt.util.Strings.contains( style, "short" );
    this._medium = rwt.util.Strings.contains( style, "medium" );
    this._long = rwt.util.Strings.contains( style, "long" );
    this._drop_down = rwt.util.Strings.contains( style, "drop_down" );
    this._internalDateChanged = false;
    this._weekday = weekdayNames;
    this._monthname = monthNames;
    this._datePattern = datePattern;
    this.addEventListener( "keypress", this._onKeyPress, this );
    this.addEventListener( "keyup", this._onKeyUp, this );
    this.addEventListener( "mousewheel", this._onMouseWheel, this );
    this.addEventListener( "focus", this._onFocusIn, this );
    this.addEventListener( "blur", this._onFocusOut, this );
    this._datePane = new rwt.widgets.base.Parent();
    this._datePane.setLeft( 0 );
    this._datePane.setHeight( "100%" );
    this.add( this._datePane );
    // Weekday
    this._weekdayTextField = new rwt.widgets.base.Label();
    this._weekdayTextField.setAppearance( "datetime-field" );
    if( this._long ) {
      this._datePane.add( this._weekdayTextField );
    }
    // Separator
    this._separator0 = new rwt.widgets.base.Label(",");
    this._separator0.setAppearance( "datetime-separator" );
    if( this._long ) {
      this._datePane.add(this._separator0);
    }
    // Month
    this._monthTextField = new rwt.widgets.base.Label();
    this._monthTextField.setAppearance( "datetime-field" );
    this._monthTextField.setTextAlign( this._medium ? "right" : "center" );
    // Integer value of the month
    this._monthInt = 1;
    if( this._medium ) {
      this._monthTextField.setText( "1" );
    } else {
      this._monthTextField.setText( this._monthname[ this._monthInt - 1 ] );
    }
    this._monthTextField.addEventListener( "mousedown",  this._onTextFieldMouseDown, this );
    this._datePane.add( this._monthTextField );
    // Separator
    this._separator1 = new rwt.widgets.base.Label( dateSeparator );
    this._separator1.setAppearance( "datetime-separator" );
    if( this._medium ) {
      this._datePane.add(this._separator1);
    }
    // Date
    this._dayTextField = new rwt.widgets.base.Label( "1" );
    this._dayTextField.setAppearance( "datetime-field" );
    this._dayTextField.setUserData( "maxLength", 2 );
    this._dayTextField.setTextAlign( "right" );
    this._dayTextField.addEventListener( "mousedown",  this._onTextFieldMouseDown, this );
    if( !this._short ) {
      this._datePane.add( this._dayTextField );
    }
    // Separator
    this._separator2 = new rwt.widgets.base.Label( "," );
    this._separator2.setAppearance( "datetime-separator" );
    if( this._medium ) {
      this._separator2.setText( dateSeparator );
    }
    this._datePane.add(this._separator2);
    // Year
    this._yearTextField = new rwt.widgets.base.Label( "1970" );
    this._yearTextField.setAppearance( "datetime-field" );
    this._yearTextField.setUserData( "maxLength", 4 );
    this._yearTextField.setTextAlign( "right" );
    // Last valid year
    this._lastValidYear = 1970;
    this._yearTextField.addEventListener( "mousedown",  this._onTextFieldMouseDown, this );
    this._datePane.add( this._yearTextField );
    // Spinner
    this._spinner = this._createSpinner();
    this._spinner.setDisplay( !this._drop_down );
    this.add( this._spinner );
    // Drop-down button and calendar
    this._dropped = false;
    this._dropDownButton = null;
    this._calendar = null;
    if( this._drop_down ) {
      // Add events listeners
      var document = rwt.widgets.base.ClientDocument.getInstance();
      document.addEventListener( "windowblur", this._onWindowBlur, this );
      this.addEventListener( "appear", this._onAppear, this );
      this.addEventListener( "changeVisibility", this._onChangeVisibility, this );
      this.addEventListener( "mousedown", this._onMouseDown, this );
      this.addEventListener( "click", this._onMouseClick, this );
      this.addEventListener( "mouseover", this._onMouseOver, this );
      this.addEventListener( "mouseout", this._onMouseOut, this );
      this._dropDownButton = new rwt.widgets.base.BasicButton( "push", true );
      this._dropDownButton.setAppearance( "datetime-drop-down-button" );
      this._dropDownButton.setTabIndex( null );
      this._dropDownButton.setAllowStretchY( true );
      this.add( this._dropDownButton );
      // Get names of weekdays and months
      rwt.widgets.base.Calendar.MONTH_NAMES = monthNames;
      rwt.widgets.base.Calendar.WEEKDAY_NAMES = weekdayShortNames;
      this._calendar = new rwt.widgets.base.Calendar();
      this._calendar.setAppearance( "datetime-drop-down-calendar" );
      this._calendar.setDate( new Date( 70, 0, 1 ) );
      this._calendar.setTabIndex( null );
      this._calendar.setVisibility( false );
      this._calendar.addEventListener( "changeDate", this._onCalendarDateChange, this );
      // TODO: [if] Calendar buttons tooltips have wrong z-index
      // Remove tooltips for now.
      this._calendar._lastYearBt.setToolTipText( null );
      this._calendar._lastMonthBt.setToolTipText( null );
      this._calendar._nextMonthBt.setToolTipText( null );
      this._calendar._nextYearBt.setToolTipText( null );
    }
    // Set the default focused text field
    this._focusedTextField = this._monthTextField;
    // Set the weekday
    this._setWeekday();
    // Set the appearance after subwidgets are created
    this.setAppearance( "datetime-date" );
  },

  destruct : function() {
    this._disposeObjects( "_datePane",
                          "_weekdayTextField",
                          "_monthTextField",
                          "_dayTextField",
                          "_yearTextField",
                          "_focusedTextField",
                          "_spinner",
                          "_separator0",
                          "_separator1",
                          "_separator2" );
    if( this._drop_down ) {
      var document = rwt.widgets.base.ClientDocument.getInstance();
      document.removeEventListener( "windowblur", this._onWindowBlur, this );
      this._dropDownButton.dispose();
      this._dropDownButton = null;
      if( !rwt.qx.Object.inGlobalDispose() ) {
        this._calendar.setParent( null );
      }
      this._calendar.dispose();
      this._calendar = null;
    }
  },

  members : {

    _getSubWidgets : function() {
      var result = [ this._weekdayTextField,
                     this._monthTextField,
                     this._dayTextField,
                     this._yearTextField,
                     this._spinner,
                     this._separator0,
                     this._separator1,
                     this._separator2 ];
      if( this._drop_down ) {
        result.push( this._dropDownButton, this._calendar );
      }
      return result;
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.getLayoutImpl().setMirror( value === "rtl" );
      this._spinner._upbutton.setDirection( value );
      this._spinner._downbutton.setDirection( value );
      if( this._drop_down ) {
        this._dropDownButton.setDirection( value );
        this._calendar.setDirection( value );
      }
    },

    _layoutX : function() {
      this._datePane.setWidth( this.getWidth() - this._spinner.getWidth() );
    },

    _createSpinner : function() {
      var spinner = new rwt.widgets.base.Spinner();
      spinner.set( {
        wrap: true,
        border: null,
        backgroundColor: null,
        selectTextOnInteract : false
      } );
      spinner.setMin( 1 );
      spinner.setMax( 12 );
      spinner.setValue( this._monthInt );
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
      this._weekdayTextField.setFont( value );
      this._dayTextField.setFont( value );
      this._monthTextField.setFont( value );
      this._yearTextField.setFont( value );
      this._separator0.setFont( value );
      this._separator1.setFont( value );
      this._separator2.setFont( value );
    },

    _onFocusIn : function() {
      this._focusedTextField.addState( "selected" );
      this._initialEditing = true;
    },

    _onFocusOut : function() {
      if( this._focusedTextField === this._yearTextField ) {
        this._checkAndApplyYearValue();
      }
      this._focusedTextField.removeState( "selected" );
      this._hideCalendar();
    },

    _onTextFieldMouseDown : function( event ) {
      if( this._focusedTextField === this._yearTextField ) {
        this._checkAndApplyYearValue();
      }
      this._setFocusedTextField( event.getTarget() );
    },

    _setFocusedTextField : function( textField ) {
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
      if( textField === this._dayTextField ) {
        this._applyDaySpinnerValue();
      } else if( textField === this._monthTextField ) {
        this._applyMonthSpinnerValue();
      } else if( textField === this._yearTextField ) {
        this._applyYearSpinnerValue();
      }
    },

    _applyDaySpinnerValue : function() {
      this._spinner.setMin( 1 );
      this._spinner.setMax( this._getDaysInMonth() );
      if( this._minimum && this._minimum.getFullYear() === this._lastValidYear &&
          this._minimum.getMonth() === this._monthInt - 1 ) {
        this._spinner.setMin( this._minimum.getDate() );
      }
      if( this._maximum && this._maximum.getFullYear() === this._lastValidYear &&
          this._maximum.getMonth() === this._monthInt - 1 ) {
        this._spinner.setMax( this._maximum.getDate() );
      }
      var tmpValue = this._removeLeadingZero( this._dayTextField.getText() );
      this._spinner.setValue( parseInt( tmpValue, 10 ) );
    },

    _applyMonthSpinnerValue : function() {
      this._spinner.setMin( 1 );
      this._spinner.setMax( 12 );
      if( this._minimum && this._minimum.getFullYear() === this._lastValidYear ) {
        this._spinner.setMin( this._minimum.getMonth() + 1 );
      }
      if( this._maximum && this._maximum.getFullYear() === this._lastValidYear ) {
        this._spinner.setMax( this._maximum.getMonth() + 1 );
      }
      this._spinner.setValue( this._monthInt );
    },

    _applyYearSpinnerValue : function() {
      this._spinner.setMax( 9999 );
      this._spinner.setMin( 1752 );
      if( this._minimum ) {
        this._spinner.setMin( this._minimum.getFullYear() );
      }
      if( this._maximum ) {
        this._spinner.setMax( this._maximum.getFullYear() );
      }
      this._spinner.setValue( this._lastValidYear );
    },

    _onSpinnerChange : function() {
      if( this._focusedTextField != null ) {
        var oldValue = this._focusedTextField.getText();
        // Set the value
        if( this._focusedTextField === this._monthTextField ) {
          this._monthInt = this._spinner.getValue();
          if( this._medium ) {
            this._focusedTextField.setText( this._addLeadingZero( this._monthInt ) );
          } else {
            this._focusedTextField.setText( this._monthname[ this._monthInt - 1 ] );
          }
        } else if( this._focusedTextField === this._yearTextField ) {
          this._lastValidYear = this._spinner.getValue();
          this._focusedTextField.setText( "" + this._spinner.getValue() );
        } else {
          this._focusedTextField.setText( this._addLeadingZero( this._spinner.getValue() ) );
        }
        // Adjust date field
        if(    this._focusedTextField == this._monthTextField // month
            || this._focusedTextField == this._yearTextField ) // year
        {
          var dateValue = this._dayTextField.getText();
          if( dateValue > this._getDaysInMonth() ) {
            this._dayTextField.setText( "" + this._getDaysInMonth() );
          }
        }
        // Set the weekday field
        this._setWeekday();
        var newValue = this._focusedTextField.getText();
        if( oldValue != newValue ) {
          this._sendChanges();
        }
        this._applyLimitRestriction();
      }
    },

    _applyLimitRestriction : function() {
      var day = parseInt( this._removeLeadingZero( this._dayTextField.getText() ) );
      var date = new Date(this._lastValidYear, this._monthInt - 1, day);
      if( this._minimum && date.getTime() < this._minimum.getTime() ) {
        this.setYear( this._minimum.getFullYear() );
        this.setMonth( this._minimum.getMonth() );
        this.setDay( this._minimum.getDate() );
      }
      if( this._maximum && date.getTime() > this._maximum.getTime()) {
        this.setYear( this._maximum.getFullYear() );
        this.setMonth( this._maximum.getMonth() );
        this.setDay( this._maximum.getDate() );
      }
    },

    _onKeyPress : function( event ) {
      var key = event.getKeyIdentifier();
      if( this._drop_down && ( key === "Up" || key === "Down" ) && event.isAltPressed() ) {
        this._toggleCalendarVisibility();
      } else if( this._dropped ) {
        this._calendar._onkeypress( event );
        if( event.getModifiers() === 0 ) {
          if( key === "Enter" ) {
            this._hideCalendar();
            this._handleKeyEnter( event );
          } else if( key === "Escape" || key === "Space" ) {
            this._hideCalendar();
          }
        }
      } else if( event.getModifiers() === 0 ) {
        switch( key ) {
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
      if( !this._dropped && event.getModifiers() === 0 ) {
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
      if( this._focusedTextField === this._yearTextField ) {
        this._checkAndApplyYearValue();
      }
      if( this._datePattern == "MDY") {
        this._rollLeft( this._monthTextField, this._dayTextField, this._yearTextField );
      } else if( this._datePattern == "DMY") {
        this._rollLeft( this._dayTextField, this._monthTextField, this._yearTextField );
      } else {
        if( this._medium ) {
          this._rollLeft( this._yearTextField, this._monthTextField, this._dayTextField );
        } else {
          this._rollLeft( this._monthTextField, this._dayTextField, this._yearTextField );
        }
      }
      this._stopEvent( event );
    },

    _handleKeyRight : function( event ) {
      if( this._focusedTextField === this._yearTextField ) {
        this._checkAndApplyYearValue();
      }
      if( this._datePattern == "MDY") {
        this._rollRight( this._monthTextField, this._dayTextField, this._yearTextField );
      } else if( this._datePattern == "DMY") {
        this._rollRight( this._dayTextField, this._monthTextField, this._yearTextField );
      } else {
        if( this._medium ) {
          this._rollRight( this._yearTextField, this._monthTextField, this._dayTextField );
        } else {
          this._rollRight( this._monthTextField, this._dayTextField, this._yearTextField );
        }
      }
      this._stopEvent( event );
    },

    _rollRight : function( first, second, third ) {
      if( this._focusedTextField === first ){
        if( second.isSeeable() ) {
          this._setFocusedTextField( second );
        } else {
          this._setFocusedTextField( third );
        }
      } else if( this._focusedTextField === second ) {
        if( third.isSeeable() ) {
          this._setFocusedTextField( third );
        } else {
          this._setFocusedTextField( first );
        }
      } else if( this._focusedTextField === third ) {
        if( first.isSeeable() ) {
          this._setFocusedTextField( first );
        } else {
          this._setFocusedTextField( second );
        }
      }
    },

    _rollLeft : function( first, second, third ) {
      if( this._focusedTextField === first ) {
        if( third.isSeeable() ) {
          this._setFocusedTextField( third );
        } else {
          this._setFocusedTextField( second );
        }
      } else if( this._focusedTextField === second ) {
        if( first.isSeeable() ) {
          this._setFocusedTextField( first );
        } else {
          this._setFocusedTextField( third );
        }
      } else if( this._focusedTextField === third ) {
        if( second.isSeeable() ) {
          this._setFocusedTextField( second );
        } else {
          this._setFocusedTextField( first );
        }
      }
    },

    _handleKeyDown : function( event ) {
      if( this._focusedTextField === this._yearTextField ) {
        this._checkAndApplyYearValue();
      }
      var value = this._spinner.getValue();
      if( value == this._spinner.getMin() ) {
        this._spinner.setValue( this._spinner.getMax() );
      } else {
        this._spinner.setValue( value - 1 );
      }
      this._stopEvent( event );
    },

    _handleKeyUp : function( event ) {
      if( this._focusedTextField === this._yearTextField ) {
        this._checkAndApplyYearValue();
      }
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
      if( this._focusedTextField === this._monthTextField ) {
        value = "" + this._monthInt;
        maxChars = 2;
      }
      var newValue = value.length < maxChars && !this._initialEditing ? value + key : key;
      var intValue = parseInt( newValue, 10 );
      if(    this._focusedTextField === this._dayTextField
          || this._focusedTextField === this._monthTextField )
      {
        if( intValue >= this._spinner.getMin() && intValue <= this._spinner.getMax() ) {
          this._spinner.setValue( intValue );
        } else {
          // Do it again without adding the old value
          intValue = parseInt( key, 10 );
          if( intValue >= this._spinner.getMin() && intValue <= this._spinner.getMax() ) {
            this._spinner.setValue( intValue );
          }
        }
      } else if( this._focusedTextField == this._yearTextField ) {
        this._focusedTextField.setText( newValue );
        if( newValue.length == 4 ) {
          this._checkAndApplyYearValue();
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
        if( !this._dropped ) {
          this._spinner._onmousewheel( event );
        }
      }
    },

    _stopEvent : function( event ) {
      event.preventDefault();
      event.stopPropagation();
    },

    _getDaysInMonth : function() {
      var result = 31;
      var tmpMonth = this._monthInt - 1;
      var tmpYear = parseInt( this._yearTextField.getText(), 10 );
      var tmpDate = new Date( tmpYear, tmpMonth, 1 );
      tmpDate.setDate( result );
      while( tmpDate.getMonth() !== tmpMonth ) {
        result--;
        tmpDate = new Date( tmpYear, tmpMonth, 1 );
        tmpDate.setDate( result );
      }
      return result;
    },

    _setWeekday : function() {
      var tmpMonth = this._monthInt - 1;
      var tmpYear = parseInt( this._yearTextField.getText(), 10 );
      var tmpDay = parseInt( this._dayTextField.getText(), 10 );
      // Set all fields together to avoid date-shift
      // See bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=517598
      var tmpDate = new Date( tmpYear, tmpMonth, tmpDay );
      this._weekdayTextField.setText( this._weekday[ tmpDate.getDay() + 1 ] );
    },

    _checkAndApplyYearValue : function() {
      var oldValue = this._lastValidYear;
      var value = parseInt( this._yearTextField.getText(), 10 );
      if( value >= 0 && value <= 29 ) {
        this._lastValidYear = 2000 + value;
      } else if( value >= 30 && value <= 99 ) {
        this._lastValidYear = 1900 + value;
      } else if( value >= 1752 ) {
        this._lastValidYear = value;
      }
      this._yearTextField.setText( "" + oldValue );
      if( oldValue != this._lastValidYear ) {
        this._spinner.setValue( this._lastValidYear );
      }
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
        var day = parseInt( this._removeLeadingZero( this._dayTextField.getText() ), 10 );
        remoteObject.set( "day", day );
        remoteObject.set( "month", this._monthInt - 1 );
        remoteObject.set( "year", this._lastValidYear );
        if( remoteObject.isListening( "Selection" ) ) {
          connection.onNextSend( this._onSend, this );
          connection.sendDelayed( 200 );
        }
      }
    },

    _onSend : function() {
      rwt.remote.EventUtil.notifySelected( this );
    },

    setDate : function( year, month, day ) {
      this._setDate( new Date( year, month, day ) );
    },

    setMonth : function( value ) {
      this._monthInt = value + 1;
      if( this._medium ) {
        this._monthTextField.setText( this._addLeadingZero( this._monthInt ) );
      } else {
        this._monthTextField.setText( this._monthname[ this._monthInt - 1 ] );
      }
      if( this._focusedTextField === this._monthTextField ) {
        this._spinner.setValue( this._monthInt );
      }
      // Set the weekday
      this._setWeekday();
    },

    setDay : function( value ) {
      this._dayTextField.setText( this._addLeadingZero( value ) );
      if( this._focusedTextField === this._dayTextField ) {
        this._spinner.setValue( value );
      }
      // Set the weekday
      this._setWeekday();
    },

    setYear : function( value ) {
      this._lastValidYear = value;
      this._yearTextField.setText( "" + value );
      if( this._focusedTextField === this._yearTextField ) {
        this._spinner.setValue( value );
      }
      // Set the weekday
      this._setWeekday();
    },

    setMinimum : function( value ) {
      this._minimum = value === null ? null : new Date( value );
      if( this._minimum ) {
        this._minimum.setHours( 0, 0, 0, 0 );
      }
      this._applySpinnerValue(this._focusedTextField);
      this._applyLimitRestriction();
      if( this._calendar ) {
        this._calendar.setMinimum( this._minimum );
      }
    },

    setMaximum : function( value ) {
      this._maximum = value === null ? null : new Date( value );
      if( this._maximum ) {
        this._maximum.setHours( 0, 0, 0, 0 );
      }
      this._applySpinnerValue(this._focusedTextField);
      this._applyLimitRestriction();
      if( this._calendar ) {
        this._calendar.setMaximum( this._maximum );
      }
    },

    _setDate : function( date ) {
      this.setYear( date.getFullYear() );
      this.setMonth( date.getMonth() );
      this.setDay( date.getDate() );
    },

    setBounds : function( index, x, y, width, height ) {
      var widget;
      switch( index ) {
        case 0:
          widget = this._weekdayTextField;
        break;
        case 1:
          widget = this._dayTextField;
        break;
        case 2:
          widget = this._monthTextField;
        break;
        case 3:
          widget = this._yearTextField;
        break;
        case 4:
          widget = this._separator0;
        break;
        case 5:
          widget = this._separator1;
        break;
        case 6:
          widget = this._separator2;
        break;
        case 7:
          widget = this._spinner;
        break;
        case 13:
          widget = this._dropDownButton;
        break;
      }
      if( widget ) {
        widget.set( {
          left: x,
          top: y,
          width: width,
          height: height
        } );
      }
    },

    //////////////////////////////////////
    // Drop-down calendar handling methods

    _onAppear : function() {
      if( this._drop_down ) {
        this.getTopLevelWidget().add( this._calendar );
        this._setCalendarLocation();
      }
    },

    _onWindowBlur : function() {
      this._hideCalendar();
    },

    _onChangeVisibility : function( event ) {
      var value = event.getValue();
      if( !value ) {
        this._hideCalendar();
      }
    },

    _onCalendarDateChange : function() {
      if( !this._internalDateChanged ) {
        var milliseconds = this._calendar.getDate().getTime();
        var min = this._minimum ? this._minimum.getTime() : Number.MIN_VALUE;
        var max = this._maximum ? this._maximum.getTime() : Number.MAX_VALUE;
        if( milliseconds >= min && milliseconds <= max ) {
          this._setDate( this._calendar.getDate() );
          this._sendChanges();
        }
      }
    },

    _onMouseDown : function( event ) {
      var target = event.getTarget();
      if( target.getUserData( "calendar-day" ) ) {
        event.stopPropagation();
      } else if( target.getUserData( "calendar-button" ) ) {
        event.stopPropagation();
      } else if( this._dropped && target !== this._dropDownButton ) {
        this._hideCalendar();
      }
    },

    _onMouseClick : function( event ) {
      if( event.isLeftButtonPressed() ) {
        var target = event.getTarget();
        if( target.getUserData( "calendar-day" ) ) {
          if( !target.hasState( "disabled" ) ) {
            this._calendar._onDayClicked( event );
            this._hideCalendar();
            this.setFocused( true );
          }
        } else if( target.getUserData( "calendar-button" ) ) {
          this._calendar._onNavButtonClicked( event );
        } else if( target === this._dropDownButton ) {
          this._toggleCalendarVisibility();
        }
      }
    },

    _onMouseOver : function( event ) {
      var target = event.getTarget();
      if( target == this._dropDownButton ) {
        this._dropDownButton.addState( "over" );
      } else if( target.getUserData( "calendar-day" ) ) {
        this._calendar._onDayMouseOver( event );
      }
    },

    _onMouseOut : function( event ) {
      var target = event.getTarget();
      if( target == this._dropDownButton ) {
        this._dropDownButton.removeState( "over" );
      } else if( target.getUserData( "calendar-day" ) ) {
        this._calendar._onDayMouseOut( event );
      }
    },

    _toggleCalendarVisibility : function() {
      if( this._dropped ) {
        this._hideCalendar();
      } else {
        this._showCalendar();
      }
    },

    _showCalendar : function() {
      if( this._drop_down && !this._dropped ) {
        this._dropped = true;
        this._calendar.setVisibility( true );
        this.setCapture( true );
        this._bringToFront();
        this._setCalendarLocation();
        var year = parseInt( this._yearTextField.getText(), 10 );
        var day = parseInt( this._dayTextField.getText(), 10 );
        var date = new Date( year, this._monthInt - 1, day );
        this._internalDateChanged = true;
        this._calendar.setDate( date );
        this._internalDateChanged = false;
        this._focusedTextField.removeState( "selected" );
      }
    },

    _hideCalendar : function() {
      if( this._drop_down && this._dropped ) {
        this._dropped = false;
        this._calendar.setVisibility( false );
        this.setCapture( false );
        if( this.getFocused() ){
          this._focusedTextField.addState( "selected" );
        }
      }
    },

    _setCalendarLocation : function() {
      if( this.getElement() && this._calendar != null ) {
        var browserWidth = window.innerWidth;
        var browserHeight = window.innerHeight;
        var elementPos = rwt.html.Location.get( this.getElement() );
        var left = elementPos.left;
        var top = elementPos.top + this.getHeight();
        var width = this._calendar.getWidthValue();
        var height = this._calendar.getHeightValue();
        if( top + height > browserHeight && elementPos.top - height > 0 ) {
          top = elementPos.top - height;
        }
        if( left + width > browserWidth ) {
          left =  Math.max( 0, browserWidth - width );
        }
        this._calendar.setLocation( left, top );
      }
    },

    _bringToFront : function() {
      var allWidgets = this.getTopLevelWidget().getChildren();
      var topZIndex = this._calendar.getZIndex();
      for( var vHashCode in allWidgets ) {
        var widget = allWidgets[ vHashCode ];
        if( widget.getZIndex ) {
          if( topZIndex < widget.getZIndex() ) {
            topZIndex = widget.getZIndex();
          }
        }
      }
      if( topZIndex > this._calendar.getZIndex() ) {
        this._calendar.setZIndex( topZIndex + 1 );
      }
    }

  }

} );
