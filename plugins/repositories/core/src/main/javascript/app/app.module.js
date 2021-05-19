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
  * The Repository Manager Main Module
  *
  * The main module for supporting the repository manager
  **/
 define([
   "angular",
   "./app.config",
   "./app.animation",
   "./components/pentaho/pentaho.module",
   "./components/database/database.module",
   "./components/file/file.module",
   "./components/manager/manager.component",
   "./components/manager/manager.service",
   "./components/add/add.component",
   "./components/shared/connect/connect.component",
   "./components/shared/connect/connect.service",
   "./components/shared/connecting/connecting.component",
   "./components/other/other.component",
   "./components/other/other.service",
   "./components/shared/loading/loading.component",
   "./components/shared/failure/failure.component",
   "./components/shared/success/success.component",
   "./components/shared/error/error.component",
   "./components/shared/focus/focus.directive",
   "./components/shared/help/help.component",
   "./components/shared/service/helper.service",
   "@uirouter/angularjs",
   "angular-animate"
 ], function(angular, appConfig, appAnimation, pentahoModule, databaseModule, fileModule, managerComponent, managerService, addComponent, connectComponent, connectService, connectingComponent, otherComponent, otherService, loadingComponent, failureComponent, successComponent, errorComponent, focusDirective, helpComponent, helperService) {
   'use strict';

   var module = {
     name: "repository-manager",
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
     angular.module(module.name, [pentahoModule.name, databaseModule.name, fileModule.name, "ui.router", "ngAnimate"])
         .component(managerComponent.name, managerComponent.options)
         .component(addComponent.name, addComponent.options)
         .component(connectComponent.name, connectComponent.options)
         .component(connectingComponent.name, connectingComponent.options)
         .component(otherComponent.name, otherComponent.options)
         .component(loadingComponent.name, loadingComponent.options)
         .component(failureComponent.name, failureComponent.options)
         .component(successComponent.name, successComponent.options)
         .component(errorComponent.name, errorComponent.options)
         .component(helpComponent.name, helpComponent.options)
         .service(managerService.name, managerService.factory)
         .service(otherService.name, otherService.factory)
         .service(connectService.name, connectService.factory)
         .service(helperService.name, helperService.factory)
         .directive(focusDirective.name, focusDirective.options)
         .animation(appAnimation.class, appAnimation.factory)
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
