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
 * The File Open and Save Error Message Dialog component.
 *
 * This provides the component for the Error Message dialogs.
 *
 * @module app/common/error/error.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 */
define([
  "text!./error.html",
  "css!./error.css"
], function(errorTemplate) {
  "use strict";

  var options = {
    bindings: {
    },
    template: errorTemplate,
    controllerAs: "vm",
    controller: errorController
  };

  function errorController() {
    var vm = this;
    vm.$onInit = onInit;

    function onInit() {
      vm.title = "Title";
      vm.topMessageBefore = "Confirm delete of ";
      vm.topMessageMiddle = "transformation 1 ";
      vm.topMessageAfter = "file?";
      vm.bottomMessage = "Do you want to delete it?";
      vm.confirmButton = "Yes, delete";
      vm.cancelButton = "Cancel";
    }
  }

  return {
    name: "error",
    options: options
  };
});
