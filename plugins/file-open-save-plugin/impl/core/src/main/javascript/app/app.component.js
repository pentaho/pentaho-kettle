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
  "text!./app.html",
  "css!./app.css"
], function(template) {
  "use strict";

  var options = {
    bindings: {

    },
    template: template,
    controllerAs: "vm",
    controller: appController
  };

  function appController() {
    var vm = this;
    vm.$onInit = onInit;

    function onInit() {
      vm.wrapperClass = "open";
      vm.headerTitle = "Save";//i18n.get("file-open-save-plugin.app.header.save.title");
      vm.searchPlaceholder = "Search";//i18n.get("file-open-save-plugin.app.header.search.placeholder");
      vm.selectedFolder = "MyFolder";//i18n.get("file-open-save-plugin.app.header.save.title");
      vm.confirmButton = "Save";//i18n.get("file-open-save-plugin.app.save.button");
      vm.cancelButton = "Cancel";//i18n.get("file-open-save-plugin.app.cancel.button");
      vm.saveFileNameLabel = "File name";//i18n.get("file-open-save-plugin.app.save.file-name.label");
    }
  }

  return {
    name: "fileOpenSaveApp",
    options: options
  };
});
