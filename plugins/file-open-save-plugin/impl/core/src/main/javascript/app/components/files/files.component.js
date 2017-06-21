/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2017 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
/**
 * The File Open and Save Files component.
 *
 * This provides the component for the Files list on search or files view.
 * @module components/files/files.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "../../services/data.service",
  "text!./files.html",
  "css!./files.css"
], function(dataService, filesTemplate) {
  "use strict";

  var options = {
    bindings: {
      folder: '<',
      search: '<',
      onClick: '&',
      onSelect: '&'
    },
    template: filesTemplate,
    controllerAs: "vm",
    controller: filesController
  };

  filesController.$inject = [dataService.name];

  /**
   * The Files Controller.
   *
   * This provides the controller for the files component.
   *
   * @param {Object} dt - Angular service that contains helper functions for the files component controller
   */
  function filesController(dt) {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;
    vm._name = "name";
    vm._type = "type";
    vm._date = "date";
    vm.selectFile = selectFile;
    vm.commitFile = commitFile;
    vm.rename = rename;
    vm.getFiles = getFiles;
    vm.sortFiles = sortFiles;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.nameHeader = "Name";
      vm.typeHeader = "Type";
      vm.lastSaveHeader = "Last saved";
      vm.hasResults = false;
      vm.noResults = "No results";// i18n.get("file-open-save-plugin.app.middle.no-results.message");
      _setSort(0, false, null);
      vm.numResults = 0;
    }

    /**
     * Called whenever one-way bindings are updated.
     *
     * @param {Object} changes - hash whose keys are the names of the bound properties
     * that have changed, and the values are an object of the form
     */
    function onChanges(changes) {
      if (changes.folder) {
        vm.selectedFile = null;
        _setSort(0, false, null);
      }
    }

    /**
     * Calls two-way binding to parent component (app) to open file if not editing.
     *
     * @param {Object} file - file object.
     */
    function commitFile(file) {
      if (file.editing !== true) {
        vm.onClick({file: file});
      }
    }

    /**
     * Calls two-way binding to parent component (app) to select file.
     *
     * @param {Object} file - file object.
     */
    function selectFile(file) {
      vm.selectedFile = file;
      vm.onSelect({selectedFile: file});
    }

    /**
     * If not search, get all the files in the elements object.
     * If it's search, get all files from search.
     *
     * @param {Array} elements - file structure
     * @return {Array} - file structure with all resulting elements
     */
    function getFiles(elements) {
      vm.numResults = 0;
      var files = [];
      vm.hasResults = false;
      if (vm.search.length > 0) {
        resolveChildren(elements, files);
      } else {
        files = elements;
        vm.numResults = (files ? files.length : 0);
        vm.hasResults = true;
      }
      return files;
    }

    /**
     * Sets the files array with files that are "inResult" from search.
     *
     * @param {Array} elements - files to check if in search results.
     * @param {Array} files - files in search results.
     */
    function resolveChildren(elements, files) {
      if (elements) {
        for (var i = 0; i < elements.length; i++) {
          files.push(elements[i]);
          if (elements[i].inResult) {
            vm.numResults++;
            vm.hasResults = true;
          }
          if (elements[i].children.length > 0) {
            resolveChildren(elements[i].children, files);
          }
        }
      }
    }

    /**
     * Rename the selected file.
     */
    function rename() {
      var path = vm.selectedFile.type === "File folder" ? vm.selectedFile.parent : vm.selectedFile.path;
      dt.rename(vm.selectedFile.objectId.id, vm.selectedFile.name, path, vm.selectedFile.type).then(function(response) {
        vm.selectedFile.objectId = response.data;
      });
    }

    /**
     * According to the state of sort, call _setSort method accordingly.
     *
     * Sort States:
     * 1. Original (no sorting)
     * 2. Ascending sort
     * 3. Descending sort
     *
     * @param {String} field - The field to sort (name, type, or date).
     */
    function sortFiles(field) {
      vm.sortState = vm.sortField === field ? vm.sortState : 0;
      switch (vm.sortState) {
        case 0:// original
          _setSort(1, false, field);
          break;
        case 1:// Ascend
          _setSort(2, true, field);
          break;
        case 2:// Descend
          _setSort(0, false, null);
          break;
        default:
          break;
      }
    }

    /**
     * @param {Object} state - Value of sortState
     * @param {Object} reverse - true or false value of reverse sorting
     * @param {String} field - field to sort
     * @private
     */
    function _setSort(state, reverse, field) {
      vm.sortState = state;
      vm.sortReverse = reverse;
      vm.sortField = field;
    }
  }

  return {
    name: "files",
    options: options
  };
});
