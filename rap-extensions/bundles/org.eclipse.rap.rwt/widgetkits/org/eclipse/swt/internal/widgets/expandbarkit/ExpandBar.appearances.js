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

  "expand-bar" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "ExpandBar", "border" );
      result.font = tv.getCssFont( "ExpandBar", "font" );
      result.textColor = tv.getCssColor( "ExpandBar", "color" );
      return result;
    }
  },

  "expand-item" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        overflow : "hidden",
        border : tv.getCssBorder( "ExpandItem", "border" ),
        chevronIcon : tv.getCssSizedImage( "ExpandItem-Button", "background-image" )
      };
    }
  },

  "expand-item-header" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        width : "100%",
        horizontalChildrenAlign : "left",
        padding : [ 0, 4, 0, 4 ],
        border : tv.getCssBorder( "ExpandItem-Header", "border" ),
        backgroundColor : tv.getCssColor( "ExpandItem-Header", "background-color" ),
        cursor : tv.getCssCursor( "ExpandItem-Header", "cursor" ),
        backgroundImage : tv.getCssImage( "ExpandItem-Header", "background-image" ),
        backgroundGradient : tv.getCssGradient( "ExpandItem-Header", "background-image" ),
        textShadow : tv.getCssShadow( "ExpandItem-Header", "text-shadow" )
      };
    }
  }

// END TEMPLATE //
};
