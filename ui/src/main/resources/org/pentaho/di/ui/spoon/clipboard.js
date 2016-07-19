(function(){
  'use strict';

  rap.registerTypeHandler( "webSpoon.Clipboard", {
      factory : function( properties ) {
        this._widget = new webSpoon.Clipboard( properties );
        if ( rwt.client.Client._browserName === 'explorer' ) {
          var doc = rwt.widgets.base.ClientDocument.getInstance();
          doc.addEventListener( "keydown", this._widget._onKeyDown, this._widget );
        }
        return this._widget;
      },
      destructor : function () {
        if ( rwt.client.Client._browserName === 'explorer' ) {
          var doc = rwt.widgets.base.ClientDocument.getInstance();
          doc.removeEventListener( "keydown", this._widget._onKeyDown, this._widget );
        }
      },
      properties : [ "text" ],
      events : [ "paste", "copy", "cut" ],
      methods : [ "downloadCanvasImage" ]
  } );

  rwt.define( "webSpoon" );

  webSpoon.Clipboard = function ( properties ) {
    var x = document.createElement("INPUT");
    x.setAttribute( "id", "input-clipboard" );
    x.setAttribute( "remoteObjectId", properties.self );
    x.addEventListener( "paste", function( event ) {
      var obj = rap.getObject( this.getAttribute( 'remoteObjectid' ) );
      var remoteObject = rap.getRemoteObject( obj );
      var text = '';
      if ( typeof event.clipboardData === 'undefined' ) {
        text = window.clipboardData.getData( 'Text' ); // IE
      } else {
        text = event.clipboardData.getData( 'text/plain' );
      }
      event.preventDefault();
      remoteObject.notify( "paste", { "text": text, "widgetId": event.target.value } );
      console.log('paste');
    }, this );
    x.addEventListener( "copy", function( event ) {
      var obj = rap.getObject( this.getAttribute( 'remoteObjectid' ) );
      var remoteObject = rap.getRemoteObject( obj );
      if ( typeof event.clipboardData === 'undefined' ) {
        window.clipboardData.setData( 'Text', obj.getText() ); // IE
      } else {
        event.clipboardData.setData( 'text/plain', obj.getText() );
      }
      event.preventDefault();
      if ( rwt.client.Client._browserName != 'explorer' ) {
        remoteObject.notify( "copy", { "widgetId": event.target.value } );
        console.log('copy');
      }
    }, this );
    x.addEventListener( "cut", function( event ) {
      var obj = rap.getObject( this.getAttribute( 'remoteObjectid' ) );
      var remoteObject = rap.getRemoteObject( obj );
      if ( typeof event.clipboardData === 'undefined' ) {
        window.clipboardData.setData( 'Text', obj.getText() ); // IE
      } else {
        event.clipboardData.setData( 'text/plain', obj.getText() );
      }
      event.preventDefault();
      remoteObject.notify( "cut", { "widgetId": event.target.value } );
      console.log('cut');
    }, this );
    document.body.appendChild( x );
  };

  webSpoon.Clipboard.prototype = {
    setText : function( text ) {
      this._text = text;
    },

    getText : function() {
      return this._text;
    },

    downloadCanvasImage : function( obj ) {
      var i = 0;
      for ( ; i < document.getElementsByTagName( 'canvas' ).length; i++ ) {
        if ( document.getElementsByTagName( 'canvas' )[ i ].rwtObject._rwtId === obj.rwtId + ".gc" ) {
          break;
        }
      }
      var canvas = document.getElementsByTagName( 'canvas' )[ i ];
      if ( window.navigator && window.navigator.msSaveOrOpenBlob ) { // For IE and Edge
        var blob = canvas.msToBlob();
        window.navigator.msSaveOrOpenBlob( blob, obj.name + ".png" );
      } else {
        var data = canvas.toDataURL();
        var link = document.createElement( 'a' )
        link.setAttribute( 'download', obj.name + ".png" );
        link.setAttribute( 'href', data.replace( "image/png", "image/octet-stream" ) );
        document.body.appendChild( link );
        link.click();
        document.body.removeChild( link );
      }
    },

    _onKeyDown : function( event ) {
      const keyName = event._valueDomEvent.key;

      if ( keyName === 'Control' ) {
        return;
      }

      if ( event._valueDomEvent.ctrlKey ) {
        var x = document.getElementById( "input-clipboard" );
        x.select();
        var remoteObject = rap.getRemoteObject( this );
        if ( keyName === 'c' ) {
          document.execCommand( 'copy' );
          remoteObject.notify( "copy", { "widgetId": x.value } );
          console.log('copy');
        } else if ( keyName === 'x' ) {
          /*
           * cut event cannot be invoked programmatically on IE11 for some reason,
           * so capture ctrl+x then execute copy (instead of cut) and notify cut.
           */
          document.execCommand( 'copy' );
          remoteObject.notify( "cut", { "widgetId": x.value } );
          console.log('cut');
        }
      }
    }
  };
}());