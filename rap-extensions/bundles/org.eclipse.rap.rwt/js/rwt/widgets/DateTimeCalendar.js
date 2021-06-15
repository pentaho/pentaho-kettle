/*******************************************************************************
 * Copyright (c) 2008, 2017 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.DateTimeCalendar", {
  extend : rwt.widgets.base.Parent,

  construct : function( style, monthNames, weekdayNames ) {
    this.base( arguments );
    this.setOverflow( "hidden" );
    this.setAppearance( "datetime-calendar" );
    // Get names of weekdays and months
    rwt.widgets.base.Calendar.MONTH_NAMES = monthNames;
    rwt.widgets.base.Calendar.WEEKDAY_NAMES = weekdayNames;
    // The Calendar
    this._calendar = new rwt.widgets.base.Calendar();
    this._calendar.setDate( new Date( 74, 5, 6 ) );
    this._calendar.setTabIndex( null );
    this._calendar.addEventListener( "changeDate", this._onChangeDate, this );
    this._calendar.addEventListener( "select", this._onSelect, this );
    this.add( this._calendar );
    this.addEventListener( "keypress", this._onKeyPress, this );
    this.addEventListener( "mousewheel", this._onMouseWheel, this );
    this.addEventListener( "focus", this._onFocusChange, this );
    this.addEventListener( "blur", this._onFocusChange, this );
    this._updateSelectedDayState();
  },

  destruct : function() {
    this._disposeObjects( "_calendar" );
  },

  members : {

    _getSubWidgets : function() {
      return [ this._calendar ];
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this._calendar.setDirection( value );
    },

    _onKeyPress : function( event ) {
      this._calendar._onkeypress( event );
    },

    _onMouseWheel : function( event ) {
      event.preventDefault();
      event.stopPropagation();
    },

    _onFocusChange : function() {
      this._updateSelectedDayState();
    },

    _updateSelectedDayState : function() {
      for( var i = 0; i < 6 * 7; i++ ) {
        this._calendar._dayLabelArr[ i ].toggleState( "parent_unfocused", !this.getFocused() );
      }
    },

    _onChangeDate : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var connection = rwt.remote.Connection.getInstance();
        var remoteObject = connection.getRemoteObject( this );
        var date = this._calendar.getDate();
        remoteObject.set( "day", date.getDate() );
        remoteObject.set( "month", date.getMonth() );
        remoteObject.set( "year", date.getFullYear() );
        if( remoteObject.isListening( "Selection" ) ) {
          connection.onNextSend( this._onSend, this );
          connection.sendDelayed( 200 );
        }
      }
    },

    _onSend : function() {
      rwt.remote.EventUtil.notifySelected( this );
    },

    _onSelect : function() {
      rwt.remote.EventUtil.notifyDefaultSelected( this );
    },

    setDate : function( year, month, day ) {
      this._calendar.setDate( new Date( year, month, day ) );
    },

    setMonth : function( value ) {
      var date = this._calendar.getDate();
      date.setMonth( value );
      this._calendar.setDate( date );
    },

    setDay : function( value ) {
      var date = this._calendar.getDate();
      date.setDate( value );
      this._calendar.setDate( date );
    },

    setYear : function( value ) {
      var date = this._calendar.getDate();
      date.setYear( value );
      this._calendar.setDate( date );
    },

    setMinimum : function( value ) {
      var minimum = value === null ? null : new Date( value );
      if( minimum ) {
        minimum.setHours( 0, 0, 0, 0 );
      }
      this._calendar.setMinimum( minimum );
    },

    setMaximum : function( value ) {
      var maximum = value === null ? null : new Date( value );
      if( maximum ) {
        maximum.setHours( 0, 0, 0, 0 );
      }
      this._calendar.setMaximum( maximum );
    },

    setFont : function() {
      // TODO: [if] Calendar font is not implemented
    }

  }

} );
