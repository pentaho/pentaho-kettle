/*******************************************************************************
 * Copyright (c) 2004, 2018 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

namespace( "rwt.event" );


rwt.event.EventHandlerUtil = {

  FIREFOX_NEW_KEY_EVENTS_VERSION : 65,

  _lastUpDownType : {},
  _lastKeyCode : null,

  cleanUp : function() {
    delete this.__onKeyEvent;
    delete this._lastUpDownType;
    delete this._lastKeyCode;
  },

  applyBrowserFixes  : rwt.util.Variant.select( "qx.client", {
    "gecko" : function() {
      // Fix for bug 295475:
      // Prevent url-dropping in FF as a whole (see bug 304651)
      var doc = rwt.widgets.base.ClientDocument.getInstance();
      doc.getElement().setAttribute( "ondrop", "event.preventDefault();" );
      var docElement = document.documentElement;
      // also see ErrorHandler.js#_enableTextSelection
      this._ffMouseFixListener = function( event ) {
        var tagName = null;
        try {
          tagName = event.originalTarget.tagName;
        } catch( e ) {
          // Firefox bug: On the very first mousedown, access to the events target
          // is forbidden and causes an error.
        }
        // NOTE: See also Bug 321372
        if( event.button === 0 && tagName != null && tagName != "INPUT" && tagName != "SELECT" ) {
          event.preventDefault();
        }
      };
      docElement.addEventListener( "mousedown", this._ffMouseFixListener, false );
    },
    "default" : function() { }
  } ),

  /////////////////////////
  // GENERAL EVENT HANDLING

  getDomTarget : rwt.util.Variant.select( "qx.client", {
    "webkit|blink" : function( vDomEvent ) {
      var vNode = vDomEvent.target || vDomEvent.srcElement;
      // Safari takes text nodes as targets for events
      if( vNode && ( vNode.nodeType == rwt.html.Nodes.TEXT ) ) {
        vNode = vNode.parentNode;
      }
      return vNode;
    },
    "default" : function( vDomEvent ) {
      return vDomEvent.target;
    }
  } ),

  stopDomEvent : function( vDomEvent ) {
    vDomEvent._prevented = true;
    if( vDomEvent.preventDefault ) {
      vDomEvent.preventDefault();
    }
    try {
      // this allows us to prevent some key press events in IE and Firefox.
      // See bug #1049
      vDomEvent.keyCode = 0;
    } catch( ex ) {
      // do nothing
    }
    vDomEvent.returnValue = false;
  },

  wasStopped : function( domEvent ) {
    return domEvent._prevented ? true : false;
  },


  blockUserDomEvents : function( element, value ) {
    if( value ) {
      for( var i = 0; i < this._userEventTypes.length; i++ ) {
        element.addEventListener( this._userEventTypes[ i ], this._domEventBlocker, false );
      }
    } else {
      for( var i = 0; i < this._userEventTypes.length; i++ ) {
        element.removeEventListener( this._userEventTypes[ i ], this._domEventBlocker, false );
      }
    }
  },

  _userEventTypes : [
    "mouseover",
    "mousemove",
    "mouseout",
    "mousedown",
    "mouseup",
    "click",
    "dblclick",
    "contextmenu",
    "onwheel" in document ? "wheel" : "mousewheel",
    "keydown",
    "keypress",
    "keyup"
  ],

  _domEventBlocker : function( event ) {
    rwt.event.EventHandlerUtil.stopDomEvent( event );
    event.cancelBubble = true; // MSIE
    if( event.stopPropagation ) {
      event.stopPropagation();
    }
  },

  getOriginalTargetObject : function( vNode ) {
    // Events on the HTML element, when using absolute locations which
    // are outside the HTML element. Opera does not seem to fire events
    // on the HTML element.
    if( vNode == document.documentElement ) {
      vNode = document.body;
    }
    // Walk up the tree and search for an rwt.widgets.base.Widget
    try {
      while( vNode != null && vNode.rwtWidget == null )       {
        vNode = vNode.parentNode;
      }
    } catch( vDomEvent ) {
      vNode = null;
    }
    return vNode ? vNode.rwtWidget : null;
  },

  getOriginalTargetObjectFromEvent : function( vDomEvent, vWindow ) {
    var vNode = this.getDomTarget( vDomEvent );
    // Especially to fix key events.
    // 'vWindow' is the window reference then
    if( vWindow ) {
      var vDocument = vWindow.document;
      if(    vNode == vWindow
          || vNode == vDocument
          || vNode == vDocument.documentElement
          || vNode == vDocument.body )
      {
        return vDocument.body.rwtWidget;
      }
    }
    return this.getOriginalTargetObject( vNode );
  },

  getRelatedTargetObjectFromEvent : function( vDomEvent ) {
    var EventHandlerUtil = rwt.event.EventHandlerUtil;
    var target = vDomEvent.relatedTarget;
    if( !target ) {
      if( vDomEvent.type == "mouseover" ) {
        target = vDomEvent.fromElement;
      } else {
        target = vDomEvent.toElement;
      }
    }
    return EventHandlerUtil.getTargetObject( target );
  },

  getTargetObject : function( vNode, vObject, allowDisabled ) {
    if( !vObject ) {
      var vObject = this.getOriginalTargetObject( vNode );
      if( !vObject ) {
        return null;
      }
    }
    while( vObject ) {
      if( !allowDisabled && !vObject.getEnabled() ) {
        return null;
      }
      if( !vObject.getAnonymous() ) {
        break;
      }
      vObject = vObject.getParent();
    }
    return vObject;
  },

  /////////////////
  // MOUSE HANDLING

  handleFocusedChild : function( target ) {
    if( target.getEnabled() && !( target instanceof rwt.widgets.base.ClientDocument ) ) {
      rwt.widgets.util.FocusHandler.mouseFocus = true;
      var root = target.getFocusRoot();
      if( root ) {
        var focusTarget = target;
        while( !focusTarget.isFocusable() && focusTarget != root ) {
          if( focusTarget instanceof rwt.widgets.MenuBar ) {
            return;
          }
          focusTarget = focusTarget.getParent();
        }
        // We need to focus first and active afterwards.
        // Otherwise the focus will activate another widget if the
        // active one is not tabable.
        rwt.event.EventHandler.setFocusRoot( root );
        root.setFocusedChild( focusTarget );
        root.setActiveChild( target );
      }
    }
  },

  ///////////////
  // KEY HANDLING

  getKeyCode : rwt.util.Variant.select( "qx.client", {
    "gecko" : function( event ) {
      return event.keyCode;
    },
    "default" : function( event ) {
      // the value in "keyCode" on "keypress" is actually the charcode:
      var hasKeyCode = event.type !== "keypress" || event.keyCode === 13 || event.keyCode === 27;
      return hasKeyCode ? event.keyCode : 0;
    }
  } ),

  getCharCode : rwt.util.Variant.select( "qx.client", {
    "default" : function( event ) {
      var hasCharCode = event.type === "keypress" && event.keyCode !== 13 && event.keyCode !== 27;
      return hasCharCode ? event.charCode : 0;
    },
    "trident" : function( event ) {
      var hasCharCode = event.type === "keypress" && event.keyCode !== 13 && event.keyCode !== 27;
      return hasCharCode ? event.keyCode : 0;
    }
  } ),

  isFirstKeyDown : function( keyCode ) {
    return this._lastUpDownType[ keyCode ] !== "keydown";
  },

  getEventPseudoTypes : function( event, keyCode ) {
    // There are two browser native key event sequences:
    // - for printable keys: keydown, keypress, keydown, keypress, keyup
    // - for non-printable keys: keydown, keydown, keyup
    var result;
    if( event.type === "keydown" ) {
      var asPrintable = rwt.client.Client.isGecko()
                      ? !this.isNonPrintableFirefoxKeyEvent( event, keyCode )
                      : !this.isNonPrintableKeyCode( keyCode );
      if( this.isFirstKeyDown( keyCode ) ) {
        // add a "keypress" for non-printable keys:
        result = asPrintable ? [ "keydown" ] : [ "keydown", "keypress" ];
      } else {
        // convert non-printable "keydown" to "keypress", suppress other:
        result = asPrintable ? [] : [ "keypress" ];
      }
    } else {
      result = [ event.type ];
    }
    return result;
  },

  mustRestoreKeyup  : function( keyCode, pseudoTypes  ) {
    // For these keys it is assumed to be more likely that a keyup event was missed
    // than the key being hold down while another key is pressed.
    var result = [];
    if( pseudoTypes[ 0 ] === "keydown" ) {
      if( !this.isFirstKeyDown( 93 ) && keyCode !== 93 ) {
        result.push( 93 );
      }
    }
    return result;
  },

  mustRestoreKeypress : function( event, pseudoTypes ) {
    // "keypress" is still fired in Firefox < 25 when "keydown" event is stopped
    if( rwt.client.Client.isGecko() && rwt.client.Client.getMajor() < 25 ) {
      return false;
    }
    if( this.wasStopped( event ) ) {
      return  ( pseudoTypes.length === 1 && pseudoTypes[ 0 ] === "keydown" )
             || pseudoTypes.length === 0;
    }
    return false;
  },

  saveData : function( event, keyCode ) {
    if( event.type !== "keypress" ) {
      this._lastUpDownType[ keyCode ] = event.type;
      this._lastKeyCode = keyCode;
    }
  },

  clearStuckKey : function( keyCode ) {
    this._lastUpDownType[ keyCode ] = "keyup";
  },

  keyCodeToIdentifier : function( keyCode ) {
    var result = "Unidentified";
    if( this._numpadToCharCode[ keyCode ] !== undefined ) {
      result = String.fromCharCode( this._numpadToCharCode[ keyCode ] );
    } else if( this._keyCodeToIdentifierMap[ keyCode ] !== undefined ) {
      result = this._keyCodeToIdentifierMap[ keyCode ];
    } else if( this._specialCharCodeMap[ keyCode ] !== undefined ) {
      result = this._specialCharCodeMap[ keyCode ];
    } else if( this.isAlphaNumericKeyCode( keyCode ) ) {
      result = String.fromCharCode( keyCode );
    }
    return result;
  },

  charCodeToIdentifier : function( charCode ) {
    var result;
    if( this._specialCharCodeMap[ charCode ] !== undefined ) {
      result = this._specialCharCodeMap[ charCode ];
    } else {
      result = String.fromCharCode( charCode ).toUpperCase();
    }
    return result;
  },

  isNonPrintableKeyCode  : rwt.util.Variant.select( "qx.client", {
    "default" : function( keyCode ) {
      return this._keyCodeToIdentifierMap[ keyCode ] ? true : false;
    },
    "webkit|blink" : function( keyCode ) {
      return ( this._keyCodeToIdentifierMap[ keyCode ] || keyCode === 27 ) ? true : false;
    }
  } ),

  isNonPrintableFirefoxKeyEvent : function( keyEvent, keyCode ) {
    // in Firefox < 65 ONLY modifier keys behave like non-printable
    if( rwt.client.Client.getMajor() < this.FIREFOX_NEW_KEY_EVENTS_VERSION ) {
      return this.isModifier( keyCode );
    }
    if( keyEvent.ctrlKey && keyEvent.key !== "Enter" ) {
      return true;
    }
    if( keyEvent.altKey && rwt.client.Client.getPlatform() !== "mac" ) {
      return true;
    }
    if( keyEvent.metaKey ) {
      return true;
    }
    return this._keyCodeToIdentifierMap[ keyCode ] ? true : false;
  },

  isSpecialKeyCode : function( keyCode ) {
    return this._specialCharCodeMap[ keyCode ] ? true : false;
  },

  isModifier : function( keyCode ) {
    return keyCode >= 16 && keyCode <= 20 && keyCode !== 19;
  },

  isAlphaNumericKeyCode : function( keyCode ) {
    var result = false;
    if(    ( keyCode >= this._charCodeA && keyCode <= this._charCodeZ )
        || ( keyCode >= this._charCode0 && keyCode <= this._charCode9 ) )
    {
      result = true;
    }
    return result;
  },

  /**
   * Determines if this key event should be blocked if key events are disabled
   */
  shouldBlock : function( type, keyCode, charCode, event ) {
    var result = true;
    var keyIdentifier;
    if( !isNaN( keyCode ) && keyCode !== 0 ) {
      keyIdentifier = this.keyCodeToIdentifier( keyCode );
    } else {
      keyIdentifier = this.charCodeToIdentifier( charCode );
    }
    if( this._nonBlockableKeysMap[ keyIdentifier ] || event.altKey ) {
      result = false;
    } else if( event.ctrlKey ) {
      // block only those combos that are used for text editing:
      result = this._blockableCtrlKeysMap[ keyIdentifier ] === true;
    }
    return result;
  },

  getElementAt : function( x, y ) {
    return document.elementFromPoint( x, y );
  },

  ///////////////
  // Helper-maps:

  _specialCharCodeMap : {
    13  : "Enter",
    27  : "Escape",
    32 : "Space"
  },

  _nonBlockableKeysMap : {
    "Control" : true,
    "Alt" : true,
    "Shift" : true,
    "Meta" : true,
    "Win" : true,
    "F1" : true,
    "F2" : true,
    "F3" : true,
    "F4" : true,
    "F5" : true,
    "F6" : true,
    "F7" : true,
    "F8" : true,
    "F9" : true,
    "F10" : true,
    "F11" : true,
    "F12" : true
  },

  _blockableCtrlKeysMap : {
    "F" : true,
    "A" : true,
    "C" : true,
    "V" : true,
    "X" : true,
    "Z" : true,
    "Y" : true
  },

  _keyCodeToIdentifierMap : {
    8   : "Backspace",
    9   : "Tab",
    16  : "Shift",
    17  : "Control",
    18  : "Alt",
    20  : "CapsLock",
    224 : "Meta",
    37  : "Left",
    38  : "Up",
    39  : "Right",
    40  : "Down",
    33  : "PageUp",
    34  : "PageDown",
    35  : "End",
    36  : "Home",
    45  : "Insert",
    46  : "Delete",
    112 : "F1",
    113 : "F2",
    114 : "F3",
    115 : "F4",
    116 : "F5",
    117 : "F6",
    118 : "F7",
    119 : "F8",
    120 : "F9",
    121 : "F10",
    122 : "F11",
    123 : "F12",
    144 : "NumLock",
    44  : "PrintScreen",
    145 : "Scroll",
    19  : "Pause",
    91  : "Win", // The Windows Logo key
    93  : "Apps" // The Application key (Windows Context Menu)
  },

  /** maps the keycodes of the numpad keys to the right charcodes */
  _numpadToCharCode : {
    96  : "0".charCodeAt( 0 ),
    97  : "1".charCodeAt( 0 ),
    98  : "2".charCodeAt( 0 ),
    99  : "3".charCodeAt( 0 ),
    100 : "4".charCodeAt( 0 ),
    101 : "5".charCodeAt( 0 ),
    102 : "6".charCodeAt( 0 ),
    103 : "7".charCodeAt( 0 ),
    104 : "8".charCodeAt( 0 ),
    105 : "9".charCodeAt( 0 ),
    106 : "*".charCodeAt( 0 ),
    107 : "+".charCodeAt( 0 ),
    109 : "-".charCodeAt( 0 ),
    110 : ",".charCodeAt( 0 ),
    111 : "/".charCodeAt( 0 )
  },

  _charCodeA : "A".charCodeAt( 0 ),
  _charCodeZ : "Z".charCodeAt( 0 ),
  _charCode0 : "0".charCodeAt( 0 ),
  _charCode9 : "9".charCodeAt( 0 )

};
