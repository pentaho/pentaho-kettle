/*!
 * Copyright 2020 Hitachi Vantara. All rights reserved.
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
 * The File Open and Save Files component.
 *
 * This is a file list component.
 * @module components/files/files.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "../../services/clipboard.service",
  "../../services/providers/fileutil",
  "../utils",
  "text!./files.html",
  "pentaho/i18n-osgi!file-open-save-new.messages",
  "css!./files.css"
], function (clipboardService, fileUtils, utils, filesTemplate, i18n) {
  "use strict";

  var options = {
    bindings: {
      files: "<",
      folder: "=",
      filter: "<",
      loading: "<",
      onSelectFile: "&",
      onHighlight: "&",
      onDelete: "&",
      onRename: "&",
      onMove: "&",
      onCopy: "&",
      onAddFolder: "&",
      onCreateFolder: "&",
      selectedFiles: "<",
      showAdditional: "<"
    },
    template: filesTemplate,
    controllerAs: "vm",
    controller: filesController
  };

  filesController.$inject = [clipboardService.name, "$timeout", "$filter", "$document", "$scope", "$state"];

  /**
   * The Files Controller.
   *
   * This provides the controller for the files component.
   *
   * @param {Function} $timeout - Angular wrapper for window.setTimeout.
   * @param $filter
   */
  function filesController(clipboardService, $timeout, $filter, $document, $scope, $state) {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;
    vm._name = "name";
    vm._type = "type";
    vm._date = "date";
    vm.fileFilter = fileFilter;
    vm.selectFile = selectFile;
    vm.commitFile = commitFile;
    vm.rename = rename;
    vm.sortFiles = sortFiles;
    vm.compareFiles = compareFiles;
    vm.onStart = onStart;
    vm.getIcon = getIcon;
    vm.onDrop = onDrop;
    vm.onDeleteClick = onDeleteClick;
    vm.onRenameClick = onRenameClick;
    vm.onClick = onClick;
    vm.onRightClick = onRightClick;
    vm.showFileContext = showFileContext;
    vm.showFolderContext = showFolderContext;
    vm.isSelected = isSelected;
    vm.isHighlighted = isHighlighted;
    vm.onCopyStart = onCopyStart;
    vm.onCut = onCut;
    vm.onPaste = onPaste;
    vm.canPaste = canPaste;
    vm.onPasteFolder = onPasteFolder;
    vm.onAddFolderClick = onAddFolderClick;
    vm.onBodyClick = onBodyClick;
    vm.errorType = 0;
    vm.onKeyDown = onKeyDown;
    vm.onKeyUp = onKeyUp;
    vm.state = $state;
    vm.highlighted = [];
    var targetFiles = [];

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
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
      $timeout(function () {
        vm.selectedFile = null;
      });
    }

    function onBodyClick(e, id) {
      var parentNode = e.target.parentNode;
      var found = false;
      while (parentNode) {
        if (parentNode.id === id) {
          found = true;
          break;
        }
        parentNode = parentNode.parentNode;
      }
      if (!found) {
        vm.highlighted = [];
      }
    }

    function onKeyDown(event) {
      if (event.target.tagName !== "INPUT") {
        var ctrlKey = event.metaKey || event.ctrlKey;
        if (event.keyCode === 27) {
          vm.highlighted = [];
        }
        if (event.keyCode === 67 && ctrlKey) {
          targetFiles = vm.selectedFiles;
          clipboardService.set(targetFiles, "copy");
        }
        if (event.keyCode === 88 && ctrlKey) {
          targetFiles = vm.selectedFiles;
          clipboardService.set(targetFiles, "cut");
        }
        if (event.keyCode === 86 && ctrlKey) {
          vm.onPasteFolder();
        }
        if (event.keyCode === 65 && ctrlKey) {
          if (event.target.tagName !== "INPUT") {
            selectAll();
          }
        }
      }
    }

    function onKeyUp(event) {
      if (event.keyCode === 8 && event.target.tagName !== "INPUT") {
        vm.onDelete({files: vm.selectedFiles});
      }
    }

    function getIcon(file) {
      return 'url(\'./img/file_icons/' + getExtension(file) + '.svg\')';
    }

    var types = [];
    types['kjb'] = "Job.S_D";
    types['ktr'] = "Transformation.S_D";
    types['jpg'] = "Picture.S_D";
    types['png'] = "Picture.S_D";
    types['gif'] = "Picture.S_D";
    types['txt'] = "Report.S_D";
    types['csv'] = "Report.S_D";
    types['json'] = "Report.S_D";
    types['xml'] = "Report.S_D";

    /**
     * Gets the file extension
     * @param file
     * @returns {*}
     */
    function getExtension(file) {
      if (file.type === "folder") {
        return vm.isSelected(file) && !vm.isHighlighted(file) ? "Archive.S_D_white" : "Archive.S_D";
      }
      var index = file.path.lastIndexOf(".");
      var extension = file.path.substr(index + 1, file.path.length);
      var type = types[extension.toLowerCase()];
      var icon = type ? type : "Doc.S_D";
      icon += vm.isSelected(file) && !vm.isHighlighted(file) ? "_white" : "";
      return icon;
    }

    /**
     * Calls two-way binding to parent component (app) to open file if not editing.
     *
     * @param {Object} e - Click event
     * @param {Object} file - file object
     */
    function commitFile(e, file) {
      e.preventDefault();
      e.stopPropagation();
      if (file.editing !== true) {
        vm.onSelectFile({file: file});
      }
    }

    /**
     * Calls two-way binding to parent component (app) to select file.
     *
     * @param {Object} file - file object
     * @param {Boolean} shift - whether or not shift key is pressed
     * @param {Boolean} ctrl - whether or not ctrl/command key is pressed
     */
    function selectFile(file, shift, ctrl) {
      vm.highlighted = [];
      if (ctrl) {
        var index = vm.selectedFiles.indexOf(file);
        if (index === -1) {
          vm.selectedFiles.push(file);
        } else {
          vm.selectedFiles.splice(index, 1);
        }
      } else if (shift) {
        var sorted = $filter('orderBy')(vm.files, null, vm.sortReverse, compareFiles);
        var startIndex = 0;
        if (vm.selectedFiles.length > 0) {
          startIndex = sorted.indexOf(vm.selectedFiles[0]);
        }
        var endIndex = sorted.indexOf(file);
        for (var i = startIndex; i !== endIndex + (startIndex < endIndex ? 1 : -1); i += (startIndex < endIndex ? 1 : -1)) {
          if (vm.selectedFiles.indexOf(sorted[i]) === -1) {
            vm.selectedFiles.push(sorted[i]);
          }
        }
      } else {
        if (vm.selectedFiles.length === 0 || vm.selectedFiles.length > 1 || ( vm.selectedFiles.length === 1 && vm.selectedFiles.indexOf(file) === -1 )) {
          vm.selectedFiles = [];
          if (file) {
            vm.selectedFiles.push(file);
          }
        }
      }
      vm.onHighlight({selectedFiles: vm.selectedFiles});
    }

    /**
     * Rename the selected file.
     *
     * @param {Object} file - File Object
     * @param {String} current - Current file
     * @param {String} previous - Previous file
     */
    function rename(file, current, previous) {
      if (current) {
        if (file.new) {
          _createFolder(file, current);
        } else {
          _renameFile(file, current, previous);
        }
      }
    }

    /**
     * Create a new folder
     *
     * @param {Object} folder - Folder Object
     * @param {String} current - Current file
     * @private
     */
    function _createFolder(folder, current) {
      folder.name = current;
      folder.path = fileUtils.replaceFilename(folder.path, current);

      vm.onCreateFolder({
        folder: folder
      }).then(function () {
        // Do nothing
      }, function (status) {
        if (status === "FILE_COLLISION") {
          folder.editing = true;
        } else {
          var index = vm.folder.children.indexOf(folder);
          if (index !== -1) {
            vm.folder.children.splice(index, 1);
          }
        }
      });
    }

    /**
     * Rename an existing file/folder
     *
     * @param {Object} file - File Object
     * @param {String} current - Current file
     * @param {String} previous - Previous file
     * @private
     */
    function _renameFile(file, current, previous) {
      var newPath = fileUtils.replaceFilename(file.path, current);
      file.name = current;
      vm.onRename({
        file: file,
        newPath: newPath
      }).then(function () {
        // Do nothing
      }, function () {
        file.name = previous;
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
      if (file.new === true) {
        selectFile(file, null, null);
      }
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


    /**
     * Handler for when a drop action occurs on a folder
     * @param {array} from - File objects to move
     * @param {file} to - Directory to where the file objects will be moved
     */
    function onDrop(from, to) {
      vm.onMove({
        from: from,
        to: to
      });
    }

    /**
     * Handler for context menu rename selection
     */
    function onRenameClick() {
      targetFiles[0].editing = true;
      $timeout(function() {
        selectFile(targetFiles[0]);
      });
    }

    /**
     * Handler for context menu delete selection
     */
    function onDeleteClick() {
      vm.onDelete({files: targetFiles});
    }


    /**
     * Handler for when a file is clicked on
     * @param {Event} e - Mouse click event
     * @param {File} file - File object that was clicked
     */
    function onClick(e, file) {
      selectFile(file, e.shiftKey, e.ctrlKey || e.metaKey);
    }

    /**
     * Handler for when file is right-clicked
     * @param {Event} e - Mouse click event
     * @param {File} file - File object that was clicked
     */
    function onRightClick(e, file) {
      if (!vm.isSelected(file)) {
        vm.selectedFiles = [];
      }
      if (file === null) {
        targetFiles = [vm.folder];
      } else {
        if (vm.selectedFiles.length === 0 || (vm.selectedFiles.length > 0 && !vm.isSelected(file))) {
          targetFiles = [file];
        } else {
          targetFiles = vm.selectedFiles;
        }
      }
      vm.highlighted = targetFiles;
    }

    /**
     * Determines if the context menu should be shown for File/Folder.
     * @returns {boolean}
     */
    function showFileContext() {
      if (vm.highlighted && vm.highlighted.length > 0) {
        for (var i = 0; i < vm.highlighted.length; i++) {
          if (!vm.highlighted[i].canEdit) {
            return false;
          }
        }
      }
      return true;
    }

    /**
     * Determines if the context menu should be shown for "Add Folder" context.
     * @returns {boolean}
     */
    function showFolderContext() {
      if (vm.folder) {
        return vm.folder.canEdit || vm.folder.canAddChildren;
      }
      return false;
    }

    /**
     * Determine if a file is selected
     * @param {File} file - File object to check
     * @returns {boolean} - File is selected
     */
    function isSelected(file) {
      return vm.selectedFiles.indexOf(file) !== -1;
    }

    function isHighlighted(file) {
      return vm.highlighted.indexOf(file) !== -1;
    }

    /**
     *
     */
    function onCopyStart() {
      clipboardService.set(targetFiles, "copy");
    }

    /**
     *
     */
    function onCut() {
      clipboardService.set(targetFiles, "cut");
    }

    /**
     * Handle pasting of files into a folder
     * @param folder - the folder in which to paste the files
     */
    function onPaste(folder) {
      if (clipboardService.operation === "copy") {
        vm.onCopy({
          from: clipboardService.get(),
          to: folder ? folder : targetFiles[0]
        });
      }
      if (clipboardService.operation === "cut") {
        vm.onMove({
          from: clipboardService.get(),
          to: folder ? folder : targetFiles[0]
        });
      }
      clipboardService.set(null, null);
    }

    /**
     *
     */
    function onPasteFolder() {
      onPaste(vm.folder);
    }

    /**
     *
     */
    function onAddFolderClick() {
      vm.onAddFolder();
    }

    function fileFilter(file) {

      if (vm.state.is('selectFolder')) {
        return file.type === "folder"
      }

      if (!vm.filter) {
        return true;
      }
      return file.type === "folder"
          || vm.filter === "*"
          || file.path.toLowerCase().match(vm.filter.toLowerCase());
    }

    /**
     *
     * @returns {boolean}
     */
    function canPaste() {
      return targetFiles.length === 1 && targetFiles[0].type === "folder" && clipboardService.get();
    }

    function selectAll() {
      $scope.$apply(function() {
        vm.selectedFiles = [];
        for (var i = 0; i < vm.files.length; i++) {
          vm.selectedFiles.push(vm.files[i]);
        }
        vm.onHighlight({selectedFiles: vm.selectedFiles});
      });
    }
  }

  return {
    name: "files",
    options: options
  };
});
