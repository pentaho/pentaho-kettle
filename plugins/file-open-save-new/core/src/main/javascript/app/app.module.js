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
  "./components/addressbar/addressbar.component",
  "./components/files/files.component",
  "./components/search/search.component",
  "./components/modals/modals.component",
  "./components/selectbox/selectbox.component",
  "./components/filebar/filebar.component",
  "./components/filecontrols/filecontrols.component",
  "./services/helper.service",
  "./services/data.service",
  "./services/providers/repository.service",
  "./services/providers/vfs.service",
  "./services/providers/local.service",
  "./services/search.service",
  "./services/file.service",
  "./services/folder.service",
  "./services/provider.service",
  "./services/clipboard.service",
  "./services/message.service",
  "./services/modal.service",
  "./shared/shared.module",
  "@uirouter/angularjs"
], function (angular, fileServices, appConfig, appComponent, cardComponent, folderComponent, errorComponent,
             loadingComponent, addressbarComponent, filesComponent, searchComponent, modalsComponent, selectBoxComponent,
             filebarComponent, fileControls, helperService, dataService, repositoryService, vfsService, localService,
             searchService, fileService, folderService, servicesService, clipboardService, messageService, modalService,
             sharedModule) {
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
    var app = angular.module(module.name, [sharedModule.name, "ui.router"])
        .component(loadingComponent.name, loadingComponent.options)
        .component(appComponent.name, appComponent.options)
        .component(cardComponent.name, cardComponent.options)
        .component(folderComponent.name, folderComponent.options)
        .component(errorComponent.name, errorComponent.options)
        .component(addressbarComponent.name, addressbarComponent.options)
        .component(filesComponent.name, filesComponent.options)
        .component(searchComponent.name, searchComponent.options)
        .component(modalsComponent.name, modalsComponent.options)
        .component(selectBoxComponent.name, selectBoxComponent.options)
        .component(filebarComponent.name, filebarComponent.options)
        .component(fileControls.name, fileControls.options)
        .service(helperService.name, helperService.factory)
        .service(dataService.name, dataService.factory)
        .service(fileService.name, fileService.factory)
        .service(folderService.name, folderService.factory)
        .service(searchService.name, searchService.factory)
        .service(servicesService.name, servicesService.factory)
        .service(clipboardService.name, clipboardService.factory)
        .service(messageService.name, messageService.factory)
        .service(modalService.name, modalService.factory)
        .config(appConfig);

    fileServices.map(function(item) {
      app.service(item.name, item.factory);
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
