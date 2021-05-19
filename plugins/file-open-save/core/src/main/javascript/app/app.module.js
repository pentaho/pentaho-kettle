/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2017-2020 Hitachi Vantara. All rights reserved.
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
 * The File Open and Save Main Module.
 *
 * The main module used for supporting the file open and save functionality.
 **/
define([
  "angular",
  "./app.config",
  "./app.component",
  "./components/card/card.component",
  "./components/folder/folder.component",
  "./components/error/error.component",
  "./components/loading/loading.component",
  "./components/breadcrumb/breadcrumb.component",
  "./components/files/files.component",
  "./components/search/search.component",
  "./shared/directives/edit.directive",
  "./shared/directives/key.directive",
  "./shared/directives/focus.directive",
  "./shared/directives/scrollToFolder.directive",
  "./components/breadcrumb/breadcrumb.directive",
  "./services/data.service",
  "./shared/directives/resize/resize.module",
  "@uirouter/angularjs"
], function(angular, appConfig, appComponent, cardComponent, folderComponent, errorComponent,
            loadingComponent, breadcrumbComponent, filesComponent, searchComponent, editDirective, keyDirective,
            focusDirective, scrollToFolderDirective, breadcrumbDirective, dataService, resizeModule) {
  "use strict";

  var module = {
    name: "file-open-save",
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
    angular.module(module.name, [resizeModule.name, "ui.router"])
      .component(loadingComponent.name, loadingComponent.options)
      .component(appComponent.name, appComponent.options)
      .component(cardComponent.name, cardComponent.options)
      .component(folderComponent.name, folderComponent.options)
      .component(errorComponent.name, errorComponent.options)
      .component(breadcrumbComponent.name, breadcrumbComponent.options)
      .component(filesComponent.name, filesComponent.options)
      .component(searchComponent.name, searchComponent.options)
      .directive(editDirective.name, editDirective.options)
      .directive(keyDirective.name, keyDirective.options)
      .directive(focusDirective.name, focusDirective.options)
      .directive(breadcrumbDirective.name, breadcrumbDirective.options)
      .directive(scrollToFolderDirective.name, scrollToFolderDirective.options)
      .service(dataService.name, dataService.factory)
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
