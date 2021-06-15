/*******************************************************************************
 * Copyright (c) 2012, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.theme.ThemeStore", {

  factory : function() {
    return rwt.theme.ThemeStore.getInstance();
  },

  service : true,

  methods : [
    "loadActiveTheme",
    "loadFallbackTheme"
  ],

  methodHandler : {
    "loadActiveTheme" : function( object, params ) {
      var request = new rwt.remote.Request( params.url, "GET", "application/json" );
      request.setAsynchronous( false );
      request.setSuccessHandler( function( event ) {
        var result = JSON.parse( event.responseText );
        object.defineValues( result.values );
        object.setThemeCssValues( params.url, result.theme, false );
        object.setCurrentTheme( params.url );
      } );
      request.send();
    },
    "loadFallbackTheme" : function( object, params ) {
      var request = new rwt.remote.Request( params.url, "GET", "application/json" );
      request.setAsynchronous( false );
      request.setSuccessHandler( function( event ) {
        var result = JSON.parse( event.responseText );
        object.defineValues( result.values );
        object.setThemeCssValues( params.url, result.theme, true );
      } );
      request.send();
    }
  }

} );
