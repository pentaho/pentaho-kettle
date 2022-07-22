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
 * The File Open and Save Resize Module.
 *
 * The main module used for supporting the file open and save window resize functionality.
 **/
define([
  "angular",
  "./resizeBreadcrumb.directive",
  "./resizeFiles.directive",
  "./resizeFolders.directive",
  "./resizeCards.directive"
], function(angular, resizeBreadcrumbDirective, resizeFilesDirective,
            resizeFoldersDirective, resizeCardsDirective) {
  "use strict";

  var module = {
    name: "resize"
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
      .directive(resizeBreadcrumbDirective.name, resizeBreadcrumbDirective.options)
      .directive(resizeFilesDirective.name, resizeFilesDirective.options)
      .directive(resizeFoldersDirective.name, resizeFoldersDirective.options)
      .directive(resizeCardsDirective.name, resizeCardsDirective.options);
  }
});
