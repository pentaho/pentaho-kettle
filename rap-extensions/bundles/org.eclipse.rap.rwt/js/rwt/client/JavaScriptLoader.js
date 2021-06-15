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

namespace( "rwt.client" );

rwt.client.JavaScriptLoader = {

  load : function( params ) {
    if( params.files.length !== 1 ) {
      throw new Error( "JavaScriptLoader does not support parallel script loading" );
    }
    rwt.remote.MessageProcessor.pauseExecution();
    this._loadFile( params.files[ 0 ] );
  },

  _loadFile : function( file ) {
    var scriptElement = document.createElement( "script" );
    scriptElement.type = "text/javascript";
    scriptElement.src = file;
    this._attachLoadedCallback( scriptElement );
    document.getElementsByTagName( "head" )[ 0 ].appendChild( scriptElement );
  },

  _attachLoadedCallback : function( scriptElement ) {
    scriptElement.onload = function() {
      rwt.remote.MessageProcessor.continueExecution();
      scriptElement.onload = null;
    };
  }

};
