/*!
 * Copyright 2010 - 2019 Hitachi Vantara. All rights reserved.
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
  "../lang/ArgumentRequiredError"
], function(ArgumentRequiredError) {
  "use strict";

  var A_slice = Array.prototype.slice;

  /**
   * The `arg` namespace contains utility methods for handling function arguments.
   *
   * @name pentaho.util.arg
   * @namespace
   * @amd pentaho/util/arg
   * @private
   */
  return /** @lends pentaho.util.arg */{
    /**
     * Gets the value of an optional property of an object.
     * The property is considered specified when its value is not a {@link Nully} value.
     * When the property is not specified, the value of `dv` is returned. The latter defaults to `undefined`.
     *
     * @param {?object} o The object from which to get a property.
     * @param {string} p The name of the property.
     * @param {*} [dv] The default value.
     *
     * @return {*} The value of the property. If the property does not exist, returns `dv`.
     */
    optional: function(o, p, dv) {
      var v;
      return o && (v = o[p]) != null ? v : dv;
    },

    /**
     * Gets the value of an defined property of an object.
     * The property is considered specified when its value is not `undefined`.
     * When the property is not specified, the value of `dv` is returned. The latter defaults to `undefined`.
     *
     * @param {?object} o The object from which to get a property.
     * @param {string} p The name of the property.
     * @param {*} [dv] The default value.
     *
     * @return {*} The value of the property. If the property does not exist, returns `dv`.
     */
    defined: function(o, p, dv) {
      var v;
      return o && (v = o[p]) !== undefined ? v : dv;
    },

    /**
     * Gets the value of an required property of an object.
     * The property is considered specified when its value is not a {@link Nully} value.
     *
     * @param {?object} o The object from which to get a property.
     * @param {string} p The name of the property.
     * @param {?string} pscope The name of the argument where the `o` is received in the caller.
     *
     * @return {*} The found required property value.
     * @throws {Error} Argument required. The `o` must contain the `p`.
     */
    required: function(o, p, pscope) {
      var v;
      if(o && (v = o[p]) != null) {
        return v;
      }

      throw new ArgumentRequiredError((pscope ? (pscope + ".") : "") + p);
    },

    /**
     * Slices the provided array.
     *
     * @param {object} args Array of anything.
     * @param {number} [start=0] The index of the `args` array to begin the slice.
     * @param {number} [end] The index of the `args` array to end the slice at.
     *
     * @return {Array} Array containing the elements from the `args` array between the `start` and the `end`.
     */
    slice: function(args, start, end) {
      if(!args) {
        throw new ArgumentRequiredError("args");
      }

      /* eslint default-case: 0 */
      switch(arguments.length) {
        case 1: return A_slice.call(args);
        case 2: return A_slice.call(args, start);
      }

      return A_slice.call(args, start, end);
    }
  };
});
