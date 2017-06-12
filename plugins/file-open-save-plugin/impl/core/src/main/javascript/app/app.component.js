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
 * The File Open and Save Main component.
 *
 * This provides the main component for supporting the file open and save functionality.
 **/
define([
  "./services/data.service",
  "text!./app.html",
  'pentaho/i18n-osgi!file-open-save.messages',
  "css!./app.css"
], function(dataService, template, i18n) {
  "use strict";

  var options = {
    bindings: {},
    template: template,
    controllerAs: "vm",
    controller: appController
  };

  appController.$inject = [dataService.name, "$location"];

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

    vm.selectedFolder = "";
    vm.fileToSave = "";
    vm.searchString = "";

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
      dt.getDirectoryTree().then(populateTree);
      dt.getRecentFiles().then(populateRecentFiles);

      function populateTree(response) {
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

      function populateRecentFiles(response) {
        vm.recentFiles = response.data;
      }

      var state = $location.search().state;
      if (state) {
        vm.setState(state);
      }
    }

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

    function selectFolder(folder) {
      vm.file = null;
      if (folder) {
        vm.showRecents = false;
        vm.folder = folder;
        vm.selectedFolder = folder.name;
      } else {
        vm.showRecents = true;
        vm.selectedFolder = "Recents";
        vm.folder = {name: "Recents", path: "Recents"};
      }
    }

    function selectFolderByPath(path) {
      for (var i = 0; i < vm.folders.length; i++) {
        if (vm.folders[i].path === path) {
          selectFolder(vm.folders[i]);
        }
      }
    }

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

    function doSearch() {
      if (vm.showRecents === true) {
        filter(vm.recentFiles, vm.searchString);
      } else {
        filter(vm.folder.children, vm.searchString);
      }
    }

    function resetSearch() {
      vm.searchString = '';
      vm.doSearch();
    }

    function filter(elements, value) {
      if (elements) {
        for (var i = 0; i < elements.length; i++) {
          var name = elements[i].name.toLowerCase();
          elements[i].inResult = name.indexOf(value.toLowerCase()) !== -1;
          if(elements[i].children.length > 0) {
            filter(elements[i].children, value);
          }
        }
      }
    }

    function highlightFile(file) {
      vm.file = file;
    }

    function openOrSave() {
      if (vm.file) {
        if (vm.file.type === "File folder") {
          selectFolder(vm.file);
        } else {
          if (vm.wrapperClass === "open") {
            open();
          } else {
            save();
          }
          close();
        }
      }
    }

    function open() {
      dt.openFile(vm.file.objectId.id, vm.file.type);
    }

    function save() {
    }

    function cancel() {
      close();
    }

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
          for ( var i = 0; i < vm.folder.children.length; i++ ) {
            if (vm.folder.children[i].type === "File folder") {
              hasChildFolders = true;
            }
          }
          vm.folder.hasChildren = hasChildFolders;
        });
      }
    }

    function addFolder() {
      if (vm.selectedFolder !== "Recents") {
        dt.create(vm.folder.path, getFolderName()).then(function(response) {
          vm.folder.hasChildren = true;
          var folder = response.data;
          folder.visible = vm.folder.open;
          folder.depth = vm.folder.depth+1;
          folder.autoEdit = true;
          folder.type = "File folder";
          vm.folder.children.splice(0, 0, folder);
          for (var i = 0; i < vm.folders.length; i++) {
            if (vm.folders[i].path === folder.parent) {
              vm.folders.splice(i+1, 0, angular.copy(folder));
              break;
            }
          }
        });
      }
    }

    function getFolderName() {
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
