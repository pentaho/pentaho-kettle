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

  appController.$inject = [dataService.name, "$location", "$scope", "$timeout"];

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
  function appController(dt, $location, $scope, $timeout) {
    var vm = this;
    vm.$onInit = onInit;
    vm.selectFolder = selectFolder;
    vm.selectFile = selectFile;
    vm.selectFolderByPath = selectFolderByPath;
    vm.doSearch = doSearch;
    vm.resetSearch = resetSearch;
    vm.addFolder = addFolder;
    vm.openClicked = openClicked;
    vm.saveClicked = saveClicked;
    vm.cancel = cancel;
    vm.highlightFile = highlightFile;
    vm.remove = remove;
    vm.setState = setState;
    vm.confirmError = confirmError;
    vm.cancelError = cancelError;
    vm.storeRecentSearch = storeRecentSearch;
    vm.updateDirectories = updateDirectories;
    vm.renameError = renameError;
    vm.displayRecentSearches = displayRecentSearches;
    vm.focusSearchBox = focusSearchBox;
    vm.setTooltip = setTooltip;
    vm.recentsHasScrollBar = recentsHasScrollBar;
    vm.addDisabled = addDisabled;
    vm.deleteDisabled = deleteDisabled;
    vm.onKeyUp = onKeyUp;
    vm.getPlaceholder = getPlaceholder;
    vm.selectedFolder = "";
    vm.fileToSave = "";
    vm.searchString = "";
    vm.showError = false;
    vm.errorType = 0;
    vm.loading = true;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.wrapperClass = "save";
      vm.headerTitle = i18n.get("file-open-save-plugin.app.header.save.title");
      vm.searchPlaceholder = i18n.get("file-open-save-plugin.app.header.search.placeholder");
      vm.saveFileNameLabel = i18n.get("file-open-save-plugin.app.save.file-name.label");
      vm.openButton = i18n.get("file-open-save-plugin.app.open.button");
      vm.cancelButton = i18n.get("file-open-save-plugin.app.cancel.button");
      vm.saveButton = i18n.get("file-open-save-plugin.app.save.button");
      vm.confirmButton = i18n.get("file-open-save-plugin.app.save.button");
      vm.saveFileNameLabel = i18n.get("file-open-save-plugin.app.save.file-name.label");
      vm.addFolderText = i18n.get("file-open-save-plugin.app.add-folder.button");
      vm.removeText = i18n.get("file-open-save-plugin.app.delete.button");
      vm.isInSearch = false;
      vm.showRecents = true;
      vm.folder = {name: "Recents", path: "Recents"};
      vm.selectedFolder = vm.folder.name;
      vm.file = null;
      vm.includeRoot = false;
      vm.autoExpand = false;
      _resetFileAreaMessage();
      dt.getDirectoryTree($location.search().filter).then(_populateTree);
      dt.getRecentFiles().then(_populateRecentFiles);
      dt.getRecentSearches().then(_populateRecentSearches);

      var state = $location.search().state;
      vm.origin = $location.search().origin;
      if (state) {
        vm.setState(state);
      }
    }

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
      vm.loading = false;
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
     * Sets the wrapper class, title, and open/save button according to open or save option
     *
     * @param {String} state - "open" or "save" state
     */
    function setState(state) {
      if (state === "open") {
        vm.wrapperClass = "open";
        vm.headerTitle = i18n.get("file-open-save-plugin.app.header.open.title");
      }
      if (state === "save") {
        vm.wrapperClass = "save";
        vm.headerTitle = i18n.get("file-open-save-plugin.app.header.save.title");
      }
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
      if (vm.wrapperClass === "save") {
        dt.getActiveFileName().then(function(response) {
          vm.fileToSave = response.data.fileName;
        }, function() {
          vm.fileToSave = "";
        });
      }
    }

    /**
     * Sets variables showRecents, folder, and selectedFolder according to the contents of parameter
     *
     * @param {Object} folder - folder object
     */
    function selectFolder(folder) {
      vm.searchString = "";
      _resetFileAreaMessage();
      if (folder !== vm.folder) {
        vm.file = null;
        if (folder) {
          vm.showRecents = false;
          vm.folder = folder;
          vm.selectedFolder = ((vm.folder.name === "home" || vm.folder.name === "public") && vm.folder.parent === "/") ?
            _capsFirstLetter(folder.name) : folder.name;
        } else {
          vm.showRecents = true;
          vm.folder = {name: "Recents", path: "Recents"};
          vm.selectedFolder = "Recents";
        }
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
      if (file.type === "folder") {
        selectFolder(file);
      } else if (vm.wrapperClass === "open") {
        _open(file);
      }
    }

    /**
     * Calls a filter for either recent files or files/folders in current folder
     */
    function doSearch() {
      vm.isInSearch = false;
      vm.showMessage = true;
      if (vm.showRecents === true) {
        _filter(vm.recentFiles, vm.searchString);
      } else {
        _filter(vm.folder.children, vm.searchString);
      }
      _setFileAreaMessage();
    }

    /**
     * Resets the search string and runs search against that string (which returns normal dir structure).
     */
    function resetSearch() {
      if (vm.searchString !== "") {
        vm.searchString = "";
        vm.doSearch();
        _resetFileAreaMessage();
      } else {
        vm.focusSearchBox();
      }
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
            });
        } else {
          select(file.objectId.id, file.name, file.path, file.type);
        }
      } catch (e) {
        if (file.repository) {
          dt.openRecent(file.repository + ":" + (file.username ? file.username : ""),
            file.objectId.id).then(function(response) {
              _closeBrowser();
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
      if (!_isDuplicate() || override) {
        try {
          select("", vm.fileToSave, vm.folder.path, "");
        } catch (e) {
          dt.saveFile(vm.folder.path, vm.fileToSave).then(function(response) {
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
      $scope.$apply();
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
     */
    function renameError(errorType, file) {
      if (file) {
        var index = vm.folder.children.indexOf(file);
        vm.folder.children.splice(index, 1);
        if (vm.file.type === "folder") {
          for (var i = 0; i < vm.folders.length; i++) {
            if (vm.folders[i].path === vm.file.path) {
              vm.folders.splice(i, 1);
              break;
            }
          }
        }
      }
      $timeout(function() {
        _triggerError(errorType);
      });
    }

    /**
     * Stores the most recent search
     */
    function storeRecentSearch() {
      vm.isInSearch = false;
      if (vm.searchString !== "") {
        dt.storeRecentSearch(vm.searchString).then(_populateRecentSearches);
      }
    }

    /**
     * Determines if there are recent searches to show
     */
    function displayRecentSearches() {
      if (vm.recentSearches.length !== 0) {
        vm.isInSearch = true;
      }
    }

    /**
     * Determines if add button is to be disabled
     * @return {boolean} - True if Recents is selected, false otherwise
     */
    function addDisabled() {
      if (vm.folder && vm.folder.path === "Recents") {
        return true;
      }
      return false;
    }

    /**
     * Determines if delete button is to be disabled
     * @return {boolean} - True if a file is not selected or if Recents is selected, false otherwise
     */
    function deleteDisabled() {
      if (vm.file === null || (vm.folder && vm.folder.path === "Recents")) {
        return true;
      }
      return false;
    }

    /**
     * Sets focus on the search box
     */
    function focusSearchBox() {
      document.getElementById("searchBoxId").focus();
    }

    /**
     * Sets the tooltip of a recent search in the search dropdown if it is wider than its container
     * @param {String} id - the suffix of an element id
     * @param {String} tooltip - the tooltip to add if necessary
     */
    function setTooltip(id, tooltip) {
      if (utils.getTextWidth(tooltip) - 1 > 247) {
        document.getElementById("search-item-index-" + id).title = tooltip;
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
    function remove() {
      if (vm.file !== null) {
        if (vm.file.type === "folder") {
          _triggerError(6);
        } else {
          _triggerError(5);
        }
      }
    }

    /**
     * Calls the service for removing the file
     */
    function commitRemove() {
      if (vm.file !== null) {
        dt.remove(vm.file.objectId.id, vm.file.name, vm.file.path, vm.file.type)
          .then(function(response) {
            var index = vm.folder.children.indexOf(vm.file);
            vm.folder.children.splice(index, 1);
            if (vm.file.type === "folder") {
              for (var i = 0; i < vm.folders.length; i++) {
                if (vm.folders[i].path.indexOf(vm.file.path) === 0) {
                  vm.folders.splice(i, 1);
                  i--;
                }
              }
            }
            var hasChildFolders = false;
            for (var j = 0; j < vm.folder.children.length; j++) {
              if (vm.folder.children[j].type === "folder") {
                hasChildFolders = true;
              }
            }
            vm.folder.hasChildren = hasChildFolders;
            vm.file = null;
            dt.getRecentFiles().then(_populateRecentFiles);
          }, function(response) {
            if (vm.file.type === "folder") {
              if (response.status === 406) {// folder has open file
                _triggerError(13);
              }
              _triggerError(8);
            } else {
              if (response.status === 406) {// file is open
                _triggerError(14);
              }
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
        vm.folder.hasChildren = true;
        var folder = {};
        var name = _getFolderName();
        folder.parent = vm.folder.path;
        folder.path = vm.folder.path + (vm.folder.path.charAt(vm.folder.path.length - 1) === "/" ? "" : "/") + name;
        folder.name = name;
        folder.visible = vm.folder.open;
        folder.depth = vm.folder.depth + 1;
        folder.indent = folder.depth * 27;
        folder.new = true;
        folder.autoEdit = true;
        folder.type = "folder";
        folder.hasChildren = false;
        folder.children = [];
        vm.folder.children.splice(0, 0, folder);
        for (var i = 0; i < vm.folders.length; i++) {
          if (vm.folders[i].path === folder.parent) {
            var copy = angular.copy(folder);
            vm.folders.splice(i + 1, 0, copy);
            break;
          }
        }
      }
    }

    /**
     * Updates the directories with a new name for a file
     * @param {string} oldPath - String path of old directory path
     * @param {string} newPath - String path of new directory path
     * @param {string} newName - New file name
     */
    function updateDirectories(oldPath, newPath, newName) {
      for (var i = 0; i < vm.folders.length; i++) {
        if (vm.folders[i].path === oldPath) {
          vm.folders[i].name = newName;
        }
      }
      for (var j = 0; j < vm.folders.length; j++) {
        _updateDirectories(vm.folders[j], oldPath, newPath);
      }
    }

    /**
     * Update all child folder paths on parent rename
     *
     * @param {Object} folder - Folder Object
     * @param {String} oldPath - String path of old directory path
     * @param {String} newPath - String path of new directory path
     * @private
     */
    function _updateDirectories(folder, oldPath, newPath) {
      if (folder.path.indexOf(oldPath) === 0) {
        folder.path = folder.path.replace(oldPath, newPath);
        if ( folder.parent != null )  {
          folder.parent = folder.parent.replace(oldPath, newPath);
        }
      }
      for (var i = 0; i < folder.children.length; i++) {
        _updateDirectories(folder.children[i], oldPath, newPath);
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
     * @return {boolean} - true if duplicate, false otherwise
     * @private
     */
    function _isDuplicate() {
      if (vm.folder && vm.folder.children) {
        for (var i = 0; i < vm.folder.children.length; i++) {
          if (vm.fileToSave === vm.folder.children[i].name) {
            vm.file = vm.folder.children[i];
            return true;
          }
        }
      }
      return false;
    }

    /**
     * Gets the key up event from the app
     *
     * @param {Object} event - Event Object
     */
    function onKeyUp(event) {
      if (event.keyCode === 13 && event.target.id !== "searchBoxId") {
        if (vm.wrapperClass === "open") {
          if (vm.file !== null) {
            selectFile(vm.file);
          }
        } else {
          _save(false);
        }
        $scope.$apply();
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
      var retVal = vm.searchPlaceholder + " " + vm.selectedFolder;
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
  }

  return {
    name: "fileOpenSaveApp",
    options: options
  };
});
