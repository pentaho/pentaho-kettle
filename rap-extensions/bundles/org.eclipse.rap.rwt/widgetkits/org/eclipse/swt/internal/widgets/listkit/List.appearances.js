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

  "list" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.cursor = "default";
      result.overflow = "hidden";
      result.font = tv.getCssFont( "List", "font" );
      result.textColor = tv.getCssColor( "List", "color" );
      result.backgroundColor = tv.getCssColor( "List", "background-color" );
      result.border = tv.getCssBorder( "List", "border" );
      return result;
    }
  },

  "list-item" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {
        height : "auto",
        horizontalChildrenAlign : "left",
        verticalChildrenAlign : "top",
        spacing : 4
      };
      var gridColor = tv.getCssColor( "List-GridLine", "color" );
      if( gridColor !== "undefined" ) {
        result.border = new rwt.html.Border( [ 0, 0, 1, 0 ], "solid", gridColor );
      }
      var textColor = tv.getCssColor( "List-Item", "color" );
      result.textColor = textColor === "undefined" ? "inherit" : textColor;
      result.backgroundColor = tv.getCssColor( "List-Item", "background-color" );
      result.backgroundImage = tv.getCssImage( "List-Item", "background-image" );
      result.backgroundRepeat = tv.getCssIdentifier( "List-Item", "background-repeat" );
      result.backgroundPosition = tv.getCssIdentifier( "List-Item", "background-position" );
      result.backgroundGradient = tv.getCssGradient( "List-Item", "background-image" );
      result.textShadow = tv.getCssShadow( "List-Item", "text-shadow" );
      result.padding = tv.getCssBoxDimensions( "List-Item", "padding" );
      return result;
    }
  }

// END TEMPLATE //
};
