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
      filePath: "<",
      includeRoot: "<",
      root: "<",
      onSelect: "&",
      type: "<"
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
    vm.onKeyDown = onKeyDown;
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
        } else {
          vm.parts = set;
          vm.extras = [];
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

    function onKeyDown() {
      console.log("onKeyDown");
    }
  }

  return {
    name: "breadcrumb",
    options: options
  };
});
