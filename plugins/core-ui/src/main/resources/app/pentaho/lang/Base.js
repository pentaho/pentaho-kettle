/*!
 * Based on Generic.js 1.1a (c) 2006-2010, Dean Edwards
 * Updated to pass JSHint and converted into a module by Kenneth Powers
 * License: http://www.opensource.org/licenses/mit-license.php
 */
/*!
 * Based on Generic.js by Dean Edwards, and later edited by Kenneth Powers.
 *
 * Changes:
 * 1. Added support for the `instanceof` operator.
 * 2. Added support for ES5 get/set properties.
 * 3. Added support for "Array classes", through `Base.Array`.
 * 4. Improved support for mixins.
 *
 *    Namely, it is now possible to call the mixed-in class constructor
 *    to initialize an instance of another class.
 *
 * 5. `Class.init` was removed.
 * 6. Added the `Class.to` method.
 *
 *    Converts its arguments to an instance of `Class`, or throws if that is impossible.
 *
 *    The default implementation tests if the first argument is an instance of that type and returns it, if it is.
 *    Otherwise, it calls `Class` with the `new` operator and all of the given arguments and returns the result.
 *
 * 7. To support 4., the previous constructor behavior, for when invoked
 *    on a non-instance of it (possibly without using the `new` operator) was dropped.
 *    It would extend the first argument with the class' prototype.
 *    Now, it ends up initializing the global object...
 * 8. Removed `Class#forEach`.
 * 9. Instances no longer have an own "base" property,
 *    it is inherited from, and set on, the corresponding "Base" root prototype object.
 * 10. `Base._prototyping` and `Base#_constructing` are no longer needed to
 *     control Class constructor and inst_extend flow.
 * 11. Class.valueOf removed. No longer needed... ?
 * 12. `Base#__base_root_proto__` is a new constant, non-enumerable property, set at each "Base" root prototype.
 * 13. `Base#__base_init__` is a new constant, non-enumerable, property, set at the prototype of "Base" classes
 *     that have an own initialization method (specified with the `constructor` property).
 * 14. Added new Class.implementStatic method to allow to specify the class interface after extend.
 *     Is parallel to Class.implement.
 * 15. Any existing static methods are inherited (not only standard Base ones).
 * 16. Dropped support for the overload Base#extend(name, value).
 *     The same method now supports only the signature Base#extend(instSpec[, keyArgs]).
 * 17. Added support to augment the set of per-class, inherited property names that are excluded from
 *     instance-extension operations.
 *     Specify `extend_exclude` when sub-classing, e.g. `Base.extend({extend_exclude: {a: true, b: true}});`.
 * 18. Added support to control, per-class, the properties extension order in instance-extension operations.
 *     Specify `extend_order` when sub-classing, e.g. `Base.extend({extend_order: ["b", "a"]});`.
 */
define([
  "module",
  "../util/object",
  "../util/fun",
  "../util/text",
  "../debug",
  "../debug/Levels"
], function(module, O, fun, text, debugMgr, DebugLevels) {
  "use strict";

  // ## Support variables

  var F_toString = Function.prototype.toString;
  var O_hasOwn = Object.prototype.hasOwnProperty;
  var A_slice = Array.prototype.slice;
  var _nextClassId = 1;
  var _excludeExtendInst = Object.freeze({
    // Generic.js gives special meaning to these properties in instance extend specifications

    // Special when creating new classes:
    //  Class#extend's instance specification
    "constructor": 1,
    "extend_order": 1,
    "extend_exclude": 1,

    // reserved for instances to call base methods.
    "base": 1,

    // Generic.js custom prototype properties (stored in prototypes of Generic.js classes)
    "__base_init__": 1,
    "__base_root_proto__": 1,
    "__base_extend_order__": 1,
    "__base_extend_exclude__": 1,
    "__base_class_id__": 1,
    "__base_bases__": 1,
    "__base_ops__": 1
  });
  var _excludeExtendStatic = Object.freeze({
    // Generic.js custom constructor properties
    "ancestor": 1,
    "mixins": 1, // holds list of mixed in classes
    "prototype": 1,
    "valueOf": 1,
    "Array": 1,
    "Object": 1,
    "base": 1, // overriding static methods sets `base` on constructors...
    "name": 1,
    "displayName": 1,
    "extend_order": 1, // why in constructors?
    "extend_exclude": 1
  });
  var _isDebugMode = debugMgr.testLevel(DebugLevels.debug, module);

  return base_create();

  // -----------

  /**
   * Defines the Base class.
   *
   * @return {Class.<pentaho.lang.Base>} The created class constructor.
   *
   * @private
   */
  function base_create() {
    /**
     * @classdesc `Base` Class for JavaScript Inheritance.
     *
     * Based on Generic.js by Dean Edwards, and later edited by Kenneth Powers.
     *
     * @class
     * @alias Base
     * @memberOf pentaho.lang
     * @amd pentaho/lang/Base
     *
     * @description Creates a new `Base` object.
     *
     * If `spec` is specified, the new object is [extended]{@link pentaho.lang.Base#extend} with it.
     *
     * @constructor
     * @param {?object} [spec] An extension specification.
     */
    function BaseObject(spec) {
      this.extend(spec);
    }

    var Base = base_root(Object, {}, "Base.Object", BaseObject);
    Base.version = "2.0";

    /**
     * The `Base.Object` root class is the base class for regular `Object` classes,
     * and an alias for [Base]{@link pentaho.lang.Base}.
     *
     * @see pentaho.lang.Base
     *
     * @name Object
     * @memberOf pentaho.lang.Base
     *
     * @class
     * @extends Object
     */
    Base.Object = Base;

    /**
     * The `Base.Array` root class is the base class for `Array` classes.
     *
     * @alias Array
     * @memberOf pentaho.lang.Base
     *
     * @class
     * @extends Array
     *
     * @borrows pentaho.lang.Base.ancestor as ancestor
     * @borrows pentaho.lang.Base.extend as extend
     * @borrows pentaho.lang.Base._extend as _extend
     * @borrows pentaho.lang.Base._subclassed as _subclassed
     * @borrows pentaho.lang.Base.mix as mix
     * @borrows pentaho.lang.Base.implement as implement
     * @borrows pentaho.lang.Base.implementStatic as implementStatic
     * @borrows pentaho.lang.Base#base as #base
     * @borrows pentaho.lang.Base#extend as #extend
     *
     * @description Initializes a new array of `Base.Array` class.
     *
     * If provided, extends the created instance with the spec in `source` parameter.
     *
     * Create an instance of `Base.Array`
     * by using {@link pentaho.lang.Base.Array.to}
     * to convert an existing array instance.
     *
     * @constructor
     * @param {Array} [source] An extension specification.
     */
    function BaseArray(source) {
      this.extend(source);
    }

    Base.Array = base_root(Array, [], "Base.Array", BaseArray);
    Base.Array.to = class_array_to;

    // ---

    /**
     * The `Base.Error` root class is the base class for `Error` classes.
     *
     * @alias Error
     * @memberOf pentaho.lang.Base
     *
     * @class
     * @extends Error
     *
     * @constructor
     * @param {string} [message] The error message.
     *
     * @borrows pentaho.lang.Base.ancestor as ancestor
     * @borrows pentaho.lang.Base.extend as extend
     * @borrows pentaho.lang.Base._extend as _extend
     * @borrows pentaho.lang.Base._subclassed as _subclassed
     * @borrows pentaho.lang.Base.mix as mix
     * @borrows pentaho.lang.Base.implement as implement
     * @borrows pentaho.lang.Base.implementStatic as implementStatic
     * @borrows pentaho.lang.Base#base as #base
     * @borrows pentaho.lang.Base#extend as #extend
     */
    function BaseError(message) {
      this.message = message;
      this.stack = (new Error()).stack;
    }

    Base.Error = base_root(Error, Object.create(Error.prototype), "Base.Error", BaseError);

    return Base;
  }

  /**
   * Gets or assigns the Generic.js class id of a given prototype.
   *
   * Having a Generic.js doesn't imply that a constructor is a class defined by Generic.js.
   * Every mixed in class gets assigned a Generic.js id.
   *
   * @param {object} proto - The prototype.
   * @param {boolean} [assign] - Indicates is an id is assigned when not present.
   * @return {number} The Generic.js class id.
   */
  function base_getOrAssignClassId(proto, assign) {
    return O_hasOwn.call(proto, "__base_class_id__")
        ? proto.__base_class_id__
        : (assign ? (proto.__base_class_id__ = _nextClassId++) : null);
  }

  /**
   * Creates a `Base` root class.
   *
   * @param {Class} NativeBase The native base constructor that this _Base_ root is rooted on.
   * @param {object} bootProto The prototype of the _boot_ constructor.
   * @param {string} baseRootName The name of the _root_ constructor.
   * @param {function} baseConstructor The base constructor.
   *
   * @return {Class.<pentaho.lang.Base>} The new `Base` root class.
   *
   * @private
   */
  function base_root(NativeBase, bootProto, baseRootName, baseConstructor) {
    // Bootstrapping "Base" class.
    // Does not have the full "Base" class interface,
    // but only enough properties set to trick `class_extend`.
    // `BaseBoot` becomes accessible only by following the prototype chain,
    // finding `bootProto` along the way.

    /* istanbul ignore next : irrelevant and hard to test */
    var BaseBoot = function() {};

    BaseBoot.prototype = bootProto;

    // Used by BaseBoot.extend, just below
    bootProto.extend = inst_extend;
    base_getOrAssignClassId(bootProto, true);
    O.setConst(bootProto, "__base_bases__", Object.freeze({}));
    O.setConst(bootProto, "__base_extend_exclude__", _excludeExtendInst);
    O.setConst(bootProto, "__base_extend_order__", Object.freeze([]));
    O.setConst(bootProto, "__base_ops__", Object.freeze([]));

    // Static interface that is inherited by all Base classes.
    BaseBoot.extend      = class_extend;
    BaseBoot._extend     = class_extend_core;
    BaseBoot._subclassed = class_subclassed;
    BaseBoot.mix         = class_mix;
    BaseBoot.implement   = class_implement;
    BaseBoot.implementStatic = class_implementStatic;
    BaseBoot.to          = class_to;
    BaseBoot.toString    = properFunToString;

    // ---

    var BaseRoot = BaseBoot.extend({
      constructor: baseConstructor
    }, {
      /**
       * The ancestor class.
       *
       * @name ancestor
       * @memberOf pentaho.lang.Base
       * @readonly
       */
      ancestor: NativeBase // Replaces BaseBoot by NativeBase
    });

    var rootProto = BaseRoot.prototype;

    /**
     * If a method has been overridden, then the base method provides access to the overridden method.
     *
     * Can also be called from within a constructor function.
     *
     * @name base
     * @memberOf pentaho.lang.Base#
     * @type {function}
     * @readonly
     * @protected
     */
    Object.defineProperty(rootProto, "base", {
      configurable: true,
      writable: true,
      enumerable: false,
      value: inst_base
    });

    // The `__base_root_proto__` property is a cheap way to obtain
    // the correct Base-root prototype for setting the `base` property,
    // in `methodOverride`.
    // Create shared, hidden, constant `__base_root_proto__` property.
    O.setConst(rootProto, "__base_root_proto__", rootProto);

    setFunName(BaseRoot, baseRootName);

    return BaseRoot;
  }

  // region Class methods

  /**
   * Creates a subclass of this one.
   *
   * All classes inherit the `extend` method, so they can also be subclassed.
   *
   * Inheritance is delegated to the [_extend]{@link pentaho.lang.Base.extend} method, which can be overridden.
   *
   * @alias extend
   * @memberOf pentaho.lang.Base
   *
   * @param {string} [name] The name of the created class. Used for debugging purposes.
   * @param {?object} [instSpec] The instance specification.
   * @param {?string[]} [instSpec.extend_order] An array of instance property names that
   * [extend]{@link pentaho.lang.Base#extend} should always apply
   * before other properties and in the given order.
   *
   * The given property names are appended to any inherited _ordered_ property names.
   *
   * @param {?object} [instSpec.extend_exclude] A set of property names to _exclude_,
   * whenever the instance side of the class
   * (through [mix]{@link pentaho.lang.Base.mix} or [implement]{@link pentaho.lang.Base.implement})
   * or
   * its instances (through [extend]{@link pentaho.lang.Base#extend})
   * are extended.
   *
   * The given property names are joined with any inherited _excluded_ property names.
   *
   * Properties can have any value.
   *
   * @param {?object} [classSpec] The static specification.
   * @param {?object} [keyArgs] The keyword arguments.
   * @param {?object} [keyArgs.exclude] A set of property names to _exclude_,
   * _both_ from the instance and class sides, in this method call.
   * Properties can have any value.
   *
   * @return {Class.<pentaho.lang.Base>} The new subclass.
   *
   * @final
   */
  function class_extend(name, instSpec, classSpec, keyArgs) {
    /* jshint validthis:true*/

    // extend()
    // extend(instSpec)
    // extend(instSpec, classSpec)
    // extend(instSpec, classSpec, keyArgs)
    if(arguments.length < 4 && typeof name !== "string") {
      keyArgs = classSpec;
      classSpec = instSpec;
      instSpec = name;
      name = null;
    }

    return this._extend(name, instSpec, classSpec, keyArgs);
  }

  /**
   * Actually creates a subclass of this one.
   *
   * The default implementation creates the subclass constructor,
   * inherits static members and handles the special
   * `instSpec.extend_exclude` and `instSpec.extend_order` properties.
   * Then, it delegates the remainder of the subclass setup to the
   * [_subclassed]{@link pentaho.lang.Base._subclassed},
   * whose default implementation mixes-in the given instance and class specifications.
   *
   * @alias _extend
   * @memberOf pentaho.lang.Base
   *
   * @param {?string} name The name of the created class.
   * @param {?object|undefined} instSpec The instance-side specification.
   * @param {?string[]} [instSpec.extend_order] An array of instance property names that
   * [extend]{@link pentaho.lang.Base#extend} should always apply
   * before other properties and in the given order.
   *
   * The given property names are appended to any inherited _ordered_ property names.
   *
   * @param {?object} [instSpec.extend_exclude] A set of property names to _exclude_,
   * whenever the instance side of the class
   * (through [mix]{@link pentaho.lang.Base.mix} or [implement]{@link pentaho.lang.Base.implement})
   * or
   * its instances (through [extend]{@link pentaho.lang.Base#extend})
   * are extended.
   *
   * The given property names are joined with any inherited _excluded_ property names.
   *
   * Properties can have any value.
   *
   * @param {?object|undefined} classSpec The class-side specification.
   * @param {?object|undefined} keyArgs The keyword arguments.
   *
   * @return {Class.<pentaho.lang.Base>} The new subclass.
   *
   * @protected
   */
  function class_extend_core(name, instSpec, classSpec, keyArgs) {
    /* jshint validthis:true*/

    var Subclass = class_extend_subclass.call(this, name, instSpec, classSpec, keyArgs);

    this._subclassed(Subclass, instSpec, classSpec, keyArgs);

    return Subclass;
  }

  /**
   * Processes the class name, so that if it is a module id (contains a "/" character),
   * it is converted to a "dotted namespaced name".
   *
   * @param {string} name - The class name.
   * @return {string} The processed class name.
   */
  function class_process_name(name) {
    // Allow the value of AMD module.id to be passed directly to name
    if(name.indexOf("/") > 0) {
      var parts = name.split("/");
      // Also, ensure last segment is upper case, as this is a class...
      // Module ids sometimes differ in casing due to other reasons.
      var lastIndex = parts.length - 1;
      parts[lastIndex] = text.firstUpperCase(parts[lastIndex]);
      name = parts.join(".");
    }

    return name;
  }

  /**
   * Creates the subclass constructor.
   *
   * Inherits base constructor properties.
   *
   * @alias _subclass
   * @memberOf pentaho.lang.Base
   *
   * @param {?string} name The name of the created class.
   * @param {?object|undefined} instSpec The instance-side specification.
   * @param {?string[]} [instSpec.extend_order] An array of instance property names that
   * [extend]{@link pentaho.lang.Base#extend} should always apply
   * before other properties and in the given order.
   *
   * The given property names are appended to any inherited _ordered_ property names.
   *
   * @param {?object} [instSpec.extend_exclude] A set of property names to _exclude_,
   * whenever the instance side of the class
   * (through [mix]{@link pentaho.lang.Base.mix} or [implement]{@link pentaho.lang.Base.implement})
   * or
   * its instances (through [extend]{@link pentaho.lang.Base#extend})
   * are extended.
   *
   * The given property names are joined with any inherited _excluded_ property names.
   *
   * Properties can have any value.
   * @param {?object|undefined} classSpec The class-side specification.
   * @param {?object|undefined} keyArgs The keyword arguments.
   *
   * @return {Class.<pentaho.lang.Base>} The new subclass.
   *
   * @private
   */
  function class_extend_subclass(name, instSpec, classSpec, keyArgs) {
    /* jshint validthis:true*/

    if(!name) {
      // Derive a name from this' name, by adding a $ character to it.
      name = this.name || this.displayName || null;
      if(name) name += "$";
    } else {
      name = class_process_name(name);
    }

    // Create PROTOTYPE and CONSTRUCTOR
    var subProto = Object.create(this.prototype);

    var Subclass = class_extend_createCtor(subProto, instSpec, name);

    // Wire proto and constructor, so that the `instanceof` operator works.
    O.setConst(subProto, "constructor", Subclass);
    Subclass.prototype = subProto;
    Subclass.ancestor = this;

    // Inherit `bases` map from base class
    var bases = Object.create(subProto.__base_bases__);
    // Adds ancestor to bases, before assigning the sub class id
    // Mixins will also be added, in duplication, to bases.
    bases[subProto.__base_class_id__] = 1;

    base_getOrAssignClassId(subProto, true);

    O.setConst(subProto, "__base_bases__", bases);
    O.setConst(subProto, "__base_ops__", [
      // Represents initial extend, as it will be done when mixing in another class.
      {name: "mix", args: [instSpec, classSpec, keyArgs]}
    ]);

    // ----

    var subExclude = instSpec && instSpec.extend_exclude;
    if(subExclude) {
      subExclude = O.assignOwn(O.assignOwn({}, subProto.__base_extend_exclude__), subExclude);

      O.setConst(subProto, "__base_extend_exclude__", Object.freeze(subExclude));
    }

    var subOrdered = instSpec && instSpec.extend_order;
    if(subOrdered) {
      subOrdered = (subProto.__base_extend_order__ || []).concat(subOrdered);

      O.setConst(subProto, "__base_extend_order__", Object.freeze(subOrdered));
    }

    // ----

    // Inherit static _methods_ or getter/setters
    class_inherit_static.call(Subclass, this);

    return Subclass;
  }

  /**
   * Called when a subclass of this class has been created.
   *
   * The default implementation mixes the given instance and class specification in the new subclass,
   * without actually recording a mix operation.
   *
   * @alias _subclassed
   * @memberOf pentaho.lang.Base
   *
   * @param {function} Subclass The created subclass.
   * @param {?object|undefined} instSpec The instance-side specification.
   * @param {?object|undefined} classSpec The static-side specification.
   * @param {?object|undefined} keyArgs The keyword arguments.
   *
   * @protected
   */
  function class_subclassed(Subclass, instSpec, classSpec, keyArgs) {
    class_mix_core.call(Subclass, instSpec, classSpec, keyArgs);
  }

  /**
   * Provides a constructor for the new subclass.
   *
   * When a `constructor` property has been specified in `instSpec`:
   *    1. If it doesn't call base: uses it as the constructor;
   *    2. If it calls base: uses its override wrapper as the constructor.
   *
   * Otherwise:
   *    1. If there is an inherited init: creates a constructor that calls it;
   *      a. If `init` calls base, it also gets wrapped;
   *    2. Otherwise, creates an empty constructor.
   *
   * @param {object} proto The subclass' prototype.
   * @param {?object|undefined} instSpec The instance-side specification.
   * @param {string} [name] The class name.
   *
   * @return {function} The subclass constructor.
   *
   * @private
   */
  function class_extend_createCtor(proto, instSpec, name) {
    /* jshint validthis:true*/

    var baseInit = proto.__base_init__;
    var Class = class_extend_readCtor(instSpec);
    if(Class) {
      // Maybe override base constructor.
      Class = methodOverride(Class, baseInit, proto.__base_root_proto__, name, /* forceOverrideIfDebug: */true);

      // Create shared, hidden, constant `__base_init__` property.
      O.setConst(proto, "__base_init__", Class);
    } else {
      Class = class_extend_createCtorInit(baseInit, name);
    }

    if(name) setFunName(Class, name);

    return Class;
  }

  /**
   * Reads the `constructor` property and returns it, if not `Object#constructor`.
   *
   * Should not be a get/set property, or it will be evaluated and the resulting value used instead.
   *
   * @param {?object|undefined} instSpec The instance-side specification.
   *
   * @return {?function} The constructor function provided in `instSpec`, if any, or `null`.
   *
   * @private
   */
  function class_extend_readCtor(instSpec) {
    var init = instSpec && instSpec.constructor;
    return init && init !== Object && fun.is(init) ? init : null;
  }

  /**
   * Creates a constructor that calls `init`.
   *
   * When mixing in, `init` won't be available through `this.__base_init__`,
   * so the fixed `init` argument is actually required.
   *
   * @param {function} init The function to be called by the constructor.
   * @param {string} [name] The class name.
   *
   * @return {function} The function that calls `init`.
   *
   * @private
   */
  function class_extend_createCtorInit(init, name) {
    if(!name || !_isDebugMode) {
      return function() {
        return init.apply(this, arguments);
      };
    }

    /* eslint no-new-func: 0 */
    var f = new Function(
      "init",
      "return function " + sanitizeJSIdentifier(name) + "() {\n" +
      "  return init.apply(this, arguments);\n" +
      "};");

    return f(init);
  }

  function sanitizeJSIdentifier(name) {
    return name.replace(/[^\w0-9$_]/gi, "_");
  }

  /**
   * Converts a value to an instance of this class, or throws if that is impossible.
   *
   * When `value` is an instance of this type, it is returned.
   *
   * Otherwise, a new instance of this class is created,
   * using all of the specified arguments as constructor arguments.
   *
   * @alias to
   * @memberOf pentaho.lang.Base
   *
   * @param {*} value The value to be cast.
   * @param {...*} other Remaining arguments passed alongside `value` to the class constructor.
   *
   * @return {pentaho.lang.Base} The converted value.
   *
   * @throws {Error} When `value` cannot be converted.
   */
  function class_to(value) {
    /* jshint validthis:true*/
    return (value instanceof this) ? value : O.make(this, arguments);
  }

  /**
   * Converts a value to an instance of this class, or throws if that is impossible.
   *
   * When `value` is {@link Nully}, and empty array instance of this class is created and returned.
   *
   * When `value` is an instance of this type, it is returned.
   *
   * When `value` is an instance of `Array`,
   * it converts it to an instance of this array class,
   * by changing it prototype and calling this class' constructor on it,
   * along with the remaining arguments specified.
   * Note that, in this case, `value` is mutated!
   *
   * Arrays of a certain sub-class cannot be newed up (in ES5, at least).
   * As such, a normal array must be created first and then "switched" to
   * inheriting from this class: `var baseArray = BaseArray.to([]);`.
   *
   * @alias pentaho.lang.Base.Array.to
   *
   * @param {pentaho.lang.Base.Array|Array} value The value to be converted.
   * @param {...*} other Remaining arguments passed alongside `value` to the class constructor.
   *
   * @return {pentaho.lang.Base.Array} The converted value.
   *
   * @throws {Error} When `value` cannot be converted.
   */
  function class_array_to(value) {
    /* jshint validthis:true*/

    // First, convert to an array.
    if(value == null)
      value = [];
    else if(value instanceof this)
      return value;
    else if(!(value instanceof Array))
      throw new Error("Cannot convert value to Base.Array.");

    return O.applyClass(value, this, A_slice.call(arguments, 1));
  }

  /**
   * Adds additional members to, or overrides existing ones of, this class.
   *
   * This method does _not_ create a new class.
   *
   * This method supports two signatures:
   *
   * 1. mix(Class: function[, keyArgs: Object]) -
   *     mixes-in the given class, both its instance and class sides.
   *
   * 2. mix(instSpec: Object[, classSpec: Object[, keyArgs: Object]]) -
   *     mixes-in the given instance and class side specifications.
   *
   * @alias mix
   * @memberOf pentaho.lang.Base
   *
   * @param {?(function|Object)} instSpec The class to mixin or the instance-side specification.
   * @param {?object} [classSpec] The class-side specification.
   * @param {?object} [keyArgs] The keyword arguments.
   * @param {?object} [keyArgs.exclude] A set of property names to _exclude_,
   *  _both_ from the instance and class sides. Properties can have any value.
   *
   * @return {Class.<pentaho.lang.Base>} This class.
   */
  function class_mix(instSpec, classSpec, keyArgs) {

    // Register mixin operation.
    this.prototype.__base_ops__.push({name: "mix", args: [instSpec, classSpec, keyArgs]});

    class_mix_core.call(this, instSpec, classSpec, keyArgs);

    return this;
  }

  /**
   * Really adds additional members to, or overrides existing ones of, this class.
   *
   * This method does _not_ create a new class and does not record an operation.
   *
   * This method supports two signatures:
   *
   * 1. mix(Class: function[, keyArgs: Object]) -
   *     mixes-in the given class, both its instance and class sides.
   *
   * 2. mix(instSpec: Object[, classSpec: Object[, keyArgs: Object]]) -
   *     mixes-in the given instance and class side specifications.
   *
   * @alias _mix
   * @memberOf pentaho.lang.Base
   *
   * @param {?(function|Object)} instSpec The class to mixin or the instance-side specification.
   * @param {?object} [classSpec] The class-side specification.
   * @param {?object} [keyArgs] The keyword arguments.
   * @param {?object} [keyArgs.exclude] A set of property names to _exclude_,
   *  _both_ from the instance and class sides. Properties can have any value.
   *
   * @return {Class.<pentaho.lang.Base>} This class.
   *
   * @private
   */
  function class_mix_core(instSpec, classSpec, keyArgs) {

    var proto = this.prototype;

    // If instSpec is a function
    var isClassMixin = fun.is(instSpec);
    if(isClassMixin) {
      // (function, keyArgs)
      keyArgs = classSpec;
      classSpec = instSpec;
      instSpec = classSpec.prototype;
    }

    // Is the mixin class already applied in an ancestor, or is an ancestor, or self.
    if(instSpec) {
      if(instSpec === proto)
        return this;

      // At least instSpec is assigned a class id.
      var classId = base_getOrAssignClassId(instSpec, true);

      // If it is already a base or mixin of this class, leave.
      // Don't mix twice. Setters called twice may have unwanted side-effects.
      if(proto.__base_bases__[classId])
        return this;
    }

    if(isClassMixin) {
      (this.mixins || (this.mixins = [])).push(classSpec);

      // Mixing in a Generic.js class?
      if(instSpec && instSpec.__base_ops__) {
        class_mix_baseJsClass.call(this, classSpec);
        return this;
      }
    }

    // Like implement, but for a single spec, with keyArgs and does not register a new operation.
    if(instSpec) proto.extend(instSpec, keyArgs);

    if(classSpec) class_implementStaticOne.call(this, classSpec, keyArgs);

    return this;
  }

  /**
   * Mixes another Generic.js class into this one,
   * by replaying the recorded operations defining it.
   *
   * @param {function} Other - A Generic.js class to mix in.
   * @private
   */
  function class_mix_baseJsClass(Other) {

    var lcaProto = O.lca(this.prototype, Other.prototype);

    // assert lcaProto != null or Other would not be a BaseJs class

    // Build array with ascendant protos of Other not shared with this.
    // Note lcaProto is shared and thus not included.
    var sourceProtos = [];
    var sourceProto = Other.prototype;
    while(sourceProto !== lcaProto) {
      sourceProtos.push(sourceProto);
      sourceProto = Object.getPrototypeOf(sourceProto);
    }

    // Apply from the most base type to Other
    var thisProto = this.prototype;
    var i = sourceProtos.length;
    while(i--) {
      sourceProto = sourceProtos[i];

      // Again, must have a class id.
      var classId = base_getOrAssignClassId(sourceProto);

      // If it is already a base or mixin of this class, leave.
      // Don't mix twice. Setters called twice may have unwanted side-effects.
      if(!thisProto.__base_bases__[classId]) {
        thisProto.__base_bases__[classId] = 1;

        // The replay of operations may add more bases and mixins.
        class_mix_replayOps.call(this, sourceProtos[i].__base_ops__);
      }
    }
  }

  /**
   * Replays an array of recorded operations in this class.
   *
   * @param {Array.<{name: string, args: Array}>} ops - An array of operations
   * @private
   */
  function class_mix_replayOps(ops) {
    var i = -1;
    var L = ops.length;
    while(++i < L) this[ops[i].name].apply(this, ops[i].args);
  }

  /**
   * Adds instance specifications to this class.
   *
   * This method does _not_ create a new class.
   *
   * Each instance specification can be a class or an object.
   * When it is a class, only its instance-side is mixed-in.
   *
   * @alias implement
   * @memberOf pentaho.lang.Base
   *
   * @param {...?function|...Object} instSpecs The instance-side specifications to mix-in.
   *
   * @return {Class.<pentaho.lang.Base>} This class.
   *
   * @see pentaho.lang.Base.implementStatic
   * @see pentaho.lang.Base.mix
   */
  function class_implement() {

    var proto = this.prototype;

    // Register implementOne operation.
    proto.__base_ops__.push({name: "implement", args: A_slice.call(arguments)});

    var i = -1;
    var L = arguments.length;
    while(++i < L) {
      var v = arguments[i];
      proto.extend(fun.is(v) ? v.prototype : v);
    }

    return this;
  }

  /**
   * Inherit each of the `BaseClass` properties to this class.
   *
   * @param {function} BaseClass Class from where to inherit static members.
   *
   * @private
   */
  function class_inherit_static(BaseClass) {
    /* jshint validthis:true*/

    for(var name in BaseClass)
      if((!(name in Object) || (BaseClass[name] !== Object[name])) &&
         !O_hasOwn.call(_excludeExtendStatic, name))
        inst_extend_propDesc.call(this, name, BaseClass, undefined, /* funOnly: */true);
  }

  /**
   * Adds static specifications to this class.
   *
   * This method does _not_ create a new class.
   *
   * Each class-side/static specification can be a class or an object.
   * When it is a class, only its class-side is mixed-in.
   *
   * @alias implementStatic
   * @memberOf pentaho.lang.Base
   *
   * @param {...(function|object|Nully)} classSpecs The class-side specifications to mix-in.
   *
   * @return {Class.<pentaho.lang.Base>} This class.
   */
  function class_implementStatic() {
    /* jshint validthis:true*/

    // Register implementStatic operation.
    this.prototype.__base_ops__.push({name: "implementStatic", args: A_slice.call(arguments)});

    var i = -1;
    var L = arguments.length;
    while(++i < L) {
      // Extend the constructor with `classSpec`.
      // Note that overriding static methods sets the `base` property on the constructor...
      var classSpec = arguments[i];
      if(classSpec) class_implementStaticOne.call(this, classSpec);
    }

    return this;
  }

  function class_implementStaticOne(source, keyArgs) {

    // Ad hoc, properties to exclude.
    var l_exclude = keyArgs && keyArgs.exclude;

    // How to detect classSpec properties, which are copy-inherited,
    // from a common base constructor? These should not be copied over, unless changed by classSpec.
    // If not changed, then the properties of this, which might themselves have been overridden,
    // should not be overwritten.

    var n;
    if(fun.is(source)) {
      /*
       * If M overrode x, then M.x_1 should overwrite T.x_?, otherwise don't touch it.
       *
       *       l-c-ancestor
       *          A - x_0
       *        /   \
       *      /       \
       *    /           \
       *   T x_?  <--   M - x_1
       * target       mixin
       */

      // This is the easiest way to get the "LCA".
      var lcaProto = O.lca(this.prototype, source.prototype);
      var LCA = lcaProto && lcaProto.constructor;

      for(n in source) {
        if((!(n in Object) || (source[n] !== Object[n])) &&
           !O_hasOwn.call(_excludeExtendStatic, n) &&
           (!l_exclude || !O_hasOwn.call(l_exclude, n)) &&
           (!LCA ||
            !equalPropDesc(
               Object.getOwnPropertyDescriptor(source, n),
               Object.getOwnPropertyDescriptor(LCA, n)))) {

          inst_extend_propDesc.call(this, n, source);
        }
      }

    } else {
      for(n in source) {
        if((!(n in Object) || (source[n] !== Object[n])) &&
           !O_hasOwn.call(_excludeExtendInst, n) &&
           !O_hasOwn.call(_excludeExtendStatic, n) &&
           (!l_exclude || !O_hasOwn.call(l_exclude, n))) {

          inst_extend_propDesc.call(this, n, source);
        }
      }
    }
  }

  // endregion

  // region Instance methods

  function inst_base() {}

  /**
   * Extend an object with the properties of another.
   *
   * Methods that are overridden are accessible through `this.base`.
   *
   * This object is extended, but its class doesn't change.
   *
   * @alias extend
   * @memberOf pentaho.lang.Base#
   *
   * @param {?object} source The instance specification.
   * @param {?object} [keyArgs] The keyword arguments.
   * @param {?object} [keyArgs.exclude] A map of property names to _exclude_ from `source`.
   *
   * @return {object} This object.
   */
  function inst_extend(source, keyArgs) {
    if(source) {
      var inst = this;
      var rootProto = this.__base_root_proto__;
      var exclude = this.__base_extend_exclude__;

      // If source shares part of the prototype chain with `this`, we do not wish to copy properties
      // of those shared prototypes.
      // These might share only the root proto, or, as is the case of mixins, a lower (common ancestor) prototype.
      var lcaExclude = rootProto && O.lca(source, this);
      if(lcaExclude) {

        // From `source` to `lcaExclude` (exclusive), must mark all classes along the path as `bases` and `mixins`.
        var ascProto = source;
        while(ascProto && ascProto !== lcaExclude && ascProto !== rootProto) {
          var classId = base_getOrAssignClassId(ascProto, false);
          if(classId) {
            inst.__base_bases__[classId] = 1;
          }
          ascProto = Object.getPrototypeOf(ascProto);
        }
      }

      // Additional, ad hoc, properties to exclude.
      var l_exclude = keyArgs && keyArgs.exclude;

      var visited = Object.create(null);

      var extendProp = function(n) {
        if(!O_hasOwn.call(visited, n) &&
           !O_hasOwn.call(exclude, n) &&
           (!l_exclude || !O_hasOwn.call(l_exclude, n))) {
          visited[n] = 1;
          inst_extend_propDesc.call(inst, n, source, rootProto, null, lcaExclude);
        }
      };

      // Ordered properties first.
      var name;
      var ordered = this.__base_extend_order__;
      if(ordered) {
        var i = -1;
        var L = ordered.length;
        while(++i < L) if((name = ordered[i]) in source) extendProp(name);
      }

      // All other properties
      /* eslint guard-for-in: 0 */
      for(name in source) extendProp(name);
    }

    return this;
  }

  /**
   * Copy member from `source` to this object.
   *
   * @param {string} name The name of the property.
   * @param {object} source Object from where to copy members.
   * @param {?object} [rootProto] The root prototype.
   * When unspecified, overriding methods requires using each actual instance to store the special `base` property.
   * @param {?boolean} [funOnly=false] If true, copy only if member is a function.
   * @param {?object} [lcaExclude] An object which base of both this and `source` and whose properties should
   * be excluded from extension.
   * @private
   */
  function inst_extend_propDesc(name, source, rootProto, funOnly, lcaExclude) {
    /* jshint validthis:true*/
    var baseDesc;
    var desc = O.getPropertyDescriptor(source, name, lcaExclude);
    if(desc) {
      baseDesc = O.getPropertyDescriptor(this, name);
      if(desc.get || desc.set) {
        if(baseDesc) {
          if(desc.get || baseDesc.get) desc.get = methodOverride(desc.get, baseDesc.get, rootProto);
          if(desc.set || baseDesc.set) desc.set = methodOverride(desc.set, baseDesc.set, rootProto);

          // Don't touch the property if get/set did not change.
          if(desc.get === baseDesc.get && desc.set === baseDesc.set) return;
        }

        Object.defineProperty(this, name, desc);
      } else {
        // Just ignore trying to set a value on a readOnly property.
        if(baseDesc && baseDesc.get && !baseDesc.set)
          return;

        // Property value
        // Note that the property of _this_ may have a setter.
        var value = desc.value;
        if(fun.is(value)) {
          // Only override if it is a normal property.
          // When there is not baseDesc, methodOverride may need to provide a default base method.
          if(baseDesc && (baseDesc.get || baseDesc.set)) {
            this[name] = value;
          } else {
            this[name] = methodOverride(value, baseDesc && baseDesc.value, rootProto, name);
          }
        } else if(!funOnly) {
          this[name] = value;
        }
      }
    }
  }

  function equalPropDesc(a, b) {
    // both nully ?
    if(a == null && b == null) return true;

    // one is nully ?
    if(!a || !b) return false;

    return (a.get === b.get) && (a.set === b.set) && (a.value === b.value);
  }
  // endregion

  // region Method Override

  /**
   * Evaluates the need to wrap a method for overriding
   * and returns the appropriate version.
   *
   * If `value` is null, returns the `baseValue`.
   * If `value` doesn't call `this.base`, returns `value`.
   *
   * Otherwise, returns a wrapped method that calls `value`,
   * and modifies its `valueOf()` and `toString()` methods
   * to return the unwrapped original function.
   *
   * If `baseValue` is null `this.base` will be the empty
   * `inst_base` function.
   *
   * @param {?function} value The method overriding.
   * @param {?function} baseValue The method to override.
   * @param {?object} [rootProto] The root prototype.
   * When unspecified, overriding methods requires using each actual instance to store the special `base` property.
   * @param {string} [name] The name to give to the overriding method.
   * @param {boolean} [forceOverrideIfDebug=false] Indicates that the method should be overridden, when in debug mode,
   * even in a case where the base method is not called. Only applies if name is specified non-empty.
   * @return {?function} The override-ready function,
   * or null if both `value` and `baseValue` are nully.
   *
   * @private
   */
  function methodOverride(value, baseValue, rootProto, name, forceOverrideIfDebug) {
    if(!value) return baseValue;

    // Get the unwrapped value.
    var method = value.valueOf();

    if(!baseValue) {
      // if `value` was wrapped, return it
      if(method !== value) {
        return value;
      }

      // if not, provide a default empty `baseValue`
      baseValue = inst_base;
    }

    // valueOf() test is to avoid circular references
    if(!method ||
       (baseValue.valueOf && baseValue.valueOf() === method) ||
       ((!forceOverrideIfDebug || !_isDebugMode) && !methodCallsBase(method)))
      return value;

    value = methodOverrideWrapWithName(method, baseValue, rootProto, name);

    // Returns the underlying, wrapped method
    value.valueOf = function(type) {
      return type === "object" ? value : method;
    };

    value.toString = properFunToString;

    return value;
  }

  /**
   * Checks if `this.base` is called in the body of `method`.
   *
   * @param {function} method The function to check.
   *
   * @return {boolean} `true` if the method calls `this.base`.
   *
   * @private
   */
  function methodCallsBase(method) {
    return /\bthis(\s*)\.(\s*)base\b/.test(method);
  }

  /**
   * Creates a wrapper method that injects `baseMethod` into `this.base` and calls `method`.
   *
   * The `baseMethod` is injected into the root constructor `rootProto`, when available.
   *
   * If not (non-Base classes) it is injected directly in the instance.
   *
   * @param {function} method The method overriding.
   * @param {function} baseMethod The method to override.
   * @param {?object} [rootProto] The root prototype.
   * When unspecified, overriding methods requires using each actual instance to store the special `base` property.
   *
   * @return {function} The wrapped function.
   *
   * @private
   */
  function methodOverrideWrap(method, baseMethod, rootProto) {
    if(rootProto)
      return function() {
        var previous = rootProto.base; rootProto.base = baseMethod;
        try {
          return method.apply(this, arguments);
        } finally { rootProto.base = previous; }
      };

    // float
    return function() {
      var previous = this.base; this.base = baseMethod;
      try {
        return method.apply(this, arguments);
      } finally { this.base = previous; }
    };
  }

  function methodOverrideWrapWithName(method, baseMethod, rootProto, name) {

    if(!name || !_isDebugMode)
      return methodOverrideWrap(method, baseMethod, rootProto);

    var f;

    if(rootProto) {
      f = new Function(
          "_method_",
          "_baseMethod_",
          "_rootProto_",
          "return function " + sanitizeJSIdentifier(name) + "() {\n" +
          "  var previous = _rootProto_.base; _rootProto_.base = _baseMethod_;\n" +
          "  try {\n" +
          "      return _method_.apply(this, arguments);\n" +
          "  } finally { _rootProto_.base = previous; }\n" +
          "};");
    } else {
      // float
      f = new Function(
          "_method_",
          "_baseMethod_",
          "_rootProto_",
          "return function " + sanitizeJSIdentifier(name) + "() {\n" +
          "  var previous = this.base; this.base = _baseMethod_;\n" +
          "  try {\n" +
          "     return _method_.apply(this, arguments);\n" +
          "  } finally { this.base = previous; }\n" +
          "};");
    }

    return f(method, baseMethod, rootProto);
  }

  // endregion

  // region Helpers

  /**
   * Returns the string representation of this method.
   *
   * The native String function or toString method do not call .valueOf() on its argument.
   * However, concatenating with a string does...
   *
   * @return {string} The string representation of the function.
   *
   * @private
   */
  function properFunToString() {
    /* jshint validthis:true*/
    return F_toString.call(this.valueOf());
  }

  /**
   * Defines the `name` and `displayName` of a function.
   *
   * Because `Function#name` is non-writable but configurable it
   * can be set, but only through `Object.defineProperty`.
   *
   * @param {function} fun The function.
   * @param {string} name The name to set on the function.
   *
   * @private
   */
  function setFunName(fun, name) {
    fun.displayName = name;
    try {
      Object.defineProperty(fun, "name", {value: name, configurable: true});
    } catch(ex) {
      // TODO: for some pre-ES6 engines the property is not configurable
      // Notably, PhantomJS 1.9.8 fails here.
    }
  }

  // endregion
});
