/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
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
  "require",
  "../shim/es6-promise"
], function(localRequire) {

  /* globals require */

  var context = getContext();

  // Cannot use arg.slice, or a cycle would occur.
  var A_slice = Array.prototype.slice;

  /**
   * The `util.requireJS` namespace contains utilities related with Require JS.
   *
   * @namespace pentaho.util.requireJS
   * @private
   */
  var requireJS = /** @lends pentaho.util.requireJS */{
    /**
     * Defines a module in the current RequireJS context.
     *
     * @param {string} id The module id.
     * @param {string[]} [deps] The ids of dependencies.
     * @param {function|*} callback The module definition function or the module's value.
     *
     * @return {pentaho.util.require} This utility.
     */
    define: function(id, deps, callback) {

      if(!id || typeof id !== "string") {
        // Anonymous modules not supported.
        // Cannot use ArgumentRequiredError, or a cycle would occur, as it is based in Base.js and
        // the latter uses this.
        throw new Error("Argument 'id' is required.");
      }

      // This module may not have dependencies
      if(!Array.isArray(deps)) {
        callback = deps;
        deps = null;
      }

      if(!deps && typeof callback === "function") {
        deps = [];
      }

      // Publish module in this context's queue.
      // A require call will process these first.
      context.defQueue.push([id, deps, callback]);
      context.defQueueMap[id] = true;

      return requireJS;
    },

    /**
     * Gets a promise for one or more modules, given their ids.
     *
     * When `deps` is a `string`, a single module is required,
     * and the promise is fulfilled directly with the value of that module.
     *
     * When `deps` is an `Array`, zero or more modules are required,
     * and the promise is fulfilled with an array with the values of those modules.
     *
     * Optionally, receives a contextual `require` function,
     * so that module ids are taken relative to its module's folder.
     *
     * @param {string|Array.<string>} deps A single module id or an array of module ids.
     * @param {?function} [relativeRequire] A contextual require function.
     * Defaults to the global `require` function of the current Require JS context.
     *
     * @return {Promise|Promise.<Array>} A promise that
     * is fulfilled with the value of the requested module(s), and
     * is rejected in case a module loader error occurs (like an undefined module or timeout).
     */
    promise: function(deps, relativeRequire) {
      if(deps == null) {
        // Cannot use ArgumentRequiredError, or a cycle would occur, as it is based in Base.js and
        // the latter uses this.
        throw new Error("Argument 'deps' is required.");
      }

      var requireFun = relativeRequire || context.require;

      if(Array.isArray(deps)) {
        return new Promise(function(resolve, reject) {
          requireFun(deps, function() {
            resolve(A_slice.call(arguments));
          }, reject);
        });
      }

      return new Promise(function(resolve, reject) {
        requireFun([deps], resolve, reject);
      });
    },

    /**
     * Gets or sets the configuration of the current Require JS context.
     *
     * When invoked with one (or more) arguments, sets the current configuration.
     * Otherwise, gets the current configuration.
     *
     * @param {object} [config] - The configuration.
     *
     * @return {object|pentaho.util.require} When setting, returns this utility;
     * when getting, returns the configuration.
     */
    config: function(config) {
      if(arguments.length > 0) {
        context.configure(config);
        return requireJS;
      }

      return context.config;
    }
  };

  return requireJS;

  function getContext() {
    return require.s.contexts[localRequire.contextName || "_"];
  }
});
