/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

/**
 * The Get Fields Main Module.
 *
 * The main module used for supporting the json input get fields functionality.
 **/
define([
  "angular",
  "./app.component",
  "./components/error/error.component",
  "./components/message/message.component",
  "./components/tree/tree.component",
  "pentaho/di/ui/core/components/search/search.component",
  "./services/data.service",
  "./filters/output.filter",
  "./directives/resize.directive",
  './app.config'
], function(angular, appComponent, errorComponent, messageComponent, treeComponent, searchComponent, dataService, outputFilter, resizeDirective, appConfig) {
  "use strict";

  var module = {
    name: "get-fields",
    bootstrap: bootstrap
  };

  activate();

  return module;

  /**
   * Creates angular module with dependencies.
   *
   * @private
   */
  function activate() {
    angular.module(module.name, [])
      .component(appComponent.name, appComponent.options)
      .component(errorComponent.name, errorComponent.options)
      .component(messageComponent.name, messageComponent.options)
      .component(treeComponent.name, treeComponent.options)
      .component(searchComponent.name, searchComponent.options)
      .service(dataService.name, dataService.factory)
      .filter(outputFilter.name, outputFilter.factory)
      .directive(resizeDirective.name, resizeDirective.options)
      .config(appConfig);
  }

  /**
   * Bootstraps angular module to the DOM element on the page
   * @private
   * @param {DOMElement} element - The DOM element
   */
  function bootstrap(element) {
    angular.element(element).ready(function() {
      angular.bootstrap(element, [module.name], {
        strictDi: true
      });
    });
  }
});
