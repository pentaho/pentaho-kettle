/*!
 * Copyright 2020 Hitachi Vantara. All rights reserved.
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
 * The filecontrols component.
 *
 * This provides the component for the filecontrols functionality.
 * @module components/filecontrols/filecontrols.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "pentaho/i18n-osgi!file-open-save-new.messages",
  "text!./filecontrols.html",
  "css!./filecontrols.css"
], function (i18n, filecontrolsTemplate) {
  "use strict";

  var options = {
    bindings: {
      folder: '<',
      files: '<',
      selectedFiles: '<',
      onAddFolder: '&',
      onDeleteFiles: '&',
      onUpDirectory: '&',
      onRefreshFolder: '&'
    },
    template: filecontrolsTemplate,
    controllerAs: "vm",
    controller: filecontrolsController
  };

  filecontrolsController.$inject = ['$timeout'];

  /**
   * The filecontrols Controller.
   *
   * This provides the controller for the filecontrols component.
   */
  function filecontrolsController($timeout) {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;

    /**
     * The $onInit} hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.addFolderText = i18n.get("file-open-save-plugin.app.add-folder.button");
      vm.removeText = i18n.get("file-open-save-plugin.app.delete.button");
      vm.refreshText = i18n.get("file-open-save-plugin.app.refresh.button");
      vm.upDirectoryText = i18n.get("file-open-save-plugin.app.up-directory.button");
    }

    /**
     * Called whenever one-way bindings are updated.
     *
     * @param {Object} changes - hash whose keys are the names of the bound properties
     * that have changed, and the values are an object of the form
     */
    function onChanges(changes) {
      $timeout(function() {
        if (vm.folder) {
          _update();
        }
      });
    }

    function _update() {
      // Handle control button
      vm.addDisabled = vm.folder && !vm.folder.canAddChildren;
      vm.refreshDisabled = vm.folder && !vm.folder.loaded;
      vm.upDisabled = (vm.folder && !vm.folder.loaded) || (!vm.folder.root && vm.folder.path.replace(/^[\w]+:\/\//, "").indexOf("/") === -1);

      vm.deleteDisabled = _setDeleteDisabled();
    }

    /**
     * Helper method to determine if the delete filecontrol should be disabled.
     * @returns {boolean}
     * @private
     */
    function _setDeleteDisabled() {
      if (vm.selectedFiles && vm.selectedFiles.length > 0) {
        for (var i = 0; i < vm.selectedFiles.length; i++) {
          if (!vm.selectedFiles[i].canEdit) {
            return true;
          }
        }
      } else if (vm.folder) {
        return !vm.folder.canEdit;
      }
      return false;
    }
  }

  return {
    name: "filecontrols",
    options: options
  };
});
