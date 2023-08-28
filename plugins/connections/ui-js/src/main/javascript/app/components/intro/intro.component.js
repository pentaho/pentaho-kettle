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
    vm.onSelect = onSelect;
    vm.validateName = validateName;
    vm.resetErrorMsg = resetErrorMsg;
    vm.checkConnectionName = checkConnectionName;
    vm.type = null;
    vm.name = "";
    var loaded = false;

    function onInit() {
      vm.connectionName = i18n.get('connections.intro.connectionName');
      vm.connectionType = i18n.get('connections.intro.connectionType')
      vm.connectionDescription = i18n.get('connections.intro.connectionDescription');
      vm.next = "/";

      if ($stateParams.data) {
        vm.data = $stateParams.data;
        vm.title = vm.data.isSaved === true
            ? i18n.get('connections.intro.edit.label')
            : i18n.get('connections.intro.new.label');
        vm.name = vm.data.model.name;
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
          loaded = true;
          if (res.data !== "") {
            setDialogTitle(vm.title);
            var model = res.data;
            vm.type = model.type;
            vm.data.model = model;
            vm.next = vm.data.model.type + "step1";
            vm.data.state = "edit";
            vm.data.isSaved = true;
            vm.name = vm.data.model.name;
          } else {
            vm.title = i18n.get('connections.intro.new.label');
            setDialogTitle(vm.title);
          }
        });
      } else {
        loaded = true;
      }
      setDialogTitle(vm.title);

      vm.buttons = getButtons();
    }

    function resetErrorMsg() {
      if (!vm.data.isSaved) {
        vm.data.state = "new";
        vm.title = i18n.get('connections.intro.new.label');
        setDialogTitle(vm.title);
      }
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

    function checkConnectionName() {
      vm.resetErrorMsg();
      vm.name = vm.name.replace(/[^\w\s]/g,'');
    }

    function validateName() {
      return $q(function(resolve, reject) {
        if (vm.data.state === "edit" || vm.data.isSaved) {
          if (vm.name !== vm.data.model.name) {
            checkName(vm.name).then(function() {
              vm.data.name = vm.data.model.name;
              vm.data.model.name = vm.name;
              resolve();
            }, function() {
              reject();
            });
          } else {
            resolve();
          }
        } else {
          checkName(vm.name).then(function() {
            vm.data.model.name = vm.name;
            resolve();
          }, function() {
            reject();
          });
        }
      });
    }

    function checkName(name) {
      return $q(function(resolve, reject) {
        dataService.exists(name).then(function (res) {
          var isValid = !res.data;
          if (isValid) {
            resolve();
          } else {
            vm.errorMessage = {
              type: "error",
              text: i18n.get('connections.intro.name.error', {
                name: name
              })
            };
            reject();
          }
        });
      });
    }

    function setDialogTitle(title) {
      if (loaded === true) {
        try {
          setTitle(title);
        } catch (e) {
          console.log(title);
        }
      }
    }

    function getButtons() {
      return [{
            label: vm.data.state === "modify" ? i18n.get('connections.controls.applyLabel') : i18n.get('connections.controls.nextLabel'),
            class: "primary",
            isDisabled: function() {
              return !vm.data.model || !vm.data.model.type || !vm.name;
            },
            position: "right",
            onClick: function() {
              validateName().then(function() {
                $state.go(vm.data.state === "modify" ? 'summary' : vm.next, {data: vm.data, transition: "slideLeft"});
              });
            }
          }];
    }
  }

  return {
    name: "intro",
    options: options
  };

});
