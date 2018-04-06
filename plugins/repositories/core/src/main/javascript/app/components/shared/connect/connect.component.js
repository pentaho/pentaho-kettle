/*!
 * Copyright 2018 Hitachi Vantara. All rights reserved.
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
  'text!./connect.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./connect.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {
      connection: "<"
    },
    controllerAs: "vm",
    template: template,
    controller: connectController
  };

  connectController.$inject = ["connectService", "$stateParams", "$state"];

  function connectController(connectService, $stateParams, $state) {
    var vm = this;
    vm.$onInit = onInit;
    vm.canConnect = canConnect;
    vm.connect = connect;
    vm.credentials = {
      username: "",
      password: ""
    };

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.connectHeader = i18n.get('repositories.connect.header');
      vm.connectToLabel = i18n.get('repositories.connect.connectto.label');
      vm.usernameLabel = i18n.get('repositories.connect.username.label');
      vm.passwordLabel = i18n.get('repositories.connect.password.label');
      vm.connectLabel = i18n.get('repositories.connect.connect.label');

      if ($stateParams.error) {
        vm.errorMessage = $stateParams.error;
      }

      if ($stateParams.connection) {
        vm.connection = $stateParams.connection;
      } else if ($stateParams.repo) {
        connectService.getRepository($stateParams.repo).then(function(res) {
          if (res.data) {
            vm.connection = res.data;
          }
        });
      }

      if ($stateParams.credentials) {
        vm.credentials = $stateParams.credentials;
      }
    }

    function canConnect() {
      return vm.credentials.username && vm.credentials.password;
    }

    function connect(credentials) {
      credentials.repositoryName = vm.connection.displayName;
      $state.go("connecting", {credentials:credentials, connection:vm.connection});
    }
  }

  return {
    name: "connect",
    options: options
  };

});
