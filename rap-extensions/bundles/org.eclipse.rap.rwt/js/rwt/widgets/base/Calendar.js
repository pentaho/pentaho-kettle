/*******************************************************************************
 * Copyright (c) 2008, 2020 Innoopract Informationssysteme GmbH.
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
  * This is a modified version of qooxdoo qx.ui.component.DateChooser component.
  */

/**
 * Shows a calendar and allows choosing a date.
 *
 * @appearance calendar-toolbar-button {qx.ui.toolbar.Button}
 * @appearance calendar-navBar {rwt.widgets.base.BoxLayout}
 * @appearance calendar-monthyear {rwt.widgets.base.Label}
 * @appearance calendar-weekday {rwt.widgets.base.Label}
 * @appearance calendar-datepane {rwt.widgets.base.Parent}
 * @appearance calendar-weekday {rwt.widgets.base.Label}
 *
 * @appearance calendar-day {rwt.widgets.base.Label}
 * @state weekend {calendar-day}
 * @state otherMonth {calendar-day}
 * @state today {calendar-day}
 * @state selected {calendar-day}
 */
rwt.qx.Class.define("rwt.widgets.base.Calendar", {

  extend : rwt.widgets.base.BoxLayout,

  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  /**
   * @param date {Date ? null} The initial date to show. If <code>null</code>
   *        the current day (today) is shown.
   */
  construct : function( date ) {
    this.base( arguments );
    this.setOrientation( "vertical" );

    // Create the navigation bar
    var navBar = new rwt.widgets.base.BoxLayout();
    navBar.setAppearance( "calendar-navBar" );

    navBar.set( {
      height  : "auto",
      spacing : 1
    } );

    var lastYearBt = new rwt.widgets.base.BasicButton( "push", true );
    var lastMonthBt = new rwt.widgets.base.BasicButton( "push", true );
    var monthYearLabel = new rwt.widgets.base.Label();
    var nextMonthBt = new rwt.widgets.base.BasicButton( "push", true );
    var nextYearBt = new rwt.widgets.base.BasicButton( "push", true );

    var wm = rwt.remote.WidgetManager.getInstance();
    wm.setToolTip( lastYearBt, "Previous year" );
    wm.setToolTip( lastMonthBt, "Previous month" );
    wm.setToolTip( nextMonthBt, "Next month" );
    wm.setToolTip( nextYearBt, "Next year" );

    lastYearBt.setTabIndex( null );
    lastYearBt.setUserData( "calendar-button", true );

    lastMonthBt.setTabIndex( null );
    lastMonthBt.setUserData( "calendar-button", true );

    nextMonthBt.setTabIndex( null );
    nextMonthBt.setUserData( "calendar-button", true );

    nextYearBt.setTabIndex( null );
    nextYearBt.setUserData( "calendar-button", true );

    lastYearBt.setAppearance("calendar-toolbar-previous-year-button");
    lastMonthBt.setAppearance("calendar-toolbar-previous-month-button");
    nextMonthBt.setAppearance("calendar-toolbar-next-month-button");
    nextYearBt.setAppearance("calendar-toolbar-next-year-button");

    lastYearBt.addEventListener("click", this._onNavButtonClicked, this);
    lastMonthBt.addEventListener("click", this._onNavButtonClicked, this);
    nextMonthBt.addEventListener("click", this._onNavButtonClicked, this);
    nextYearBt.addEventListener("click", this._onNavButtonClicked, this);

    this._lastYearBt = lastYearBt;
    this._lastMonthBt = lastMonthBt;
    this._nextMonthBt = nextMonthBt;
    this._nextYearBt = nextYearBt;

    monthYearLabel.setAppearance("calendar-monthyear");
    monthYearLabel.set( { width : "1*" } );

    navBar.add(lastYearBt, lastMonthBt, monthYearLabel, nextMonthBt, nextYearBt);
    this._monthYearLabel = monthYearLabel;

    // Create the date pane
    var datePane = new rwt.widgets.base.Parent();
    datePane.setAppearance("calendar-datepane");

    datePane.set( {
      width  : rwt.widgets.base.Calendar.CELL_WIDTH * 8,
      height : rwt.widgets.base.Calendar.CELL_HEIGHT * 7
    } );

    // Create the weekdays
    // Add an empty label as spacer for the week numbers
    var label = new rwt.widgets.base.Label();
    label.setAppearance( "calendar-week" );

    label.set( {
      width  : rwt.widgets.base.Calendar.CELL_WIDTH,
      height : rwt.widgets.base.Calendar.CELL_HEIGHT,
      left   : 0
    } );

    label.addState( "header" );
    datePane.add( label );

    this._weekdayLabelArr = [];

    for( var i=1; i < 8; i++ ) {
      var label = new rwt.widgets.base.Label();
      label.setAppearance( "calendar-weekday" );
      label.setSelectable( false );
      label.setCursor( "default" );

      label.set( {
        width  : rwt.widgets.base.Calendar.CELL_WIDTH,
        height : rwt.widgets.base.Calendar.CELL_HEIGHT,
        left   : i * rwt.widgets.base.Calendar.CELL_WIDTH
      } );

      datePane.add( label );
      this._weekdayLabelArr.push( label );
    }

    // Add the days
    this._dayLabelArr = [];
    this._weekLabelArr = [];

    for( var y = 0; y < 6; y++ ) {
      // Add the week label
      var label = new rwt.widgets.base.Label();
      label.setAppearance( "calendar-week" );
      label.setSelectable( false );
      label.setCursor( "default" );

      label.set( {
        width  : rwt.widgets.base.Calendar.CELL_WIDTH,
        height : rwt.widgets.base.Calendar.CELL_HEIGHT,
        left   : 0,
        top    : (y + 1) * rwt.widgets.base.Calendar.CELL_HEIGHT
      } );

      datePane.add( label );
      this._weekLabelArr.push( label );

      // Add the day labels
      for (var x=1; x < 8; x++) {
        var label = new rwt.widgets.base.Label();
        label.setAppearance( "calendar-day" );
        label.setSelectable( false );
        label.setCursor( "default" );

        label.set( {
          width  : rwt.widgets.base.Calendar.CELL_WIDTH,
          height : rwt.widgets.base.Calendar.CELL_HEIGHT,
          left   : x * rwt.widgets.base.Calendar.CELL_WIDTH,
          top    : (y + 1) * rwt.widgets.base.Calendar.CELL_HEIGHT
        } );

        label.addEventListener( "mousedown", this._onDayClicked, this );
        label.addEventListener( "dblclick", this._onDayDblClicked, this );
        label.addEventListener( "mouseover", this._onDayMouseOver, this );
        label.addEventListener( "mouseout", this._onDayMouseOut, this );
        label.setUserData( "calendar-day", true );
        datePane.add( label );
        this._dayLabelArr.push( label );
      }
    }

    // [if] The focus and key keypress event are handled by DateTimeCalendar
    // Make focusable
    // this.setTabIndex(1);
    // this.addEventListener("keypress", this._onkeypress);

    // Show the right date
    var shownDate = ( date != null ) ? date : new Date();
    this.showMonth( shownDate.getMonth(), shownDate.getFullYear() );

    // Add the main widgets
    this.add( navBar );
    this.add( datePane );

    this._navBar = navBar;
    this._datePane = datePane;

    // Initialize dimensions
    this.initWidth();
    this.initHeight();
  },

  /*
  *****************************************************************************
     EVENTS
  *****************************************************************************
  */

  events: {
    /** Fired when a date was selected. The event holds the new selected date in its data property.*/
    "select"     : "rwt.event.DataEvent"
  },

  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics : {
    CELL_WIDTH : 24,
    CELL_HEIGHT : 16,
    MONTH_NAMES : [],
    WEEKDAY_NAMES : []
  },

  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties : {
    width : {
      refine : true,
      init : "auto"
    },

    height : {
      refine : true,
      init : "auto"
    },

    /** The currently shown month. 0 = january, 1 = february, and so on. */
    shownMonth : {
      check : "Integer",
      init : null,
      nullable : true,
      event : "changeShownMonth"
    },

    /** The currently shown year. */
    shownYear : {
      check : "Integer",
      init : null,
      nullable : true,
      event : "changeShownYear"
    },

    /** {Date} The currently selected date. */
    date : {
      check : "Date",
      init : null,
      nullable : true,
      apply : "_applyDate",
      event : "changeDate",
      transform : "_checkDate"
    },

    minimum : {
      check : "Date",
      init : null,
      nullable : true,
      apply : "_applyMinimum"
    },

    maximum : {
      check : "Date",
      init : null,
      nullable : true,
      apply : "_applyMaximum"
    }
  },

  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members : {

    _getSubWidgets : function() {
      var result = [ this._monthYearLabel ];
      result.concat( this._weekdayLabelArr, this._dayLabelArr, this._weekLabelArr );
      return result;
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      var rtl = value === "rtl";
      this._lastYearBt.setDirection( value );
      this._lastMonthBt.setDirection( value );
      this._nextMonthBt.setDirection( value );
      this._nextYearBt.setDirection( value );
      this._navBar.setReverseChildrenOrder( rtl );
      this._navBar.setHorizontalChildrenAlign( rtl ? "right" : "left" );
      this._datePane.getLayoutImpl().setMirror( rtl );
      this._datePane.forEachChild( function() {
        this.setDirection( value );
      } );
    },

    // property checker
    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @return {var} TODOC
     */
    _checkDate : function(value) {
      // Use a clone of the date internally since date instances may be changed
      return (value == null) ? null : new Date(value.getTime());
    },

    // property modifier
    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyDate : function(value) {
      if ((value != null) && (this.getShownMonth() != value.getMonth() || this.getShownYear() != value.getFullYear())) {
        // The new date is in another month -> Show that month
        this.showMonth(value.getMonth(), value.getFullYear());
      } else {
        // The new date is in the current month -> Just change the states
        var newDay = (value == null) ? -1 : value.getDate();

        for (var i=0; i<6*7; i++) {
          var dayLabel = this._dayLabelArr[i];

          if (dayLabel.hasState("otherMonth")) {
            if (dayLabel.hasState("selected")) {
              dayLabel.removeState("selected");
            }
          } else {
            var day = parseInt( dayLabel.getText(), 10 );
            if( day == newDay ) {
              dayLabel.addState("selected");
            } else if (dayLabel.hasState("selected")) {
              dayLabel.removeState("selected");
            }
          }
        }
      }
    },

    /**
     * Event handler. Called when a navigation button has been clicked.
     *
     * @type member
     * @param evt {Map} the event.
     * @return {void}
     */
    _onNavButtonClicked : function(evt) {
      var year = this.getShownYear();
      var month = this.getShownMonth();

      switch(evt.getTarget()) {
        case this._lastYearBt:
          year--;
          break;

        case this._lastMonthBt:
          month--;

          if (month < 0) {
            month = 11;
            year--;
          }
          break;

        case this._nextMonthBt:
          month++;

          if (month >= 12) {
            month = 0;
            year++;
          }
          break;

        case this._nextYearBt:
          year++;
          break;
      }

      this.showMonth(month, year);
      this.setDate( new Date( year, month, 1 ) );
    },

    /**
     * Event handler. Called when a day has been clicked.
     *
     * @type member
     * @param evt {Map} the event.
     * @return {void}
     */
    _onDayClicked : function(evt) {
      if( evt.isLeftButtonPressed() && !evt.getTarget().hasState("disabled") ) {
        var time = evt.getTarget().dateTime;
        this.setDate(new Date(time));
      }
    },

    /**
     * TODOC
     *
     * @type member
     * @return {void}
     */
    _onDayDblClicked : function() {
      this.createDispatchDataEvent("select", this.getDate());
    },

    _onDayMouseOver : function( evt ) {
      evt.getTarget().addState( "over" );
    },

    _onDayMouseOut : function( evt ) {
      evt.getTarget().removeState( "over" );
    },

    /**
     * Event handler. Called when a key was pressed.
     *
     * @type member
     * @param evt {Map} the event.
     * @return {boolean | void} TODOC
     */
    _onkeypress : function( evt ) {
      var dayIncrement = null;
      var monthIncrement = null;
      var yearIncrement = null;
      var rtl = this.getDirection() === "rtl";
      if( evt.getModifiers() === 0 ) {
        switch( evt.getKeyIdentifier() ) {
          case "Left":
            dayIncrement = rtl ? 1 : -1;
            evt.preventDefault();
            evt.stopPropagation();
            break;

          case "Right":
            dayIncrement = rtl ? -1 : 1;
            evt.preventDefault();
            evt.stopPropagation();
            break;

          case "Up":
            dayIncrement = -7;
            evt.preventDefault();
            evt.stopPropagation();
            break;

          case "Down":
            dayIncrement = 7;
            evt.preventDefault();
            evt.stopPropagation();
            break;

          case "PageUp":
            monthIncrement = -1;
            evt.preventDefault();
            evt.stopPropagation();
            break;

          case "PageDown":
            monthIncrement = 1;
            evt.preventDefault();
            evt.stopPropagation();
            break;

          case "Home":
          case "End":
            evt.preventDefault();
            evt.stopPropagation();
            break;

          case "Enter":
            if (this.getDate() != null) {
              this.createDispatchDataEvent("select", this.getDate());
            }

            evt.preventDefault();
            evt.stopPropagation();
            return;
        }
      }
      else if (evt.isShiftPressed()) {
        switch(evt.getKeyIdentifier()) {
          case "PageUp":
            yearIncrement = -1;
            evt.preventDefault();
            evt.stopPropagation();
            break;

          case "PageDown":
            yearIncrement = 1;
            evt.preventDefault();
            evt.stopPropagation();
            break;
        }
      }

      if (dayIncrement != null || monthIncrement != null || yearIncrement != null) {
        var date = this.getDate();

        if (date != null) {
          date = new Date(date.getTime()); // TODO: Do cloning in getter
        }

        if (date == null) {
          date = new Date();
        } else {
          if( dayIncrement != null ) {
            date.setDate( date.getDate() + dayIncrement );
          }
          if( monthIncrement != null ) {
            date.setMonth( date.getMonth() + monthIncrement );
          }
          if( yearIncrement != null ) {
            date.setFullYear( date.getFullYear() + yearIncrement );
          }
        }

        this.setDate(date);
      }
    },

    // ***** Methods *****
    /**
     * Shows a certain month.
     *
     * @type member
     * @param month {Integer ? null} the month to show (0 = january). If not set the month
     *      will remain the same.
     * @param year {Integer ? null} the year to show. If not set the year will remain the
     *      same.
     * @return {void}
     */
    showMonth : function(month, year) {
      if ((month != null && month != this.getShownMonth()) || (year != null && year != this.getShownYear())) {
        if (month != null) {
          this.setShownMonth(month);
        }

        if (year != null) {
          this.setShownYear(year);
        }

        this._updateDatePane();
      }
    },

    /**
     * Updates the date pane.
     *
     * @type member
     * @return {void}
     */
    _updateDatePane : function() {
      var today = new Date();
      var todayYear = today.getFullYear();
      var todayMonth = today.getMonth();
      var todayDayOfMonth = today.getDate();

      var selDate = this.getDate();
      var selYear = (selDate == null) ? -1 : selDate.getFullYear();
      var selMonth = (selDate == null) ? -1 : selDate.getMonth();
      var selDayOfMonth = (selDate == null) ? -1 : selDate.getDate();

      var shownMonth = this.getShownMonth();
      var shownYear = this.getShownYear();

      var startOfWeek = this.__getWeekStart();

      // Create a help date that points to the first of the current month
      var helpDate = new Date(this.getShownYear(), this.getShownMonth(), 1);

      var year = this.getShownYear();
      var month = rwt.widgets.base.Calendar.MONTH_NAMES[ this.getShownMonth() ];
      this._monthYearLabel.setText( month + " " + year );

      // Show the day names
      var firstDayOfWeek = helpDate.getDay();
      var firstSundayInMonth = (1 + 7 - firstDayOfWeek) % 7;

      for (var i=0; i<7; i++) {
        var day = (i + startOfWeek) % 7;

        var dayLabel = this._weekdayLabelArr[i];

        helpDate.setDate(firstSundayInMonth + day);

        var weekdayName = rwt.widgets.base.Calendar.WEEKDAY_NAMES[ helpDate.getDay() + 1 ];

        dayLabel.setText( weekdayName );

        dayLabel.toggleState( "weekend", this.__isWeekend( day ) );
      }

      // Show the days
      helpDate = new Date( shownYear, shownMonth, 1 );
      var nrDaysOfLastMonth = (7 + firstDayOfWeek - startOfWeek) % 7;
      helpDate.setDate(helpDate.getDate() - nrDaysOfLastMonth);

      for (var week=0; week<6; week++) {
        this._weekLabelArr[week].setText("" + this.__getWeekInYear(helpDate));

        for (var i=0; i<7; i++) {
          var dayLabel = this._dayLabelArr[week * 7 + i];

          var year = helpDate.getFullYear();
          var month = helpDate.getMonth();
          var dayOfMonth = helpDate.getDate();
          var minimum = this.getMinimum();
          var maximum = this.getMaximum();

          var isBeforeMinimumLimit = minimum && helpDate.getTime() < minimum.getTime();
          var isAfterMaximumLimit = maximum && helpDate.getTime() > maximum.getTime();
          var isSelectedDate = (selYear == year && selMonth == month && selDayOfMonth == dayOfMonth);
          var isToday = (year == todayYear && month == todayMonth && dayOfMonth == todayDayOfMonth);

          dayLabel.toggleState( "selected", isSelectedDate );
          dayLabel.toggleState( "otherMonth", month != shownMonth );
          dayLabel.toggleState( "today", isToday );
          dayLabel.toggleState( "disabled", isBeforeMinimumLimit || isAfterMaximumLimit );

          dayLabel.setText("" + dayOfMonth);
          dayLabel.dateTime = helpDate.getTime();

          // Go to the next day
          helpDate.setDate(helpDate.getDate() + 1);
        }
      }
    },

    /**
     * Returns the thursday in the same week as the date.
     *
     * @type member
     * @param date {Date} the date to get the thursday of.
     * @return {Date} the thursday in the same week as the date.
     */
    __thursdayOfSameWeek : function(date) {
      return new Date(date.getTime() + (3 - ((date.getDay() + 6) % 7)) * 86400000);
    },

    /**
     * Returns the week in year of a date.
     *
     * @type member
     * @param date {Date} the date to get the week in year of.
     * @return {Integer} the week in year.
     */
    __getWeekInYear : function(date) {
      // This algorithm gets the correct calendar week after ISO 8601.
      // This standard is used in almost all european countries.
      // TODO: In the US week in year is calculated different!
      // See http://www.merlyn.demon.co.uk/weekinfo.htm
      // The following algorithm comes from http://www.salesianer.de/util/kalwoch.html
      // Get the thursday of the week the date belongs to
      var thursdayDate = this.__thursdayOfSameWeek(date);

      // Get the year the thursday (and therefor the week) belongs to
      var weekYear = thursdayDate.getFullYear();

      // Get the thursday of the week january 4th belongs to
      // (which defines week 1 of a year)
      var thursdayWeek1 = this.__thursdayOfSameWeek(new Date(weekYear, 0, 4));

      // Calculate the calendar week
      return Math.floor(1.5 + (thursdayDate.getTime() - thursdayWeek1.getTime()) / 86400000 / 7);
    },

    /**
     * Return the day the week starts with
     *
     * Reference: Common Locale Data Repository (cldr) supplementalData.xml
     *
     * @type member
     * @return {Integer} index of the first day of the week. 0=sunday, 1=monday, ...
     */
    __getWeekStart : function() {
      var weekStart = {
        // default is monday
        "MV" : 5, // friday
        "AE" : 6, // saturday
        "AF" : 6,
        "BH" : 6,
        "DJ" : 6,
        "DZ" : 6,
        "EG" : 6,
        "ER" : 6,
        "ET" : 6,
        "IQ" : 6,
        "IR" : 6,
        "JO" : 6,
        "KE" : 6,
        "KW" : 6,
        "LB" : 6,
        "LY" : 6,
        "MA" : 6,
        "OM" : 6,
        "QA" : 6,
        "SA" : 6,
        "SD" : 6,
        "SO" : 6,
        "TN" : 6,
        "YE" : 6,
        "AS" : 0, // sunday
        "AU" : 0,
        "AZ" : 0,
        "BW" : 0,
        "CA" : 0,
        "CN" : 0,
        "FO" : 0,
        "GE" : 0,
        "GL" : 0,
        "GU" : 0,
        "HK" : 0,
        "IE" : 0,
        "IL" : 0,
        "IS" : 0,
        "JM" : 0,
        "JP" : 0,
        "KG" : 0,
        "KR" : 0,
        "LA" : 0,
        "MH" : 0,
        "MN" : 0,
        "MO" : 0,
        "MP" : 0,
        "MT" : 0,
        "NZ" : 0,
        "PH" : 0,
        "PK" : 0,
        "SG" : 0,
        "TH" : 0,
        "TT" : 0,
        "TW" : 0,
        "UM" : 0,
        "US" : 0,
        "UZ" : 0,
        "VI" : 0,
        "ZA" : 0,
        "ZW" : 0,
        "MW" : 0,
        "NG" : 0,
        "TJ" : 0
      };

      var territory = this.__getTerritory();

      // default is monday
      return weekStart[territory] != null ? weekStart[territory] : 1;
    },

    /**
     * Return the day the weekend starts with
     *
     * Reference: Common Locale Data Repository (cldr) supplementalData.xml
     *
     * @type member
     * @return {Integer} index of the first day of the weekend. 0=sunday, 1=monday, ...
     */
    __getWeekendStart : function() {
      var weekendStart = {
        // default is saturday
        "EG" : 5, // friday
        "IL" : 5,
        "SY" : 5,
        "IN" : 0, // sunday
        "AE" : 4, // thursday
        "BH" : 4,
        "DZ" : 4,
        "IQ" : 4,
        "JO" : 4,
        "KW" : 4,
        "LB" : 4,
        "LY" : 4,
        "MA" : 4,
        "OM" : 4,
        "QA" : 4,
        "SA" : 4,
        "SD" : 4,
        "TN" : 4,
        "YE" : 4
      };

      var territory = this.__getTerritory();

      // default is saturday
      return weekendStart[territory] != null ? weekendStart[territory] : 6;
    },

    /**
     * Return the day the weekend ends with
     *
     * Reference: Common Locale Data Repository (cldr) supplementalData.xml
     *
     * @type member
     * @return {Integer} index of the last day of the weekend. 0=sunday, 1=monday, ...
     */
    __getWeekendEnd : function() {
      var weekendEnd = {
        // default is sunday
        "AE" : 5, // friday
        "BH" : 5,
        "DZ" : 5,
        "IQ" : 5,
        "JO" : 5,
        "KW" : 5,
        "LB" : 5,
        "LY" : 5,
        "MA" : 5,
        "OM" : 5,
        "QA" : 5,
        "SA" : 5,
        "SD" : 5,
        "TN" : 5,
        "YE" : 5,
        "AF" : 5,
        "IR" : 5,
        "EG" : 6, // saturday
        "IL" : 6,
        "SY" : 6
      };

      var territory = this.__getTerritory();

      // default is sunday
      return weekendEnd[territory] != null ? weekendEnd[territory] : 0;
    },

    /**
     * Returns whether a certain day of week belongs to the week end.
     *
     * @type member
     * @param day {Integer} index of the day. 0=sunday, 1=monday, ...
     * @return {Boolean} whether the given day is a weekend day
     */
    __isWeekend : function(day) {
      var weekendStart = this.__getWeekendStart();
      var weekendEnd = this.__getWeekendEnd();

      if (weekendEnd > weekendStart) {
        return ((day >= weekendStart) && (day <= weekendEnd));
      } else {
        return ((day >= weekendStart) || (day <= weekendEnd));
      }
    },

    /**
     * Extract the territory part from a locale
     *
     * @type member
     * @return {String} territory
     */
    __getTerritory : function() {
      return rwt.client.Client.getTerritory().toUpperCase();
    },

    _applyMinimum : function() {
      this._updateDatePane();
    },

    _applyMaximum : function() {
      this._updateDatePane();
    }
  },

  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeObjects("_lastYearBt", "_lastMonthBt", "_nextMonthBt", "_nextYearBt", "_monthYearLabel");

    this._disposeObjectDeep("_weekdayLabelArr", 1);
    this._disposeObjectDeep("_dayLabelArr", 1);
    this._disposeObjectDeep("_weekLabelArr", 1);
  }
} );
