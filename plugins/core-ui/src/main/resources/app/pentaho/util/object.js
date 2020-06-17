/*!
 * Copyright 2010 - 2017 Hitachi Vantara. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
define(["./has"], function(has) {
  "use strict";

  var O_hasOwn = Object.prototype.hasOwnProperty;
  var A_empty = [];
  var setProtoOf = has("Object.setPrototypeOf")
    ? Object.setPrototypeOf :
    (has("Object.prototype.__proto__") ? setProtoProp : setProtoCopy);
  var constPropDesc = {value: undefined, writable: false, configurable: false, enumerable: false};
  var O_root = Object.prototype;
  var O_isProtoOf = Object.prototype.isPrototypeOf;

  var PROP_UNIQUE_ID = "___OBJUID___";
  var nextUniqueId = 1;

  /**
   * The `object` namespace contains functions for
   * common tasks dealing with the manipulation of the internal state of objects.
   *
   * @name object
   * @namespace
   * @memberOf pentaho.util
   * @amd pentaho/util/object
   * @private
   */
  return /** @lends pentaho.util.object */{
    /**
     * Deletes a direct property of an object and returns its value before deletion.
     *
     * Constant properties cannot be deleted.
     *
     * If the specified object is a {@link Nully} value,
     * or the specified property does not exist in the object,
     * own or not,
     * the value of `dv` is returned,
     * or `undefined`, if unspecified.
     *
     * Otherwise, if the specified property exists in the object,
     * but is not an own property,
     * it is not deleted and the inherited value is returned.
     *
     * Finally, if the specified property exists in the object
     * and is an own property,
     * it is deleted and its _previous_ own value is returned.
     *
     * @param {?(object|function)} o - The object whose own property is to be deleted.
     * @param {string} p - The name of the property.
     * @param {*} [dv] - The default value. Defaults to `undefined`.
     * @return {*} The value of the property before deletion.
     *
     * @throws {TypeError} Cannot delete a constant property.
     */
    "delete": function(o, p, dv) {
      var v = dv;
      if(o && (p in o)) {
        v = o[p];
        delete o[p];
      }

      return v;
    },

    /**
     * Calls a function that uses a disposable resource.
     *
     * Returns the function result.
     * The disposable resource is disposed before returning.
     *
     * @param {pentaho.lang.IDisposable} disposable The disposable resource.
     * @param {function(pentaho.lang.IDisposable):*} fun The function to call with the given resource.
     * @param {?object} [context] The context in which to call `fun`.
     *
     * @return {*} The value returned by `fun`.
     */
    using: function(disposable, fun, context) {
      try {
        return fun.call(context, disposable);
      } finally {
        disposable.dispose();
      }
    },

    /**
     * Determines if a property is a direct property of an object.
     *
     * This method does not check down the object's prototype chain.
     *
     * If the specified object is a {@link Nully} value, `false` is returned.
     *
     * @param {?(object|function)} o - The object to be tested.
     * @param {string} p - The name of the property.
     * @return {boolean} `true` if this is a direct/own property, or `false` otherwise.
     */
    hasOwn: function(o, p) {
      return !!o && O_hasOwn.call(o, p);
    },

    /**
     * Returns the value of a direct property, or the default value.
     *
     * This method does not check down the object's prototype chain.
     *
     * If the specified object is a {@link Nully} value, the default value is returned.
     *
     * @param {?(object|function)} o - The object whose property is to be retrieved.
     * @param {string} p - The name of the property.
     * @param {*} [dv] - The default value. Defaults to `undefined`.
     * @return {boolean} The value of the property if it exists in the object and is an own property,
     * otherwise returns `dv`.
     */
    getOwn: function(o, p, dv) {
      return o && O_hasOwn.call(o, p) ? o[p] : dv;
    },

    /**
     * Sets a property in an object to a value and makes it constant (immutable).
     *
     * The created property cannot be overwritten, deleted, enumerated or configured.
     *
     * @param {object} o - The object whose property is to be set.
     * @param {string} p - The name of the property.
     * @param {*} v - The value of the property.
     */
    setConst: setConst,

    /**
     * Iterates over all **direct enumerable** properties of an object,
     * yielding each in turn to an iteratee function.
     *
     * The iteratee is bound to the context object, if one is passed,
     * otherwise it is bound to the iterated object.
     * Each invocation of iteratee is called with two arguments: (propertyValue, propertyName).
     * If the iteratee function returns `false`, the iteration loop is broken out.
     *
     * @param {?(object|function)} o - The object containing the properties to be iterated.
     * @param {function} fun - The function that will be iterated.
     * @param {?object} [x] - The object which will provide the execution context of the iteratee function.
     * If nully, the iteratee will run with the context of the iterated object.
     *
     * @return {boolean} `true` when the iteration completed regularly,
     * or `false` if the iteration was forcefully terminated.
     */
    eachOwn: function(o, fun, x) {
      for(var p in o) {
        if(O_hasOwn.call(o, p) && fun.call(x || o, o[p], p) === false)
          return false;
      }

      return true;
    },

    /**
     * Iterates over the own properties of a source object and assigns them to a target object.
     *
     * @param {object|function} to - The target object.
     * @param {?object} from - The source object.
     * @return {object} The target object.
     */
    assignOwn: function(to, from) {
      for(var p in from) {
        if(O_hasOwn.call(from, p))
          to[p] = from[p];
      }

      return to;
    },

    /**
     * Iterates over the own properties of a source object,
     * checks if their values are defined, and if so, assigns them to a target object.
     *
     * @param {object} to - The target object.
     * @param {?object} from - The source object.
     * @return {object} The target object.
     * @method
     * @see pentaho.util.object.assignOwn
     */
    assignOwnDefined: assignOwnDefined,

    /**
     * Creates a shallow clone of a plain object or array.
     *
     * Undefined properties are ignored.
     * If `v` is an instance of a class, or a simple value (e.g. string, number),
     * no clone is created and the original object is returned instead.
     *
     * @param {object|Array|*} v - The source object.
     * @return {*} A shallow copy of the object,
     * or the object itself if it is neither a plain object nor an array.
     */
    cloneShallow: function(v) {
      if(v && typeof v === "object") {
        if(v instanceof Array)
          v = v.slice();

        // TODO: FIXME: take care for objects with a null prototype which do not fall into the following test.
        else if(v.constructor === Object)
          v = assignOwnDefined({}, v);
      }

      return v;
    },

    // only used by pentaho.lang.Base
    /**
     * Retrieves an object that describes a property, traversing the inheritance chain if necessary.
     *
     * @param {object} object - The object that contains the property.
     * @param {string} property - The name of property.
     * @param {?object} lcaExclude - A lowest-common-ancestor object whose inherited properties should
     * not be returned.
     * @return {?object} The
     * [property descriptor]{@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/defineProperty}.
     * @method
     */
    getPropertyDescriptor: getPropertyDescriptor,

    /**
     * Obtains the **l**owest **c**ommon **a**ncestor of both of the given objects, if any.
     *
     * If one of the objects has a `null` prototype, then there is no common ancestor and `null` is returned.
     *
     * @param {?object} o1 - The first object.
     * @param {?object} o2 - The second object.
     *
     * @return {?object} The lowest common ancestor object, if any; or `null`, if none.
     */
    lca: function(o1, o2) {
      if(!o1 || !o2) return null;

      var lca = o2;
      while(o1 !== lca && (lca !== O_root) && !O_isProtoOf.call(lca, o1) && (lca = Object.getPrototypeOf(lca)));

      // o1 may not descend from O_root, when any of its ancestors has no prototype.
      if(lca === O_root && !O_isProtoOf.call(lca, o1)) lca = null;

      return lca;
    },

    // only used by pentaho.lang.Base
    /**
     * Constructs an instance of a class,
     * from an array of arguments.
     *
     * @param {function} Ctor - The constructor function of the class to be instantiated.
     * @param {?Array} [args] - The array of arguments, or arguments object, which will be passed to the constructor.
     * @return {object} The constructed instance.
     */
    make: function(Ctor, args) {
      /* eslint default-case: 0 */
      switch(args ? args.length : 0) {
        case 0: return new Ctor();
        case 1: return new Ctor(args[0]);
        case 2: return new Ctor(args[0], args[1]);
        case 3: return new Ctor(args[0], args[1], args[2]);
      }

      // generic implementation, possibly slower
      var inst = Object.create(Ctor.prototype);
      return Ctor.apply(inst, args) || inst;
    },

    /**
     * Sets the _prototype_ (i.e., the internal `prototype` property) of a specified object
     * to another object.
     *
     * Setting the _prototype_ to `null` breaks the object's inheritance.
     *
     * Delegates to the native implementation of `Object.setPrototypeOf`, if supported.
     *
     * @param {object} object - The object which is to have its prototype set.
     * @param {?object} prototype - The object's new prototype.
     * @return {object} The `object`.
     */
    setPrototypeOf: setProtoOf,

    // only used by pentaho.lang.Base
    /**
     * Mutates an object so that it becomes an instance of a given class, if not already.
     *
     * In particular, the _prototype_ and _constructor_ properties of a given object are replaced, if necessary.
     *
     * @param {object} inst - The object to be mutated.
     * @param {function} Class - The constructor of the class to be applied to the object.
     * @param {?Array} [args] - The array of arguments to be passed to the constructor of the class.
     * @return {object} The mutated object.
     */
    applyClass: function(inst, Class, args) {
      var proto = Class.prototype;
      if(proto === inst || proto.isPrototypeOf(inst))
        return inst;

      setProtoOf(inst, proto);

      if(inst.constructor !== Class) {
        Object.defineProperty(inst, "constructor", {
          // enumerable: false,
          configurable: true,
          writable: true,
          value: Class
        });
      }

      return Class.apply(inst, args || A_empty) || inst;
    },

    /**
     * Gets the unique id of an object, optionally assigning one if it does not have one.
     *
     * @param {object} inst - The object.
     * @param {?boolean} [assignIfMissing=false] - Indicates that a unique id should be assigned if
     * it does not have one.
     * @return {?string} The unique id or `null`.
     */
    getUniqueId: getUniqueId,

    /**
     * Gets the key of a value suitable for identifying it amongst values of the same type.
     *
     * @param {*} value - The value.
     * @return {string} The value's key.
     */
    getSameTypeKey: getSameTypeKey,

    /**
     * Gets a key function suitable for identifying values amongst values of the same, given type.
     *
     * @param {string} typeName - The name of the type.
     * Besides the possible known results of JavaScript's `typeof` operator,
     * the value `date`, representing instances of {@link Date},
     * is also supported.
     *
     * @return {function(*):string} The key function.
     */
    getSameTypeKeyFun: function(typeName) {
      switch(typeName) {
        case "string":
          return stringKey;
        case "number":
        case "boolean":
          return numberOrBooleanKey;
        case "date":
          return dateKey;
        default:
          return getSameTypeKey;
      }
    }
  };

  function getUniqueId(inst, assignIfMissing) {
    var uid = inst[PROP_UNIQUE_ID];
    if(uid == null) {
      if(assignIfMissing) {
        uid = "i" + (nextUniqueId++);
        setConst(inst, PROP_UNIQUE_ID, uid);
      } else {
        uid = null;
      }
    }

    return uid;
  }

  function assignOwnDefined(to, from) {
    var v;
    for(var p in from) {
      if(O_hasOwn.call(from, p) && (v = from[p]) !== undefined)
        to[p] = v;
    }

    return to;
  }

  function setConst(o, p, v) {
    // Specifying writable ensures overriding previous writable value.
    // Otherwise, only new properties receive a default of false...
    constPropDesc.value = v;

    // Leaks `v` if the following throws, but its an acceptable risk, being an error condition.
    Object.defineProperty(o, p, constPropDesc);

    constPropDesc.value = undefined;
  }

  /**
   * Copies a single property from a source object to a target object, provided it is defined.
   * A property is defined if either its value, getter or setter are defined.
   *
   * @param {object} to - The target object.
   * @param {object} from - The source object.
   * @param {string} p - the name of the property.
   * @return {object} The target object.
   * @private
   */
  function copyOneDefined(to, from, p) {
    var pd = getPropertyDescriptor(from, p);
    if(pd && pd.get || pd.set || pd.value !== undefined)
      Object.defineProperty(to, p, pd);
    return to;
  }

  function getPropertyDescriptor(o, p, lcaExclude) {
    var pd;
    while(!(pd = Object.getOwnPropertyDescriptor(o, p)) && (o = Object.getPrototypeOf(o)) &&
          (!lcaExclude || o !== lcaExclude));
    return pd || null;
  }

  function setProtoProp(o, proto) {
    /* eslint no-proto: 0 */
    o.__proto__ = proto;
    return o;
  }

  function setProtoCopy(o, proto) {
    /* eslint guard-for-in: 0 */
    for(var p in proto) copyOneDefined(o, proto, p);
    return o;
  }

  /**
   * Gets the key of a string value.
   *
   * @param {?string} v - The value.
   * @return {string} The key.
   */
  function stringKey(v) {
    return v == null ? "" : v;
  }

  /**
   * Gets the key of a number or boolean value.
   *
   * @param {null|number|boolean} v - The value.
   * @return {string} The key.
   */
  function numberOrBooleanKey(v) {
    return v == null ? "" : v.toString();
  }

  /**
   * Gets the key of a `Date` value.
   *
   * @param {Date} v - The value.
   * @return {string} The key.
   */
  function dateKey(v) {
    // The normal toString ignores ms...
    return v == null ? "" : v.toISOString();
  }

  function getSameTypeKey(value) {

    if(value == null) {
      // null, undefined
      return "";
    }

    // eslint-disable-next-line default-case
    switch(typeof value) {
      case "string": return value;
      case "number":
      case "boolean": return value.toString();
    }

    // function, object (Array included)

    if(value instanceof Date) {
      // Use toISOString(), because Date#toString does not include ms.
      return value.toISOString();
    }

    return getUniqueId(value, /* assignIfMissing: */ true);
  }
});
