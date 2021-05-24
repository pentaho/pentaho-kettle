/*******************************************************************************
 * Copyright (c) 2007, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Browser", {

  extend : rwt.widgets.base.Iframe,

  construct : function() {
    this.base( arguments );
    this._browserFunctions = {};
    // TODO [rh] preliminary workaround to make Browser accessible by tab
    this.setTabIndex( 1 );
    this.setSelectable( true );
    this.setAppearance( "browser" );
    this.addEventListener( "create", this._onCreate, this );
  },

  properties : {

    asynchronousResult : {
      check : "Boolean",
      init : false
    },

    executedFunctionPending : {
      check : "Boolean",
      init : false
    },

    executedFunctionResult : {
      nullable : true,
      init : null
    },

    executedFunctionError : {
      check : "String",
      nullable : true,
      init : null
    }

  },

  statics : {

    getDomain : function( url ) {
      var domain = null;
      if( url !== null ) {
        var lowerCaseUrl = url.toLowerCase();
        // Accepted limitation: In case of other protocls this detection fails.
        var defaultPort;
        if( lowerCaseUrl.indexOf( "http://" ) === 0 ) {
          defaultPort = 80;
        } else if( lowerCaseUrl.indexOf( "https://" ) === 0 ) {
          defaultPort = 443;
        } else if( lowerCaseUrl.indexOf( "ftp://" ) === 0 ) {
          defaultPort = 21;
        } else if( lowerCaseUrl.indexOf( "ftps://" ) === 0 ) {
          defaultPort = 990;
        }
        if( defaultPort ) {
          domain = lowerCaseUrl.slice( lowerCaseUrl.indexOf( "://" ) + 3 );
          var pathStart = domain.indexOf( "/" );
          if( pathStart !== -1 ) {
            domain = domain.slice( 0, pathStart );
          }
          if( domain.indexOf( ":" ) === -1 ) {
            domain += ":" + defaultPort;
          }
        }
      }
      return domain;
    }

  },

  members : {

    syncSource : function() {
      if( this.isCreated() ) {
        this._syncSource();
      }
    },

    // overwritten
    _applySource : function() {
      // server syncs manually
    },

    // overwritten
    _applyEnabled : function( value, oldValue ) {
      this.base( arguments, value, oldValue );
      if( value ) {
        this.release();
      } else {
        this.block();
      }
    },

    // overwritten
    release : function() {
      if( this.getEnabled() ) {
        this.base( arguments );
      }
    },

    _onload : function( evt ) {
      // syncSource in destroy may cause unwanted load event when widget is about to be disposed
      if( !this._isInGlobalDisposeQueue && !this.isDisposed() ) {
        this.base( arguments, evt );
        if( this._isContentAccessible() ) {
          this._attachBrowserFunctions();
        }
        this._sendProgressEvent();
      }
    },

    _onCreate : function() {
      if( !this.getEnabled() ) {
        this.block();
      }
    },

    _sendProgressEvent : function() {
      // The IFrame destroy method does not mark the widget as disposed (or in dispose) immediately,
      // and getRemoteObject can fail if the widget has already been removed from ObjectRegistry
      if( rwt.remote.ObjectRegistry.containsObject( this ) ) {
        rwt.remote.Connection.getInstance().getRemoteObject( this ).notify( "Progress" );
      }
    },

    execute : function( script ) {
      // NOTE [tb] : For some very strange reason the access check must not be done directly
      // before the try-catch for the ipad to recognize the error is may throw.
      this._checkIframeAccess();
      var success = true;
      var result = null;
      try {
        result = this._parseEvalResult( this._eval( script ) );
      } catch( ex ) {
        success = false;
      }
      var connection = rwt.remote.Connection.getInstance();
      var id = rwt.remote.ObjectRegistry.getId( this );
      var method = success ? "evaluationSucceeded" : "evaluationFailed";
      var properties = success ? { "result" : result } : {};
      connection.getMessageWriter().appendCall( id, method, properties );
      if( this.getExecutedFunctionPending() ) {
        connection.sendImmediate( false );
      } else {
        connection.send();
      }
    },

    _srcInLocalDomain : function() {
      var src = this.getSource();
      var statics = rwt.widgets.Browser;
      var localDomain = statics.getDomain( document.URL );
      var srcDomain = statics.getDomain( src );
      var isSameDomain = localDomain === srcDomain;
      var isRelative = srcDomain === null;
      return isRelative || isSameDomain;
    },

    _isContentAccessible : function() {
      if( !this._isLoaded ) {
        return false;
      }
      var accessible = false;
      try {
        var src = this.getSource() || "";
        src = src.split( "?" )[ 0 ];
        if( src.indexOf( "://" ) === -1 ) { // relative path?
          src = document.URL; // works since we only check that the domain matches
        }
        accessible = src.indexOf( this.getContentDocument().domain ) !== -1;
      } catch( ex ) {
        // ignore
      }
      return accessible;
    },

    _checkIframeAccess : function() {
      if( !this._isContentAccessible() ) {
        var isSameDomain = this._srcInLocalDomain();
        if( !isSameDomain ) {
          this._throwSecurityException( false );
        }
        if( this._isLoaded && isSameDomain ) {
          // not accessible when it appears it should be
          // => user navigated to external site.
          this._throwSecurityException( true );
        }
      }
    },

    _throwSecurityException : function( domainUnkown ) {
      var statics = rwt.widgets.Browser;
      var localDomain = statics.getDomain( document.URL );
      var srcDomain = domainUnkown ? null : statics.getDomain( this.getSource() );
      var msg = "SecurityRestriction:\nBrowser-Widget can not access ";
      msg +=   srcDomain !== null
             ? "\"" + srcDomain + "\""
             : "unkown domain";
      msg += " from \"" + localDomain + "\".";
      throw new Error( msg );
    },

    _eval : function( script ) {
      var win = this.getContentWindow();
      if( !win[ "eval" ] && win[ "execScript" ] ) {
        // Workaround for IE bug, see: http://www.thismuchiknow.co.uk/?p=25
        win.execScript( "null;", "JScript" );
      }
      return win[ "eval" ]( script );
    },

    _parseEvalResult : function( value ) {
      var result = null;
      var win = this.getContentWindow();
      // NOTE: This mimics the behavior of the evaluate method in SWT:
      if( value instanceof win.Function || value instanceof Function ) {
        result = this.toJSON( [ [] ] );
      } else if( value instanceof win.Array || value instanceof Array ) {
        result = this.toJSON( [ value ] );
      } else if( typeof value !== "object" && typeof value !== "function" ) {
        // above: some browser say regular expressions of the type "function"
        result = this.toJSON( [ value ] );
      }
      return result;
    },

    createFunction : function( name ) {
      this._browserFunctions[ name ] = true;
      this._checkIframeAccess();
      if( this.isLoaded() ) {
        try {
          if ( name === "closeWindow" ) {
            this._createAsyncFunctionImpl( name );
          } else {
            this._createFunctionImpl( name );
          }
          this._createFunctionWrapper( name );
        } catch( e ) {
          var msg = "Unable to create function: \"" + name + "\".\n" + e;
          if( rwt.remote.EventUtil.getSuspended() ) {
            throw msg;
          } else {
            rwt.runtime.ErrorHandler.processJavaScriptError( msg );
          }
        }
      }
    },

    _attachBrowserFunctions : function() {
      // NOTE: In case the user navigates to a page outside the domain,
      // this function will not be triggered due to the lack of a loading event.
      // That also means that this is the only case were creating a browser
      // function in a cross-domain scenario silently fails.
      for( var name in this._browserFunctions ) {
        this.createFunction( name );
      }
    },

    _createFunctionImpl : function( name ) {
      var win = this.getContentWindow();
      var connection = rwt.remote.Connection.getInstance();
      var id = rwt.remote.ObjectRegistry.getId( this );
      var that = this;
      win[ name + "_impl" ] = function() {
        var result = {};
        try {
          if( that.getExecutedFunctionPending() ) {
            result.error = "Unable to execute browser function \""
              + name
              + "\". Another browser function is still pending.";
          } else {
            var properties = {
              "name" : name,
              "arguments" : that.toJSON( arguments )
            };
            connection.getMessageWriter().appendCall( id, "executeFunction", properties );
            that.setExecutedFunctionResult( null );
            that.setExecutedFunctionError( null );
            that.setExecutedFunctionPending( true );
            that.setAsynchronousResult( false );
            connection.sendImmediate( false );
            if( that.getExecutedFunctionPending() ) {
              that.setAsynchronousResult( true );
            } else {
              var error = that.getExecutedFunctionError();
              if( error != null ) {
                result.error = error;
              } else {
                result.result = that.getExecutedFunctionResult();
              }
            }
          }
        } catch( ex ) {
          rwt.runtime.ErrorHandler.processJavaScriptError( ex );
        }
        return result;
      };
    },

    // Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=483075
    _createAsyncFunctionImpl : function( name ) {
      var win = this.getContentWindow();
      var connection = rwt.remote.Connection.getInstance();
      var id = rwt.remote.ObjectRegistry.getId( this );
      var that = this;
      win[ name + "_impl" ] = function() {
        var result = {};
        try {
          var properties = {
            "name" : name,
            "arguments" : that.toJSON( arguments )
          };
          connection.getMessageWriter().appendCall( id, "executeFunction", properties );
          connection.sendImmediate( true );
        } catch( ex ) {
          rwt.runtime.ErrorHandler.processJavaScriptError( ex );
        }
        return result;
      };
    },

    // [if] This wrapper function is a workaround for bug 332313
    _createFunctionWrapper : function( name ) {
      var script = [];
      script.push( "window." + name + " = function(){" );
      script.push( "  var result = " + name + "_impl.apply( window, arguments );" );
      script.push( "  if( result.error ) {" );
      script.push( "    throw new Error( result.error );" );
      script.push( "  }" );
      script.push( "  return result.result;" );
      script.push( "}");
      this._eval( script.join( "" ) );
    },

    destroyFunction :function( name ) {
      delete this._browserFunctions[ name ];
      var win = this.getContentWindow();
      if( win != null ) {
        try {
          var script = [];
          script.push( "delete window." +  name + ";" );
          script.push( "delete window." +  name + "_impl;" );
          this._eval( script.join( "" ) );
        } catch( e ) {
          throw new Error( "Unable to destroy function: " + name + " error: " + e );
        }
      }
    },

    setFunctionResult : function( name, result, error ) {
      this.setExecutedFunctionResult( result );
      this.setExecutedFunctionError( error );
      this.setExecutedFunctionPending( false );
    },

    toJSON : function( object ) {
      var result;
      var type = typeof object;
      if( object === null ) {
        result = object;
      } else if( type === "object" ) {
        result = [];
        for( var i = 0; i < object.length; i++ ) {
          result.push( this.toJSON( object[ i ] ) );
        }
      } else if( type === "function" ) {
        result = String( object );
      } else {
        result = object;
      }
      return result;
    },

    destroy : function() {
      this.base( arguments );
      // Needed for IE dipose fix in Iframe.js because _applySource is overwritten in Browser.js
      this.syncSource();
    }

  }

} );
