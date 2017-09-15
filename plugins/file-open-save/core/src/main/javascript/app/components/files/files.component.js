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
  "../utils",
  "text!./files.html",
  "pentaho/i18n-osgi!file-open-save.messages",
  "css!./files.css"
], function(dataService, utils, filesTemplate, i18n) {
  "use strict";

  var options = {
    bindings: {
      folder: "<",
      search: "<",
      onClick: "&",
      onSelect: "&",
      onError: "&",
      onRename: "&",
      onEditStart: "&",
      onEditComplete: "&",
      wrapper: "<"
    },
    template: filesTemplate,
    controllerAs: "vm",
    controller: filesController
  };

  filesController.$inject = [dataService.name, "$timeout"];

  /**
   * The Files Controller.
   *
   * This provides the controller for the files component.
   *
   * @param {Object} dt - Angular service that contains helper functions for the files component controller
   * @param {Function} $timeout - Angular wrapper for window.setTimeout.
   */
  function filesController(dt, $timeout) {
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
    vm.compareFiles = compareFiles;
    vm.onStart = onStart;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.wrapperClass = vm.wrapper;
      vm.nameHeader = i18n.get("file-open-save-plugin.files.name.header");
      vm.typeHeader = i18n.get("file-open-save-plugin.files.type.header");
      vm.modifiedHeader = i18n.get("file-open-save-plugin.files.modified.header");
      _setSort(0, false, "name");
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
        $timeout(function() {
          vm.selectedFile = null;
          _setSort(0, false, "name");
        }, 200);
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
      if (vm.selectedFile !== file) {
        vm.selectedFile = file;
        vm.onSelect({selectedFile: file});
      }
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
      if (vm.search.length > 0) {
        resolveChildren(elements, files);
      } else {
        files = elements;
        vm.numResults = (files ? files.length : 0);
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
          }
          if (elements[i].children.length > 0) {
            resolveChildren(elements[i].children, files);
          }
        }
      }
    }

    /**
     * Rename the selected file.
     *
     * @param {Object} file - File Object
     * @param {String} current - Current file
     * @param {String} previous - Previous file
     * @param {Function} errorCallback - Function to call error
     */
    function rename(file, current, previous, errorCallback) {
      if (current) {
        if (file.new) {
          _createFolder(file, current, previous, errorCallback);
        } else {
          _renameFile(file, current, previous, errorCallback);
        }
      }
      vm.onEditComplete();
    }

    /**
     * Create a new folder
     *
     * @param {Object} file - File Object
     * @param {String} current - Current file
     * @param {String} previous - Previous file
     * @param {Function} errorCallback - Function to call error
     * @private
     */
    function _createFolder(file, current, previous, errorCallback) {
      var newName = current;
      if (_hasDuplicate(current, file)) {
        file.newName = current;
        errorCallback();
        _doError(file.type === "folder" ? 2 : 7);
        newName = previous;
      }
      file.new = false;
      dt.create(file.parent, newName).then(function(response) {
        var index = file.path.lastIndexOf("/");
        var oldPath = file.path;
        var newPath = file.path.substr(0, index) + "/" + newName;
        vm.onRename({oldPath: oldPath, newPath: newPath, newName: newName});
        file.objectId = response.data.objectId;
        file.parent = response.data.parent;
        file.path = response.data.path;
        file.name = newName;
      }, function() {
        _doError(4, file);
      });
    }

    /**
     * Rename an existing file/folder
     *
     * @param {Object} file - File Object
     * @param {String} current - Current file
     * @param {String} previous - Previous file
     * @param {Function} errorCallback - Function to call error
     * @private
     */
    function _renameFile(file, current, previous, errorCallback) {
      dt.rename(file.objectId.id, file.path, current, file.type, file.name).then(function(response) {
        file.name = current;
        file.objectId = response.data;
        if (file.type === "folder") {
          var index = file.path.lastIndexOf("/");
          var oldPath = file.path;
          var newPath = file.path.substr(0, index) + "/" + current;
          vm.onRename({oldPath: oldPath, newPath: newPath, newName: current});
        }
      }, function(response) {
        file.newName = current;
        errorCallback();
        if (response.status === 304 || response.status === 500) {
          _doError(file.type === "folder" ? 10 : 11);
        } else if (response.status === 409) {
          if (_hasDuplicate(current, file)) {
            _doError(file.type === "folder" ? 2 : 7);
          } else {
            _doError(file.type === "folder" ? 10 : 11);
          }
        } else if (response.status === 406) {
          _doError(12);
        } else {
          _doError(file.type === "folder" ? 10 : 11);
        }
      });
    }

    /**
     * Calls vm.onError using the parameter errorType
     * @param {number} errorType - the number corresponding to the appropriate error
     * @private
     */
    function _doError(errorType, file) {
      vm.onError({errorType: errorType, file: file});
    }

    /**
     * Checks for a duplicate name
     *
     * @param {String} name - file name to check if it already exists within vm.folder.children
     * @param {Object} file - File Object
     * @return {Boolean} true if it vm.folder.children already has a file named "name", false otherwise
     * @private
     */
    function _hasDuplicate(name, file) {
      for (var i = 0; i < vm.folder.children.length; i++) {
        var check = vm.folder.children[i];
        if (check !== file) {
          if (check.name.toLowerCase() === name.toLowerCase() && check.type === file.type) {
            return true;
          }
        }
      }
      return false;
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
        case 2:// Descend
          _setSort(1, false, field);
          break;
        case 1:// Ascend
          _setSort(2, true, field);
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

    /**
     * Calls selectFile for file and onEditStart()
     * @param {Object} file - File Object
     */
    function onStart(file) {
      selectFile(file);
      vm.onEditStart();
    }

    /**
     * Compare files according to sortField, keeping folders first
     * @param {Object} first - File Object
     * @param {Object} second - File Object
     * @return {Number} -1 or 1 according to comparisons of first and second names
     **/
    function compareFiles(first, second) {
      var obj1 = first.value;
      var obj2 = second.value;

      var comp = foldersFirst(obj1.type, obj2.type);
      if (comp !== 0) {
        return comp;
      }
      // field compare
      switch (vm.sortField) {
        case "name":
          comp = utils.naturalCompare(obj1.name, obj2.name);
          break;
        default:
          var val1 = obj1[vm.sortField];
          var val2 = obj2[vm.sortField];
          comp = (val1 < val2) ? -1 : (val1 > val2) ? 1 : 0;
          if (comp === 0) {
            comp = utils.naturalCompare(obj1.name, obj2.name);
          }
      }

      if (comp !== 0) {
        return comp;
      }
      // keep order if equal
      return first.index < second.index ? -1 : 1;
    }

    /**
     * Tests 2 strings to determine if they are different or if they equal "folder"
     * @param {String} type1 - String object
     * @param {String} type2 - String object
     * @return {number} - a number according to the values of type1 and type2
     */
    function foldersFirst(type1, type2) {
      if (type1 !== type2) {
        return (type1 === "folder") ? -1 : (type2 === "folder") ? 1 : 0;
      }
      return 0;
    }
  }

  return {
    name: "files",
    options: options
  };
});
