/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

define([
  "../service",
  "../util",
  "pentaho/shim/es6-promise"
], function(moduleService, moduleUtil) {

  "use strict";

  /**
   * @classDesc The base class of AMD/RequireJs loader plugins that expose the
   * methods of [module.IService]{@link pentaho.module.IService}.
   *
   * @memberOf pentaho.module.impl
   * @class
   * @implements {IAmdLoaderPlugin}
   *
   * @private
   * @param {function(pentaho.module.IService, string) : Promise} callModuleService - The module service
   * caller function. Called with the module service and with the absolute plugin argument.
   */
  function ServicePlugin(callModuleService) {
    this._callModuleService = callModuleService;
  }

  ServicePlugin.prototype.load = function(name, requesterRequire, onLoad, config) {
    if(config.isBuild) {
      // Don't resolve when building.
      onLoad();
    } else {
      var requesterId = moduleUtil.getId(requesterRequire);
      var moduleId = moduleUtil.absolutizeIdRelativeToSibling(name, requesterId);

      this._callModuleService(moduleService, moduleId).then(onLoad, onLoad.error);
    }
  };

  return ServicePlugin;
});
