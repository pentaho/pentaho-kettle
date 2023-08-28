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

define([
  'text!./controls.html',
  'pentaho/i18n-osgi!connections.messages',
  'css!./controls.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {
      buttons: "<",
      data: "<"
    },
    controllerAs: "vm",
    template: template,
    controller: controlsController
  };

  controlsController.$inject = ["$state"];

  function controlsController($state) {
    var vm = this;
    vm.$onInit = onInit;
    vm.getRightButtons = getRightButtons;
    vm.getMiddleButtons = getMiddleButtons;

    function onInit() {

    }

    function getButtonsByPosition(position) {
      var buttons = [];
      if (vm.buttons) {
        for (var i = 0; i < vm.buttons.length; i++) {
          if (vm.buttons[i].position === position) {
            buttons.push(vm.buttons[i]);
          }
        }
      }
      return buttons;
    }

    function getRightButtons() {
      return getButtonsByPosition("right");
    }

    function getMiddleButtons() {
      return getButtonsByPosition("middle");
    }
  }

  return {
    name: "controls",
    options: options
  };

});
