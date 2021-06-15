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

  "scale" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        border : tv.getCssBorder( "Scale", "border" ),
        font : tv.getCssFont( "*", "font" ),
        textColor : tv.getCssColor( "*", "color" ),
        backgroundColor : tv.getCssColor( "Scale", "background-color" ),
        backgroundImage : tv.getCssImage( "Scale", "background-image" ),
        backgroundGradient : tv.getCssGradient( "Scale", "background-image" ),
      };
    }
  },

  "scale-line" : {
    include : "image",

    style : function( states ) {
      var result = {};
      var path = rwt.remote.Connection.RESOURCE_PATH + "widget/rap/scale/";
      if( states[ "rwt_HORIZONTAL" ] ) {
        result.left = rwt.widgets.Scale.PADDING;
        result.top = rwt.widgets.Scale.SCALE_LINE_OFFSET;
        result.source = path + "h_line.gif";
      } else {
        result.left = rwt.widgets.Scale.SCALE_LINE_OFFSET;
        result.top = rwt.widgets.Scale.PADDING;
        result.source = path + "v_line.gif";
      }
      return result;
    }
  },

  "scale-thumb" : {
    include : "atom",

    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      if( states[ "rwt_HORIZONTAL" ] ) {
        result.left = rwt.widgets.Scale.PADDING;
        result.top = rwt.widgets.Scale.THUMB_OFFSET;
        // TODO: make it themable
        result.width = 11;
        result.height = 21;
      } else {
        result.left = rwt.widgets.Scale.THUMB_OFFSET;
        result.top = rwt.widgets.Scale.PADDING;
        // TODO: make it themable
        result.width = 21;
        result.height = 11;
      }
      result.border = tv.getCssBorder( "Scale-Thumb", "border" );
      result.backgroundColor = tv.getCssColor( "Scale-Thumb", "background-color" );
      result.backgroundImage = tv.getCssImage( "Scale-Thumb", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "Scale-Thumb", "background-image" );
      return result;
    }
  }

// END TEMPLATE //
};
