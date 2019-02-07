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
  'text!./other.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./other.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: otherController
  };

  otherController.$inject = ["otherService", "$state", "$stateParams"];

  function otherController(otherService, $state, $stateParams) {
    var vm = this;
    vm.$onInit = onInit;
    vm.selectRepositoryType = selectRepositoryType;
    vm.getStarted = getStarted;
    vm.close = close;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.otherLabel = i18n.get("repositories.other.label");
      vm.newDescriptionLabel = i18n.get("repositories.new.description.label");
      vm.newPentahoRepositoryLabel = i18n.get("repositories.new.pentahorepository.label");
      vm.closeLabel = i18n.get("repositories.close.label");
      vm.getStartedLabel = i18n.get("repositories.getstarted.label");
      otherService.getTypes().then(function(res) {
        vm.repositoryTypes = res.data;
        if ($stateParams.type) {
          for (var i = 0; i < vm.repositoryTypes.length; i++) {
            if (vm.repositoryTypes[i].id === $stateParams.type) {
              vm.selectedRepositoryType = vm.repositoryTypes[i];
            }
          }
        }
      });
    }

    function selectRepositoryType(repositoryType) {
      vm.selectedRepositoryType = repositoryType;
    }

    function getStarted(repositoryType) {
      if (repositoryType.id === "KettleDatabaseRepository") {
        $state.go("database.details");
      }
      if (repositoryType.id === "KettleFileRepository") {
        $state.go("file.details");
      }
    }

    function close() {
      closeWindow();
    }
  }

  return {
    name: "other",
    options: options
  };

});
