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
define([
  "../Levels",
  "../../util/object"
], function(DebugLevels, O) {

  "use strict";

  /**
   * @classDesc The `Manager` class is a basic implementation of `debug.IManager`.
   *
   * @alias Manager
   * @memberOf pentaho.debug.impl
   * @class
   * @implements {pentaho.debug.IManager}
   *
   * @description Creates a debugging manager.
   * @constructor
   * @private
   */
  function pentaho_debug_impl_Manager() {

    this.__level = DebugLevels.error;
    this.__modules = {};
  }

  pentaho_debug_impl_Manager.prototype = /** @lends pentaho.debug.impl.Manager# */{

    configure: function(spec) {

      if(spec.level != null) this.setLevel(spec.level);

      O.eachOwn(spec.modules, function(level, mid) {
        this.setLevel(level, mid);
      }, this);
    },

    getLevel: function(module) {

      if(module == null) return this.__level;

      var level = O.getOwn(this.__modules, getModuleId(module));
      return level == null ? this.__level : level;
    },

    setLevel: function(level, module) {

      var l = DebugLevels.parse(level);
      if(module == null) {
        this.__level = l;
      } else {
        this.__modules[getModuleId(module)] = l;
      }
    },

    testLevel: function(level, module) {

      return this.getLevel(module) >= DebugLevels.parse(level);
    }
  };

  return pentaho_debug_impl_Manager;

  function getModuleId(module) {

    return typeof module === "string" ? module : module.id;
  }
});
