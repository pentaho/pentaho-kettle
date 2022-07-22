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
  'text!./connecting.html',
  'pentaho/i18n-osgi!repositories-plugin.messages'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: connectingController
  };

  connectingController.$inject = ["connectService", "$stateParams", "$state", "$timeout"];

  function connectingController(connectService, $stateParams, $state, $timeout) {
    var vm = this;
    vm.$onInit = onInit;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      var credentials = $stateParams.credentials;
      var connection = $stateParams.connection;
      vm.message =  i18n.get('repositories.loading.connectingYouTo.label') + " " + connection.displayName + ".";
      $timeout(function() {
        connectService.login(credentials).then(function(res) {
          closeWindow();
        }, function(err) {
          $state.go("connect", {
            error: vm.errorMessage = {timestamp: Date.now(), message: err.data.message},
            connection:$stateParams.connection,
            credentials:$stateParams.credentials
          });
        });
      }, 1000);
    }
  }

  return {
    name: "connecting",
    options: options
  };

});
