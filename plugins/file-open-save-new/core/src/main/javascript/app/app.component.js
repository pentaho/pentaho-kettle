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
 * The File Open and Save Main App component.
 *
 * This provides the main component for supporting the file open and save functionality.
 * @module app.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "angular",
  "./services/data.service",
  "./services/file.service",
  "./services/folder.service",
  "./services/modal.service",
  "./services/search.service",
  "./services/message.service",
  "text!./app.html",
  "pentaho/i18n-osgi!file-open-save-new.messages",
  "./components/utils",
  "css!./app.css"
], function (angular, dataService, fileService, folderService, modalService, searchService, messageService, template,
             i18n, utils) {
  "use strict";

  var options = {
    bindings: {},
    template: template,
    controllerAs: "vm",
    controller: appController
  };

  appController.$inject = [dataService.name, fileService.name, folderService.name,
    modalService.name, searchService.name, messageService.name, "$location", "$timeout", "$interval", "$state", "$q"];

  /**
   * The App Controller.
   *
   * This provides the controller for the app component.
   *
   * @param {Object} dt - Angular service that contains helper functions for the app component controller
   * @param fileService
   * @param folderService
   * @param modalService
   * @param searchService
   * @param {Function} $location - Angular service used for parsing the URL in browser address bar
   * @param {Object} $timeout - Angular wrapper around window.setTimeout*
   * @param $interval
   * @param $state
   * @param $q
   */
  function appController(dt, fileService, folderService, modalService, searchService, messageService,$location,
                         $timeout, $interval, $state, $q) {
    var vm = this;
    vm.$onInit = onInit;
    vm.openFolder = openFolder;
    vm.selectFolder = selectFolder;
    vm.selectFolderByPath = selectFolderByPath;
    vm.openPath = openPath;
    vm.onRenameFile = onRenameFile;
    vm.onCreateFolder = onCreateFolder;
    vm.onMoveFiles = onMoveFiles;
    vm.onCopyFiles = onCopyFiles;
    vm.onSelectFile = onSelectFile;
    vm.onOpenClick = onOpenClick;
    vm.onSaveClick = onSaveClick;
    vm.onCancelClick = onCancelClick;
    vm.onHighlight = onHighlight;
    vm.confirmError = confirmError;
    vm.cancelError = cancelError;
    vm.storeRecentSearch = storeRecentSearch;
    vm.renameError = renameError;
    vm.onKeyUp = onKeyUp;
    vm.onKeyDown = onKeyDown;
    vm.onSelectFilter = selectFilter;
    vm.isSaveState = isSaveState;

    vm.isCardView = false;
    vm.isFileView = false;
    vm.isSaveEnabled = false;
    vm.addDisabled = true;
    vm.deleteDisabled = true;
    vm.upDisabled = true;
    vm.refreshDisabled = true;
    vm.placeholder = "";
    vm.fileList = [];

    vm.onAddFolder = onAddFolder;
    vm.onDeleteFiles = onDeleteFiles;
    vm.onUpDirectory = onUpDirectory;
    vm.onRefreshFolder = onRefreshFolder;

    vm.doSearch = doSearch;

    vm.currentRepo = "";
    vm.selectedFolder = "";
    vm.fileToSave = "";
    vm.showError = false;
    vm.errorType = 0;
    vm.loading = true;
    vm.fileLoading = false;
    vm.searching = false;
    vm.state = $state;
    vm.searchResults = null;
    vm.status = "";
    vm.breadcrumbPath = null;
    vm.type = null;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.loadingTitle = i18n.get("file-open-save-plugin.loading.title");
      vm.loadingMessage = i18n.get("file-open-save-plugin.loading.message");
      vm.selectedFiles = [];
      vm.errorFiles = [];
      vm.autoExpand = false;
      vm.searchString = "";
      vm.myfile = "";

      vm.tree = [];
      vm.filename = $location.search().filename;
      vm.fileType = $location.search().fileType;
      vm.origin = $location.search().origin;
      vm.filter = $location.search().filter;
      vm.defaultFilter = $location.search().defaultFilter;
      vm.connectionTypes = $location.search().connectionTypes;
      vm.fileTypes = vm.filter ? vm.filter.split(',') : false;
      vm.providerFilter = $location.search().providerFilter;
      vm.selectedFolder = "";
      $timeout(function () {
        var state = $state.current.name;
        vm.headerTitle = i18n.get("file-open-save-plugin.app.header." + state + ".title");
        dt.getDirectoryTree(vm.providerFilter, vm.connectionTypes).then(function(response) {
          _populateTree(response);
          _init();
        });
        vm.loading = false;
      });
    }

    function _init() {
      var path = decodeURIComponent($location.search().path);

      // URLs come over with '+' instead of spaces
      path = path.replace(/\+/g, " ");

      if (path && path !== "undefined") {
        vm.autoExpand = true;
        openPath(path, $location.search()).then(function() {
          _setFileToSaveName();
        });
      } else {
        _setFileToSaveName();
      }
    }

    /**
     * Sets the folder directory tree
     *
     * @param {Object} response - $http response from call to the data service
     * @private
     */
    function _populateTree(response) {
      vm.tree = vm.tree.concat(response.data);
      folderService.tree = vm.tree;
      for (var i = 0; i < vm.tree.length; i++) {
        if (vm.tree[i].order === 0) {
          selectFolder(vm.tree[i], true);
        }
      }
    }

    function selectFilter(value) {
      vm.selectedFilter = value.value;
    }

    /**
     * Gets the active file name from Spoon to set vm.fileToSave
     * @private
     */
    function _setFileToSaveName() {
      if (isSaveState()) {
        vm.fileToSave = "";

        if (vm.filename !== undefined) {
          vm.fileToSave = vm.filename;
        } else if (vm.state.is('save')) {
          // Only update with trans/job filename in save state.
          dt.getActiveFileName().then(function (response) {
            vm.fileToSave = response.data.fileName;
          }, function () {
            vm.fileToSave = "";
          });
        }
      }
    }

    /**
     * Open the folder
     * @param {folder} folder - Folder to open
     */
    function openFolder(folder) {
      return folderService.selectFolder(folder);
    }

    /**
     * Sets variables showRecents, folder, and selectedFolder according to the contents of parameter
     *
     * @param {Object} folder - folder object
     * @param {Boolean} useCache - should use cache
     */
    function selectFolder(folder, useCache) {
      messageService.set("file", null);
      vm.searchResults = null;
      if (vm.searching) {
        vm.searchValue = "";
      }
      fileService.files = [];
      vm.fileLoading = true;
      vm.folder = folder;
      folderService.selectFolder(folder, undefined, useCache).then(function(folder) {
        vm.fileLoading = false;
        _update();
      }, function(error) {
        modalService.open("error-dialog", error.title, error.message).then(function() {
          folder.loading = false;
          vm.fileLoading = false;
          _update();
        });
      });
    }

    /**
     * Selects a folder according to the path parameter
     *
     * @param {String} path - path to file
     * @param {Object} props - properties
     */
    function selectFolderByPath(path, props) {
      vm.fileLoading = true;
      folderService.selectFolderByPath(path, props).then(function() {
        vm.fileLoading = false;
        _update();
      });
    }

    function _resetFolder() {
      if (vm.folder.provider) {
        selectFolder(vm.folder, true);
      } else {
        selectFolderByPath(vm.folder.path, null);
      }
    }

    function openPath(path, properties) {
      return $q(function(resolve) {
        vm.fileLoading = true;
        folderService.openPath(path, properties).then(function() {
          vm.fileLoading = false;
          _update();
          resolve();
        }, function(error) {
          vm.fileLoading = false;
          if (error) {
            modalService.open("error-dialog", error.title, error.message).then(function () {
              _resetFolder();
            });
          } else {
            _resetFolder();
          }
          resolve();
        });
      });
    }

    function _update() {
      vm.folder = folderService.folder;
      vm.selectedFiles = fileService.files;
      vm.breadcrumbPath = folderService.getBreadcrumbPath(vm.selectedFiles.length === 1 ? vm.selectedFiles[0] : vm.folder);
      vm.myfile = vm.selectedFiles.length === 1 ? vm.selectedFiles[0] : vm.folder;
      vm.placeholder = utils.getPlaceholder(i18n.get("file-open-save-plugin.app.header.search.placeholder"), vm.folder, vm.currentRepo);
      vm.fileList = _getFiles();
      if (vm.selectedFiles.length === 1) {
        vm.fileToSave = vm.selectedFiles[0].type === "folder" ? vm.fileToSave : vm.selectedFiles[0].name;
      } else {
        vm.fileToSave = "";
      }
      vm.errorFiles = vm.selectedFiles;
      vm.isCardView = !vm.folder.root && !vm.folder.hasChildren && !vm.folder.path;
      vm.isFileView = (!vm.showMessage && (vm.folder.hasChildren || vm.folder.root)) || vm.folder.path;
      vm.fileAreaMessage = messageService.get("file");
    }

    /**
     * Calls function to open folder if type of file is a folder. Else it opens either
     * the recent file or other file and closes the browser.
     *
     * @param {Object} file - file object
     */
    function onSelectFile(file) {
      if (file.type === "folder") {
        vm.searchString = "";
        selectFolder(file, true);
      } else if (!isSaveState()) {
        _open(file);
      }
    }

    /**
     * Get file list for current state
     * @returns {Array} - Search result files or selected folder children
     */
    function _getFiles() {
      return vm.searchResults !== null ? vm.searchResults : vm.folder.children;
    }

    /**
     * Sets the selected file to the file parameter to highlight it in UI.
     * Also, sets the file to save text according to the selected file.
     *
     * @param {Object} files - file objects
     */
    function onHighlight(files) {
      fileService.files = files;
      _update();
    }

    /**
     * Called when user clicks "Open"
     */
    function onOpenClick() {
      if (fileService.files.length === 1) {
        // If something is selected either open the folder or return it depending on state
        if (fileService.files[0].type === "folder" && !$state.is("selectFolder") && !$state.is("selectFileFolder")) {
          vm.searchString = "";
          selectFolder(fileService.files[0], true);
        } else {
          _open(fileService.files[0]);
        }
      } else {
        // Nothing is selected return the folder path if in Select Folder state
        if ($state.is("selectFolder") || $state.is("selectFileFolder")) {
          _open(folderService.folder)
        }
      }
    }

    /**
     * Called when user clicks "Save"
     */
    function onSaveClick() {
      _save(false);
    }

    /**
     * Calls data service to open file
     * @param {Object} file - File object
     * @private
     */
    function _open(file) {
      fileService.open(file);
    }

    /**
     * Calls data service to save file if there is no error
     * @param {boolean} override - Override file?
     * @private
     */
    function _save(override) {
      var duplicate = null;
      var folderToUse = vm.folder;

      if (vm.fileToSave === "" && vm.state.is("saveToFileFolder") ) {
        if ( fileService.files.length >= 1 ) {
          // Set the file/folder to the selected item if fileToSave is blank and something is selected.
          duplicate = folderService.getDuplicate(fileService.files[0].name);
          folderToUse = fileService.files[0];
        } else {
          // Nothing selected use the current directory which is a duplicate.
          duplicate = folderService.folder;
        }
      }
      if (duplicate === null ) {
        // Determine if the file/folder to save is a duplicate to prompt the user to overwrite.
        duplicate = folderService.getDuplicate(vm.fileToSave);
      }

      if (_isInvalidName()) {
        _triggerError(17);
      } else if (override || duplicate === null) {
        var currentFilename = duplicate !== null ? duplicate.name : null;
        fileService.save(vm.fileToSave, folderToUse, currentFilename, override).then(function() {
          // Dialog should close
        }, function() {
          _triggerError(3);
        });
      } else {
        vm.errorFiles = [duplicate];
        vm.state.is("save") ? _triggerError(1) : _triggerError(22);
      }
    }

    /**
     * Called if user clicks cancel in either open or save to close the browser
     */
    function onCancelClick() {
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
        case 22:
          _save(true);
          break;
        case 5: // Delete File
          commitRemove();
          break;
        case 6: // Delete Folder
          commitRemove();
          break;
        case 21:
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
      $timeout(function () {
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
    //TODO: Get a message based on the provider, file, etc.
    function onDeleteFiles(files) {
      if (files) {
        fileService.files = files;
      }
      if (fileService.files.length === 1) {
        if (fileService.files[0].type === "folder") {
          _triggerError(6);
        } else {
          _triggerError(5);
        }
      } else {
        if (fileService.files.length === 0) {
          _triggerError(6);
        } else {
          _triggerError(21);
        }
      }
      _update();
    }

    // TODO: Fix all the error messages
    /**
     * Calls the service for removing the file
     */
    function commitRemove() {
      if (vm.selectedFiles.length === 0) {
        folderService.deleteFolder(vm.folder).then(function (parentFolder) {
          vm.folder = null;
          selectFolder(parentFolder);
          vm.selectedFiles = null;
          vm.searchString = "";
          vm.showMessage = false;
        }, function (response) {
          if (response.status === 406) {// folder has open file
            _triggerError(13);
          } else {
            _triggerError(8);
          }
        });
      } else {
        fileService.deleteFiles(vm.folder, vm.selectedFiles).then(function (response) {
          fileService.files = [];
        }, function (response) {
          if (fileService.files[0].type === "folder") {
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
      _update();
    }

    function isSelectState() {
      return (vm.state.is('selectFile') || vm.state.is('selectFolder') || vm.state.is('selectFileFolder'));
    }

    function isSaveState() {
      return (vm.state.is('save') || vm.state.is('saveTo') || vm.state.is('saveToFileFolder'));
    }

    /**
     * Called if user selects to add a folder or file while Recents is not selected.
     */
    function onAddFolder() {
      folderService.addFolder(vm.folder);
      vm.fileList = _getFiles();
    }

    /**
     * Create new folder
     * @param {folder} folder - folder object definition
     * @returns {*} - A promise for the creation
     */
    function onCreateFolder(folder) {
      return $q(function (resolve, reject) {
        folderService.createFolder(folder).then(function () {
          _update();
          resolve();
        }, function (result) {
          switch (result.status) {
            case "FILE_COLLISION":
              // TODO: This should be a file already exists error
              _triggerError(4);
              break;
            case "ERROR":
              _triggerError(4);
              break;
          }
          _resetFolder();
          reject(result.status);
        });
      });
    }

    /**
     * Copy files to a new directory
     * @param {array} from - List of file objects to move
     * @param {file} to - File object to move
     * @returns {*} - a promise to handle the creation
     */
    function onCopyFiles(from, to) {
      copyFiles(from, to);
    }

    /**
     * Move files to a new directory
     * @param {array} from - List of file objects to move
     * @param {file} to - File object to move
     * @returns {*} - a promise to handle the creation
     */
    function onMoveFiles(from, to) {
      if (fileService.isCopy(from[0], to)) {
        copyFiles(from, to);
      } else {
        moveFiles(from, to);
      }
    }

    /**
     * Move files from one directory to another
     * @param from
     * @param to
     */
    // TODO: Bust cache on moved directory
    function moveFiles(from, to) {
      fileService.moveFiles(from, to).then(function (response) {
        to.loaded = false;
        // TODO: Do some cleanup here instead of full refresh
        var parentFolder = folderService.findFolderByPath(from[0]);
        parentFolder.loaded = false;
        onRefreshFolder();
      }, function (response) {
        onRefreshFolder();
        // TODO: Trigger error that files couldn't be copied and why
      });
    }

    /**
     * Copies a file from on provider to another
     * @param from
     * @param to
     */
    function copyFiles(from, to) {
      fileService.copyFiles(from, to).then(function (response) {
        to.loaded = false;
        onRefreshFolder();
      }, function (response) {
        onRefreshFolder();
        // TODO: Trigger error that files couldn't be copied
      });
    }

    /**
     * Renames a file
     * @param file
     * @param newPath
     * @returns {*}
     */
    function onRenameFile(file, newPath) {
      return $q(function (resolve, reject) {
        fileService.renameFile(file, newPath).then(function (result) {
          resolve();
        }, function (result) {
          switch (result.status) {
            case "FILE_COLLISION":
              _triggerError(11);
              break;
          }
          reject();
        });
      });
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
     * Gets the key down event from the app
     *
     * @param {Object} event - Event Object
     */
    function onKeyDown(event) {
      var ctrlKey = event.metaKey || event.ctrlKey;
      if (event.keyCode === 38 && ctrlKey) {
        onUpDirectory();
      }
    }

    /**
     * Gets the key up event from the app
     *
     * @param {Object} event - Event Object
     */
    function onKeyUp(event) {
      if (event.keyCode === 13 && event.target.tagName !== "INPUT") {
        if (isSaveState()) {
          _save(false);
        } else if (vm.selectedFiles.length === 1) {
          onSelectFile(vm.selectedFiles[0]);
        }
      }
    }

    /**
     * Navigate up a directory
     */
    function onUpDirectory() {
      vm.fileLoading = true;
      folderService.upDirectory().then(function() {
        vm.fileLoading = false;
        _update();
      });
    }

    /**
     * Refresh the currently visible folder
     */
    function onRefreshFolder() {
      vm.fileLoading = true;
      folderService.refreshFolder().then(function() {
        vm.fileLoading = false;
        _update();
      });
    }

    function doSearch(value) {
      vm.searchResults = null;
      if (value !== "") {
        vm.searchResults = [];
        searchService.search(vm.folder, vm.searchResults, value);
      }
      _update();
    }
  }

  return {
    name: "fileOpenSaveApp",
    options: options
  };
});
