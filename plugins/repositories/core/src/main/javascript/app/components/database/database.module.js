/*!
 * Copyright 2018-2020 Hitachi Vantara. All rights reserved.
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

/**
 * The Database repository type module
 *
 * The main module for supporting the repository manager
 **/
define([
  "angular",
  "./database.config",
  "./database.component",
  "./details/details.component",
  "./details/details.service",
  "./select/select.component",
  "./select/select.service",
  "./loading/loading.component",
  "./loading/loading.service",
  "./failure/failure.component",
  "./success/success.component",
  "@uirouter/angularjs"
], function(angular, appConfig, databaseComponent, detailsComponent, detailsService, selectComponent, selectService, loadingComponent, loadingService, failureComponent, successComponent) {
  'use strict';

  var module = {
    name: "database"
  };

  activate();

  return module;

  /**
   * Creates angular module with dependencies.
   *
   * @private
   */
  function activate() {
    angular.module(module.name, ["ui.router"])
        .component(databaseComponent.name, databaseComponent.options)
        .component(detailsComponent.name, detailsComponent.options)
        .component(selectComponent.name, selectComponent.options)
        .component(loadingComponent.name, loadingComponent.options)
        .component(failureComponent.name, failureComponent.options)
        .component(successComponent.name, successComponent.options)
        .service(selectService.name, selectService.factory)
        .service(loadingService.name, loadingService.factory)
        .service(detailsService.name, detailsService.factory)
        .config(appConfig);
  }
});
