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
 * The filebar component.
 *
 * This provides the component for the filebar functionality.
 * @module components/filebar/filebar.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "pentaho/i18n-osgi!file-open-save-new.messages",
  "text!./filebar.html",
  "text!./supported_file_filters.json",
  "css!./filebar.css"
], function (i18n, filebarTemplate, supportedFileFilters) {
  "use strict";

  var options = {
    bindings: {
      selectedFile: "<",
      fileTypes: "<",
      defaultFilter: "<",
      path: '<',
      state: "<",
      filename: "=",
      fileType: "<",
      onSelectFilter: "&",
      onOpenClick: "&",
      onSaveClick: "&",
      onCancelClick: "&"
    },
    template: filebarTemplate,
    controllerAs: "vm",
    controller: filebarController
  };

  filebarController.$inject = ['$timeout'];

  /**
   * The filebar Controller.
   *
   * This provides the controller for the filebar component.
   */
  function filebarController($timeout) {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;
    vm.onSelect = onSelect;
    vm.onChange = onChange;
    vm.isSaveState = isSaveState;
    vm.onHelpClick = onHelpClick;

    vm.disabled = true;
    vm.fileFilters = [];

    /**
     * The $onInit} hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.saveFileNameLabel = i18n.get("file-open-save-plugin.app.save.file-name.label");
      vm.openButton = i18n.get("file-open-save-plugin.app.open.button");
      vm.cancelButton = i18n.get("file-open-save-plugin.app.cancel.button");
      vm.saveButton = i18n.get("file-open-save-plugin.app.save.button");
      vm.okButton = i18n.get("file-open-save-plugin.app.ok.button");
      vm.confirmButton = i18n.get("file-open-save-plugin.app.save.button");
      vm.helpLabel = i18n.get("file-open-save-plugin.app.help.label");
      vm.saveFileNameLabel = i18n.get("file-open-save-plugin.app.save.file-name.label");
      vm.fileFilterLabel = i18n.get("file-open-save-plugin.app.save.file-filter.label");
      vm.saveFileNameLabel = i18n.get("file-open-save-plugin.app.save.file-name.label");

      var filterSet = new Set(null);
      filterSet.add("ALL");

      for (var i = 0; i < vm.fileTypes.length; i++) {
        filterSet.add(vm.fileTypes[i]);
      }

      var parsedSupportedFileFilters = JSON.parse(supportedFileFilters);
      vm.fileFilters = parsedSupportedFileFilters.filter( function(fltr) {
        return filterSet.has(fltr.id); });

      if (vm.fileFilters.length) {
        vm.selectedFilter = vm.fileFilters[0].value;
      }

      if (vm.defaultFilter) {
        var defaultFltr = parsedSupportedFileFilters.filter( function(fltr) {
          return fltr.id === vm.defaultFilter});

        if (defaultFltr.length) {
          vm.selectedFilter = defaultFltr[0].value;
        }
      }

    }

    /**
     * Called whenever one-way bindings are updated.
     *
     * @param {Object} changes - hash whose keys are the names of the bound properties
     * that have changed, and the values are an object of the form
     */
    function onChanges(changes) {
      $timeout(function () {
        _isDisabled();
      });
    }

    function onSelect(filter) {
      vm.onSelectFilter({filter: filter});
    }

    function onChange() {
      _isDisabled();
    }

    function isSaveState() {
      return (vm.state.is('save') || vm.state.is('saveTo') || vm.state.is('saveToFileFolder'));
    }

    function onHelpClick() {
      help();
    }

    function _isDisabled() {
      var enabled = true;

      switch (vm.state.current.name) {
        case 'selectFolder':
          enabled = _isFolder(vm.selectedFile);
          break;
        case 'selectFile':
          enabled = _hasFileType(vm.selectedFile) && !_isFolder(vm.selectedFile);
          break;
        case 'selectFileFolder':
          enabled = _hasFileType(vm.selectedFile) || _isFolder(vm.selectedFile);
          break;
        case 'open':
          enabled = vm.filename;
          break;
        case 'save':
        case 'saveTo':
          vm.isSaveEnabled = vm.filename === '';
          break;
        case 'saveToFileFolder':
          vm.isSaveEnabled = !vm.path;
          break;
        default:
          break;
      }
      vm.disabled = !enabled;
    }

    function _hasFileType(file) {
      return file && file.type;
    }

    function _isFolder(file) {
      return file.type === "folder";
    }
  }

  return {
    name: "filebar",
    options: options
  };
});
