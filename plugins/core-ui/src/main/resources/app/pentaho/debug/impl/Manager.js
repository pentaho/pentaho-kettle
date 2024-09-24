/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
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
