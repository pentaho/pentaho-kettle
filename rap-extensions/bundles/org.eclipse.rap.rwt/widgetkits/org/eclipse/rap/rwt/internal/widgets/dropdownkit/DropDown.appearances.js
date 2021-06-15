/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
/*jshint unused:false */
var appearances = {
// BEGIN TEMPLATE //

  "dropdown" : {
    style : function() {
      return {};
    }
  },

  "dropdown-popup" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        border : tv.getCssBorder( "DropDown", "border" ),
        shadow : tv.getCssShadow( "DropDown", "box-shadow" )
      };
    }
  },

  "dropdown-row" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        foreground : tv.getCssColor( "DropDown-Item", "color" ),
        background : tv.getCssColor( "DropDown-Item", "background-color" ),
        backgroundImage : tv.getCssImage( "DropDown-Item", "background-image" ),
        backgroundGradient : tv.getCssGradient( "DropDown-Item", "background-image" ),
        textDecoration : tv.getCssIdentifier( "DropDown-Item", "text-decoration" ),
        textShadow : tv.getCssShadow( "DropDown-Item", "text-shadow" )
      };
    }
  },

  "dropdown-row-overlay" : {
    style : function() {
      return {
        foreground : "undefined",
        background : "undefined",
        backgroundImage : null,
        backgroundGradient : null
      };
    }
  },

  "dropdown-cell" : {
    style : function( states ) {
       var tv = new rwt.theme.ThemeValues( states );
       return {
         padding : tv.getCssBoxDimensions( "DropDown-Item", "padding" )
       };
    }
  }

// END TEMPLATE //
};
