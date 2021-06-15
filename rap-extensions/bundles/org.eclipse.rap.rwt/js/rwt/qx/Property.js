/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
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


/**
 * Internal class for handling of dynamic properties. Should only be used
 * through the methods provided by {@link rwt.qx.Class}.
 *
 * For a complete documentation of properties take a
 * look at http://qooxdoo.org/documentation/developer_manual/properties.
 *
 *
 * *Normal properties*
 *
 * The <code>properties</code> key in the class definition map of {@link rwt.qx.Class#define}
 * is used to generate the properties.
 *
 * Valid keys of a property definition are:
 *
 * - check {Array, String, Function}
 *   The check is used to validate the incoming value of a property. The check can be:
 *   - a custom check function. The function takes the incoming value as a parameter and must
 *     return a boolean value to indicate whether the values is valid.
 *   - inline check code as a string e.g. <code>"value &gt; 0 && value &lt; 100"</code>
 *   - a class name e.g. <code>rwt.widgets.Button</code>
 *   - a name of an interface the value must implement, e.g. <code>qx.application.IAplpication</code>
 *   - an array of all valid values</li>
 *   - one of the predefined checks: Boolean, String, Number, Integer, Float, Double,
 *     Object, Array, Map, Class, Mixin, Interface, Theme, Error, RegExp, Function,
 *     Date, Node, Element, Document, Window, Event
 * - init {var}
 *   Sets the default/initial value of the property. If no property value is set or the property
 *   gets reset, the getter will return the <code>init</code> value.
 * - apply {String}
 *   On change of the property value the method of the specified name will be called. The
 *   signature of the method is <code>function(newValue, oldValue)</code>.
 * - event {String}
 *   On change of the property value an event with the given name will be dispached. The event
 *   type is {@link rwt.event.ChangeEvent}.
 * - themeable {Boolean}
 *   Whether this property can be set using themes.
 * - inheritable {Boolean}
 *   Whether the property value should be inheritable. If the property does not have a user
 *   defined or an init value, the property will try to get the value from the parent of the
 *   current object.
 * - nullable {Boolean}
 *   Whether <code>null</code> is an allowed value of the property. This is complemental to the
 *   check defined using the <code>check</code> key.
 * - refine {Boolean}
 *   Whether the property definition is a refinemnet of a property in one of the super classes of
 *   the class. Only the <code>init</code> value can be changed using refine.
 * - transform {String}
 *   On setting of the property value the method of the specified name will
 *   be called. The signature of the method is <code>function(value)</code>.
 *   The parameter <code>value</code> is the value passed to the setter.
 *   The function must return the modified or unmodified value.
 *   Transformation occurs before the check function, so both may be
 *   specified if desired.  Alternatively, the transform function may throw
 *   an error if the value passed to it is invalid.
 *
 * Property groups
 * ---------------
 *
 * Property groups are defined in a similar way but support a different set of keys:
 *
 * - group {String[]}
 *   A list of property names which should be set using the propery group.
 * - mode {String}
 *   If mode is set to <code>"shorthand"</code>, the properties can be set using a CSS like
 *   shorthand mode.
 * - themeable {Boolean}
 *   Whether this property can be set using themes.
 */
rwt.qx.Class.define( "rwt.qx.Property", {

  statics : {

    /**
     * Built-in checks
     * The keys could be used in the check of the properties
     */
    __checks : {
      "Boolean"   : 'typeof value === "boolean"',
      "String"    : 'typeof value === "string"',
      "NonEmptyString" : 'typeof value === "string" && value.length > 0',

      "Number"    : 'typeof value === "number" && isFinite(value)',
      "Integer"   : 'typeof value === "number" && isFinite(value) && value%1 === 0',
      "Float"     : 'typeof value === "number" && isFinite(value)',
      "Double"    : 'typeof value === "number" && isFinite(value)',

      "Error"     : 'value instanceof Error',
      "RegExp"    : 'value instanceof RegExp',

      "Object"    : 'value !== null && typeof value === "object"',
      "Array"     : 'value instanceof Array',
      "Map"       : 'value !== null && typeof value === "object" && !(value instanceof Array) && !(value instanceof rwt.qx.Object)',

      "Function"  : 'value instanceof Function',
      "Date"      : 'value instanceof Date',
      "Node"      : 'value !== null && value.nodeType !== undefined',
      "Element"   : 'value !== null && value.nodeType === 1 && value.attributes',
      "Document"  : 'value !== null && value.nodeType === 9 && value.documentElement',
      "Window"    : 'value !== null && window.document',
      "Event"     : 'value !== null && value.type !== undefined',

      "Class"     : 'value !== null && value.$$type === "Class"',
      "Mixin"     : 'value !== null && value.$$type === "Mixin"',
      "Interface" : 'value !== null && value.$$type === "Interface"',
      "Theme"     : 'value !== null && value.$$type === "Theme"',

      "Color"     : 'typeof value === "string" && rwt.util.Colors.isValid(value)',
      "Border"    : 'value !== null',
      "Font"      : 'value !== null',
      "Label"     : 'value !== null && typeof value === "string"'
    },

    /**
     * Contains types from {@link #__checks} list which need to be disposed
     */
    __dispose : {
      "Object"    : true,
      "Array"     : true,
      "Map"       : true,
      "Function"  : true,
      "Date"      : true,
      "Node"      : true,
      "Element"   : true,
      "Document"  : true,
      "Window"    : true,
      "Event"     : true,
      "Class"     : true,
      "Mixin"     : true,
      "Interface" : true,
      "Theme"     : true,
      "Border"    : true,
      "Font"      : true
    },

    /**
     * Inherit value, used to override defaults etc. to force inheritance
     * even if property value is not undefined (through multi-values)
     */
    $$inherit : "inherit",

    /**
     * Used in build version for storage names
     */
    $$idcounter : 0,

    /**
     * Caching field names for each property created
     */
    $$store : {
      user : {},
      theme : {},
      inherit : {},
      init : {},
      useinit : {}
    },

    /**
     * Caching function names for each property created
     */
    $$method : {
      get : {},
      set : {},
      reset : {},
      init : {},
      refresh : {},
      style : {},
      unstyle : {}
    },

    /**
     * Supported keys for property defintions
     */
    $$allowedKeys : {
      name         : "string",   // String
      dispose     : "boolean",  // Boolean
      inheritable : "boolean",  // Boolean
      nullable    : "boolean",  // Boolean
      themeable   : "boolean",  // Boolean
      refine      : "boolean",  // Boolean
      init        : null,       // var
      apply       : "string",   // String
      event       : "string",   // String
      check       : null,       // Array, String, Function
      transform   : "string",   // String
      deferredInit : "boolean"   // Boolean
    },

    $$allowedGroupKeys : {
      name       : "string",   // String
      group      : "object",   // Array
      mode       : "string",   // String
      themeable  : "boolean"   // Boolean
    },

    /** Contains names of inheritable properties, filled by {@link rwt.qx.Class.define} */
    $$inheritable : {},

    /**
     * Refreshes widget whose parent has changed (including the children)
     *
     * @param widget {rwt.widgets.base.Widget} the widget
     */
    refresh : function( widget ) {
      var parent = widget.getParent();
      if( parent ) {
        var clazz = widget.constructor;
        var inherit = this.$$store.inherit;
        var refresh = this.$$method.refresh;
        var properties;
        while( clazz ) {
          properties = clazz.$$properties;
          if( properties ) {
            for( var name in this.$$inheritable ) {
              // Whether the property is available in this class
              // and whether it is inheritable in this class as well
              if( properties[name] && widget[refresh[name]] ) {
                widget[refresh[name]]( parent[inherit[name]] );
              }
            }
          }
          clazz = clazz.superclass;
        }
      }
    },

    /**
     * Attach properties to class prototype
     *
     * @param clazz {Class} Class to attach properties to
     */
    attach : function( clazz ) {
      var properties = clazz.$$properties;
      if( properties ) {
        for( var name in properties ) {
          this.attachMethods( clazz, name, properties[ name ] );
        }
      }
      clazz.$$propertiesAttached = true;
    },

    /**
     * Attach one property to class
     *
     * @param clazz {Class} Class to attach properties to
     * @param name {String} Name of property
     * @param config {Map} Configuration map of property
     */
    attachMethods : function( clazz, name, config ) {
      // Filter old properties
      if( config._fast || config._cached ) {
        return;
      }

      // Generate property method prefixes and postfixes
      var prefix, postfix;

      if( name.charAt(0) === "_" ) {
        if( name.charAt(1) === "_" ) {
          prefix = "__";
          postfix = rwt.util.Strings.toFirstUp(name.substring(2));
        } else {
          prefix = "_";
          postfix = rwt.util.Strings.toFirstUp(name.substring(1));
        }
      } else {
        prefix = "";
        postfix = rwt.util.Strings.toFirstUp(name);
      }

      // Attach methods
      if( config.group ) {
        this.__attachGroupMethods( clazz, config, prefix, postfix );
      } else {
        this.__attachPropertyMethods( clazz, config, prefix, postfix );
      }
    },

    /**
     * Attach group methods
     *
     * @param clazz {Class} Class to attach properties to
     * @param config {Map} Property configuration
     * @param prefix {String} Prefix of property e.g. "__" or "_" for private or protected properties
     * @param postfix {String} Camelcase name of property e.g. name=width => postfix=Width
     */
    __attachGroupMethods : function( clazz, config, prefix, postfix ) {
      var members = clazz.prototype;
      var name = config.name;
      var themeable = config.themeable === true;

      var setter = [];
      var resetter = [];

      if( themeable ) {
        var styler = [];
        var unstyler = [];
      }

      var argHandler = "var a=arguments[0] instanceof Array?arguments[0]:arguments;";

      setter.push( argHandler );

      if( themeable ) {
        styler.push( argHandler );
      }

      if( config.mode == "shorthand" ) {
        var shorthand = "a=rwt.util.Arrays.fromShortHand(rwt.util.Arrays.fromArguments(a));";
        setter.push( shorthand );

        if( themeable ) {
          styler.push( shorthand );
        }
      }

      for( var i = 0, a = config.group, l = a.length; i < l; i++ ) {
        if( rwt.util.Variant.isSet( "qx.debug", "on" ) ) {
          if( !this.$$method.set[ a[ i ] ] || !this.$$method.reset[ a[ i ] ] ) {
            throw new Error( "Cannot create property group '" + name + "' including non-existing property '" + a[ i ] + "'!" );
          }
        }

        setter.push( "this.", this.$$method.set[a[i]], "(a[", i, "]);" );
        resetter.push( "this.", this.$$method.reset[a[i]], "();" );

        if( themeable ) {
          if( rwt.util.Variant.isSet( "qx.debug", "on" ) ) {
            if( !this.$$method.style[a[i]] ) {
              throw new Error( "Cannot add the non themable property '" + a[i] + "' to the themable property group '"+ name +"'" );
            }
          }
          styler.push("this.", this.$$method.style[a[i]], "(a[", i, "]);" );
          unstyler.push("this.", this.$$method.unstyle[a[i]], "();" );
        }
      }

      // Attach setter
      this.$$method.set[name] = prefix + "set" + postfix;
      members[this.$$method.set[name]] = new Function( setter.join( "" ) );

      // Attach resetter
      this.$$method.reset[name] = prefix + "reset" + postfix;
      members[this.$$method.reset[name]] = new Function( resetter.join( "" ) );

      if( themeable ) {
        // Attach styler
        this.$$method.style[name] = prefix + "style" + postfix;
        members[this.$$method.style[name]] = new Function( styler.join( "" ) );

        // Attach unstyler
        this.$$method.unstyle[name] = prefix + "unstyle" + postfix;
        members[this.$$method.unstyle[name]] = new Function( unstyler.join( "" ) );
      }
    },

    /**
     * Attach property methods
     *
     * @param clazz {Class} Class to attach properties to
     * @param config {Map} Property configuration
     * @param prefix {String} Prefix of property e.g. "__" or "_" for private or protected properties
     * @param postfix {String} Camelcase name of property e.g. name=width => postfix=Width
     */
    __attachPropertyMethods : function( clazz, config, prefix, postfix ) {
      var members = clazz.prototype;
      var name = config.name;

      // Fill dispose value
      if( config.dispose === undefined && typeof config.check === "string" ) {
        config.dispose = this.__dispose[ config.check ] || rwt.qx.Class.isDefined( config.check );
      }

      var method = this.$$method;
      var store = this.$$store;

      store.user[ name ] = "__user$" + name;
      store.theme[ name ] = "__theme$" + name;
      store.init[ name ] = "__init$" + name;
      store.inherit[ name ] = "__inherit$" + name;
      store.useinit[ name ] = "__useinit$" + name;

      method.get[name] = prefix + "get" + postfix;
      members[method.get[name]] = function() {
        return rwt.qx.Property.executeOptimizedGetter( this, clazz, name, "get" );
      };

      method.set[name] = prefix + "set" + postfix;
      members[method.set[name]] = function() {
        return rwt.qx.Property.executeOptimizedSetter( this, clazz, name, "set", arguments );
      };

      method.reset[name] = prefix + "reset" + postfix;
      members[method.reset[name]] = function() {
        return rwt.qx.Property.executeOptimizedSetter( this, clazz, name, "reset" );
      };

      if( config.inheritable || config.apply || config.event || config.deferredInit ) {
        method.init[name] = prefix + "init" + postfix;
        members[method.init[name]] = function() {
          return rwt.qx.Property.executeOptimizedSetter( this, clazz, name, "init", arguments );
        };
      }

      if( config.inheritable ) {
        method.refresh[name] = prefix + "refresh" + postfix;
        members[method.refresh[name]] = function() {
          return rwt.qx.Property.executeOptimizedSetter( this, clazz, name, "refresh", arguments );
        };
      }

      if( config.themeable ) {
        method.style[name] = prefix + "style" + postfix;
        members[method.style[name]] = function() {
          return rwt.qx.Property.executeOptimizedSetter( this, clazz, name, "style", arguments );
        };

        method.unstyle[name] = prefix + "unstyle" + postfix;
        members[method.unstyle[name]] = function() {
          return rwt.qx.Property.executeOptimizedSetter( this, clazz, name, "unstyle" );
        };
      }

      if( config.check === "Boolean" ) {
        members[prefix + "toggle" + postfix] = new Function("return this." + method.set[name] + "(!this." + method.get[name] + "())");
        members[prefix + "is" + postfix] = new Function("return this." + method.get[name] + "()");
      }
    },

    /** {Map} Internal data field for error messages used by {@link #error} */
    __errors : {
      0 : 'Could not change or apply init value after constructing phase!',
      1 : 'Requires exactly one argument!',
      2 : 'Undefined value is not allowed!',
      3 : 'Does not allow any arguments!',
      4 : 'Null value is not allowed!',
      5 : 'Is invalid!'
    },

    /**
     * Error method used by the property system to report errors.
     *
     * @param obj {rwt.qx.Object} Any qooxdoo object
     * @param id {Integer} Numeric error identifier
     * @param property {String} Name of the property
     * @param variant {String} Name of the method variant e.g. "set", "reset", ...
     * @param value {var} Incoming value
     */
    error : function( obj, id, property, variant, value ) {
      var classname = obj.constructor.classname;
      var msg = "Error in property " + property + " of class " + classname + " in method " + this.$$method[variant][property] + " with incoming value '" + value + "': ";

      // Additional object error before throwing exception because gecko
      // often has issues to throw the error correctly in the debug console otherwise

      throw new Error( msg + ( this.__errors[id] || "Unknown reason: " + id ) );
    },

    /**
     * Compiles a string builder object to a function, executes the function and
     * returns the return value.
     *
     * @param instance {Object} Instance which have called the original method
     * @param members {Object} Prototype members map where the new function should be stored
     * @param name {String} Name of the property
     * @param variant {String} Function variant e.g. get, set, reset, ...
     * @param code {Array} Array which contains the code
     * @param args {arguments} Incoming arguments of wrapper method
     * @return {var} Return value of the generated function
     */
    __unwrapFunctionFromCode : function(instance, members, name, variant, code, args ) {
      var store = this.$$method[variant][name];

      // Output generate code
      if( rwt.util.Variant.isSet( "qx.debug", "on" ) ) {

        // Overriding temporary wrapper
        try{
          members[store] = new Function( "value", code.join( "" ) );
          // eval("members[store] = function " + instance.classname.replace(/\./g, "_") + "$" + store + "(value) { " + code.join("") + "}");
        } catch( ex ) {
          throw new Error( "Malformed generated code to unwrap method: " + this.$$method[variant][name] + "\n" + code.join( "" ) );
        }
      }
      else {
        members[store] = new Function("value", code.join(""));
        // eval("members[store] = function " + instance.classname.replace(/\./g, "_") + "$" + store + "(value) { " + code.join("") + "}");
      }

      // Executing new function
      if( args === undefined ) {
        return instance[store]();
      } else if( rwt.util.Variant.isSet( "qx.debug", "on" ) ) {
        return instance[store].apply( instance, args );
      } else {
        return instance[store]( args[0] );
      }
    },

    /**
     * Generates the optimized getter
     * Supported variants: get
     *
     * @param instance {Object} the instance which calls the method
     * @param clazz {Class} the class which originally defined the property
     * @param name {String} name of the property
     * @param variant {String} Method variant.
     * @return {var} Execute return value of apply generated function, generally the incoming value
     */
    executeOptimizedGetter : function( instance, clazz, name, variant ) {
      var config = clazz.$$properties[name];
      var members = clazz.prototype;
      var code = [];

      if( config.inheritable ) {
        code.push( 'if(this.', this.$$store.inherit[name], '!==undefined)' );
        code.push( 'return this.', this.$$store.inherit[name], ';' );
        code.push( 'else ' );
      }

      code.push( 'if(this.', this.$$store.user[name], '!==undefined)' );
      code.push( 'return this.', this.$$store.user[name], ';' );

      if( config.themeable ) {
        code.push( 'else if(this.', this.$$store.theme[name], '!==undefined)' );
        code.push( 'return this.', this.$$store.theme[name], ';' );
      }

      if( config.deferredInit && config.init === undefined ) {
        code.push( 'else if(this.', this.$$store.init[name], '!==undefined)' );
        code.push( 'return this.', this.$$store.init[name], ';' );
      }

      code.push( 'else ' );

      if( config.init !== undefined ) {
        code.push( 'return this.', this.$$store.init[name], ';' );
      } else if( config.inheritable || config.nullable ) {
        code.push( 'return null;' );
      } else {
        code.push( 'throw new Error("Property ', name, ' of an instance of ', clazz.classname, ' is not (yet) ready!");' );
      }

      return this.__unwrapFunctionFromCode( instance, members, name, variant, code );
    },

    /**
     * Generates the optimized setter
     * Supported variants: set, reset, init, refresh, style, unstyle
     *
     * @param instance {Object} the instance which calls the method
     * @param clazz {Class} the class which originally defined the property
     * @param name {String} name of the property
     * @param variant {String} Method variant.
     * @param args {arguments} Incoming arguments of wrapper method
     * @return {var} Execute return value of apply generated function, generally the incoming value
     */
    executeOptimizedSetter : function( instance, clazz, name, variant, args ) {
      var config = clazz.$$properties[name];
      var members = clazz.prototype;
      var code = [];

      var incomingValue =    variant === "set"
                          || variant === "style"
                          || ( variant === "init" && config.init === undefined );
      var resetValue = variant === "reset" || variant === "unstyle";
      var hasCallback = config.apply || config.event || config.inheritable;
      if( variant === "style" || variant === "unstyle" ) {
        var store = this.$$store.theme[name];
      } else if( variant === "init" ) {
        var store = this.$$store.init[name];
      } else {
        var store = this.$$store.user[name];
      }

      // [1] INTEGRATE ERROR HELPER METHOD

      // [2] PRE CONDITIONS

      if( !config.nullable || config.check || config.inheritable ) {
        code.push( 'var prop=rwt.qx.Property;' );
      }

      // Undefined check
      // TODO [rh] unused: changed as in patch to http://bugzilla.qooxdoo.org/show_bug.cgi?id=599
      if( rwt.util.Variant.isSet( "qx.debug", "on" ) && variant === "set" ) {
        code.push( 'if(value===undefined)prop.error(this,2,"' + name + '","' + variant + '",value);' );
      }

      // [3] PREPROCESSING INCOMING VALUE

      if( incomingValue ) {
        // Call user-provided transform method, if one is provided.  Transform
        // method should either throw an error or return the new value.
        if( config.transform ) {
          code.push( 'value=this.', config.transform, '(value);' );
        }
      }

      // [4] COMPARING (LOCAL) NEW AND OLD VALUE

      // Old/new comparision
      if( hasCallback ) {
        if( incomingValue ) {
          code.push( 'if(this.', store, '===value)return value;' );
        } else if( resetValue ) {
          code.push( 'if(this.', store, '===undefined)return;' );
        }
      }

      // [5] CHECKING VALUE

      if( config.inheritable ) {
        code.push( 'var inherit=prop.$$inherit;' );
      }

      // Generate checks only in debug mode

      // TODO [rh] unused: changed as in patch to http://bugzilla.qooxdoo.org/show_bug.cgi?id=599
      if( incomingValue && rwt.util.Variant.isSet( "qx.debug", "on" ) ) {
        // Null check
        if( !config.nullable ) {
          code.push( 'if(value===null)prop.error(this,4,"'+name+'","'+variant+'",value);' );
        }

        // Processing check definition
        if( config.check !== undefined ) {
          // Accept "null"
          if( config.nullable ) {
            code.push( 'if(value!==null)' );
          }

          // Inheritable properties always accept "inherit" as value
          if( config.inheritable ) {
            code.push( 'if(value!==inherit)' );
          }

          code.push( 'if(' );

          if( this.__checks[config.check] !== undefined ) {
            code.push( '!(', this.__checks[config.check], ')' );
          }
          else if( rwt.qx.Class.isDefined( config.check ) ) {
            code.push( '!(value instanceof ', config.check, ')' );
          }
          else if( typeof config.check === "function" ) {
            code.push( '!', clazz.classname, '.$$properties.', name );
            code.push( '.check.call(this, value)' );
          }
          else if( typeof config.check === "string" ) {
            code.push( '!(', config.check, ')' );
          }
          else if( config.check instanceof Array ) {
            // reconfigure for faster access trough map usage
            config.checkMap = rwt.util.Objects.fromArray(config.check);

            code.push(clazz.classname, '.$$properties.', name);
            code.push( '.checkMap[value]===undefined' );
          }
          else {
            throw new Error( "Could not add check to property " + name + " of class " + clazz.classname );
          }

          code.push( ')prop.error(this,5,"'+name+'","'+variant+'",value);' );
        }
      }

      if( !hasCallback ) {
        if( variant === "set" ) {
          code.push( 'this.', this.$$store.user[name], '=value;' );
        }
        else if( variant === "reset" ) {
          code.push( 'if(this.', this.$$store.user[name], '!==undefined)' );
          code.push( 'delete this.', this.$$store.user[name], ';' );
        }
        // Store incoming value
        else if( variant === "style" ) {
          code.push( 'this.', this.$$store.theme[name], '=value;' );
        }
        else if( variant === "unstyle" ) {
          code.push( 'if(this.', this.$$store.theme[name], '!==undefined)' );
          code.push( 'delete this.', this.$$store.theme[name], ';' );
        }
        else if( variant === "init" && incomingValue ) {
          code.push( 'this.', this.$$store.init[name], '=value;' );
        }
      }
      else {
        if( config.inheritable ) {
          code.push( 'var computed, old=this.', this.$$store.inherit[name], ';' );
        }
        else {
          code.push( 'var computed, old;' );
        }

        // OLD = USER VALUE

        code.push( 'if(this.', this.$$store.user[name], '!==undefined){' );

        if( variant === "set" ) {
          if( !config.inheritable ) {
            // Remember old value
            code.push( 'old=this.', this.$$store.user[name], ';' );
          }

          // Replace it with new value
          code.push( 'computed=this.', this.$$store.user[name], '=value;' );
        }
        else if( variant === "reset" ) {
          if( !config.inheritable ) {
            // Remember old value
            code.push( 'old=this.', this.$$store.user[name], ';' );
          }

          // Delete field
          code.push( 'delete this.', this.$$store.user[name], ';' );

          // Complex compution of new value
          code.push( 'if(this.', this.$$store.theme[name], '!==undefined)' );
          code.push( 'computed=this.', this.$$store.theme[name], ';' );
          code.push( 'else if(this.', this.$$store.init[name], '!==undefined){' );
          code.push( 'computed=this.', this.$$store.init[name], ';' );
          code.push( 'this.', this.$$store.useinit[name], '=true;' );
          code.push( '}' );
        }
        else {
          if( config.inheritable ) {
            // Use user value where it has higher priority
            code.push( 'computed=this.', this.$$store.user[name], ';' );
          }
          else {
            // Use user value where it has higher priority
            code.push( 'old=computed=this.', this.$$store.user[name], ';' );
          }

          // Store incoming value
          if( variant === "style" ) {
            code.push( 'this.', this.$$store.theme[name], '=value;' );
          }
          else if( variant === "unstyle" ) {
            code.push( 'delete this.', this.$$store.theme[name], ';' );
          }
          else if( variant === "init" && incomingValue ) {
            code.push( 'this.', this.$$store.init[name], '=value;' );
          }
        }

        code.push( '}' );

        // OLD = THEMED VALUE

        if( config.themeable ) {
          code.push( 'else if(this.', this.$$store.theme[name], '!==undefined){' );

          if( !config.inheritable ) {
            code.push( 'old=this.', this.$$store.theme[name], ';' );
          }

          if( variant === "set" ) {
            code.push( 'computed=this.', this.$$store.user[name], '=value;' );
          }

          // reset() is impossible, because the user has higher priority than
          // the themed value, so the themed value has no chance to ever get used,
          // when there is a user value, too.

          else if( variant === "style" ) {
            code.push( 'computed=this.', this.$$store.theme[name], '=value;' );
          }
          else if( variant === "unstyle" ) {
            // Delete entry
            code.push( 'delete this.', this.$$store.theme[name], ';' );

            // Fallback to init value
            code.push( 'if(this.', this.$$store.init[name], '!==undefined){' );
            code.push( 'computed=this.', this.$$store.init[name], ';' );
            code.push( 'this.', this.$$store.useinit[name], '=true;' );
            code.push( '}' );
          }
          else if( variant === "init" ) {
            if( incomingValue ) {
              code.push( 'this.', this.$$store.init[name], '=value;' );
            }

            code.push( 'computed=this.', this.$$store.theme[name], ';' );
          }
          else if( variant === "refresh" ) {
            code.push( 'computed=this.', this.$$store.theme[name], ';' );
          }

          code.push( '}' );
        }

        // OLD = INIT VALUE

        code.push( 'else if(this.', this.$$store.useinit[name], '){' );

        if( !config.inheritable ) {
          code.push( 'old=this.', this.$$store.init[name], ';' );
        }

        if( variant === "init" ) {
          if( incomingValue ) {
            code.push( 'computed=this.', this.$$store.init[name], '=value;' );
          } else {
            code.push( 'computed=this.', this.$$store.init[name], ';' );
          }

          // useinit flag is already initialized
        }

        // reset() and unstyle() are impossible, because the user and themed values have a
        // higher priority than the init value, so the themed value has no chance to ever get used,
        // when there is a user or themed value, too.

        else if( variant === "set" || variant === "style" || variant === "refresh" ) {
          code.push( 'delete this.', this.$$store.useinit[name], ';' );

          if( variant === "set" ) {
            code.push( 'computed=this.', this.$$store.user[name], '=value;' );
          } else if( variant === "style" ) {
            code.push( 'computed=this.', this.$$store.theme[name], '=value;' );
          } else if( variant === "refresh" ) {
            code.push( 'computed=this.', this.$$store.init[name], ';' );
          }
        }

        code.push( '}' );

        // OLD = NONE

        // reset() and unstyle() are impossible because otherwise there
        // is already an old value

        if( variant === "set" || variant === "style" || variant === "init" ) {
          code.push( 'else{' );

          if( variant === "set" ) {
            code.push( 'computed=this.', this.$$store.user[name], '=value;' );
          } else if( variant === "style" ) {
            code.push( 'computed=this.', this.$$store.theme[name], '=value;' );
          } else if(  variant === "init" ) {
            if( incomingValue ) {
              code.push( 'computed=this.', this.$$store.init[name], '=value;' );
            } else {
              code.push( 'computed=this.', this.$$store.init[name], ';' );
            }
            code.push( 'this.', this.$$store.useinit[name], '=true;' );
          }
          // refresh() will work with the undefined value, later
          code.push( '}' );
        }
      }

      if( config.inheritable ) {
        code.push( 'if(computed===undefined||computed===inherit){' );

          if( variant === "refresh" ) {
            code.push( 'computed=value;' );
          } else {
            code.push( 'var pa=this.getParent();if(pa)computed=pa.', this.$$store.inherit[name], ';' );
          }

          // Fallback to init value if inheritance was unsuccessful
          code.push( 'if((computed===undefined||computed===inherit)&&' );
          code.push( 'this.', this.$$store.init[name], '!==undefined&&' );
          code.push( 'this.', this.$$store.init[name], '!==inherit){' );
          code.push( 'computed=this.', this.$$store.init[name], ';' );
          code.push( 'this.', this.$$store.useinit[name], '=true;' );
          code.push( '}else{' );
          code.push( 'delete this.', this.$$store.useinit[name], ';}' );

        code.push( '}' );

        // Compare old/new computed value
        code.push( 'if(old===computed)return value;' );

        // Note: At this point computed can be "inherit" or "undefined".

        // Normalize "inherit" to undefined and delete inherited value
        code.push( 'if(computed===inherit){' );
        code.push( 'computed=undefined;delete this.', this.$$store.inherit[name], ';' );
        code.push( '}' );

        // Only delete inherited value
        code.push( 'else if(computed===undefined)' );
        code.push( 'delete this.', this.$$store.inherit[name], ';' );

        // Store inherited value
        code.push( 'else this.', this.$$store.inherit[name], '=computed;' );

        // Protect against normalization
        code.push( 'var backup=computed;' );

        // After storage finally normalize computed and old value
        code.push( 'if(computed===undefined)computed=null;' );
        code.push( 'if(old===undefined)old=null;' );
      }
      else if( hasCallback ) {
        // Properties which are not inheritable have no possiblity to get
        // undefined at this position. (Hint: set() and style() only allow non undefined values)
        if( variant !== "set" && variant !== "style" ) {
          code.push( 'if(computed===undefined)computed=null;' );
        }

        // Compare old/new computed value
        code.push( 'if(old===computed)return value;' );

        // Normalize old value
        code.push( 'if(old===undefined)old=null;' );
      }

      // [12] NOTIFYING DEPENDEND OBJECTS

      if( hasCallback ) {
        // Execute user configured setter
        if( config.apply ) {
          code.push( 'this.', config.apply, '(computed, old);' );
        }

        // Fire event
        if( config.event ) {
          code.push( 'this.createDispatchChangeEvent("', config.event, '", computed, old);' );
        }

        // Refresh children
        // Require the parent/children interface
        if( config.inheritable && members.getChildren ) {
          code.push( 'var a=this.getChildren();if(a)for(var i=0,l=a.length;i<l;i++){' );
          code.push( 'if(a[i].', this.$$method.refresh[name], ')a[i].', this.$$method.refresh[name], '(backup);' );
          code.push( '}' );
        }
      }

      // [13] RETURNING WITH ORIGINAL INCOMING VALUE

      // Return value
      if( incomingValue ) {
        code.push( 'return value;' );
      }

      return this.__unwrapFunctionFromCode( instance, members, name, variant, code, args );
    }
  }

} );
