/*******************************************************************************
 * Copyright (c) 2007, 2017 Innoopract Informationssysteme GmbH and others.
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

  "menu" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        width : "auto",
        height : "auto",
        textColor : tv.getCssColor( "Menu", "color" ),
        backgroundColor : tv.getCssColor( "Menu", "background-color" ),
        backgroundImage : tv.getCssImage( "Menu", "background-image" ),
        backgroundGradient : tv.getCssGradient( "Menu", "background-image" ),
        animation : tv.getCssAnimation( "Menu", "animation" ),
        font : tv.getCssFont( "Menu", "font" ),
        overflow : "hidden",
        border : tv.getCssBorder( "Menu", "border" ),
        padding : tv.getCssBoxDimensions( "Menu", "padding" ),
        opacity : tv.getCssFloat( "Menu", "opacity" ),
        shadow : tv.getCssShadow( "Menu", "box-shadow" )
      };
    }
  },

  "menu-item" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {
        spacing : 2,
        padding : tv.getCssBoxDimensions( "MenuItem", "padding" ),
        backgroundImage : tv.getCssImage( "MenuItem", "background-image" ),
        backgroundGradient : tv.getCssGradient( "MenuItem", "background-image" ),
        backgroundColor : tv.getCssColor( "MenuItem", "background-color" ),
        height : states.onMenuBar ? "100%" : "auto",
        opacity : tv.getCssFloat( "MenuItem", "opacity" ),
        textShadow : tv.getCssShadow( "MenuItem", "text-shadow" )
      };
      result.textColor = tv.getCssColor( "MenuItem", "color" );
      if( states.cascade ) {
        result.arrow = tv.getCssSizedImage( "MenuItem-CascadeIcon", "background-image" );
      } else {
        result.arrow = null;
      }
      if( states.selected ) {
        if( states.check ) {
           result.selectionIndicator
             = tv.getCssSizedImage( "MenuItem-CheckIcon", "background-image" );
        } else if( states.radio ) {
           result.selectionIndicator
             = tv.getCssSizedImage( "MenuItem-RadioIcon", "background-image" );
        }
      } else {
        if( states.radio ) {
          var radioWidth = tv.getCssSizedImage( "MenuItem-RadioIcon", "background-image" )[ 1 ];
          result.selectionIndicator = [ null, radioWidth, 0 ];
        } else if( states.check ) {
          var checkWidth = tv.getCssSizedImage( "MenuItem-CheckIcon", "background-image" )[ 1 ];
          result.selectionIndicator = [ null, checkWidth, 0 ];
        } else {
          result.selectionIndicator = null;
        }
      }
      return result;
    }
  },

  "menu-separator" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        height : "auto",
        marginTop : 3,
        marginBottom : 2,
        padding : tv.getCssBoxDimensions( "MenuItem", "padding" ),
        backgroundImage : tv.getCssImage( "MenuItem", "background-image" ),
        backgroundGradient : tv.getCssGradient( "MenuItem", "background-image" ),
        backgroundColor : tv.getCssColor( "MenuItem", "background-color" ),
        opacity : tv.getCssFloat( "MenuItem", "opacity" )
      };
    }
  },

  "menu-separator-line" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        right : 0,
        left : 0,
        height : 0,
        border : tv.getCssNamedBorder( "verticalDivider" )
      };
    }
  }

// END TEMPLATE //
};
