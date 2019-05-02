/*!
 * Copyright 2019 Hitachi Vantara. All rights reserved.
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
 * The File Open and Save Main Module.
 *
 * The main module used for supporting the file open and save functionality.
 **/
define([
  "angular",
  "pentaho/module/instancesOf!IPenFileService",
  "./app.config",
  "./app.component",
  "./components/card/card.component",
  "./components/folder/folder.component",
  "./components/error/error.component",
  "./components/loading/loading.component",
  "./components/breadcrumb/breadcrumb.component",
  "./components/files/files.component",
  "./components/search/search.component",
  "./components/modals/modals.component",
  "./components/selectbox/selectbox.component",
  "./shared/directives/edit.directive",
  "./shared/directives/key.directive",
  "./shared/directives/focus.directive",
  "./shared/directives/scrollToFolder.directive",
  "./shared/directives/rightClick.directive",
  "./shared/directives/context.directive",
  "./shared/directives/drag.directive",
  "./shared/directives/drop.directive",
  "./shared/directives/ngbodyclick.directive",
  "./components/breadcrumb/breadcrumb.directive",
  "./services/helper.service",
  "./services/data.service",
  "./services/repository.service",
  "./services/vfs.service",
  "./services/local.service",
  "./services/search.service",
  "./services/file.service",
  "./services/folder.service",
  "./shared/directives/resize/resize.module",
  "./services/services.service",
  "./services/clipboard.service",
  "./shared/directives/modal.directive",
  "./services/modal.service",
  "angular-ui-router"
], function (angular, fileServices, appConfig, appComponent, cardComponent, folderComponent, errorComponent,
             loadingComponent, breadcrumbComponent, filesComponent, searchComponent, modalsComponent, selectBoxComponent, editDirective, keyDirective,
             focusDirective, scrollToFolderDirective, rightClickDirective, contextDirective, dragDirective, dropDirective, bodyClickDirective,
             breadcrumbDirective, helperService, dataService, repositoryService, vfsService, localService,
             searchService, fileService, folderService, resizeModule, servicesService, clipboardService, modalDirective, modalService) {
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
    var a = angular.module(module.name, [resizeModule.name, "ui.router"])
        .component(loadingComponent.name, loadingComponent.options)
        .component(appComponent.name, appComponent.options)
        .component(cardComponent.name, cardComponent.options)
        .component(folderComponent.name, folderComponent.options)
        .component(errorComponent.name, errorComponent.options)
        .component(breadcrumbComponent.name, breadcrumbComponent.options)
        .component(filesComponent.name, filesComponent.options)
        .component(searchComponent.name, searchComponent.options)
        .component(modalsComponent.name, modalsComponent.options)
        .component(selectBoxComponent.name, selectBoxComponent.options)
        .directive(editDirective.name, editDirective.options)
        .directive(keyDirective.name, keyDirective.options)
        .directive(focusDirective.name, focusDirective.options)
        .directive(breadcrumbDirective.name, breadcrumbDirective.options)
        .directive(scrollToFolderDirective.name, scrollToFolderDirective.options)
        .directive(rightClickDirective.name, rightClickDirective.options)
        .directive(contextDirective.name, contextDirective.options)
        .directive(dragDirective.name, dragDirective.options)
        .directive(dropDirective.name, dropDirective.options)
        .directive(modalDirective.name, modalDirective.options)
        .directive(bodyClickDirective.name, bodyClickDirective.options)
        .service(helperService.name, helperService.factory)
        .service(dataService.name, dataService.factory)
        .service(fileService.name, fileService.factory)
        .service(folderService.name, folderService.factory)
        .service(searchService.name, searchService.factory)
        .service(servicesService.name, servicesService.factory)
        .service(clipboardService.name, clipboardService.factory)
        .service(modalService.name, modalService.factory)
        .config(appConfig);

    fileServices.map(function(item) {
      a.service(item.name, item.factory);
    });
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
