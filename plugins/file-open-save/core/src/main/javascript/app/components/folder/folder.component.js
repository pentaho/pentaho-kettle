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
 * The File Open and Save Folder component.
 *
 * This provides the component for the Folders in the directory tree.
 * @module components/folder/folder.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.

 **/
define([
  "text!./folder.html",
  "../utils",
  "css!./folder.css"
], function(folderTemplate, utils) {
  "use strict";

  var options = {
    bindings: {
      folders: "<",
      onSelect: "&",
      onOpen: "&",
      showRecents: "<",
      selectedFolder: "<",
      autoExpand: "<"
    },
    template: folderTemplate,
    controllerAs: "vm",
    controller: folderController
  };

  folderController.$inject = ["$timeout"];

  /**
   * The Folder Controller.
   *
   * This provides the controller for the folder component.
   * @param {Function} $timeout - Angular wrapper for window.setTimeout.
   */
  function folderController($timeout) {
    var _iconsWidth = 58;
    var _paddingLeft = 27;
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;
    vm.openFolder = openFolder;
    vm.selectFolder = selectFolder;
    vm.selectAndOpenFolder = selectAndOpenFolder;
    vm.compareFolders = compareFolders;
    vm.maxWidth = 0;
    vm.width = 0;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
    }

    /**
     * Called whenever one-way bindings are updated.
     *
     * @param {Object} changes - hash whose keys are the names of the bound properties
     * that have changed, and the values are an object of the form
     */
    function onChanges(changes) {
      if (changes.selectedFolder) {
        var selectedFolder = changes.selectedFolder.currentValue;
        if (selectedFolder && selectedFolder.path) {
          if (selectedFolder.path !== "Recents") {
            _selectFolderByPath(selectedFolder.path);
          }
        }
      }
    }

    /**
     * Opens the folder to display and children folders in directory tree.
     * Also, sets maxDepth variable of open folders.
     *
     * @param {Object} folder - folder object
     */
    function openFolder(folder) {
      vm.maxWidth = 0;
      if (folder.hasChildren) {
        folder.open = folder.open !== true;
      }
      for (var i = 0; i < vm.folders.length; i++) {
        if (folder.open === true && vm.folders[i].depth === folder.depth + 1 && _isChild(folder, vm.folders[i])) {
          vm.folders[i].visible = true;
          vm.folders[i].indent = vm.folders[i].depth * _paddingLeft;
        } else if (folder.open === false && vm.folders[i].depth > folder.depth && _isChild(folder, vm.folders[i])) {
          vm.folders[i].visible = false;
          vm.folders[i].open = false;
        }
        if (vm.folders[i].visible) {
          vm.maxWidth = Math.max(vm.maxWidth, utils.getTextWidth(vm.folders[i].name) +
            (vm.folders[i].depth * _paddingLeft) + _iconsWidth);
        }
      }
      _setWidth();
    }

    /**
     * Selects folder
     *
     * @param {Object} folder - folder object
     */
    function selectFolder(folder) {
      vm.showRecents = folder === null;
      vm.selectedFolder = folder;
      vm.onSelect({selectedFolder: folder});
    }

    /**
     * Call functions above to select and open folder
     *
     * @param {Object} folder - folder object
     */
    function selectAndOpenFolder(folder) {
      selectFolder(folder);
      openFolder(folder);
    }

    /**
     * Determines if child is a child of folder
     *
     * @param {Object} folder - folder object
     * @param {Object} child - child object
     * @return {boolean} - true if child, false otherwise
     * @private
     */
    function _isChild(folder, child) {
      var childPath = child.path;
      var depthDiff = child.depth - folder.depth;
      for (var i = 0; i < depthDiff; i++) {
        childPath = childPath.slice(0, childPath.lastIndexOf("/"));
      }
      return childPath === folder.path || childPath === "";
    }

    /**
     * Selects a folder by path
     * @param {String} path - Path to folder
     * @private
     */
    function _selectFolderByPath(path) {
      vm.maxWidth = 0;
      for (var i = 0; i < vm.folders.length; i++) {
        vm.folders[i].indent = vm.folders[i].depth * _paddingLeft;
        if (vm.folders[i].path === path) {
          selectFolder(vm.folders[i]);
          if (vm.autoExpand) {
            _openParentFolder(vm.folders[i].parent);
            vm.autoExpand = false;
          }
        }
        if (vm.folders[i].visible) {
          var width = utils.getTextWidth(vm.folders[i].name);
          vm.maxWidth = Math.max(vm.maxWidth, width + (vm.folders[i].depth * _paddingLeft) + _iconsWidth);
        }
      }
      _setWidth();
    }

    /**
     * Opens parent folder of path
     *
     * @param {String} path - Path to a folder
     * @private
     */
    function _openParentFolder(path) {
      if (path) {
        for (var i = 0; i < vm.folders.length; i++) {
          if (path.indexOf(vm.folders[i].path) === 0) {
            _openParentFolders(vm.folders[i]);
          }
        }
      }
    }

    /**
     * Opens parent folders of folder
     *
     * @param {Object} folder - Folder Object
     * @private
     */
    function _openParentFolders(folder) {
      if (folder.hasChildren) {
        folder.open = true;
      }
      for (var i = 0; i < vm.folders.length; i++) {
        if (vm.folders[i].parent === folder.path) {
          vm.folders[i].visible = true;
          var width = utils.getTextWidth(vm.folders[i].name);
          vm.maxWidth = Math.max(vm.maxWidth, width + (vm.folders[i].depth * _paddingLeft) + _iconsWidth);
        }
      }
    }

    /**
     * Sets vm.width for scrolling purposes.
     * @private
     */
    function _setWidth() {
      $timeout(function() {
        var tmpClientWidth = document.getElementById("directoryTreeArea").clientWidth;
        vm.width = vm.maxWidth > tmpClientWidth ? vm.maxWidth : tmpClientWidth;
      }, 0);
    }

    /**
     * Compare folders according to sortField
     * @param {Object} first - Folder Object
     * @param {Object} second - Folder Object
     * @return {Number} -1 or 1 according to comparisons of first and second names
     **/
    function compareFolders(first, second) {
      var folder1 = first.value;
      var folder2 = second.value;
      var path1 = folder1.path.split("/");
      var path2 = folder2.path.split("/");
      var comp = 0;
      var len = Math.min(path1.length, path2.length);
      for (var i = 0; i < len; i++) {
        comp = utils.naturalCompare(path1[i], path2[i]);
        if (comp !== 0) {
          return comp;
        }
      }
      if (path1.length !== path2.length) {
        return path1.length - path2.length;
      }
      return first.index - second.index;
    }
  }

  return {
    name: "folder",
    options: options
  };
});
