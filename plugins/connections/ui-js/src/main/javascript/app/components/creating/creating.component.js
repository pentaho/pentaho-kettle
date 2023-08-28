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
  'text!./creating.html',
  'pentaho/i18n-osgi!connections.messages',
  'css!./creating.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: creatingController
  };

  creatingController.$inject = ["$state", "$timeout", "$stateParams", "dataService"];

  function creatingController($state, $timeout, $stateParams, dataService) {
    var vm = this;
    vm.$onInit = onInit;

    function onInit() {
      vm.data = $stateParams.data;

      vm.almostDone = i18n.get('connections.creating.almostdone.label');
      vm.message = vm.data.isSaved === true ? i18n.get('connections.updating.message') : i18n.get('connections.creating.message');
      $timeout(function() {
        dataService.testConnection(vm.data.model).then(function (response) {
          dataService.createConnection(vm.data.model, vm.data.name).then(function () {
            vm.data.name = vm.data.model.name;
            $state.go("success", {data: vm.data});
          });
        }, function (response) {
          dataService.createConnection(vm.data.model, vm.data.name).then(function () {
            vm.data.name = vm.data.model.name;
            $state.go("failure", {data: vm.data});
          });
        });
      }, 1000);
    }
  }

  return {
    name: "creating",
    options: options
  };

});
