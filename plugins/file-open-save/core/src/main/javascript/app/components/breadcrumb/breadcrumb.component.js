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
 * The File Open and Save Breadcrumb component.
 *
 * This provides the component for the breadcrumb functionality.
 * @module components/breadcrumb/breadcrumb.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "text!./breadcrumb.html",
  "../utils",
  "css!./breadcrumb.css"
], function(breadcrumbTemplate, utils) {
  "use strict";

  var options = {
    bindings: {
      path: '<',
      includeRoot: '<',
      onSelect: '&'
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
    var _font = "14px OpenSansRegular";
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
          _updateMaxWidths();
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

    /**
     * Calculates and sets the max width for 1, 2, or all 3 breadcrumb items.
     * Also, sets the tooltips if a breadcrumb is truncated.
     * @private
     */
    function _updateMaxWidths() {
      var extras = vm.extras.length > 0;
      var f1Width = 0;
      var f2Width = 0;
      var f3Width = 0;
      var mw0 = extras ? 402 : 440;
      var mw1 = 0;
      var mw2 = 0;
      var mw3 = 0;
      var title1 = "";
      var title2 = "";
      var title3 = "";
      switch (vm.parts.length) {
        case 1:
          mw1 = mw0;
          title1 = utils.getTextWidth(vm.parts[0].part, _font) > mw1 ? vm.parts[0].part : "";
          break;
        case 2:
          mw0 = extras ? 187 : 206;
          mw1 = mw0;
          mw2 = mw0;
          f1Width = utils.getTextWidth(vm.parts[0].part, _font);
          f2Width = utils.getTextWidth(vm.parts[1].part, _font);
          if (f1Width < mw0) {
            mw1 = f1Width;
            mw2 = mw0 + mw0 - mw1;
            title1 = false;
          }
          title1 = f1Width > mw1 ? vm.parts[0].part : "";
          title2 = f2Width > mw2 ? vm.parts[1].part : "";
          break;
        case 3:
          mw0 = extras ? 116 : 128;
          mw1 = mw0;
          mw2 = mw0;
          mw3 = mw0;
          f1Width = utils.getTextWidth(vm.parts[0].part, _font);
          f2Width = utils.getTextWidth(vm.parts[1].part, _font);
          f3Width = utils.getTextWidth(vm.parts[2].part, _font);
          if (f1Width < mw0) {
            mw1 = f1Width;
            mw2 = mw0 + (mw0 - mw1) / 2;
            mw3 = mw2;
          }
          if (f2Width < mw2) {
            mw2 = f2Width;
            mw3 += (mw3 - mw2);
          }
          title1 = f1Width > mw1 ? vm.parts[0].part : "";
          title2 = f2Width > mw2 ? vm.parts[1].part : "";
          title3 = f3Width > mw3 ? vm.parts[2].part : "";
          break;
        default:
          break;
      }
      vm.maxWidths = [mw1, mw2, mw3];
      vm.titles = [title1, title2, title3];
    }
  }

  return {
    name: "breadcrumb",
    options: options
  };
});
