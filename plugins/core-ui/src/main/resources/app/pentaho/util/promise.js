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
  "./arg",
  "./error",
  "../shim/es6-promise"
], function(arg, error) {
  "use strict";

  /* global Promise:false*/

  /**
   * The `promise` namespace contains utilities for working with the future-standard ES6 `Promise` API,
   * using patterns not directly covered by it.
   *
   * Loading this module also loads the ES6 `Promise` shim module, `pentaho/shim/es6-promise`.
   *
   * @name pentaho.util.promise
   * @namespace
   * @amd pentaho/util/promise
   * @private
   */
  return /** @lends pentaho.util.promise */{
    /**
     * Wraps a function call within a promise.
     *
     * The function is called **synchronously**.
     * However, the returned promise is only settled asynchronously.
     *
     * The promise is fulfilled with the function's return value
     * or rejected with an error thrown by the function.
     *
     * @param {function() : *} fun The function to call.
     * @param {?object} [ctx] The object on which to call `fun`.
     *
     * @return {Promise} A promise for the function's return value.
     */
    wrapCall: function(fun, ctx) {
      if(!fun) throw error.argRequired("fun");

      return new Promise(function(resolve) {
        resolve(fun.call(ctx));
      });
    },

    // TODO: Despite useful, for now, this function is only being used
    // by visual/Wrapper - that is to be removed in a near future.
    // Let it be here for a while more, cause we think this might be used
    // for the View class.
    /**
     * Calls a function when a promise settled,
     * whether it was fulfilled or rejected,
     * and returns a new, identical promise for others to follow.
     *
     * @param {Promise} promise The promise to wait for.
     * @param {function} fun The function to call.
     * @param {?object} [ctx] The object on which to call `fun`.
     *
     * @return {Promise} An identical promise.
     */
    "finally": function(promise, fun, ctx) {
      if(!promise) throw error.argRequired("promise");
      if(!fun) throw error.argRequired("fun");

      return promise.then(function(value) {
        fun.call(ctx);
        return value;
      }, function(reason) {
        fun.call(ctx);
        return Promise.reject(reason);
      });
    },

    "return": function(value, sync) {
      return sync ? value : Promise.resolve(value);
    },

    "error": function(ex, sync) {
      if(sync) throw ex;
      return Promise.reject(ex);
    }
  };
});
