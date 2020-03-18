/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2017 Hitachi Vantara. All rights reserved.
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
 * The File Open and Save Folder component.
 *
 * This provides the component for the Folders in the directory tree.
 * @module components/folder/folder.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.

 **/
define([
  "../../services/data.service",
  "text!./folder.html",
  "../utils",
  "css!./folder.css"
], function(dataService, folderTemplate, utils) {
  "use strict";

  var options = {
    bindings: {
      tree: "<",
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

  folderController.$inject = [dataService.name, "$timeout", "$state"];

  /**
   * The Folder Controller.
   *
   * This provides the controller for the folder component.
   * @param {Function} $timeout - Angular wrapper for window.setTimeout.
   */
  function folderController(dt, $timeout, $state) {
    var vm = this;
    vm.$onChanges = onChanges;
    vm.openFolder = openFolder;
    vm.selectFolder = selectFolder;
    vm.selectAndOpenFolder = selectAndOpenFolder;
    vm.compareFolders = compareFolders;
    vm.getTree = getTree;
    vm.width = 0;
    vm.state = $state;

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
            if (vm.autoExpand) {
              vm.autoExpand = false;
              _openFolderTree(selectedFolder.path);
            }
            _selectFolderByPath(selectedFolder.path);
          }
        }
        _setWidth();
      }
    }

    function getTree() {
      if (vm.tree) {
        return vm.tree.includeRoot ? vm.tree.children : vm.tree.children[0].children;
      }
      return [];
    }

    /**
     * Opens the folder to display and children folders in directory tree.
     * Also, sets maxDepth variable of open folders.
     *
     * @param {Object} folder - folder object
     */
    function openFolder(folder, callback) {
      _setFolder(folder);
      if (!folder.subfoldersLoaded) {
        folder.loading = true;
        dt.getFolders(folder.path).then(function(response) {
          folder.subfoldersLoaded = true;
          var loadedFolder = response.data;
          folder.children = loadedFolder.children;
          folder.loading = false;
          if (callback) {
            callback();
          } else {
            _setWidth();
          }
        });
      } else {
        _setWidth();
      }
    }

    function _setFolder(folder) {
      vm.width = 0;
      folder.open = folder.open !== true;
      if (folder.open === false) {
        folder.loading = false;
      }
    }

    /**
     * Selects folder
     *
     * @param {Object} folder - folder object
     */
    function selectFolder(folder) {
      if (folder !== undefined) {
        vm.showRecents = folder === null;
        vm.selectedFolder = folder;
        vm.onSelect({selectedFolder: folder});
      }
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
     * Selects a folder by path
     * @param {String} path - Path to folder
     * @private
     */
    function _openFolderTree(path) {
      vm.tree.children[0].name = "/";
      path = path === "/" ? "" : path;
      var parts = path.split("/");
      parts[0] = "/";
      var index = 0;
      _findAndOpenFolder(vm.tree.children, index, parts, function() {
        // If the folder contents have loaded before the tree we want to use that object's children
        if (vm.selectedFolder && vm.selectedFolder.path === path) {
          var folder = _findFolderByPath(path);
          folder.children = vm.selectedFolder.children;
          selectFolder(folder);
        } else {
          _selectFolderByPath(path);
        }
      });
    }

    function _findAndOpenFolder(children, index, parts, callback) {
      if (parts.length === 1) {
        if (callback) {
          callback();
        }
        return;
      }
      if (children[index].name === parts[0]) {
        if (parts.length >= 1) {
          parts.shift();
          openFolder(children[index], function() {
            _findAndOpenFolder(children[index].children, 0, parts, callback);
          });
        }
      } else {
        _findAndOpenFolder(children, ++index, parts, callback);
      }
    }

    /**
     * Selects a folder by path
     * @param {String} path - Path to folder
     * @private
     */
    function _selectFolderByPath(path) {
      selectFolder(_findFolderByPath(path));
    }

    function _findFolderByPath(path) {
      vm.tree.children[0].name = "/";
      path = path === "/" ? "" : path;
      var parts = path.split("/");
      parts[0] = "/";
      return _findFolder(vm.tree.children, parts);
    }

    function _findFolder(children, parts) {
      for (var i = 0; i < children.length; i++) {
        if (children[i].name === parts[0]) {
          if (parts.length > 1) {
            parts.shift();
            return _findFolder(children[i].children, parts);
          } else {
            return children[i];
          }
        }
      }
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

    /**
     * Sets vm.width for scrolling purposes.
     * @private
     */
    function _setWidth() {
      vm.width = 0;
      $timeout(function() {
        vm.width = document.getElementById("directoryTreeArea").scrollWidth;
      }, 0);
    }
  }

  return {
    name: "folder",
    options: options
  };
});
