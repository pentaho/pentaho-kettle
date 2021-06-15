/*******************************************************************************
 * Copyright (c) 2007, 2014 Innoopract Informationssysteme GmbH and others.
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

  "window" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      // padding is only applied on the server, since client area content is
      // positioned absolutely
      result.backgroundColor = tv.getCssColor( "Shell", "background-color" );
      result.backgroundImage = tv.getCssImage( "Shell", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "Shell", "background-image" );
      result.border = tv.getCssBorder( "Shell", "border" );
      result.minWidth = states.rwt_TITLE ? 80 : 5;
      result.minHeight = states.rwt_TITLE ? 25 : 5;
      result.opacity = tv.getCssFloat( "Shell", "opacity" );
      result.shadow = tv.getCssShadow( "Shell", "box-shadow" );
      result.animation = tv.getCssAnimation( "Shell", "animation" );
      return result;
    }
  },

  "window-captionbar" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {
        verticalChildrenAlign : "middle"
      };
      result.margin = tv.getCssBoxDimensions( "Shell-Titlebar", "margin" );
      result.padding = tv.getCssBoxDimensions( "Shell-Titlebar", "padding" );
      result.textColor = tv.getCssColor( "Shell-Titlebar", "color" );
      result.backgroundColor = tv.getCssColor( "Shell-Titlebar", "background-color" );
      result.backgroundImage = tv.getCssImage( "Shell-Titlebar", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "Shell-Titlebar", "background-image" );
      result.border = tv.getCssBorder( "Shell-Titlebar", "border" );
      if( states.rwt_TITLE ) {
        result.minHeight = tv.getCssDimension( "Shell-Titlebar", "height" );
      } else {
        result.minHeight = 0;
      }
      result.maxHeight = result.minHeight;
      result.textShadow = tv.getCssShadow( "Shell-Titlebar", "text-shadow" );
      return result;
    }
  },

  "window-resize-frame" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        border : tv.getCssNamedBorder( "shadow" )
      };
    }
  },

  "window-captionbar-icon" : {
    style : function() {
      return {
        marginRight : 2
      };
    }
  },

  "window-captionbar-title" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        cursor : "default",
        font : tv.getCssFont( "Shell-Titlebar", "font" ),
        marginRight : 2
      };
    }
  },

  "window-captionbar-minimize-button" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.icon = tv.getCssSizedImage( "Shell-MinButton", "background-image" );
      result.margin = tv.getCssBoxDimensions( "Shell-MinButton", "margin" );
      return result;
    }
  },

  "window-captionbar-maximize-button" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.icon = tv.getCssSizedImage( "Shell-MaxButton", "background-image" );
      result.margin = tv.getCssBoxDimensions( "Shell-MaxButton", "margin" );
      return result;
    }
  },

  "window-captionbar-restore-button" : {
    include : "window-captionbar-maximize-button"
  },

  "window-captionbar-close-button" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.icon = tv.getCssSizedImage( "Shell-CloseButton", "background-image" );
      result.margin = tv.getCssBoxDimensions( "Shell-CloseButton", "margin" );
      return result;
    }
  },

  "window-statusbar" : {
    style : function() {
      return {};
    }
  },

  "window-statusbar-text" : {
    style : function() {
      return {};
    }
  }

// END TEMPLATE //
};
