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

  "group-box" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundColor : tv.getCssColor( "Group", "background-color" ),
        border : tv.getCssBorder( "Group", "border" ),
        font : tv.getCssFont( "Group", "font"),
        textColor : tv.getCssColor( "Group", "color" )
      };
    }
  },

  "group-box-legend" : {
    include : "atom",
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        border : tv.getCssBorder( "Group-Label", "border" ),
        padding : tv.getCssBoxDimensions( "Group-Label", "padding" ),
        margin : tv.getCssBoxDimensions( "Group-Label", "margin" ),
        backgroundColor : tv.getCssColor( "Group-Label", "background-color" ),
        backgroundImage : tv.getCssImage( "Group-Label", "background-image" ),
        backgroundRepeat : tv.getCssIdentifier( "Group-Label", "background-repeat" ),
        backgroundPosition : tv.getCssIdentifier( "Group-Label", "background-position" ),
        backgroundGradient : tv.getCssGradient( "Group-Label", "background-image" ),
        font : tv.getCssFont( "Group", "font"),
        textColor : tv.getCssColor( "Group-Label", "color" ),
        textShadow : tv.getCssShadow( "Group-Label", "text-shadow" )
      };
    }
  },

  "group-box-frame" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var margin = tv.getCssBoxDimensions( "Group-Frame", "margin" );
      return {
        top : margin[ 0 ],
        right : margin[ 1 ],
        bottom : margin[ 2 ],
        left : margin[ 3 ],
        border : tv.getCssBorder( "Group-Frame", "border" )
      };
    }
  }

// END TEMPLATE //
};
