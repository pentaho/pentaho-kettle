/*******************************************************************************
 * Copyright (c) 2014, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.util" );

(function(){

  var Style = rwt.html.Style;
  var WidgetUtil = rwt.widgets.util.WidgetUtil;

/**
 * @private
 * @class An JQuery-like object which allows manipulation of HTML elements.
 * @exports rwt.util.RWTQuery as $
 * @description The constructor is not public. Instances can currently only be obtained from
 * {@link Widget#$el}.
 * @since 2.3
 */
rwt.util.RWTQuery = function( target ) {
  return new rwt.util.RWTQuery.fn.init( target );
};

rwt.util._RWTQuery = function( target ) {
  // TODO: If more methods are going to be made public almost all will also need restrictions
  return new rwt.util.RWTQuery.fn.init( target, true );
};

var $ = rwt.util.RWTQuery;

rwt.util._RWTQuery.prototype = $.prototype = {

  init : function( target, privileged ) {
    if( typeof target === "string" ) {
      target = parseHTML( target );
    }
    this.__access = function( args, callbackWidget, callbackElement ) {
      if( WidgetUtil.isWidget( target ) ) {
        if( typeof callbackWidget === "function" ) {
          return callbackWidget.apply( this, [ target, args, privileged ] );
        } else if( typeof callbackWidget === "string" ) {
          var element = asElement( target, callbackWidget );
          return callbackElement.apply( this, [ element, args, privileged ] );
        }
      }
      return callbackElement.apply( this, [ target, args, privileged ] );
    };
  },

  /**
   * @description A method to either set or get the value of an HTML-attribute.
   * Note that the attributes "id" and "class" can not be set this way.
   * @param {string|Object} attribute The name of the attribute to return or modify. Alternatively
   * a plain object with key-value pairs to set.
   * @param {string} [value] The value to set the attribute to.
   * @return {string|$} The value of the given attribute, if the function is called with a
   * string only. Otherwise a reference to this object.
   */
  attr : function() {
    return this.__access( arguments, attr_widget, attr_element );
  },

  removeAttr : function() {
    return this.__access( arguments, removeAttr_widget, removeAttr_element );
  },

  prop : function() { // TODO: widget could use inner outer outer depending on property
    return this.__access( arguments, "getElement", prop_element );
  },

  removeProp : function() {
    return this.__access( arguments, "getElement", removeProp_element );
  },

  append : function() {
    return this.__access( arguments, "_getTargetNode", append_element );
  },

  prepend : function() {
    return this.__access( arguments, "_getTargetNode", prepend_element );
  },

  detach : function() {
    return this.__access( arguments, null, detach_element );
  },

  appendTo : function() {
    return this.__access( arguments, null, appendTo_element );
  },

  insertAfter : function() {
    return this.__access( arguments, null, insertAfter_element );
  },

  insertBefore : function() {
    return this.__access( arguments, null, insertBefore_element );
  },

  /**
   * @description A method to either set or get the value of a CSS property. Note that RAP may overwrite any changes
   * made to a widget's style properties, and that the changes are not applied to the Java-side widget instance.
   * Modifying properties not used by RAP is safe. Supports only elements of control widgets.
   * @param {string|Object} property The name of the property to return or modify. Alternatively
   * a plain object with key-value pairs to set.
   * @param {string} [value] The value to set the property to.
   * @return {string|$} The value of the given property, if the function is called with a
   * string only. Otherwise a reference to this object.
   * @since 3.0
   */
  css : function() {
    return this.__access( arguments, css_widget, css_element );
  },

  outerHeight : function() {
    return this.__access( arguments, "getElement", outerHeight_element );
  },

  outerWidth : function() {
    return this.__access( arguments, "getElement", outerWidth_element );
  },

  text : function() {
    return this.__access( arguments, "_getTargetNode", text_element );
  },

  html : function() {
    return this.__access( arguments, "_getTargetNode", html_element );
  },

  empty : function() {
    return this.__access( arguments, "_getTargetNode", empty_element );
  },

  clone : function() {
    return this.__access( arguments, "getElement", clone_element );
  },

  /**
   * @name get
   * @methodOf $#
   * @description Retrieve the DOM element represented by this object. Supports only elements of control widgets.
   * @return {[HTMLElement]} Array with exactly one HTML element.
   * @since 3.0
   */
  "get" : function() {
    return this.__access( arguments, "getElement", get_element );
  },

  is : function() {
    return this.__access( arguments, null, is_element );
  },

  offset: function() {
    return this.__access( arguments, null, offset_element );
  }

};

$.fn = $.prototype; // for extendability
$.fn.init.prototype = $.prototype; // for creation without "new"

// TODO: these hooks are for element only, widgets would need separate ones
$.cssHooks = {
  "backgroundColor" : {
    "set" : function( element, value ) {
      rwt.html.Style.setBackgroundColor( element, value );
    },
    "get" : function( element ) {
      return rwt.html.Style.getBackgroundColor( element );
    }
  },
  "userSelect" : {
    "set" : function( element, value ) {
      rwt.html.Style.setUserSelect( element, value );
    },
    "get" : function( element ) {
      return rwt.html.Style.getUserSelect( element );
    }
  },
  "backgroundImage" : {
    "set" : function( element, value ) {
      rwt.html.Style.setBackgroundImage( element, fixBackgroundImage( value ) );
    },
    "get" : function( element ) {
      return rwt.html.Style.getBackgroundImage( element );
    }
  },
  "backgroundRepeat" : {
    "set" : function( element, value ) {
      rwt.html.Style.setBackgroundRepeat( element, value );
    },
    "get" : function( element ) {
      return rwt.html.Style.getBackgroundRepeat( element );
    }
  },
  "backgroundPosition" : {
    "set" : function( element, value ) {
      rwt.html.Style.setBackgroundPosition( element, value );
    },
    "get" : function( element ) {
      return rwt.html.Style.getBackgroundPosition( element );
    }
  },
  "backgroundGradient" : {
    "set" : function( element, value ) {
      rwt.html.Style.setBackgroundGradient( element, fixBackgroundGradient( value ) );
    },
    "get" : function( element ) {
      return rwt.html.Style.getBackgroundGradient( element );
    }
  },
  "textShadow" : {
    "set" : function( element, value ) {
      if( value instanceof Array ) {
        rwt.html.Style.setTextShadow( element, value );
      } else {
        element.style.textShadow = value;
      }
    }
  },
  "boxShadow" : {
    "set" : function( element, value ) {
      if( value instanceof Array ) {
        rwt.html.Style.setBoxShadow( element, value );
      } else {
        element.style.boxShadow = value;
      }
    }
  },
  "border" : {
    "set" : function( element, value ) {
      if( value && typeof value === "object" && value.renderElement ) {
        value.renderElement( element );
      } else {
        element.style.border = value;
      }
    }
  },
  "font" : {
    "set" : function( element, value ) {
      if( value && typeof value === "object" && value.renderElement ) {
        value.renderElement( element );
      } else {
        element.style.font = value;
      }
    }
  }
};

// NOTE: this list is still incomplete, extend as needed
$.widgetCssHooks = {
  "font": "font",
  "border": "border",
  "backgroundColor": "backgroundColor",
  "textShadow": "textShadow",
  "boxShadow": "shadow",
  "color": "textColor",
  "left": "left",
  "top": "top",
  "width": "width",
  "height": "height",
  "bottom": "bottom",
  "right": "right",
  "opacity": "opacity",
  "overflow": "overflow",
  "userSelect": "selectable",
  "backgroundImage": {
    "set" : function( widget, value ) {
      widget.set( "backgroundImage", fixBackgroundImage( value ) );
    }
  },
  "backgroundGradient" : {
    "set" : function( widget, value ) {
      widget.set( "backgroundGradient", fixBackgroundGradient( value ) );
    }
  }
};

$.cssNumber = {
  "columnCount" : true,
  "fillOpacity" : true,
  "fontWeight" : true,
  "lineHeight" : true,
  "opacity" : true,
  "zIndex" : true,
  "zoom" : true
};

var unwrapSetterArgsFor = function( originalSetter ) {
  return function( target, args, privileged ) {
    if( args.length === 1 && ( typeof args[ 0 ] === "object" ) ) {
      var map = args[ 0 ];
      for( var key in map ) {
        originalSetter.apply( this, [ target, [ key, map[ key ] ], privileged ] );
      }
      return this;
    }
    return originalSetter.apply( this, [ target, args, privileged ] );
  };
};

var unwrapStringListFor = function( originalHandler ) {
  return function( target, args, privileged ) {
    var arr = args[ 0 ].trim().split( " " );
    for( var i = 0; i < arr.length; i++ ) {
      if( arr[ i ] || arr[ i ].length > 0 ) {
        originalHandler.apply( this, [ target, arr[ i ], privileged ] );
      }
    }
    return this;
  };
};

var attr_widget = unwrapSetterArgsFor( function( widget, args, privileged ) {
  if( args.length === 1 ) {
    return widget.getHtmlAttributes()[ args[ 0 ] ];
  } else if( privileged || !restrictedAttributes[ args[ 0 ] ] ) {
    widget.setHtmlAttribute( args[ 0 ], args[ 1 ] );
  }
  return this;
} );

var attr_element = unwrapSetterArgsFor( function( element, args, privileged ) {
  if( args.length === 1 ) {
    return element.getAttribute( args[ 0 ] ) || undefined;
  } else if( privileged || !restrictedAttributes[ args[ 0 ] ] ) {
    element.setAttribute( args[ 0 ], args[ 1 ] );
  }
  return this;
} );

var removeAttr_widget = unwrapStringListFor( function( widget, arg ) {
  widget.setHtmlAttribute( arg, null );
} );

var removeAttr_element = unwrapStringListFor( function( element, arg ) {
  element.removeAttribute( arg );
} );

var prop_element = unwrapSetterArgsFor( function( element, args ) {
  if( args.length === 1 ) {
    return element[ args[ 0 ] ];
  } else {
    element[ args[ 0 ] ] = args[ 1 ];
  }
  return this;
} );

var removeProp_element = unwrapSetterArgsFor( function( element, args ) {
  try {
    element[ args[ 0 ] ] = "";
    delete element[ args[ 0 ] ];
  } catch( ex ) {
    // some properties can not be deleted
  }
  return this;
} );

var css_widget = unwrapSetterArgsFor( function( widget, args ) {
  var hook = $.widgetCssHooks[ args[ 0 ] ];
  if( args.length === 1 ) {
    if( hook && ( typeof hook === "string" || hook.get ) ) {
      if( typeof hook === "string" ) {
        var getter = "get" + hook.slice( 0, 1 ).toUpperCase() +  hook.slice( 1 );
        return widget[ getter ]();
      } else {
        return hook.get( widget );
      }
    }
    return widget.getStyleProperties()[ args[ 0 ] ];
  }
  if( hook && ( typeof hook === "string" || hook.set ) ) {
    if( typeof hook === "string"  ) {
      widget.set( hook, args[ 1 ] );
    } else {
      hook.set( widget, args[ 1 ] );
    }
  } else {
    widget.setStyleProperty( args[ 0 ], args[ 1 ] );
  }
  return this;
} );

var css_element = unwrapSetterArgsFor( function( element, args ) {
  var hook = $.cssHooks[ args[ 0 ] ];
  if( args.length === 1 ) {
    if( hook && hook.get ) {
      return hook.get( element );
    }
    return Style.getStyleProperty( element, args[ 0 ] );
  }
  if( hook && hook.set ) {
    hook.set( element, args[ 1 ] );
  } else {
    element.style[ args[ 0 ] ] = parseCssValue( args );
  }
  return this;
} );

var outerHeight_element = function( element ) {
  return element.offsetHeight;
};

var outerWidth_element = function( element ) {
  return element.offsetWidth;
};

var append_element = function( element, args ) {
  element.appendChild( asElement( args[ 0 ] ) );
  return this;
};

var prepend_element = function( element, args ) {
  element.insertBefore( asElement( args[ 0 ] ), element.firstChild );
  return this;
};

var insertAfter_element = function( element, args ) {
  var reference = asElement( args[ 0 ] );
  reference.parentNode.insertBefore( element, reference.nextSibling );
  return this;
};

var insertBefore_element = function( element, args ) {
  var reference = asElement( args[ 0 ] );
  reference.parentNode.insertBefore( element, reference );
  return this;
};

var appendTo_element = function( element, args ) {
  asElement( args[ 0 ], "_getTargetNode" ).appendChild( element );
  return this;
};

var detach_element = function( element ) {
  element.parentNode.removeChild( element );
  return this;
};

var get_element = function( element, args ) {
  var result = [ element ];
  return args.length > 0 ? result[ args[ 0 ] ] : result;
};

var is_element = function( element, args ) {
  return element === asElement( args[ 0 ] );
};

var offset_element = function( element ) {
  var location = rwt.html.Location.get( element );
  return {left: location.left, top: location.top};
};

var text_element = function( element, args ) {
  if( args.length === 0 )  {
    return element.textContent;
  }
  element.textContent = args[ 0 ];
  return this;
};

var html_element = function( element, args ) {
  if( args.length === 0 )  {
    return element.innerHTML;
  }
  element.innerHTML = args[ 0 ];
  return this;
};

var empty_element = function( element ) {
  while( element.firstChild ) {
    element.removeChild( element.firstChild );
  }
  return this;
};

var clone_element = function( element ) {
  var clone = element.cloneNode( true );
  for( var i = 0; i < cloneableProperties.length; i++ ) {
    var property = cloneableProperties[ i ];
    if( element[ property ] ) {
      clone[ property ] = element[ property ];
    }
  }
  return $( clone );
};

var restrictedAttributes = {
  "id" : true, // RAP renders IDs. While it does not rely on them, addons and future versions may.
  "class" : true, // May be used by RAP in the future, separate API could allow access
  "style" : true // Would destroy layout, separate API could allow (limited) access
};

var parseCssValue = function( args ) {
  if( typeof args[ 1 ] === "number" && !$.cssNumber[ args[ 0 ] ] ) {
    return args[ 1 ] + "px";
  }
  return args[ 1 ];
};

var parseHTML = function( str ) {
  var parsed = rsingleTag.exec( str );
  if( parsed ) {
    return document.createElement( parsed[ 1 ] );
  }
  throw new Error( "Invalid or unsupported HTML string" );
};

var fixBackgroundImage = function( value ) {
  if( value && value.trim().toLowerCase().indexOf( "url(" ) === 0 ) {
    return getCssFunctionParams( "url", value )[ 0 ];
  }
  return value;
};

var fixBackgroundGradient = function( value ) {
  if( value && typeof value === "string" ) {
    var params = getCssFunctionParams("linear-gradient", value);
    var horizontal = params[ 0 ] === "to right";
    cssCheck(horizontal || params[ 0 ] === "to bottom");
    var gradient = [];
    gradient.horizontal = horizontal;
    cssCheck(params.length > 2);
    for(var i = 1; i < params.length; i++) {
      var stop = trimAll(params[ i ].split(" ").reverse());
      stop[ 0 ] = parseInt(stop[ 0 ], 10) / 100;
      cssCheck(!isNaN(stop[ 0 ]));
      gradient.push(stop);
    }
    return gradient;
  }
  return value;
};

var rsingleTag = (/^<(\w+)\s*\/?>(?:<\/\1>|)$/);

var getCssFunctionParams = function( fnName, cssValue ) {
  var trimmed = cssValue.trim().toLowerCase();
  var start = fnName + "(";
  if( trimmed.indexOf( start ) === 0 && trimmed.lastIndexOf( ")" ) === ( trimmed.length - 1 ) ) {
    return trimAll( trimmed.slice( start.length, -1 ).trim().split( "," ) );
  }
  cssCheck( false );
};

var trimAll = function( arr ) {
  return arr.map( function( str ){ return str.trim(); } );
};

var cssCheck = function( passed ) {
  if( !passed ) {
    throw new Error( "Invalid or unsupported css value" );
  }
};

var ensureWidgetElement = function( widget ) {
  if( !widget.getElement() ) {
    rwt.widgets.base.Widget.removeFromGlobalElementQueue( widget );
    widget._createElementImpl();
  }
};

var asElement = function( target, widgetElementGetter ) {
  if( WidgetUtil.isWidget( target ) ) {
    ensureWidgetElement( target );
    return target[ ( widgetElementGetter || "getElement" ) ]();
  }
  return target instanceof $ ? target.get( 0 ) : target;
};

var cloneableProperties = [
  "___rwtStyle__backgroundPosition",
  "___rwtStyle__backgroundGradient",
  "___rwtStyle__backgroundImage",
  "___rwtStyle__backgroundRepeat",
  "___rwtStyle__backgroundColor"
];

}());
