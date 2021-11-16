/*!
 * Copyright 2019-2021 Hitachi Vantara. All rights reserved.
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
  'text!./selectbox.html',
  'css!./selectbox.css'
], function (template) {

  'use strict';

  var options = {
    bindings: {
      options: "<",
      value: "<",
      onSelect: "&"
    },
    controllerAs: "vm",
    template: template,
    controller: selectboxController
  };

  selectboxController.$inject = ["$document", "$scope"];

  function selectboxController($document, $scope) {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;
    vm.selectOption = selectOption;
    vm.toggleOptions = toggleOptions;
    vm.onBodyClick = onBodyClick;
    vm.isShowOptions = false;
    vm.selectedValue = null;

    function onInit() {

    }

    function onChanges(changes) {
      if (changes.value && changes.value.currentValue !== null && vm.options) {
        for (var i = 0; i < vm.options.length; i++) {
          if (vm.options[i].value === changes.value.currentValue) {
            selectOption(vm.options[i]);
          }
        }
      }
    }

    function onBodyClick() {
      $scope.$apply(function() {
        vm.isShowOptions = ( IS_RUNNING_ON_WEBSPOON_MODE ) ? !vm.isShowOptions : false;
      });

    }

    function toggleOptions($event) {
      $event.stopPropagation();
      vm.isShowOptions = !vm.isShowOptions;
      setTimeout(function() {
        var index = vm.options.indexOf(vm.selectedValue);
        angular.element(document.querySelector("#select-options")).scrollTop(index*30);
      });
    }

    function selectOption(option) {
      vm.selectedValue = option;
      vm.onSelect({value: option});
      if ( IS_RUNNING_ON_WEBSPOON_MODE ) {
        $scope.$apply(function () {
            vm.isShowOptions = !vm.isShowOptions;
        });
      } else {
        vm.isShowOptions = false;
      }
    }
  }

  return {
    name: "selectbox",
    options: options
  };

});
