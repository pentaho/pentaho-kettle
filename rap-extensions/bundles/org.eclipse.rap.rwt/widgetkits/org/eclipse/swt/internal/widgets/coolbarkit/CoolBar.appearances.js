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

  "coolbar" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "*", "border" );
      result.backgroundGradient = tv.getCssGradient( "CoolBar", "background-image" );
      result.backgroundImage = tv.getCssImage( "CoolBar", "background-image" );
      return result;
    }
  },

  "coolitem" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "*", "border" );
      return result;
    }
  },

  "coolitem-handle" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      if( states.vertical ) {
        result.height = tv.getCssDimension( "CoolItem-Handle", "width" );
      } else {
        result.width = tv.getCssDimension( "CoolItem-Handle", "width" );
      }
      result.border = tv.getCssBorder( "CoolItem-Handle", "border" );
      result.margin = [ 1, 2, 1, 0 ];
      result.cursor = "col-resize";
      return result;
    }
  }

// END TEMPLATE //
};
