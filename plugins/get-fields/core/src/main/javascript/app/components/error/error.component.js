/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 Hitachi Vantara. All rights reserved.
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
 * The Get Fields Error component.
 *
 * This provides the component for the Get Fields Error Message dialogs.
 *
 * @module components/error/error.component
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
      errorType: "<"
    },
    template: errorTemplate,
    controllerAs: "vm",
    controller: errorController
  };

  /**
   * The Error Controller. This provides the controller for the error component.
   */
  function errorController() {
    var vm = this;
    vm.$onInit = onInit;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
    }

    /**
     * Calls the setMessage function according to the errorType.
     * errorType:
     * 1. Unable to make selection
     * 2. Resample data error
     *
     * @private
     */
    function _setMessages() {
      vm.breakAll = false;
      switch (vm.errorType) {
        case 1:
          break;
        case 2:
          break;
        default:
          break;
      }
    }
  }

  return {
    name: "error",
    options: options
  };
});
