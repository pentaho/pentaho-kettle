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

/**
 * Defines the component for selecting/adding/editing a database connection
 */
define([
  'text!./select.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./select.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {
      connection: "="
    },
    controllerAs: "vm",
    template: template,
    controller: selectController
  };

  selectController.$inject = ["selectService", "$state"];

  /**
   * Select controller for selecting a database connection
   *
   * @param {Object} selectService - service for selecting
   * @param {Object} $state - object for changing app state
   */
  function selectController(selectService, $state) {
    var vm = this;
    vm.$onInit = onInit;
    vm.selectDatabase = selectDatabase;
    vm.createNewConnection = createNewConnection;
    vm.editConnection = editConnection;
    vm.deleteConnection = deleteConnection;
    vm.back = back;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.selectLabel = i18n.get('repositories.database.select.label');
      vm.deleteLabel = i18n.get('repositories.delete.label');
      vm.editLabel = i18n.get('repositories.edit.label');
      vm.newLabel = i18n.get('repositories.database.new.label');
      vm.databaseSelectLabel = i18n.get('repositories.database.select.label');
      vm.databaseNoneLabel = i18n.get('repositories.database.none.label');
      vm.createLabel = i18n.get('repositories.database.create.label');
      vm.backLabel = i18n.get('repositories.back.label');

      _loadDatabases();
    }

    /**
     * Method for loading all the database connections
     *
     * @private
     */
    function _loadDatabases() {
      selectService.getDatabases().then(function(res) {
        vm.databases = res.data;
        if (vm.connection.databaseConnection === "None" && vm.databases.length > 0) {
          vm.connection.databaseConnection = vm.databases[0].name;
        }
      });
    }

    /**
     * Method for creating a new database connection
     */
    function createNewConnection() {
      selectService.createNewConnection().then(function(res) {
        vm.connection.databaseConnection = res.data.name;
        _loadDatabases();
      });
    }

    /**
     * Method for selecting a database connection
     *
     * @param {Object} database - database object to select
     */
    function selectDatabase(database) {
      vm.connection.databaseConnection = database.name;
    }

    /**
     * Method for editing a database connection
     */
    function editConnection() {
      selectService.editConnection(vm.connection.databaseConnection).then(function(res) {
        vm.connection.databaseConnection = res.data.name;
        _loadDatabases();
      });
    }

    /**
     * Method for deleting a database connection
     */
    function deleteConnection() {
      selectService.deleteConnection(vm.connection.databaseConnection).then(function(res) {
        vm.connection.databaseConnection = "None";
        _loadDatabases();
      });
    }

    /**
     * Transition back to the database connection details screen
     */
    function back() {
      $state.go("database.details");
    }
  }

  return {
    name: "database.select",
    options: options
  };

});
