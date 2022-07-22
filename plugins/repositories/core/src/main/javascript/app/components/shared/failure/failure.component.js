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
  'text!./failure.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./failure.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {
      onManage: "&",
      onChange: "&",
      onClose: "&"
    },
    controllerAs: "vm",
    template: template,
    controller: failureController
  };

  failureController.$inject = [];

  function failureController() {
    var vm = this;
    vm.$onInit = onInit;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.notConnectLabel = i18n.get("repositories.failure.notconnect.label");
      vm.statusCreate = i18n.get("repositories.failure.statuscreate.label");
      vm.questionLabel = i18n.get("repositories.question.label");
      vm.changeSettings = i18n.get("repositories.failure.changesettings.label");
      vm.manageConnections = i18n.get("repositories.manageconnections.label");
      vm.finishLabel = i18n.get("repositories.finish.label");
    }
  }

  return {
    name: "failure",
    options: options
  };

});
