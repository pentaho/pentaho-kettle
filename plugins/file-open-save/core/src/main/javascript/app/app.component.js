/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2017-2018 Hitachi Vantara. All rights reserved.
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
  "./components/utils",
  "angular",
  "css!./app.css"
], function(dataService, template, i18n, utils, angular) {
  "use strict";

  var options = {
    bindings: {},
    template: template,
    controllerAs: "vm",
    controller: appController
  };

  appController.$inject = [dataService.name, "$location", "$scope", "$timeout", "$state"];

  /**
   * The App Controller.
   *
   * This provides the controller for the app component.
   *
   * @param {Object} dt - Angular service that contains helper functions for the app component controller
   * @param {Function} $location - Angular service used for parsing the URL in browser address bar
   * @param {Object} $scope - Application model
   * @param {Object} $timeout - Angular wrapper around window.setTimeout
   */
  function appController(dt, $location, $scope, $timeout, $state) {
    var vm = this;
    vm.$onInit = onInit;
    vm.selectFolder = selectFolder;
    vm.selectFile = selectFile;
    vm.selectFolderByPath = selectFolderByPath;
    vm.doSearch = doSearch;
    vm.addFolder = addFolder;
    vm.openClicked = openClicked;
    vm.saveClicked = saveClicked;
    vm.okClicked = okClicked;
    vm.cancel = cancel;
    vm.highlightFile = highlightFile;
    vm.remove = remove;
    vm.confirmError = confirmError;
    vm.cancelError = cancelError;
    vm.storeRecentSearch = storeRecentSearch;
    vm.updateDirectories = updateDirectories;
    vm.renameError = renameError;
    vm.recentsHasScrollBar = recentsHasScrollBar;
    vm.addDisabled = addDisabled;
    vm.deleteDisabled = deleteDisabled;
    vm.onKeyUp = onKeyUp;
    vm.getPlaceholder = getPlaceholder;
    vm.isPentahoRepo = isPentahoRepo;
    vm.getSelectedFolderName = getSelectedFolderName;
    vm.isSaveEnabled = isSaveEnabled;
    vm.isShowRecents = isShowRecents;
    vm.getFiles = getFiles;
    vm.currentRepo = "";
    vm.selectedFolder = "";
    vm.fileToSave = "";
    vm.showError = false;
    vm.errorType = 0;
    vm.loading = true;
    vm.fileLoading = false;
    vm.searching = false;
    vm.state = $state;
    vm.searchResults = [];

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.searchPlaceholder = i18n.get("file-open-save-plugin.app.header.search.placeholder");
      vm.saveFileNameLabel = i18n.get("file-open-save-plugin.app.save.file-name.label");
      vm.openButton = i18n.get("file-open-save-plugin.app.open.button");
      vm.cancelButton = i18n.get("file-open-save-plugin.app.cancel.button");
      vm.saveButton = i18n.get("file-open-save-plugin.app.save.button");
      vm.okButton = i18n.get("file-open-save-plugin.app.ok.button");
      vm.confirmButton = i18n.get("file-open-save-plugin.app.save.button");
      vm.saveFileNameLabel = i18n.get("file-open-save-plugin.app.save.file-name.label");
      vm.addFolderText = i18n.get("file-open-save-plugin.app.add-folder.button");
      vm.removeText = i18n.get("file-open-save-plugin.app.delete.button");
      vm.loadingTitle = i18n.get("file-open-save-plugin.loading.title");
      vm.loadingMessage = i18n.get("file-open-save-plugin.loading.message");
      vm.showRecents = false;
      vm.folder = {name: "Recents", path: "Recents"};
      vm.selectedFolder = vm.folder.name;
      vm.file = null;
      vm.includeRoot = false;
      vm.autoExpand = false;
      vm.didDeleteFolder = false;
      vm.searchString = "";
      _resetFileAreaMessage();

      vm.filename = $location.search().filename;
      vm.fileType = $location.search().fileType;
      vm.origin = $location.search().origin;
      $timeout(function() {
        if ($state.is('open')) {
          vm.headerTitle = i18n.get("file-open-save-plugin.app.header.open.title");
        }
        if ($state.is('save')) {
          vm.headerTitle = i18n.get("file-open-save-plugin.app.header.save.title");
        }
        if ($state.is('selectFolder')) {
          vm.headerTitle = i18n.get("file-open-save-plugin.app.header.select.title");
        }
        if (!$state.is('selectFolder')) {
          dt.getDirectoryTree($location.search().filter).then(_populateTree);
          dt.getRecentFiles().then(_populateRecentFiles);
        } else {
          dt.getDirectoryTree("false").then(_populateTree);
        }
        dt.getRecentSearches().then(_populateRecentSearches);
      });
    }

    /**
     * Sets the folder directory tree
     *
     * @param {Object} response - $http response from call to the data service
     * @private
     */
    function _populateTree(response) {
      vm.tree = response.data;
      var path = $location.search().path;
      if (path) {
        vm.autoExpand = true;
        selectFolderByPath(path);
      } else {
        if ($state.is('selectFolder')) {
          if (isPentahoRepo()) {
            selectFolderByPath("/home");
          } else {
            selectFolderByPath("/");
          }
        }
        vm.showRecents = true;
      }
      dt.getCurrentRepo().then(function(response) {
        vm.currentRepo = response.data.name;
        vm.loading = false;
      });
      _setFileToSaveName();
    }

    /**
     * Determines whether or no this is a pentaho repository
     *
     * @returns {boolean}
     * @private
     */
    function isPentahoRepo() {
      return vm.tree.children[0].children.length === 2 && vm.tree.children[0].children[0].name === "home";
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

    /**
     * Determines if the Recents view has a vertical scrollbar
     * @return {boolean} - true if Recents view has a vertical scrollbar, false otherwise
     */
    function recentsHasScrollBar() {
      var recentsView = document.getElementsByClassName("recentsView");
      return recentsView.scrollHeight > recentsView.clientHeight;
    }

    /**
     * Gets the active file name from Spoon to set vm.fileToSave
     * @private
     */
    function _setFileToSaveName() {
      if ($state.is("save")) {
        if (vm.filename !== undefined) {
          vm.fileToSave = vm.filename;
        } else {
          dt.getActiveFileName().then(function (response) {
            vm.fileToSave = response.data.fileName;
          }, function () {
            vm.fileToSave = "";
          });
        }
      }
    }

    /**
     * Sets variables showRecents, folder, and selectedFolder according to the contents of parameter
     *
     * @param {Object} folder - folder object
     * @param {Function} callback - an optional callback for when selection completes
     */
    function selectFolder(folder, callback) {
      vm.searchString = "";
      if (vm.searching) {
        _clearSearch();
      }
      _resetFileAreaMessage();
      vm.file = null;
      if ( folder === vm.folder ) {
        return;
      }
      if (folder) {
        vm.folder = folder;
        vm.showRecents = false;
        if (!vm.folder.loaded && vm.folder.subfoldersLoaded) {
          vm.fileLoading = true;
          vm.folder.loaded = true;
          dt.getFiles(vm.folder.path).then(function(response) {
            var loadedFolder = response.data;
            vm.folder.children = vm.folder.children.concat(loadedFolder.children);
            vm.fileLoading = false;
            _updateDisplay(vm.folder);
            if (callback) {
              callback();
            }
          });
        } else if (!vm.folder.loaded && !vm.folder.subfoldersLoaded) {
          vm.fileLoading = true;
          vm.folder.subfoldersLoaded = true;
          vm.folder.loaded = true;
          dt.getFilesAndFolders(vm.folder.path).then(function(response) {
            var loadedFolder = response.data;
            vm.folder.children = loadedFolder.children;
            vm.fileLoading = false;
            _updateDisplay(vm.folder);
            if (callback) {
              callback();
            }
          });
        } else {
          _updateDisplay(folder);
          if (callback) {
            callback();
          }
        }
      } else {
        vm.showRecents = true;
        vm.folder = {name: "Recents", path: "Recents"};
        vm.selectedFolder = "Recents";
      }
    }

    function _updateDisplay(folder) {
      vm.showRecents = false;
      vm.selectedFolder = ((vm.folder.name === "home" || vm.folder.name === "public") && vm.folder.parent === "/") ?
          _capsFirstLetter(folder.name) : folder.name;
    }

    /**
     * Selects a folder according to the path parameter
     *
     * @param {String} path - path to file
     */
    function selectFolderByPath(path) {
      var folder = _findFolderByPath(path);
      if (folder) {
        $timeout(function() {
          vm.folder = folder;
        });
      }
    }

    /**
     * Calls function to open folder if type of file is a folder. Else it opens either
     * the recent file or other file and closes the browser.
     *
     * @param {Object} file - file object
     */
    function selectFile(file) {
      if (file.type === "folder") {
        vm.searchString = "";
        selectFolder(file);
      } else if ($state.is("open")) {
        _open(file);
      }
    }

    function getFiles() {
      if (vm.searchString !== "") {
        return vm.searchResults;
      }
      return vm.folder.children;
    }

    var searchPaths = [];
    var maxSearching = 2;
    var currentSearches = [];
    var searchInterval;
    var checkInterval;
    var start;

    /**
     * Calls a filter for either recent files or files/folders in current folder
     */
    function doSearch(searchValue) {
      if (searchValue) {
        _initSearch();
        start = (new Date()).getTime();
        if (vm.showRecents === true) {
          selectFolder(vm.tree.children[0], function() {
            $timeout(function() {
              _doSearch(searchValue);
            })
          });
        } else {
          _doSearch(searchValue);
        }
      } else {
        _clearSearch();
        _setSearchState();
      }
    }

    function _setSearchState() {
      if (isPentahoRepo() && vm.searchString === "" && vm.selectedFolder === "/") {
        vm.showRecents = true;
        vm.folder = {name: "Recents", path: "Recents"};
        vm.selectedFolder = vm.folder.name;
        _resetFileAreaMessage();
      } else if (vm.searchString === "") {
        _resetFileAreaMessage();
      }
    }

    function _initSearch() {
      vm.searchResults = [];
      searchPaths = [];
      currentSearches = [];
    }

    function _completeSearch() {
      clearInterval(searchInterval);
      clearInterval(checkInterval);
      $timeout(function() {
        vm.searching = false;
      });
    }

    /**
     * Resets the search string and runs search against that string (which returns normal dir structure).
     */
    function _clearSearch() {
      dt.cancelSearch();
      _completeSearch();
      vm.searchResults = [];
      vm.searchString = "";
      _resetFileAreaMessage();
    }

    function _doSearch(searchValue) {
      vm.searching = true;
      vm.showMessage = true;
      vm.searchString = searchValue;
      _setFileAreaMessage();
      _loadSearchPaths(vm.folder, searchValue);
      searchInterval = setInterval(function() {
        if (currentSearches.length <= maxSearching) {
          var path = searchPaths.pop();
          if (path) {
            _loadSearchResults(path, searchValue);
          }
        }
      });
      checkInterval = setInterval(function() {
        if (currentSearches.length === 0) {
          _completeSearch();
        }
      }, 1000);
    }

    function _loadSearchPaths(folder, searchValue) {
      dt.search(folder.path, searchValue).then(function(response) {
        vm.searchResults = vm.searchResults.concat(response.data);
        if (vm.searchResults.length > 0) {
          vm.showMessage = false;
        }
      });
      for (var i = 0; i < folder.children.length; i++) {
        if (folder.children[i].type === "folder") {
          searchPaths.push(folder.children[i].path);
        }
      }
    }

    function _loadSearchResults(path, searchValue) {
      currentSearches.push(path);
      dt.getFolders(path).then(function(response) {
        _loadSearchPaths(response.data, searchValue);
        var index = currentSearches.indexOf(path);
        currentSearches.splice(index, 1);
      }, function() {
        var index = currentSearches.indexOf(path);
        currentSearches.splice(index, 1);
      });
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
          var inResult = name.indexOf(value.toLowerCase()) !== -1;
          vm.showMessage = inResult ? false : vm.showMessage;
          elements[i].inResult = inResult;
          if (elements[i].children.length > 0) {
            _filter(elements[i].children, value);
          }
        }
      }
    }

    /**
     * Sets the message for the file area to No Results
     * @private
     */
    function _setFileAreaMessage() {
      if (vm.showMessage) {
        vm.fileAreaMessage = i18n.get("file-open-save-plugin.app.middle.no-results.message");
      }
    }

    /**
     * Resets the showMessage and file area message to default values
     * @private
     */
    function _resetFileAreaMessage() {
      vm.showMessage = false;
      vm.fileAreaMessage = i18n.get("file-open-save-plugin.app.middle.no-recents.message");
    }

    /**
     * Sets the selected file to the file parameter to highlight it in UI.
     * Also, sets the file to save text according to the selected file.
     *
     * @param {Object} file - file object
     */
    function highlightFile(file) {
      vm.file = file;
      vm.fileToSave = file.type === "folder" ? vm.fileToSave : file.name;
    }

    /**
     * Called when user clicks "Open"
     */
    function openClicked() {
      if (vm.file && vm.file.type === "folder") {
        vm.searchString = "";
        selectFolder(vm.file);
      } else {
        _open(vm.file);
      }
    }

    /**
     * Called when user clicks "Save"
     */
    function saveClicked() {
      _save(false);
    }

    /**
     * Calls data service to open file
     * @param {Object} file - File object
     * @private
     */
    function _open(file) {
      try {
        if (vm.selectedFolder === "Recents" && vm.origin === "spoon") {
          dt.openRecent(file.repository + ":" + (file.username ? file.username : ""),
            file.objectId.id).then(function(response) {
              _closeBrowser();
            }, function(response) {
              _triggerError(16);
            });
        } else {
          select(file.objectId.id, file.name, file.path, file.type);
        }
      } catch (e) {
        if (file.repository) {
          dt.openRecent(file.repository + ":" + (file.username ? file.username : ""),
            file.objectId.id).then(function(response) {
              _closeBrowser();
            }, function(response) {
              _triggerError(16);
            });
        } else {
          dt.openFile(file.objectId.id, file.type).then(function(response) {
            _closeBrowser();
          });
        }
      }
    }

    /**
     * Calls data service to save file if there is no error
     * @param {boolean} override - Override file?
     * @private
     */
    function _save(override) {
      if (_isInvalidName()) {
        _triggerError(17);
      }
      else if (override || !_isDuplicate()) {
        try {
          dt.checkForSecurityOrDupeIssues(vm.folder.path, vm.fileToSave, vm.file === null ? null : vm.file.name,
            override).then(function(response) {
              if (response.status === 200) {
                select("", vm.fileToSave, vm.folder.path, "");
              } else {
                _triggerError(3);
              }
            });
        } catch (e) {
          dt.saveFile(vm.folder.path, vm.fileToSave, vm.file === null ? null : vm.file.name, override)
            .then(function(response) {
              if (response.status === 200) {
                _closeBrowser();
              } else {
                _triggerError(3);
              }
            });
        }
      } else {
        _triggerError(1);
      }
    }

    function okClicked() {
      select(vm.file.objectId.id, vm.file.name, vm.file.path, vm.file.type);
    }

    /**
     * Called if user clicks cancel in either open or save to close the browser
     */
    function cancel() {
      _closeBrowser();
    }

    /**
     * Called to close the browser if there is no current error
     * @private
     */
    function _closeBrowser() {
      if (!vm.showError) {
        close();
      }
    }

    /**
     * Sets the error type and boolean to show the error dialog.
     * @param {Number} type - the type of error (0-6)
     * @private
     */
    function _triggerError(type) {
      vm.errorType = type;
      vm.showError = true;
    }

    /**
     * Called if user clicks the confirmation button on an error dialog to handle the
     * error and cancel it
     */
    function confirmError() {
      switch (vm.errorType) {
        case 1: // File exists...override
          _save(true);
          break;
        case 5: // Delete File
          commitRemove();
          break;
        case 6: // Delete Folder
          commitRemove();
          break;
        default:
          break;
      }
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
     * Shows an error if one occurs during rename
     * @param {number} errorType - the number corresponding to the appropriate error
     * @param {object} file - file object
     */
    function renameError(errorType, file) {
      if (file) {
        var index = vm.folder.children.indexOf(file);
        vm.folder.children.splice(index, 1);
      }
      $timeout(function() {
        _triggerError(errorType);
      });
    }

    /**
     * Stores the most recent search
     */
    function storeRecentSearch() {
      if (vm.searchString !== "") {
        if (vm.recentSearches.indexOf(vm.searchString) === -1) {
          vm.recentSearches.push(vm.searchString);
          dt.storeRecentSearch(vm.searchString).then(_populateRecentSearches);
        }
      }
    }

    /**
     * Determines if add button is to be disabled
     * @return {boolean} - True if no folder is selected or if Recents is selected, false otherwise
     */
    function addDisabled() {
      return addDeleteDisabled() || vm.searchString !== "";
    }

    /**
     * Determines if delete button is to be disabled
     * @return {boolean} - True if no folder is selected, if Recents is selected,
     * or if root folder/file is selected, false otherwise
     */
    function deleteDisabled() {
      return addDeleteDisabled() || isRoot();
    }

    /**
     * Determines if add or delete button is to be disabled
     * @return {boolean} - True if no folder is selected, if Recents is selected, or if a message is showing.
     * False otherwise
     */
    function addDeleteDisabled() {
      return (vm.folder === null || vm.folder.path === "Recents" || vm.showMessage);
    }

    /**
     * Determines if (selected folder is a root folder according to repository type AND if no file is selected) OR
     * if the selected file is a root folder
     * @return {boolean} - True if root folder is selected and no file is selected, false otherwise
     */
    function isRoot() {
      var isFileNull = vm.file === null;
      if (isPentahoRepo()) {
        if (isFileNull) {
          return vm.folder.path === "/home" || vm.folder.path === "/home/admin" || vm.folder.path === "/";
        }
        return vm.file.type === "folder" && (vm.file.path === "/home" || vm.file.path === "/home/admin");
      }
      if (isFileNull) {
        return vm.folder.path === "/";
      }
      return vm.file.type === "folder" && vm.file.path === "/";
    }

    /**
     * @param {Object} response - Response object
     * Populates the most recent searches
     * @private
     */
    function _populateRecentSearches(response) {
      vm.recentSearches = response.data;
    }

    /**
     * Called if user selects to delete the selected folder or file.
     */
    function remove() {
      if (vm.file === null || vm.file.type === "folder") {
        _triggerError(6);
      } else {
        _triggerError(5);
      }
    }

    function _findFolderByTraverse(children, path) {
      for (var i = 0; i < children.length; i++) {
        if (children[i].path === path) {
          return children[i];
        }
        if (children[i].children.length > 0) {
          return _findFolderByTraverse(children[i].children, path);
        }
      }
      return null;
    }

    function _findFolderByPath(path) {
      path = path === "/" ? "" : path;
      var parts = path.split("/");
      var folder = _findFolder(vm.tree.children, parts, 0);
      return folder;
    }

    function _findFolder(children, parts, index) {
      for (var i = 0; i < children.length; i++) {
        if (children[i].name === parts[index]) {
          if (index < parts.length - 1) {
            return _findFolder(children[i].children, parts, ++index);
          } else {
            return children[i];
          }
        }
      }

      var folder = {name: parts[index], path: parts.slice(0, index+1).join("/"), children: []};
      children.push(folder);
      if (index < parts.length - 1) {
        return _findFolder(folder.children, parts, ++index);
      } else {
        return folder;
      }
    }


    /**
     * Calls the service for removing the file
     */
    function commitRemove() {
      if (vm.file === null) {// delete folder from directory tree panel
        dt.remove(vm.folder.objectId ? vm.folder.objectId.id : "", vm.folder.name, vm.folder.path, vm.folder.type)
          .then(function() {
            var parentFolder = _findFolderByTraverse(vm.tree.children, vm.folder.parent);
            var index = parentFolder.children.indexOf(vm.folder);
            parentFolder.children.splice(index, 1);
            selectFolder(parentFolder);
            vm.selectedFolder = vm.folder.name;
            vm.file = null;
            vm.searchString = "";
            vm.showMessage = false;
            dt.getRecentFiles().then(_populateRecentFiles);
            vm.didDeleteFolder = true;
          }, function(response) {
            if (response.status === 406) {// folder has open file
              _triggerError(13);
            } else {
              _triggerError(8);
            }
          });
      } else {// delete file or folder from files list panel
        dt.remove(vm.file.objectId.id, vm.file.name, vm.file.path, vm.file.type)
          .then(function() {
            var index = vm.folder.children.indexOf(vm.file);
            vm.folder.children.splice(index, 1);
            vm.file = null;
            dt.getRecentFiles().then(_populateRecentFiles);
          }, function(response) {
            if (vm.file.type === "folder") {
              if (response.status === 406) {// folder has open file
                _triggerError(13);
              } else {
                _triggerError(8);
              }
            } else if (response.status === 406) {// file is open
              _triggerError(14);
            } else {
              _triggerError(9);
            }
          });
      }
    }

    /**
     * Called if user selects to add a folder or file while Recents is not selected.
     */
    function addFolder() {
      if (vm.selectedFolder !== "Recents") {
        var folder = {};
        var name = _getFolderName();
        folder.parent = vm.folder.path;
        folder.path = vm.folder.path + (vm.folder.path.charAt(vm.folder.path.length - 1) === "/" ? "" : "/") + name;
        folder.name = name;
        folder.visible = vm.folder.open;
        folder.new = true;
        folder.autoEdit = true;
        folder.type = "folder";
        folder.children = [];
        vm.folder.children.splice(0, 0, folder);
      }
    }

    function updateDirectories(folder, oldPath, newPath) {
      folder.path = newPath;
      for (var i = 0; i < folder.children.length; i++) {
        _updateDirectories(folder.children[i], oldPath, newPath);
      }
      for (var k = 0; k < vm.recentFiles.length; k++) {
        if ((vm.recentFiles[k].path + "/").lastIndexOf(oldPath + "/", 0) === 0) {
          dt.updateRecentFiles(oldPath, newPath).then(function() {
            dt.getRecentFiles().then(_populateRecentFiles);
          });
          break;
        }
      }
    }

    /**
     * Update all child folder paths on parent rename
     *
     * @param {Object} child - Folder Object
     * @param {String} oldPath - String path of old directory path
     * @param {String} newPath - String path of new directory path
     * @private
     */
    function _updateDirectories(child, oldPath, newPath) {
      child.parent = child.parent.replace(oldPath, newPath);
      child.path = child.path.replace(oldPath, newPath);
      for (var i = 0; i < child.children.length; i++) {
        _updateDirectories(child.children[i], oldPath, newPath);
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

    /**
     * Checks to see if the user has entered a file to save the same as a file already in current directory
     * NOTE: does not check for hidden files. That is done in the checkForSecurityOrDupeIssues rest call
     * @return {boolean} - true if duplicate, false otherwise
     * @private
     */
    function _isDuplicate() {
      if (vm.folder && vm.folder.children) {
        for (var i = 0; i < vm.folder.children.length; i++) {
          if (vm.fileToSave === vm.folder.children[i].name && vm.fileType === vm.folder.children[i].type) {
            vm.file = vm.folder.children[i];
            return true;
          }
        }
      }
      return false;
    }

    /**
     * Checks if the file name to save is valid or not. An invalid name contains forward or backward slashes
     * @returns {boolean} - true if the name is invalid, false otherwise
     * @private
     */
    function _isInvalidName() {
      return vm.fileToSave.match(/[\\\/]/g) !== null;
    }

    /**
     * Gets the key up event from the app
     *
     * @param {Object} event - Event Object
     */
    function onKeyUp(event) {
      if (event.keyCode === 13 && event.target.id !== "searchBoxId") {
        if ($state.is("open")) {
          if (vm.file !== null) {
            selectFile(vm.file);
          }
        } else if (!vm.showRecents) {
          _save(false);
        }
      }
    }

    /**
     * Determines if the browser is Internet Explorer.
     * If it is, it truncates the placeholder for the search box if it's width is greater than the
     * search box. It then adds ellipsis to the end of that string and returns that value.
     * If it is not Internet Explorer, it just returns the search box placeholder and any
     * truncation/ellipsis is handled using CSS. NOTE: this is a workaround for an IE bug
     * that doesn't allow placeholders to be ellipsis unless the input is readonly.
     * @return {string} - the Placeholder for the search box
     */
    function getPlaceholder() {
      var isIE = navigator.userAgent.indexOf("Trident") !== -1 && Boolean(document.documentMode);
      var retVal = vm.searchPlaceholder;
      if (vm.folder.path !== "Recents") {
        retVal += " " + vm.selectedFolder;
      } else {
        retVal += " " + vm.currentRepo;
      }
      if (isIE && utils.getTextWidth(retVal) > 210) {
        var tmp = "";
        for (var i = 0; i < retVal.length; i++) {
          tmp = retVal.slice(0, i);
          if (utils.getTextWidth(tmp) > 196) {
            break;
          }
        }
        retVal = tmp + "...";
      }
      return retVal;
    }

    /**
    * Returns input with first letter capitalized
    * @param {string} input - input string
    * @return {string} - returns input with first letter capitalized
    * @private
    */
    function _capsFirstLetter(input) {
      return input.charAt(0).toUpperCase() + input.slice(1);
    }

    /**
     * Returns the name of the selected folder
     *
     * @return {String} - "Search results in "<The name of the selected folder>", truncating with ellipsis accordingly
     */
    function getSelectedFolderName() {
      var retVal = i18n.get("file-open-save-plugin.app.search-results-in.label");
      if (vm.selectedFolder === "" && isPentahoRepo()) {
        retVal += "\"" + vm.currentRepo;
      } else {
        retVal += "\"" + vm.selectedFolder;
      }
      if ($state.is("open") && utils.getTextWidth(retVal) > 435) {
        retVal = utils.truncateString(retVal, 426) + "...";
      } else if ($state.is("save") && utils.getTextWidth(retVal) > 395) {
        retVal = utils.truncateString(retVal, 386) + "...";
      }
      return retVal + "\"";
    }

    /**
     * Returns whether or not the save button should be enabled
     *
     * @returns {boolean} - true if the save button should be enabled
     */
    function isSaveEnabled() {
      return vm.fileToSave === '' || vm.folder.path === 'Recents';
    }

    /**
     * Returns whether or not the recents panel should be shown
     *
     * @returns {boolean} - true if recents should be shown
     */
    function isShowRecents() {
      if (vm.recentFiles) {
        return !vm.showMessage && vm.showRecents && vm.recentFiles.length > 0 && !$state.is('selectFolder');
      }
    }
  }

  return {
    name: "fileOpenSaveApp",
    options: options
  };
});
