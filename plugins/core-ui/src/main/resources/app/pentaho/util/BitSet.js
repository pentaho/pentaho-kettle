/*!
 * Copyright 2010 - 2018 Hitachi Vantara. All rights reserved.
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
define([
  "module",
  "pentaho/lang/Base"
], function(module, Base) {

  "use strict";

  // Allow ~0
  // jshint -W016

  return Base.extend(module.id, /** @lends pentaho.util.BitSet# */{

    /**
     * The `BitSet` class represents a set data structure that is
     * very efficient and can hold up to 31 elements, or bits.
     *
     * @alias BitSet
     * @memberOf pentaho.util
     * @class
     * @extends pentaho.lang.Base
     *
     * @description Creates a bit set instance.
     * @constructor
     * @param {?number} [bits] - The bits to be set, initially. Defaults to no bits.
     */
    constructor: function(bits) {
      this.set(bits || 0);
    },

    /**
     * Gets a value that indicates if there are no bits set.
     *
     * @type {boolean}
     * @readOnly
     */
    get isEmpty() {
      return this.__bits === 0;
    },

    /**
     * Gets an integer number with the bits currently set.
     *
     * @return {number} The internal bits as a number.
     */
    get: function() {
      return this.__bits;
    },

    /**
     * Returns a value that indicates if the current state is equal to a given mask.
     *
     * @param {number} mask - An integer containing the bit mask to test.
     *
     * @return {boolean} `true` when `mask` is equal to the current state; `false` otherwise.
     */
    is: function(mask) {
      return this.__bits === mask;
    },

    /**
     * Sets on the given bits.
     *
     * @param {?number} [mask] - The bits to be set. Defaults to all bits.
     */
    set: function(mask) {
      this.__bits = (mask == null) ? ~0 : (this.__bits | mask);
    },

    /**
     * Clears the given bits.
     *
     * @param {?number} [mask] - The bits to be cleared. Defaults to all bits.
     */
    clear: function(mask) {
      this.__bits = (mask == null) ? 0 : (this.__bits & ~mask);
    },

    /**
     * Returns a value that indicates if the current state is a subset of a given mask.
     *
     * Use this method to assert if no bits other than those described by the mask are currently set.
     *
     * @param {number} mask - An integer containing the bit mask to test.
     *
     * @return {boolean} `true` if the bits currently set are within the specified mask; `false` otherwise.
     */
    isSubsetOf: function(mask) {
      var bits = this.__bits;
      return (bits !== 0) && ((bits | mask) === mask);
    }
  });
});
