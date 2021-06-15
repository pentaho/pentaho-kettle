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

rwt.define( "rwt.scripting", {} );

(function(){

// TODO [rst] Define directly using rwt.define, remove surrounding function scope
rwt.scripting.FunctionFactory = {

  createFunction : function( functionScript, name ) {
    var result;
    var code = [
      functionScript,
      "\n\n",
      "typeof ",
      name,
      " === \"undefined\" ? null : ",
      name,
      ";" ];
    try {
      result = this._secureEval.apply( window, [ code.join( "" ) ] );
    } catch( ex ) {
      var msg = "Could not parse Script for " + name + ":" + ( ex.message ? ex.message : ex );
      throw new Error( msg );
    }
    if( typeof result !== "function" ) {
      throw new Error( "Script does not define a function " + name );
    }
    return result;
  },

  _secureEval : function() {
    return eval( arguments[ 0 ] );
  }

};

}());
