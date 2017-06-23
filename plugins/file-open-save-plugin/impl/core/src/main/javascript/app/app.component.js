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
 * The File Open and Save Main App component.
 *
 * This provides the main component for supporting the file open and save functionality.
 * @module app.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "./services/data.service",
  "text!./app.html",
  "pentaho/i18n-osgi!file-open-save.messages",
  "angular",
  "css!./app.css",
  "css!bootstrap/dist/css/bootstrap.css"
], function(dataService, template, i18n, angular) {
  "use strict";

  var options = {
    bindings: {},
    template: template,
    controllerAs: "vm",
    controller: appController
  };

  appController.$inject = [dataService.name, "$location"];

  /**
   * The App Controller.
   *
   * This provides the controller for the app component.
   *
   * @param {Object} dt - Angular service that contains helper functions for the app component controller
   * @param {Function} $location - Angular service used for parsing the URL in browser address bar
   */
  function appController(dt, $location) {
    var vm = this;
    vm.$onInit = onInit;
    vm.selectFolder = selectFolder;
    vm.selectFile = selectFile;
    vm.selectFolderByPath = selectFolderByPath;
    vm.doSearch = doSearch;
    vm.resetSearch = resetSearch;
    vm.addFolder = addFolder;
    vm.openOrSave = openOrSave;
    vm.cancel = cancel;
    vm.highlightFile = highlightFile;
    vm.remove = remove;
    vm.setState = setState;
    vm.confirmError = confirmError;
    vm.cancelError = cancelError;
    vm.storeRecentSearch = storeRecentSearch;
    vm.selectedFolder = "";
    vm.fileToSave = "";
    vm.searchString = "";
    vm.showError = false;
    vm.errorType = 0;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.wrapperClass = "save";
      vm.headerTitle = i18n.get("file-open-save-plugin.app.header.save.title");
      vm.searchPlaceholder = i18n.get("file-open-save-plugin.app.header.search.placeholder");
      vm.selectedFolder = i18n.get("file-open-save-plugin.app.header.save.title");
      vm.confirmButton = i18n.get("file-open-save-plugin.app.save.button");
      vm.cancelButton = i18n.get("file-open-save-plugin.app.cancel.button");
      vm.saveFileNameLabel = i18n.get("file-open-save-plugin.app.save.file-name.label");
      vm.noRecentsMsg = i18n.get("file-open-save-plugin.app.middle.no-recents.message");
      vm.isInSearch = false;
      vm.showRecents = true;
      vm.folder = {name: "Recents", path: "Recents"};
      vm.file = null;
      vm.includeRoot = false;
      vm.autoExpand = false;
      dt.getDirectoryTree().then(_populateTree);
      dt.getRecentFiles().then(_populateRecentFiles);
      dt.getRecentSearches().then(_populateRecentSearches);

      /**
       * Sets the folder directory tree
       *
       * @param {Object} response - $http response from call to the data service
       * @private
       */
      function _populateTree(response) {
        vm.folders = response.data;
        for (var i = 0; i < vm.folders.length; i++) {
          if (vm.folders[i].depth === 0) {
            vm.folders[i].visible = true;
          }
        }
        var path = $location.search().path;
        if (path) {
          selectFolderByPath(path);
          vm.autoExpand = true;
        }
        if (vm.folders[0].path === "/") {
          vm.includeRoot = true;
        }
      }

      /**
       * Sets the recents folders
       *
       * @param {Object} response - $http response from call to the data service
       * @private
       */
      function _populateRecentFiles(response) {
        vm.recentFiles = response.data;
      }

      var state = $location.search().state;
      if (state) {
        vm.setState(state);
      }
    }

    /**
     * Sets the wrapper class, title, and open/save button according to open or save option
     *
     * @param {String} state - "open" or "save" state
     */
    function setState(state) {
      if (state === "open") {
        vm.wrapperClass = "open";
        vm.headerTitle = i18n.get("file-open-save-plugin.app.header.open.title");
        vm.confirmButton = i18n.get("file-open-save-plugin.app.open.button");
      }
      if (state === "save") {
        vm.wrapperClass = "save";
        vm.headerTitle = i18n.get("file-open-save-plugin.app.header.save.title");
        vm.confirmButton = i18n.get("file-open-save-plugin.app.save.button");
      }
    }

    /**
     * Sets variables showRecents, folder, and selectedFolder according to the contents of parameter
     *
     * @param {Object} folder - folder object
     */
    function selectFolder(folder) {
      vm.file = null;
      if (folder) {
        vm.showRecents = false;
        vm.folder = folder;
        vm.selectedFolder = folder.name;
      } else {
        vm.showRecents = true;
        vm.folder = {name: "Recents", path: "Recents"};
        vm.selectedFolder = "Recents";
      }
    }

    /**
     * Selects a folder according to the path parameter
     *
     * @param {String} path - path to file
     */
    function selectFolderByPath(path) {
      for (var i = 0; i < vm.folders.length; i++) {
        if (vm.folders[i].path === path) {
          selectFolder(vm.folders[i]);
        }
      }
    }

    /**
     * Calls function to open folder if type of file is a folder. Else it opens either
     * the recent file or other file and closes the browser.
     *
     * @param {Object} file - file object
     */
    function selectFile(file) {
      if (file.type === "File folder") {
        selectFolder(file);
      } else {
        if (file.repository) {
          dt.openRecent(file.repository, file.objectId.id);
        } else {
          dt.openFile(file.objectId.id, file.type);
        }
        close();
      }
    }

    /**
     * Calls a filter for either recent files or files/folders in current folder
     */
    function doSearch() {
      if (vm.showRecents === true) {
        _filter(vm.recentFiles, vm.searchString);
      } else {
        _filter(vm.folder.children, vm.searchString);
      }
    }

    /**
     * Resets the search string and runs search against that string (which returns normal dir structure).
     */
    function resetSearch() {
      vm.searchString = '';
      vm.doSearch();
    }

    /**
     * Recursively searches for the value in elements and any of its children
     *
     * @param {Object} elements - Object with files and folders
     * @param {String} value - String used to search within elements
     * @private
     */
    function _filter(elements, value) {
      if (elements) {
        for (var i = 0; i < elements.length; i++) {
          var name = elements[i].name.toLowerCase();
          elements[i].inResult = name.indexOf(value.toLowerCase()) !== -1;
          if (elements[i].children.length > 0) {
            _filter(elements[i].children, value);
          }
        }
      }
    }

    /**
     * Sets the selected file to the file parameter to highlight it in UI.
     * Also, sets the file to save text according to the selected file.
     *
     * @param {Object} file - file object
     */
    function highlightFile(file) {
      vm.file = file;
      vm.fileToSave = file.name;
    }

    /**
     * Called if user clicks "Open" or "Save" in UI. OR if user double clicks a folder or file.
     * If file type is a folder, calls function to select that folder.
     * If file type is file, calls either open or save function and closes browser if there is no error.
     */
    function openOrSave() {
      if (vm.file) {
        if (vm.file.type === "File folder") {
          selectFolder(vm.file);
        } else {
          if (vm.wrapperClass === "open") {
            _open();
          } else {
            _save();
          }
          if (!vm.showError) {
            close();
          }
        }
      }
    }

    /**
     * Calls data service to open file
     * @private
     */
    function _open() {
      dt.openFile(vm.file.objectId.id, vm.file.type);
    }

    /**
     * Calls data service to save file if there is no error
     * @private
     */
    function _save() {
      if (vm.fileToSave === vm.file.name) {
        // show Error
        vm.showError = true;
        vm.errorType = 1;
      }
    }

    /**
     * Called if user clicks cancel in either open or save to close the browser
     */
    function cancel() {
      close();
    }

    /**
     * Called if user clicks the confirmation button on an error dialog to handle the
     * error and cancel it
     */
    function confirmError() {
      // handle error
      cancelError();
    }

    /**
     * Resets the error variables as if no error is present
     */
    function cancelError() {
      vm.errorType = 0;
      vm.showError = false;
    }

    /**
     * Stores the most recent search
     */
    function storeRecentSearch() {
      vm.isInSearch = false;
      if(vm.searchString !== "") {
        dt.storeRecentSearch(vm.searchString).then(_populateRecentSearches);
      }
    }

    /**
     * Populates the most recent searches
     */
    function _populateRecentSearches(response) {
      vm.recentSearches = response.data;
    }

    /**
     * Called if user selects to delete the selected folder or file.
     */
    function remove() {
      if (vm.file !== null) {
        dt.remove(vm.file.path, vm.file.type).then(function() {
          var index = vm.folder.children.indexOf(vm.file);
          vm.folder.children.splice(index, 1);
          if (vm.file.type === "File folder") {
            for (var i = 0; i < vm.folders.length; i++) {
              if (vm.folders[i].path === vm.file.path) {
                vm.folders.splice(i, 1);
                break;
              }
            }
          }
          var hasChildFolders = false;
          for (var j = 0; j < vm.folder.children.length; j++) {
            if (vm.folder.children[j].type === "File folder") {
              hasChildFolders = true;
            }
          }
          vm.folder.hasChildren = hasChildFolders;
        });
      }
    }

    /**
     * Called if user selects to add a folder or file while Recents is not selected.
     */
    function addFolder() {
      if (vm.selectedFolder !== "Recents") {
        dt.create(vm.folder.path, _getFolderName()).then(function(response) {
          vm.folder.hasChildren = true;
          var folder = response.data;
          folder.visible = vm.folder.open;
          folder.depth = vm.folder.depth + 1;
          folder.autoEdit = true;
          folder.type = "File folder";
          vm.folder.children.splice(0, 0, folder);
          for (var i = 0; i < vm.folders.length; i++) {
            if (vm.folders[i].path === folder.parent) {
              vm.folders.splice(i + 1, 0, angular.copy(folder));
              break;
            }
          }
        });
      }
    }

    /**
     * Sets the default folder name to "New Folder" plus an incrementing integer
     * each time there is a folder called "New Folder <i+1>"
     *
     * @return {string} - Name of the folder
     * @private
     */
    function _getFolderName() {
      var name = "New Folder";
      var index = 0;
      var check = name;
      var search = true;
      while (search) {
        var found = false;
        for (var i = 0; i < vm.folder.children.length; i++) {
          if (vm.folder.children[i].name === check) {
            found = true;
            break;
          }
        }
        if (found) {
          index++;
          check = name + " " + index;
        } else {
          search = false;
        }
      }
      return check;
    }
  }

  return {
    name: "fileOpenSaveApp",
    options: options
  };
});
