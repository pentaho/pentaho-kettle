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
  'text!./success.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./success.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {
      connection: "<",
      onConnect: "&",
      onManage: "&",
      onClose: "&"
    },
    controllerAs: "vm",
    template: template,
    controller: successController
  };

  successController.$inject = [];

  function successController() {
    var vm = this;
    vm.$onInit = onInit;
    vm.connectNowVisible = true;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.congratulationsLabel = i18n.get("repositories.success.congratulations.label");
      vm.readyEdit = i18n.get("repositories.readyedit.label");
      vm.readyCreate = i18n.get("repositories.readycreate.label");
      vm.question = i18n.get("repositories.question.label");
      vm.connectNow = i18n.get("repositories.success.connectNow.label");
      vm.manageConnections = i18n.get("repositories.manageconnections.label");
      vm.finishLabel = i18n.get("repositories.finish.label");
    }
  }

  return {
    name: "success",
    options: options
  };

});
