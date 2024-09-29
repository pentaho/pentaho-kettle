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
define(function() {
  // missing use strict

  /**
   * The `fun` namespace contains a collection of utility functions.
   *
   * @name pentaho.util.fun
   * @namespace
   * @private
   * @amd pentaho/util/fun
   */
  return (function() {
    "use strict";

    return /** @lends pentaho.util.fun */{

      /**
       * Determine if the value passed is a `function`.
       *
       * @param {*} f Value to be tested.
       * @return {boolean} `true` if `f` is a function, `false` otherwise.
       */
      is: function(f) {
        return typeof f === "function";
      },

      /**
       * Converts a value to a function, if possible.
       *
       * @param {string|function|Nully} [f] Value to be converted.
       * @return {?function} A function or `null`.
       */
      as: asFun,

      /**
       * Creates a function that always returns the same value.
       *
       * @param {*} v Value to be return by the constant function.
       * @return {function} Constant function.
       */
      constant: function(v) {
        return function() {
          return v;
        };
      },

      /**
       * Compares its two arguments for order.
       * Returns `-1`, `0`, or `1` as the first argument is less than, equal to, or greater than the second.
       *
       * @param {*} a First comparison value
       * @param {*} b Second comparison value
       * @return {number} The comparison value.
       */
      compare: function(a, b) {
        return (a === b) ? 0 : ((a > b) ? 1 : -1);
      },

      /**
       * Looks through each value in `attrs`, creating a list
       * that contains all the key-value pairs and use that list
       * to create a predicate function.
       *
       * When the created list is empty, will return `null`.
       *
       * @param {?object} [attrs] Attribute names and values to test for.
       * @return {?function} A predicate function, or `null`.
       */
      predicate: function(attrs) {
        var attrValues = [];

        if(attrs) Object.keys(attrs).forEach(function(name) {
          var value = attrs[name];
          if(value !== undefined) attrValues.push([name, value]);
        });

        return attrValues.length ? buildPredicate(attrValues) : null;
      }
    };
  })();

  /**
   * Auxiliary function of {@link pentaho.util.fun.predicate}
   *
   * @param {Array} attrValues Processed attribute names and values, as an array of arrays,
   * each with the name and the value to test for.
   * @return {function} The predicate function.
   */
  function buildPredicate(attrValues) {
    "use strict";

    return function instancePredicate(inst) {
      if(!inst) return false;

      var i = attrValues.length;
      var attrValue;
      while(i--) {
        attrValue = attrValues[i];
        if(inst[attrValue[0]] !== attrValue[1])
          return false;
      }
      return true;
    };
  }

  function asFun(f) {
    /* eslint no-eval: 0 */
    if(f) {
      if(typeof f === "string") f = eval("(" + f + ")");
      if(typeof f === "function") return f;
    }
    return null;
  }
});
