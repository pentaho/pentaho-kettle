/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2017 Hitachi Vantara. All rights reserved.
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
 * The File Open and Save Breadcrumb component.
 *
 * This provides the component for the breadcrumb functionality.
 * @module components/breadcrumb/breadcrumb.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "text!./breadcrumb.html",
  "css!./breadcrumb.css"
], function(breadcrumbTemplate) {
  "use strict";

  var options = {
    bindings: {
      path: "<",
      includeRoot: "<",
      onSelect: "&"
    },
    template: breadcrumbTemplate,
    controllerAs: "vm",
    controller: breadcrumbController
  };

  /**
   * The Breadcrumb Controller.
   *
   * This provides the controller for the breadcrumb component.
   */
  function breadcrumbController() {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;
    vm.select = select;
    vm.maxWidths = [440, 0, 0];
    vm.titles = ["", "", ""];
    vm.parts = [];
    vm.extras = [];

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.showExtras = false;
    }

    /**
     * Called whenever one-way bindings are updated.
     *
     * @param {Object} changes - hash whose keys are the names of the bound properties
     * that have changed, and the values are an object of the form
     */
    function onChanges(changes) {
      if (changes.path) {
        var path = changes.path.currentValue;
        if (path) {
          _updatePath(path);
        }
      }
    }

    /**
     * Updates the breadcrumb path to display
     *
     * @param {String} path - current value of the directory path.
     * @private
     */
    function _updatePath(path) {
      if (path) {
        var parts = path.split("/");
        var set = [];
        if (vm.includeRoot && path !== "Recents") {
          set.push({path: "/", part: "/"});
        }
        if (path === "/" && vm.includeRoot === false) {
          set.push({path: "/", part: "/"});
        }
        for (var i = 0; i < parts.length; i++) {
          if (parts[i] !== "") {
            set.push({path: parts.slice(0, i + 1).join("/"), part: parts[i]});
          }
        }
        if (set.length > 3) {
          vm.parts = set.splice(set.length - 3, set.length);
          vm.extras = set;
          vm.extras[0].part = vm.extras[0].part.charAt(0).toUpperCase() + vm.extras[0].part.slice(1);
        } else {
          vm.parts = set;
          vm.extras = [];
          vm.parts[0].part = vm.parts[0].part.charAt(0).toUpperCase() + vm.parts[0].part.slice(1);
        }
      }
    }

    /**
     * Calls two-way binding to parent component (app) to go to the selected breadcrumb file path.
     *
     * @param {String} path - The breadcrumb path to go to.
     */
    function select(path) {
      vm.showExtras = false;
      vm.onSelect({selectedPath: path});
    }
  }

  return {
    name: "breadcrumb",
    options: options
  };
});
