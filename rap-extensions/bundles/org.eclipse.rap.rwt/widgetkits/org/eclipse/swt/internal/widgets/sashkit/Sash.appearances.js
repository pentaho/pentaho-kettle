/*******************************************************************************
 * Copyright (c) 2007, 2012 Innoopract Informationssysteme GmbH and others.
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

  "sash" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundColor : tv.getCssColor( "Sash", "background-color" ),
        backgroundImage : tv.getCssImage( "Sash", "background-image" ),
        border : tv.getCssBorder( "Sash", "border" ),
        cursor : states.disabled ? "undefined" : states.horizontal ? "row-resize" : "col-resize"
      };
    }
  },

  "sash-slider" : {
    style : function() {
      return {
        zIndex : 1e7,
        opacity : 0.3,
        backgroundColor : "black"
      };
    }
  },

  "sash-handle" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.backgroundImage = tv.getCssImage( "Sash-Handle", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "Sash-Handle", "background-image" );
      result.backgroundRepeat = "no-repeat";
      return result;
    }
  }

// END TEMPLATE //
};
