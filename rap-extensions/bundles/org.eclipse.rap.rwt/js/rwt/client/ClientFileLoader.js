/*******************************************************************************
 * Copyright (c) 2015, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.client" );

(function() {

  rwt.client.ClientFileLoader = {

    load : function( params ) {
      loadFile( params.file, params.type );
    }

  };

  function loadFile( url, type ) {
    rwt.remote.MessageProcessor.pauseExecution();
    var element = createElement( url, type );
    element.onload = function() {
      rwt.remote.MessageProcessor.continueExecution();
      element.onload = null;
    };
    document.getElementsByTagName( "head" )[ 0 ].appendChild( element );
  }

  function createElement( url, type ) {
    var element;
    if (type === "css") {
      element = document.createElement( "link" );
      element.rel = "stylesheet";
      element.type = "text/css";
      element.href = url;
    } else {
      element = document.createElement( "script" );
      element.type = "text/javascript";
      element.src = url;
    }
    return element;
  }

})();
