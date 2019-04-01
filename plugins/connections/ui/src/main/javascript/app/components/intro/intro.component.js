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
  'text!./intro.html',
  'pentaho/i18n-osgi!connections.messages',
  'css!./intro.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: introController
  };

  introController.$inject = ["$location", "$state", "$q", "$stateParams", "dataService", "vfsTypes", "$timeout"];

  function introController($location, $state, $q, $stateParams, dataService, vfsTypes, $timeout) {
    var vm = this;
    vm.$onInit = onInit;
    vm.canNext = canNext;
    vm.onSelect = onSelect;
    vm.validateName = validateName;
    vm.resetErrorMsg = resetErrorMsg;
    vm.type = null;

    function onInit() {
      vm.connectionName = i18n.get('connections.intro.connectionName');
      vm.connectionType = i18n.get('connections.intro.connectionType')
      vm.connectionDescription = i18n.get('connections.intro.connectionDescription');
      vm.next = "/";

      if ($stateParams.data) {
        vm.data = $stateParams.data;
        vm.title = vm.data.state === "edit" ? i18n.get('connections.intro.edit.label') : i18n.get('connections.intro.new.label');
        vm.type = vm.data.model.type;
        vm.next = vm.data.model.type + "step1";
      } else {
        vm.title = i18n.get('connections.intro.new.label');
        vm.data = {
          model: {
            name: "",
            description: ""
          }
        };
        vm.data.state = "new";
      }
      var connection = $location.search().connection;
      vm.connectionTypes = vfsTypes;
      if (vm.data.type) {
        vm.type = vm.data.type.value;
      }
      if (connection) {
        vm.title = i18n.get('connections.intro.edit.label');
        dataService.getConnection(connection).then(function (res) {
          var model = res.data;
          vm.type = model.type;
          vm.data.model = model;
          vm.next = vm.data.model.type + "step1";
          vm.data.state = "edit";
          vm.data.isSaved = true;
        });
      }
    }

    function resetErrorMsg() {
      vm.data.state = "new";
      vm.title = i18n.get('connections.intro.new.label');
      vm.errorMessage = null;
    }

    function onSelect(option) {
      if (!vm.data.model || vm.data.model.type !== option.value) {
        dataService.getFields(option.value).then(function (res) {
          var name = vm.data.model.name;
          var description = vm.data.model.description;
          vm.data.model = res.data;
          vm.data.model.name = name;
          vm.data.model.description = description;
          vm.next = vm.data.model.type + "step1";
          vm.data.state = "new";
          vm.data.isSaved = false;
        });
      }
    }

    function validateName() {
      return $q(function(resolve, reject) {
        if (vm.data.state === "edit" || vm.data.isSaved) {
          resolve(true);
        } else {
          dataService.exists(vm.data.model.name).then(function (res) {
            console.log(res);
            var isValid = !res.data;
            if (!isValid) {
              vm.errorMessage = {
                type: "error",
                text: i18n.get('connections.intro.name.error', {
                  name: vm.data.model.name
                })
              }
            }
            resolve(isValid);
          });
        }
      });
    }

    function canNext() {
      return vm.data.model && vm.data.model.type && vm.data.model.name;
    }
  }

  return {
    name: "intro",
    options: options
  };

});
