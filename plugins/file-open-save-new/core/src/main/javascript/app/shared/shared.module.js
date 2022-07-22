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
  "./directives/resize/resize.module",
  "./directives/edit.directive",
  "./directives/key.directive",
  "./directives/focus.directive",
  "./directives/scrollToFolder.directive",
  "./directives/rightClick.directive",
  "./directives/context.directive",
  "./directives/drag.directive",
  "./directives/drop.directive",
  "./directives/ngbodyclick.directive",
  "./directives/modal.directive"
], function (angular, resizeModule, editDirective, keyDirective, focusDirective, scrollToFolderDirective,
             rightClickDirective, contextDirective, dragDirective, dropDirective, bodyClickDirective, modalDirective) {
  "use strict";

  var module = {
    name: "shared"
  };

  activate();

  return module;

  /**
   * Creates angular module with dependencies.
   *
   * @private
   */
  function activate() {
    angular.module(module.name, [resizeModule.name])
        .directive(editDirective.name, editDirective.options)
        .directive(keyDirective.name, keyDirective.options)
        .directive(focusDirective.name, focusDirective.options)
        .directive(scrollToFolderDirective.name, scrollToFolderDirective.options)
        .directive(rightClickDirective.name, rightClickDirective.options)
        .directive(contextDirective.name, contextDirective.options)
        .directive(dragDirective.name, dragDirective.options)
        .directive(dropDirective.name, dropDirective.options)
        .directive(modalDirective.name, modalDirective.options)
        .directive(bodyClickDirective.name, bodyClickDirective.options)
  }
});
