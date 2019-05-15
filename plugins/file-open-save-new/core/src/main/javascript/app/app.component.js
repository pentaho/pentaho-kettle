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
  "text!./app.html",
  "pentaho/i18n-osgi!file-open-save-new.messages",
  "./components/utils",
  "css!./app.css"
], function (angular, dataService, fileService, folderService, modalService, searchService, template, i18n, utils) {
  "use strict";

  var options = {
    bindings: {},
    template: template,
    controllerAs: "vm",
    controller: appController
  };

  appController.$inject = [dataService.name, fileService.name, folderService.name,
    modalService.name, searchService.name, "$location", "$timeout", "$interval", "$state", "$q", "$document"];

  /**
   * The App Controller.
   *
   * This provides the controller for the app component.
   *
   * @param {Object} dt - Angular service that contains helper functions for the app component controller
   * @param {Function} $location - Angular service used for parsing the URL in browser address bar
   * @param {Object} $timeout - Angular wrapper around window.setTimeout
   * @param fileService
   * @param folderService
   * @param $state
   * @param $q
   */
  function appController(dt, fileService, folderService, modalService, searchService, $location, $timeout, $interval, $state, $q, $document) {
    var vm = this;
    vm.$onInit = onInit;
    vm.openFolder = openFolder;
    vm.selectFolder = selectFolder;
    vm.selectFolderByPath = selectFolderByPath;
    vm.onRenameFile = onRenameFile;
    vm.onCreateFolder = onCreateFolder;
    vm.onMoveFiles = onMoveFiles;
    vm.onCopyFiles = onCopyFiles;
    vm.onSelectFile = onSelectFile;
    vm.onDeleteFiles = onDeleteFiles;
    vm.openClicked = openClicked;
    vm.saveClicked = saveClicked;
    vm.okClicked = okClicked;
    vm.cancel = cancel;
    vm.onHighlight = onHighlight;
    vm.confirmError = confirmError;
    vm.cancelError = cancelError;
    vm.storeRecentSearch = storeRecentSearch;
    vm.renameError = renameError;
    vm.recentsHasScrollBar = recentsHasScrollBar;
    vm.addDisabled = addDisabled;
    vm.deleteDisabled = deleteDisabled;
    vm.upDisabled = upDisabled;
    vm.refreshDisabled = refreshDisabled;
    vm.onKeyUp = onKeyUp;
    vm.onKeyDown = onKeyDown;
    vm.getPlaceholder = getPlaceholder;
    vm.getSelectedFolderName = getSelectedFolderName;
    vm.isSaveEnabled = isSaveEnabled;
    vm.isShowRecents = isShowRecents;
    vm.getFiles = getFiles;
    vm.getPath = getPath;
    vm.getFilePath = getFilePath;
    vm.getType = getType;
    vm.selectFilter = selectFilter;

    vm.onAddFolder = onAddFolder;
    vm.onUpDirectory = onUpDirectory;
    vm.onRefreshFolder = onRefreshFolder;
    vm.onBackHistory = onBackHistory;
    vm.onForwardHistory = onForwardHistory;

    vm.doSearch = doSearch;

    vm.backHistoryDisabled = backHistoryDisabled;
    vm.forwardHistoryDisabled = forwardHistoryDisabled;
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
    vm.status = "";
    var history = [];
    var historyIndex = -1;

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
      vm.fileFilterLabel = i18n.get("file-open-save-plugin.app.save.file-filter.label");
      vm.saveFileNameLabel = i18n.get("file-open-save-plugin.app.save.file-name.label");
      vm.showRecents = false;
      vm.files = [];
      vm.includeRoot = false;
      vm.autoExpand = false;
      vm.searchString = "";
      _resetFileAreaMessage();

      vm.fileFilters = [
        {
          value: "all",
          label: "All Files"
        },
        {
          value: "\\.kjb$|\\.ktr$",
          label: "Kettle Files"
        },
        {
          value: "\\.csv$",
          label: "*.csv"
        },
        {
          value: "\\.txt$",
          label: "*.txt"
        },
        {
          value: "\\.json$",
          label: "*.json"
        }
      ];

      vm.selectedFilter = vm.fileFilters[0].value;
      vm.filename = $location.search().filename;
      vm.fileType = $location.search().fileType;
      vm.origin = $location.search().origin;
      vm.filters = $location.search().filters;
      vm.tree = [
        {name: "Recents", hasChildren: false, provider: "recents", order: 0}
      ];
      vm.folder = vm.tree[0];
      vm.selectedFolder = "";
      $timeout(function () {
        var state = $state.current.name;
        vm.headerTitle = i18n.get("file-open-save-plugin.app.header." + state + ".title");
        if (!$state.is('selectFolder')) {
          dt.getDirectoryTree($location.search().filter).then(_populateTree);
          dt.getRecentFiles().then(_populateRecentFiles);
          vm.showRecents = true;
        } else {
          dt.getDirectoryTree("false").then(_populateTree);
        }
        dt.getRecentSearches().then(_populateRecentSearches);
        vm.loading = false;
      });
    }

    /**
     * Sets the folder directory tree
     *
     * @param {Object} response - $http response from call to the data service
     * @private
     */
    function _populateTree(response) {
      vm.tree = vm.tree.concat(response.data);
      var path = decodeURIComponent($location.search().path);
      var connection = $location.search().connection;
      var provider = $location.search().provider;
      if (path && path !== "undefined") {
        vm.autoExpand = true;
        selectFolderByPath(path, {path: path, connection: connection, provider: provider}, true);
      }
      _setFileToSaveName();
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

    function selectFilter(value) {
      vm.selectedFilter = value.value;
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
     * Open the folder
     * @param {folder} folder - Folder to open
     */
    function openFolder(folder) {
      return folderService.openFolder(folder);
    }

    /**
     * Sets variables showRecents, folder, and selectedFolder according to the contents of parameter
     *
     * @param {Object} folder - folder object
     * @param {Boolean} saveHistory - should save history
     * @param {Boolean} useCache - should use cache
     */
    function selectFolder(folder, saveHistory, useCache) {
      if (saveHistory) {
        addHistory(folder);
      }
      vm.searchResults = [];
      if (vm.searching) {
        _clearSearch();
      }
      _resetFileAreaMessage();
      vm.files = [];
      if (folder !== vm.folder || vm.folder.loaded === false) {
        vm.folder = folder;
        vm.folder.open = true;
        vm.showRecents = false;
        if (folder.provider === "recents") {
          vm.showRecents = true;
        } else {
          vm.fileLoading = true;
          folderService.selectFolder(folder, vm.filters, useCache).then(function () {
            vm.fileLoading = false;
          });
        }
      }
    }

    /**
     * Selects a folder according to the path parameter
     *
     * @param {String} path - path to file
     * @param {Object} folder - currently selected folder to get parameters from
     * @param {Boolean} saveHistory - should save history
     */
    function selectFolderByPath(path, folder, saveHistory) {
      folderService.findFolderByPath(vm.tree, folder, path).then(function (selectedFolder) {
        selectFolder(selectedFolder, saveHistory);
      });
    }

    /**
     * Calls function to open folder if type of file is a folder. Else it opens either
     * the recent file or other file and closes the browser.
     *
     * @param {Object} file - file object
     * @param {Boolean} saveHistory - should save history
     */
    function onSelectFile(file, saveHistory) {
      if (file.type === "folder") {
        vm.searchString = "";
        selectFolder(file, saveHistory);
      } else if ($state.is("open")) {
        _open(file);
      }
    }

    /**
     * Get file list for current state
     * @returns {Array} - Search result files or selected folder children
     */
    function getFiles() {
      if (vm.searchResults.length !== 0) {
        return vm.searchResults;
      }
      return vm.folder.children;
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
     * @param {Object} files - file objects
     */
    function onHighlight(files) {
      vm.files = files;
      if (files.length === 1) {
        vm.fileToSave = files[0].type === "folder" ? vm.fileToSave : files[0].name;
      }
    }

    /**
     * Called when user clicks "Open"
     */
    function openClicked() {
      if (vm.files.length === 1 && vm.files[0].type === "folder") {
        vm.searchString = "";
        selectFolder(vm.files[0]);
      } else if (vm.files.length === 1) {
        console.log(vm.files[0]);
        _open(vm.files[0]);
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
              file.objectId).then(function (response) {
            _closeBrowser();
          }, function (response) {
            _triggerError(16);
          });
        } else {
          fileService.open(file);
        }
      } catch (e) {
        if (file.repository) {
          dt.openRecent(file.repository + ":" + (file.username ? file.username : ""),
              file.objectId).then(function (response) {
            _closeBrowser();
          }, function (response) {
            _triggerError(16);
          });
        } else {
          dt.openFile(file.objectId, file.type, file.path).then(function (response) {
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
      } else if (override || !_isDuplicate()) {
        var currentFilename = "";
        if (vm.files.length > 0) {
          currentFilename = vm.files[0].name;
        }
        fileService.save(vm.fileToSave, vm.folder, currentFilename, override).then(function() {
          // Dialog should close
        }, function() {
          _triggerError(3);
        });
      } else {
        _triggerError(1);
      }
    }

    /**
     * Handler for when the ok button is clicked
     */
    function okClicked() {
      select(vm.file.objectId, vm.file.name, vm.file.path, vm.file.connection, vm.file.provider, vm.file.type);
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
     * Determines if add button is to be disabled
     * @return {boolean} - True if no folder is selected or if Recents is selected, false otherwise
     */
    function addDisabled() {
      return vm.folder && !vm.folder.canAddChildren;
    }


    /**
     * Determines if the refresh button is to be disabled
     * @returns {boolean} - True if current folder is selected and not loaded
     */
    function refreshDisabled() {
      return vm.folder && !vm.folder.loaded;
    }

    /**
     * Determines if the up directory button is to be disabled
     * @returns {boolean} - True if current folder is select and not loaded
     */
    function upDisabled() {
      return vm.folder && !vm.folder.loaded;
    }

    /**
     * Determines if delete button is to be disabled
     * @return {boolean} - True if no folder is selected, if Recents is selected,
     * or if root folder/file is selected, false otherwise
     */
    function deleteDisabled() {
      if (vm.files.length > 0) {
        for (var i = 0; i < vm.files.length; i++) {
          if (!vm.files[i].canEdit) {
            return true;
          }
        }
        return false;
      } else if (vm.folder) {
        return !vm.folder.canEdit;
      }
      return false;
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
        vm.files = files;
      }
      if (vm.files.length === 1) {
        if (vm.files[0].type === "folder") {
          _triggerError(6);
        } else {
          _triggerError(5);
        }
      } else {
        if (vm.files.length === 0) {
          _triggerError(6);
        } else {
          _triggerError(21);
        }
      }
    }

    // TODO: Fix all the error messages
    /**
     * Calls the service for removing the file
     */
    function commitRemove() {
      if (vm.files.length === 0) {
        folderService.deleteFolder(vm.tree, vm.folder).then(function (parentFolder) {
          vm.folder = null;
          selectFolder(parentFolder);
          vm.files = null;
          vm.searchString = "";
          vm.showMessage = false;
          dt.getRecentFiles().then(_populateRecentFiles);
        }, function (response) {
          if (response.status === 406) {// folder has open file
            _triggerError(13);
          } else {
            _triggerError(8);
          }
        });
      } else {
        fileService.deleteFiles(vm.folder, vm.files).then(function (response) {
          vm.files = [];
          dt.getRecentFiles().then(_populateRecentFiles);
        }, function (response) {
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
    function onAddFolder() {
      folderService.addFolder(vm.folder);
    }

    /**
     * Create new folder
     * @param {folder} folder - folder object definition
     * @returns {*} - A promise for the creation
     */
    function onCreateFolder(folder) {
      return $q(function (resolve, reject) {
        folderService.createFolder(folder).then(function () {
          resolve();
        }, function (result) {
          switch (result.status) {
            case "FILE_COLLISION":
              // TODO: This should be a file already exists error
              _triggerError(4);
              break;
          }
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
        console.log("Move Complete");
        to.loaded = false;
        // TODO: Do some cleanup here instead of full refresh
        var parentPath = from[0].path.substr(0, from[0].path.lastIndexOf("/"));
        var parentFolder = folderService.findFolderByPath(vm.tree, from[0], parentPath);
        parentFolder.loaded = false;
        onRefreshFolder();
      }, function (response) {
        console.log("Copy Failed.");
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
     * Checks to see if the user has entered a file to save the same as a file already in current directory
     * NOTE: does not check for hidden files. That is done in the checkForSecurityOrDupeIssues rest call
     * @return {boolean} - true if duplicate, false otherwise
     * @private
     */
    function _isDuplicate() {
      if (vm.folder && vm.folder.children) {
        for (var i = 0; i < vm.folder.children.length; i++) {
          if (vm.fileToSave === vm.folder.children[i].name) {
            vm.files = [vm.folder.children[i]];
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
        if ($state.is("open")) {
          if (vm.files.length === 1) {
            onSelectFile(vm.files[0]);
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
        retVal += " " + vm.folder.name;
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
      if (vm.selectedFolder === "") {
        // if (vm.selectedFolder === "" && isPentahoRepo()) {
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
      return vm.fileToSave === '' || !vm.folder.path;
    }

    /**
     * Returns whether or not the recents panel should be shown
     *
     * @returns {boolean} - true if recents should be shown
     */
    function isShowRecents() {
      if (vm.recentFiles) {
        return !vm.showMessage && vm.showRecents && vm.recentFiles.length > 0 && !$state.is('selectFolder') && !$state.is('selectFile');
      }
    }

    /**
     * Get the path of the current selected file
     * @returns {string} - the path of the currently selected file/folder
     */
    function getPath() {
      if (vm.files.length === 1) {
        return folderService.getPath(vm.files[0])
      } else {
        return folderService.getPath(vm.folder)
      }
    }

    function getType() {
      if (vm.files.length === 1) {
        return vm.files[0].provider;
      } else {
        return vm.folder.provider;
      }
    }

    /**
     * Get the path of the current selected file
     * @returns {string} - the path of the currently selected file/folder
     */
    function getFilePath() {
      if (vm.files.length === 1) {
        return vm.files[0].path;
      } else {
        return vm.folder.path;
      }
    }

    /**
     * Navigate up a directory
     */
    function onUpDirectory() {
      if (vm.folder) {
        var path = folderService.getPath(vm.folder);
        selectFolderByPath(path.substr(0, path.lastIndexOf("/")), vm.folder, true);
      }
    }

    /**
     * Add a location to the history
     * @param {folder} folder - the folder to add to the history
     */
    function addHistory(folder) {
      if (historyIndex !== history.length) {
        history = history.slice(0, historyIndex + 1);
      }
      historyIndex++;
      history.push(folder);
    }

    /**
     * Navigate back in the history
     */
    function onBackHistory() {
      vm.files = [];
      historyIndex--;
      var path = folderService.getPath(history[historyIndex]);
      selectFolderByPath(path, history[historyIndex], false);
    }

    /**
     * Navigate back in the history
     */
    function onForwardHistory() {
      vm.files = [];
      historyIndex++;
      var path = folderService.getPath(history[historyIndex]);
      selectFolderByPath(path, history[historyIndex], false);
    }

    /**
     * Refresh the currently visible folder
     */
    function onRefreshFolder() {
      if (vm.folder) {
        vm.folder.loaded = false;
        selectFolder(vm.folder, false, false);
      }
    }

    /**
     * Whether or not to disable the back history button
     * @returns {boolean} - Whether or not to disable the back history button
     */
    function backHistoryDisabled() {
      return historyIndex <= 0;
    }

    function forwardHistoryDisabled() {
      return historyIndex >= history.length - 1;
    }

    function doSearch(value) {
      vm.searchResults = [];
      if (value !== "") {
        searchService.search(vm.folder, vm.searchResults, value);
      }
    }
  }

  return {
    name: "fileOpenSaveApp",
    options: options
  };
})
;
