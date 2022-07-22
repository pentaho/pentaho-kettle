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
  'text!./manager.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./manager.css'
], function(template, i18n) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: managerController
  };

  managerController.$inject = ["$state", "managerService"];

  function managerController($state, managerService) {
    var vm = this;
    vm.$onInit = onInit;
    vm.add = add;
    vm.edit = edit;
    vm.selectRepository = selectRepository;
    vm.setDefault = setDefault;
    vm.clearDefault = clearDefault;
    vm.remove = remove;
    vm.close = close;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.managerTitle = i18n.get("repositories.manager.title.label");
      vm.deleteLabel = i18n.get("repositories.delete.label");
      vm.editLabel = i18n.get("repositories.edit.label");
      vm.addLabel = i18n.get("repositories.manager.add.label");
      vm.closeLabel = i18n.get("repositories.close.label");
      vm.launchLabel = i18n.get("repositories.launch.label");

      managerService.getRepositories().then(function(res) {
        vm.repositories = res.data;
      });
    }

    function setDefault(repository) {
      clearDefaults();
      repository.isDefault = true;
      managerService.setDefault(repository);
    }

    function clearDefault() {
      clearDefaults();
      managerService.clearDefault();
    }

    function clearDefaults() {
      for (var i = 0; i < vm.repositories.length; i++) {
        vm.repositories[i].isDefault = false;
      }
    }

    function selectRepository(repository) {
      vm.selectedRepository = repository;
    }

    function remove(repository) {
      var index = vm.repositories.indexOf(repository);
      vm.repositories.splice(index, 1);
      managerService.remove(repository);
    }

    function add() {
      $state.go("add");
    }

    function edit(repository) {
      if (repository.id === "PentahoEnterpriseRepository") {
        $state.go("pentaho.details", {connection: repository});
      }
      if (repository.id === "KettleDatabaseRepository") {
        $state.go("database.details", {connection: repository});
      }
      if (repository.id === "KettleFileRepository") {
        $state.go("file.details", {connection: repository});
      }
    }

    function close() {
      closeWindow();
    }
  }

  return {
    name: "manager",
    options: options
  };

});
