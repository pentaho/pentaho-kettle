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
 * The addressbar component.
 *
 * This provides the component for the addressbar functionality.
 * @module components/addressbar/addressbar.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "text!./addressbar.html",
  "css!./addressbar.css"
], function (addressbarTemplate) {
  "use strict";

  var options = {
    bindings: {
      path: "<",
      onSelect: "&",
      onEnter: "&"
    },
    template: addressbarTemplate,
    controllerAs: "vm",
    controller: addressbarController
  };

  /**
   * The addressbar Controller.
   *
   * This provides the controller for the addressbar component.
   */
  function addressbarController() {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;
    vm.onBodyClick = onBodyClick;
    vm.onKeyUp = onKeyUp;
    vm.onClick = onClick;
    vm.onBlur = onBlur;
    vm.onNameClick = onNameClick;
    vm.onBackHistory = onBackHistory;
    vm.onForwardHistory = onForwardHistory;
    vm.onDropdownClick = onDropdownClick;
    vm.onRecentClick = onRecentClick;
    vm.onClickExtras = onClickExtras;
    vm.isShowHistory = false;

    vm.index = -1;
    vm.history = [];
    vm.recents = [];
    vm.parts = [];
    vm.extras = [];
    vm.state = "breadcrumb";
    vm.showExtras = false;

    /**
     * The $onInit} hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
    }

    /**
     * Called whenever one-way bindings are updated.
     *
     * @param {Object} changes - hash whose keys are the names of the bound properties
     * that have changed, and the values are an object of the form
     */
    function onChanges(changes) {
      if (changes.path && changes.path.currentValue != null) {
        var path = changes.path.currentValue;
        if (path.path) {
          vm.uri = path.uri;
          vm.type = path.type;
          vm.prefix = path.prefix;
          _updatePath(path.path, path.path.indexOf("/") === -1 || path.prefix);
          if (vm.history.length === 0 || (vm.history[vm.index] && path.path !== vm.history[vm.index].path.path)) {
            vm.history.splice(vm.index+1, vm.history.length);
            vm.history.push({
              timestamp: Date.now(),
              path: path
            });
            vm.index++;
          }
          if (path.fileType === "folder" && !_inRecents(path.path)) {
            vm.recents.push({
              timestamp: Date.now(),
              path: path
            });
          }
        }
      }
    }

    function onBodyClick() {
      vm.isShowHistory = false;
      vm.showExtras = false;
    }

    function onClickExtras(e) {
      e.stopPropagation();
      vm.showExtras=!vm.showExtras
    }

    function onNameClick(e, path) {
      e.stopPropagation();
      if (vm.prefix) {
        vm.onEnter({path: vm.prefix + path});
      } else {
        vm.onSelect({path: path})
      }
    }

    function onRecentClick(e, index, path) {
      e.stopPropagation();
      vm.index = index;
      if (path.prefix) {
        vm.onEnter({path: path.prefix + path.path});
      } else {
        vm.onSelect({path: path.path});
      }
      vm.isShowHistory = false;
    }

    function onDropdownClick(e) {
      e.stopPropagation();
      vm.isShowHistory = !vm.isShowHistory;
    }

    function onClick(e) {
      vm.state = "address";
    }

    function onBlur(e) {
      vm.uri = vm.path.uri;
      vm.state = "breadcrumb";
    }

    function onKeyUp(e) {
      // Handle enter key press
      if (e.keyCode === 13) {
        vm.onEnter({path: vm.uri});
        vm.state = "breadcrumb";
      }
      e.stopPropagation();
    }

    function _updatePath(path, showRoot) {
      var parts = path.split("/");
      var set = [];
      for (var i = 0; i < parts.length; i++) {
        set.push({path: parts.slice(0, i + 1).join("/"), name: parts[i]});
      }
      vm.type = showRoot ? null : vm.type;
      if (!showRoot) {
        set.splice(0, 1);
      }
      if (set.length > 3) {
        vm.parts = set.splice(set.length - 3, set.length);
        vm.extras = set;
      } else {
        vm.parts = set;
        vm.extras = [];
      }
    }

    function onBackHistory() {
      if (vm.index > 0) {
        vm.index--;
        var path = vm.history[vm.index].path;
        if (path.prefix) {
          vm.onEnter({path: path.prefix + path.path});
        } else {
          vm.onSelect({path: path.path});
        }
      }
    }

    function onForwardHistory() {
      if (vm.index < vm.history.length - 1) {
        vm.index++;
        var path = vm.history[vm.index].path;
        if (path.prefix) {
          vm.onEnter({path: path.prefix + path.path});
        } else {
          vm.onSelect({path: path.path});
        }
      }
    }

    function _inRecents(path) {
      for (var i = 0; i < vm.recents.length; i++) {
        if (vm.recents[i].path.path === path) {
          return true;
        }
      }
      return false;
    }
  }

  return {
    name: "addressbar",
    options: options
  };
});
