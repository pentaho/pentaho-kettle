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
/*jshint unused:false */
var appearances = {
// BEGIN TEMPLATE //

  "datetime-date" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "DateTime", "border" );
      result.font = tv.getCssFont( "DateTime", "font" );
      result.textColor = tv.getCssColor( "DateTime", "color" );
      result.backgroundColor = tv.getCssColor( "DateTime", "background-color" );
      result.backgroundGradient = tv.getCssGradient( "DateTime", "background-image" );
      result.textShadow = tv.getCssShadow( "DateTime", "text-shadow" );
      result.shadow = tv.getCssShadow( "DateTime", "box-shadow" );
      return result;
    }
  },

  "datetime-time" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "DateTime", "border" );
      result.font = tv.getCssFont( "DateTime", "font" );
      result.textColor = tv.getCssColor( "DateTime", "color" );
      result.backgroundColor = tv.getCssColor( "DateTime", "background-color" );
      result.backgroundGradient = tv.getCssGradient( "DateTime", "background-image" );
      result.textShadow = tv.getCssShadow( "DateTime", "text-shadow" );
      result.shadow = tv.getCssShadow( "DateTime", "box-shadow" );
      return result;
    }
  },

  "datetime-calendar" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "DateTime", "border" );
      result.font = tv.getCssFont( "DateTime", "font" );
      result.textColor = tv.getCssColor( "DateTime", "color" );
      result.backgroundColor = tv.getCssColor( "DateTime", "background-color" );
      result.textShadow = tv.getCssShadow( "DateTime", "text-shadow" );
      return result;
    }
  },

  "datetime-field" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {
        cursor : "default",
        textAlign : "center",
        padding : [ 0, 3 ]
      };
      if( !states.disabled ) {
        result.textColor = tv.getCssColor( "DateTime-Field", "color" );
        result.backgroundColor = tv.getCssColor( "DateTime-Field", "background-color" );
      } else {
        result.textColor = tv.getCssColor( "*", "color" );
        result.backgroundColor = "undefined";
      }
      result.textShadow = tv.getCssShadow( "DateTime-Field", "text-shadow" );
      return result;
    }
  },

  "datetime-separator" : {
    style : function() {
      return {
        cursor : "default"
      };
    }
  },

  "datetime-button-up" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.border = tv.getCssBorder( "DateTime-UpButton", "border" );
      result.width = tv.getCssDimension( "DateTime-UpButton", "width" );
      result.icon = tv.getCssSizedImage( "DateTime-UpButton-Icon", "background-image" );
      if( result.icon === rwt.theme.ThemeValues.NONE_IMAGE ) {
        result.icon = tv.getCssSizedImage( "DateTime-UpButton", "background-image" );
      } else {
        result.backgroundImage = tv.getCssImage( "DateTime-UpButton", "background-image" );
      }
      result.backgroundGradient = tv.getCssGradient( "DateTime-UpButton", "background-image" );
      result.backgroundColor = tv.getCssColor( "DateTime-UpButton", "background-color" );
      result.cursor = tv.getCssCursor( "DateTime-UpButton", "cursor" );
      return result;
    }
  },

  "datetime-button-down" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.border = tv.getCssBorder( "DateTime-DownButton", "border" );
      result.width = tv.getCssDimension( "DateTime-DownButton", "width" );
      result.icon = tv.getCssSizedImage( "DateTime-DownButton-Icon", "background-image" );
      if( result.icon === rwt.theme.ThemeValues.NONE_IMAGE ) {
        result.icon = tv.getCssSizedImage( "DateTime-DownButton", "background-image" );
      } else {
        result.backgroundImage = tv.getCssImage( "DateTime-DownButton", "background-image" );
      }
      result.backgroundGradient = tv.getCssGradient( "DateTime-DownButton", "background-image" );
      result.backgroundColor = tv.getCssColor( "DateTime-DownButton", "background-color" );
      result.cursor = tv.getCssCursor( "DateTime-DownButton", "cursor" );
      return result;
    }
  },

  "datetime-drop-down-button" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "DateTime-DropDownButton", "border" );
      result.icon = tv.getCssSizedImage( "DateTime-DropDownButton-Icon", "background-image" );
      if( result.icon === rwt.theme.ThemeValues.NONE_IMAGE ) {
        result.icon = tv.getCssSizedImage( "DateTime-DropDownButton", "background-image" );
      } else {
        result.backgroundImage = tv.getCssImage( "DateTime-DropDownButton", "background-image" );
      }
      result.backgroundGradient = tv.getCssGradient( "DateTime-DropDownButton", "background-image" );
      result.backgroundColor = tv.getCssColor( "DateTime-DropDownButton", "background-color" );
      result.cursor = tv.getCssCursor( "DateTime-DropDownButton", "cursor" );
      return result;
    }
  },

  "datetime-drop-down-calendar" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "DateTime-DropDownCalendar", "border" );
      result.backgroundColor = tv.getCssColor( "DateTime", "background-color" );
      return result;
    }
  },

  //------------------------------------------------------------------------
  // Calendar

  "calendar-navBar" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        border : tv.getCssBorder( "DateTime-Calendar-Navbar", "border" ),
        backgroundColor : tv.getCssColor( "DateTime-Calendar-Navbar", "background-color" ),
        backgroundImage : tv.getCssImage( "DateTime-Calendar-Navbar", "background-image" ),
        backgroundGradient : tv.getCssGradient( "DateTime-Calendar-Navbar", "background-image" ),
        padding : [ 4, 4, 4, 4 ]
      };
    }
  },

  "calendar-toolbar-button" : {
    style : function( states ) {
      var result = {
        spacing : 4,
        width : 16,
        height : 16,
        clipWidth : 16,
        clipHeight : 16,
        verticalChildrenAlign : "middle"
      };
      if (states.pressed || states.checked || states.abandoned) {
        result.padding = [ 2, 0, 0, 2 ];
      } else {
        result.padding = 2;
      }
      return result;
    }
  },

  "calendar-toolbar-previous-year-button" : {
    include: "calendar-toolbar-button",

    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        icon : tv.getCssSizedImage( "DateTime-Calendar-PreviousYearButton", "background-image" ),
        cursor : tv.getCssCursor( "DateTime-Calendar-PreviousYearButton", "cursor" )
      };
    }
  },

  "calendar-toolbar-previous-month-button" : {
    include: "calendar-toolbar-button",

    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        icon : tv.getCssSizedImage( "DateTime-Calendar-PreviousMonthButton", "background-image" ),
        cursor : tv.getCssCursor( "DateTime-Calendar-PreviousMonthButton", "cursor" )
      };
    }
  },

  "calendar-toolbar-next-month-button" : {
    include: "calendar-toolbar-button",

    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        icon : tv.getCssSizedImage( "DateTime-Calendar-NextMonthButton", "background-image" ),
        cursor : tv.getCssCursor( "DateTime-Calendar-NextMonthButton", "cursor" )
      };
    }
  },

  "calendar-toolbar-next-year-button" : {
    include: "calendar-toolbar-button",

    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        icon : tv.getCssSizedImage( "DateTime-Calendar-NextYearButton", "background-image" ),
        cursor : tv.getCssCursor( "DateTime-Calendar-NextYearButton", "cursor" )
      };
    }
  },

  "calendar-monthyear" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        font : tv.getCssFont( "DateTime-Calendar-Navbar", "font" ),
        textAlign : "center",
        textColor : tv.getCssColor( "DateTime-Calendar-Navbar", "color" ),
        textShadow : tv.getCssShadow( "DateTime-Calendar-Navbar", "text-shadow" ),
        verticalAlign : "middle",
        cursor : "default"
      };
    }
  },

  "calendar-datepane" : {
    style : function( states ) {
      return {
        backgroundColor : "undefined"
      };
    }
  },

  "calendar-week" : {
    style : function( states ) {
      var borderWidths;
      if( states.header ) {
        borderWidths = states.rwt_RIGHT_TO_LEFT ? [ 0, 0, 1, 1 ] : [ 0, 1, 1, 0 ];
      } else {
        borderWidths = states.rwt_RIGHT_TO_LEFT ? [ 0, 0, 0, 1 ] : [ 0, 1, 0, 0 ];
      }
      return {
        textAlign : "center",
        verticalAlign : "middle",
        border : new rwt.html.Border( borderWidths, "solid", "gray" )
      };
    }
  },

  "calendar-weekday" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var border = new rwt.html.Border( [ 0, 0, 1, 0 ], "solid", "gray" );
      // FIXME: [if] Bigger font size leads to text cutoff
      var font = tv.getCssFont( "DateTime", "font" );
      var smallFont = rwt.html.Font.fromString( font.toCss() );
      smallFont.setSize( 11 );
      return {
        font : smallFont,
        border : border,
        textAlign : "center"
      };
    }
  },

  "calendar-day" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {
        textAlign : "center",
        verticalAlign : "middle"
      };
      if( states.disabled ) {
        result.textColor = tv.getCssColor( "*", "color" );
        result.backgroundColor = "undefined";
      } else {
        result.textColor = tv.getCssColor( "DateTime-Calendar-Day", "color" );
        result.backgroundColor = tv.getCssColor( "DateTime-Calendar-Day", "background-color" );
      }
      result.border = tv.getCssBorder( "DateTime-Calendar-Day", "border" );
      result.textShadow = tv.getCssShadow( "DateTime-Calendar-Day", "text-shadow" );
      return result;
    }
  }

// END TEMPLATE //
};
