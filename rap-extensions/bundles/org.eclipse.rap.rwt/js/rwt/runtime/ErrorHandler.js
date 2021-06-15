/*******************************************************************************
 * Copyright (c) 2011, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

/*global console: false */

rwt.qx.Class.define( "rwt.runtime.ErrorHandler", {

  statics : {

    _overlay : null,
    _box : null,

    processJavaScriptErrorInResponse : function( script, error, currentRequest ) {
      var content = this._getErrorPageHeader();
      content += "<pre>" + this._gatherErrorDetails( error, script, currentRequest ) + "</pre>";
      this.showErrorBox( "client error", true, content );
    },

    processJavaScriptError : function( error ) {
      this.errorObject = error; // for later inspection by developer
      if( typeof console === "object" ) {
        var msg = "Error: " + ( error.message ? error.message : error );
        if( typeof console.error !== "undefined" ) { // IE returns "object" for typeof
          console.error( msg );
        } else if( typeof console.log !== "undefined" ) {
          console.log( msg );
        }
        if( typeof console.log === "function" && error.stack ) {
          console.log( "Error stack:\n" + error.stack );
        } else if( typeof console.trace !== "undefined" ) {
          console.trace();
        }
      }
      var debug = true;
      try {
        debug = rwt.util.Variant.isSet( "qx.debug", "on" );
      } catch( ex ) {
        // ignore: Variant may not be loaded yet
      }
      if( debug ) {
        var content = this._getErrorPageHeader();
        content += "<pre>" + this._gatherErrorDetails( error ) + "</pre>";
        this.showErrorBox( "client error", true, content );
        throw error;
      }
    },

    showErrorPage : function( content ) {
      this._enableTextSelection();
      this._freezeApplication();
      document.title = "Error Page";
      this._createErrorPageArea().innerHTML = content;
    },

    showErrorBox : function( errorType, freeze, errorDetails ) {
      if( freeze ) {
        this._freezeApplication();
      }
      if( errorType !== "connection error" ) {
        this._unloadAllIframes();
      }
      this._overlay = this._createOverlay();
      this._box = this._createErrorBoxArea( 450, 150 );
      var themeStore = rwt.theme.ThemeStore.getInstance();
      var border = themeStore.getBorder( "ErrorBox", {}, "border" );
      border.renderElement( this._box );
      this._box.style.padding = "0px";
      this._box.style.overflow = "hidden";
      var errorBoxData = this._getErrorBoxData( errorType );
      this._title = this._createErrorBoxTitleArea( this._box );
      this._title.innerHTML = errorBoxData.title;
      this._description = this._createErrorBoxDescriptionArea( this._box );
      this._description.innerHTML = errorBoxData.description;
      this._action = this._createErrorBoxActionArea( this._box );
      var actions = errorBoxData.action;
      if( errorDetails && errorDetails.trim().length > 0 ) {
        actions += this._getDetailsAction();
      }
      if( actions ) {
        this._action.innerHTML = actions;
      }
      var hyperlink = this._action.getElementsByTagName( "a" )[ 0 ];
      if( hyperlink ) {
        this._styleHyperlinkAsButton( hyperlink );
        hyperlink.focus();
      }
      var detailsHyperlink = document.getElementById( "rwt_detailsActionHyperlink" );
      if( detailsHyperlink ) {
        this._styleHyperlinkAsButton( detailsHyperlink );
        var self = this;
        detailsHyperlink.addEventListener( "click", function() { self.showErrorPage( errorDetails ); } );
      }
    },

    _getDetailsAction : function() {
      var detailsMessage = rwt.client.ClientMessages.getInstance().getMessage( "Details" );
      return "<a id=\"rwt_detailsActionHyperlink\" href=\"javascript:void(0)\">" + detailsMessage + "</a>";
    },

    showWaitHint : function() {
      this._overlay = this._createOverlay();
      var themeStore = rwt.theme.ThemeStore.getInstance();
      var cssElement = "SystemMessage-DisplayOverlay";
      var icon = themeStore.getSizedImage( cssElement, {}, "background-image" );
      if( icon && icon[ 0 ] ) {
        this._box = this._createErrorBoxArea( icon[ 1 ], icon[ 2 ] );
        rwt.html.Style.setBackgroundImage( this._box, icon[ 0 ] );
        this._box.style.backgroundColor = "transparent";
        this._box.style.border = "none";
        this._box.style.overflow = "hidden";
      }
    },

    hideErrorBox : function() {
      if( this._box ) {
        this._box.parentNode.removeChild( this._box );
        this._box = null;
      }
      if( this._overlay ) {
        this._overlay.parentNode.removeChild( this._overlay );
        this._overlay = null;
      }
      rwt.event.EventHandler.setBlockKeyEvents( false );
    },

    _getErrorPageHeader : function() {
       var errorBoxData = this._getErrorBoxData( "client error" );
       var result = "<h2>" + errorBoxData.title + "</h2>";
       result += "<h3>" + errorBoxData.details + "</h3>";
       result += "<hr/>";
       return result;
    },

    _gatherErrorDetails : function( error, script, currentRequest ) {
      var info = [];
      try {
        info.push( "Error: " + error + "\n" );
        if( script ) {
          info.push( "Script: " + script );
        }
        if( error instanceof Error ) {
          for( var key in error ) { // NOTE : does not work in webkit (no iteration)
            info.push( key + ": " + error[ key ] );
          }
          if( error.stack ) { // ensures stack is printed in webkit, might be printed twice in gecko
            info.push( "Stack: " + error.stack );
          }
       }
        info.push( "Debug: " + rwt.util.Variant.get( "qx.debug" ) );
        if( currentRequest ) {
          info.push( "Request: " + currentRequest.getData() );
        }
        var inFlush = rwt.widgets.base.Widget._inFlushGlobalQueues;
        if( inFlush ) {
          info.push( "Phase: " + rwt.widgets.base.Widget._flushGlobalQueuesPhase );
        }
      } catch( ex ) {
        // ensure we get a info no matter what
      }
      return info.join( "\n  " );
    },

    _createOverlay : function() {
      var element = document.createElement( "div" );
      var themeStore = rwt.theme.ThemeStore.getInstance();
      var color = themeStore.getColor( "SystemMessage-DisplayOverlay", {}, "background-color" );
      var alpha = themeStore.getAlpha( "SystemMessage-DisplayOverlay", {}, "background-color" );
      var style = element.style;
      style.position = "fixed";
      style.width = "100%";
      style.height = "100%";
      style.backgroundColor = color === "undefined" ? "transparent" : color;
      style.opacity = alpha;
      style.zIndex = 100000000;
      document.body.appendChild( element );
      rwt.event.EventHandler.setBlockKeyEvents( true );
      return element;
    },

    _createErrorPageArea : function() {
      var element = document.createElement( "div" );
      var style = element.style;
      style.position = "absolute";
      style.width = "100%";
      style.height = "100%";
      style.backgroundColor = "#ffffff";
      style.zIndex = 100000001;
      style.overflow = "auto";
      style.padding = "10px";
      document.body.appendChild( element );
      return element;
    },

    _createErrorBoxArea : function( width, height ) {
      var element = document.createElement( "div" );
      var style = element.style;
      style.position = "fixed";
      style.width = width + "px";
      style.height = height + "px";
      var doc = rwt.widgets.base.ClientDocument.getInstance();
      var left = ( doc.getClientWidth() - width ) / 2;
      var top = ( doc.getClientHeight() - height ) / 2;
      style.left = ( left < 0 ? 0 : left ) + "px";
      style.top = ( top < 0 ? 0 : top ) + "px";
      style.zIndex = 100000001;
      style.padding = "10px";
      style.textAlign = "center";
      style.fontFamily = 'verdana,"lucida sans",arial,helvetica,sans-serif';
      style.fontSize = "12px";
      style.fontStyle = "normal";
      style.fontWeight = "normal";
      document.body.appendChild( element );
      return element;
    },

    _createErrorBoxTitleArea : function( parentElement ) {
      var element = document.createElement( "div" );
      var themeStore = rwt.theme.ThemeStore.getInstance();
      var cssElement = "ErrorBox-Titlebar";
      var color = themeStore.getColor( cssElement, {}, "color" );
      var font = themeStore.getFont( cssElement, {}, "font" );
      var backgroundColor = themeStore.getColor( cssElement, {}, "background-color" );
      var style = element.style;
      style.position = "absolute";
      style.left = "0px";
      style.top = "0px";
      style.width = "100%";
      style.height = "40px";
      style.padding = "10px";
      style.textAlign = "left";
      style.color = color;
      style.font = font.toCss();
      style.backgroundColor = backgroundColor;
      parentElement.appendChild( element );
      return element;
    },

    _createErrorBoxDescriptionArea : function( parentElement ) {
      var element = document.createElement( "div" );
      var themeStore = rwt.theme.ThemeStore.getInstance();
      var cssElement = "ErrorBox";
      var color = themeStore.getColor( cssElement, {}, "color" );
      var font = themeStore.getFont( cssElement, {}, "font" );
      var backgroundColor = themeStore.getColor( cssElement, {}, "background-color" );
      var style = element.style;
      style.position = "absolute";
      style.left = "0px";
      style.top = "40px";
      style.width = "100%";
      style.height = "70px";
      style.padding = "10px";
      style.overflow = "auto";
      style.textAlign = "left";
      style.font = font.toCss();
      style.color = color;
      style.backgroundColor = backgroundColor;
      parentElement.appendChild( element );
      return element;
    },

    _createErrorBoxActionArea : function( parentElement ) {
      var element = document.createElement( "div" );
      var style = element.style;
      style.position = "absolute";
      style.left = "0px";
      style.top = "110px";
      style.width = "100%";
      style.height = "40px";
      style.padding = "10px";
      style.textAlign = "center";
      style.borderTop = "1px solid #CCCCCC";
      style.backgroundColor = "#F2F2F2";
      style.fontSize = "14px";
      parentElement.appendChild( element );
      return element;
    },

    _freezeApplication : function() {
      try {
        var display = rwt.widgets.Display.getCurrent();
        display.setExitConfirmation( null );
        //qx.io.remote.RequestQueue.getInstance().setEnabled( false );
        rwt.event.EventHandler.detachEvents();
        rwt.qx.Target.prototype.dispatchEvent = function() {};
        rwt.animation.Animation._stopLoop();
      } catch( ex ) {
        try {
          console.log( "_freezeApplication exception: " + ex );
        } catch( exTwo ) {
          // ignore
        }
      }
    },

    _unloadAllIframes : function() {
      var iframes = document.getElementsByTagName( "iframe" );
      for( var i = 0; i < iframes.length; i++ ) {
        iframes[ i ].src = rwt.remote.Connection.RESOURCE_PATH + "static/html/blank.html";
      }
    },

    _enableTextSelection : function() {
      var doc = rwt.widgets.base.ClientDocument.getInstance();
      doc.setSelectable( true );
      if( rwt.client.Client.isGecko() ) {
        var EventHandlerUtil = rwt.event.EventHandlerUtil;
        document.documentElement.removeEventListener( "mousedown",
                                                      EventHandlerUtil._ffMouseFixListener,
                                                      false );
      }
    },

    _getErrorBoxData : function( errorType ) {
      var result = {
        title : "",
        description : "",
        details : "",
        action : ""
      };
      var messages = rwt.client.ClientMessages.getInstance();
      switch( errorType ) {
        case "invalid request counter":
        case "request failed":
          result.title = messages.getMessage( "ServerError" );
          result.description = messages.getMessage( "ServerErrorDescription" );
          result.action = "<a href=\"" + this._getRestartURL() + "\">"
                        + messages.getMessage( "Restart" ) + "</a>";
          break;
        case "session timeout":
          result.title = messages.getMessage( "SessionTimeout" );
          result.description = messages.getMessage( "SessionTimeoutDescription" );
          result.action = "<a href=\"" + this._getRestartURL() + "\">"
                        + messages.getMessage( "Restart" ) + "</a>";
          break;
        case "connection error":
          result.title = messages.getMessage( "ConnectionError" );
          result.description = messages.getMessage( "ConnectionErrorDescription" );
          result.action = "<a href=\"javascript:rwt.remote.Connection.getInstance()._retry();\">"
                        + messages.getMessage( "Retry" ) + "</a>";
          break;
        case "client error":
          result.title = messages.getMessage( "ClientError" );
          result.description = messages.getMessage( "ClientErrorDescription" );
          result.details = messages.getMessage( "Details" );
          result.action = "<a href=\"" + this._getRestartURL() + "\">"
                        + messages.getMessage( "Restart" ) + "</a>";
          break;
        default:
          result.title = messages.getMessage( "ServerError" );
          result.description = messages.getMessage( "ServerErrorDescription" );
          result.action = "<a href=\"" + this._getRestartURL() + "\">"
                        + messages.getMessage( "Restart" ) + "</a>";
      }
      result.title = rwt.util.Encoding.replaceNewLines( result.title, "" );
      result.description = rwt.util.Encoding.replaceNewLines( result.description, "<br/>" );
      return result;
    },

    _getRestartURL : function() {
      var result = String( window.location );
      var index = result.indexOf( "#" );
      if( index != -1 ) {
        result = result.substring( 0, index );
      }
      return result;
    },

    _styleHyperlinkAsButton : function( element ) {
      var style = element.style;
      style.outline = "none";
      style.textDecoration = "none";
      style.backgroundColor = "#E8E8E8";
      style.color = "#4a4a4a";
      style.padding = "5px 15px";
      style.margin = "5px";
      style.borderTop = "1px solid #CCCCCC";
      style.borderRight = "1px solid #333333";
      style.borderBottom = "1px solid #333333";
      style.borderLeft = "1px solid #CCCCCC";
    }

  }

} );
