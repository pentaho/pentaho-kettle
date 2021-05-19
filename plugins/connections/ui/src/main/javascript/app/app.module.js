/*!
 * Copyright 2019-2020 Hitachi Vantara. All rights reserved.
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
 * The Connections Main Module.
 *
 * The main module used for supporting the connections functionality.
 **/
define([
  "angular",
  "pentaho/module/instancesOf!IPenConnectionProvider",
  "./app.config",
  "./app.animation",
  "./components/intro/intro.component",
  "./components/summary/summary.component",
  "./components/creating/creating.component",
  "./components/success/success.component",
  "./components/failure/failure.component",
  "./components/selectbox/selectbox.component",
  "./components/controls/controls.component",
  "./components/message/message.component",
  "./components/help/help.component",
  "./directives/focus.directive",
  "./directives/ngbodyclick.directive",
  "./directives/showpassword.directive",
  "./service/helper.service",
  "./service/data.service",
  "@uirouter/angularjs",
  "angular-animate"
], function (angular, plugins, appConfig, appAnimation, introComponent, summaryComponent, creatingComponent,
             successComponent, failureComponent, selectboxComponent, controlsComponent, messageComponent, helpComponent,
             focusDirective, bodyClickDirective, showPasswordDirective, helperService, dataService) {
  "use strict";

  var module = {
    name: "connections",
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

    var deps = ['ui.router', 'ngAnimate'];
    var types = [];
    var summaries = [];
    plugins.map(function (item) {
      deps.push(item.name);
      types.push({
        value: item.scheme,
        label: item.label
      });
      summaries[item.scheme] = item.summary;
    });

    function vfsTypeProvider() {
      function getTypes() {
        return types;
      }

      return {
        $get: getTypes
      }
    }

    function vfsSummaryProvider() {
      function getSummaries() {
        return summaries;
      }

      return {
        $get: getSummaries
      }
    }

    angular.module(module.name, deps)
        .component(introComponent.name, introComponent.options)
        .component(summaryComponent.name, summaryComponent.options)
        .component(creatingComponent.name, creatingComponent.options)
        .component(successComponent.name, successComponent.options)
        .component(failureComponent.name, failureComponent.options)
        .component(selectboxComponent.name, selectboxComponent.options)
        .component(controlsComponent.name, controlsComponent.options)
        .component(messageComponent.name, messageComponent.options)
        .component(helpComponent.name, helpComponent.options)
        .directive(focusDirective.name, focusDirective.options)
        .directive(bodyClickDirective.name, bodyClickDirective.options)
        .directive(showPasswordDirective.name, showPasswordDirective.options)
        .service(helperService.name, helperService.factory)
        .service(dataService.name, dataService.factory)
        .animation(appAnimation.class, appAnimation.factory)
        .provider('vfsTypes', vfsTypeProvider)
        .provider('vfsSummaries', vfsSummaryProvider)
        .config(appConfig);
  }

  /**
   * Bootstraps angular module to the DOM element on the page
   * @private
   * @param {DOMElement} element - The DOM element
   */
  function bootstrap(element) {
    angular.element(element).ready(function () {
      angular.bootstrap(element, [module.name], {
        strictDi: true
      });
    });
  }
});
