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
 **/
define([
  "../../services/data.service",
  "text!./files.html",
  "css!./files.css"
], function(dataService, filesTemplate) {
  "use strict";

  var options = {
    bindings: {
      folder: '<',
      search: '<',
      onClick: '&',
      onSelect: '&'
    },
    template: filesTemplate,
    controllerAs: "vm",
    controller: filesController
  };

  filesController.$inject = ["$timeout", dataService.name, "$scope"];

  function filesController($timeout, dt, $scope) {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;
    vm.selectFile = selectFile;
    vm.commitFile = commitFile;
    vm.rename = rename;

    function onInit() {
      vm.nameHeader = "Name";
      vm.typeHeader = "Type";
      vm.lastSaveHeader = "Last saved";
    }

    function onChanges(changes) {
      if (changes.folder) {
        vm.selectedFile = null;
      }
    }

    function commitFile(file) {
      if (file.editing !== true) {
        vm.onClick({file:file});
      }
    }

    function selectFile(file) {
      vm.selectedFile = file;
      vm.onSelect({selectedFile:file});
    }

    function rename() {
      var path = vm.selectedFile.type === "File folder" ? vm.selectedFile.parent : vm.selectedFile.path;
      dt.rename(vm.selectedFile.objectId.id, vm.selectedFile.name, path, vm.selectedFile.type).then(function(response) {
        vm.selectedFile.objectId = response.data;
      });
    }
  }

  return {
    name: "files",
    options: options
  };
});
