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
  'text!./step1.html',
  'pentaho/i18n-osgi!connections.messages',
  'css!./step1.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: step1Controller
  };

  step1Controller.$inject = ["$state", "$stateParams", "$templateCache", "dataService"];

  function step1Controller($state, $stateParams, $templateCache, dataService) {
    var vm = this;
    vm.$onInit = onInit;
    vm.canNext = canNext;

    function onInit() {
      vm.connectionDetails = i18n.get('connections.summary.connectionDetails');
      vm.protocol = i18n.get('connections.plugins.other.protocol');
      vm.host = i18n.get('connections.plugins.other.host');
      vm.port = i18n.get('connections.plugins.other.port');
      vm.username = i18n.get('connections.plugins.other.username');
      vm.password = i18n.get('connections.plugins.other.password');

      vm.data = $stateParams.data ? $stateParams.data : {};
      vm.data.sections = [];
      vm.data.sections.push({
          title: i18n.get('connections.plugins.other.connectionDetails'),
          editLink: "otherstep1",
          mapping: {
            "protocol": vm.protocol,
            "host": vm.host,
            "port": vm.port,
            "username": vm.username,
            "password": vm.password
          }
        });
    }

    function canNext() {
      if (vm.data && vm.data.model) {
        return vm.data.model.host;
      }
      return false;
    }
  }

  return {
    name: "otherstep1",
    options: options
  };

});
