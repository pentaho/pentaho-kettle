/*!
 * Copyright 2018 Hitachi Vantara. All rights reserved.
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
