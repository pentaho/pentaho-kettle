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
  "./impl/ServicePlugin"
], function(ServicePlugin) {

  "use strict";

  /**
   * The `pentaho/module/subtypeOf!` module is an AMD/RequireJS loader plugin
   * that allows loading a module which provides a subtype of a specified base type.
   *
   * **AMD Plugin Usage**: `"pentaho/module/instanceOf!{baseTypeId}"`
   *
   * 1. `{baseTypeId}` — The identifier or alias of the base type of the desired subtype.
   *
   * **Example**
   *
   * The following AMD/RequireJS configuration registers two subtypes,
   * `mine/HomeScreen` and `yours/ProHomeScreen`,
   * of the abstract type `IHomeScreen`:
   *
   * ```js
   * require.config({
   *   config: {
   *     "pentaho/modules": {
   *       "IHomeScreen":         {base: null, isVirtual: true},
   *       "mine/HomeScreen":     {base: "IHomeScreen"},
   *       "yours/ProHomeScreen": {base: "IHomeScreen", ranking: 2}
   *     }
   *   }
   * });
   * ```
   *
   * Later, some other component can request for a registered subtype of the type `IHomeScreen`:
   *
   * ```js
   * define(["pentaho/module/subtypeOf!IHomeScreen"], function(HomeScreen) {
   *
   * });
   * ```
   *
   * The highest ranking subtype is chosen.
   *
   * @name subtypeOf
   * @memberOf pentaho.module
   * @type {IAmdLoaderPlugin}
   * @amd pentaho/module/subtypeOf
   *
   * @see pentaho.module.service
   * @see pentaho.module.IService#getSubtypeOfAsync
   */

  return new ServicePlugin(function(moduleService, moduleId) {
    return moduleService.getSubtypeOfAsync(moduleId);
  });
});
