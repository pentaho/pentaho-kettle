/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

/*jshint newcap: false */
namespace( "rwt.runtime" );

rwt.runtime.Singletons = {

  _holder : {},
  _sequence : 0,

  get : function( type ) {
    var id = this._getId( type );
    if( !this._holder[ id ] ) {
      this._holder[ id ] = new type();
    }
    return this._holder[ id ];
  },

  clear : function( type ) {
    if( type ) {
      this._clearId( this._getId( type ) );
    } else {
      for( var id in this._holder ) {
        this._dispose( this._holder[ id ] );
      }
      this._holder = {};
    }
  },

  // NOTE: Marked "private" since it's supposed to be a temporary solution that should not be used
  //       anywhere but TestUtil.js. Remove once tests don't need it anymore.
  _clearExcept : function( types ) {
    for( var id in this._holder ) {
      if( !this._isException( this._holder[ id ], types ) ) {
        this._clearId( id );
      }
    }
  },

  _isException : function( instance, exceptions ) {
    for( var i = 0; i < exceptions.length; i++ ) {
      if( instance instanceof exceptions[ i ] ) {
        return true;
      }
    }
    return false;
  },

  _clearId : function( id ) {
    if( this._holder[ id ] ) {
      this._dispose( this._holder[ id ] );
    }
    delete this._holder[ id ];
  },

  _getId : function( type ) {
    if( typeof type.__singleton === "undefined" ) {
      type.__singleton = "s" + this._sequence++;
    }
    return type.__singleton;
  },

  _dispose : function( instance ) {
    if( typeof instance.destroy === "function" ) {
      instance.destroy();
    } else if( typeof instance.dispose === "function" ) {
      instance.dispose();
    }
  }

};

