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

rwt.client.JavaScriptExecutor = function() {

  this.execute = function( code ) {
    eval( code );
  };

};

rwt.client.JavaScriptExecutor.getInstance = function() {
  return rwt.runtime.Singletons.get( rwt.client.JavaScriptExecutor );
};
