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
 * Defines the component for the database repository type
 */
define([
  'text!./database.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./database.css'
], function (template) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: databaseController
  };

  databaseController.$inject = ["$stateParams"];

  /**
   * Defines the controller for the database respository type
   *
   * @param $stateParams
   */
  function databaseController($stateParams) {
    var vm = this;
    vm.$onInit = onInit;
    vm.error = error;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      if ($stateParams.connection) {
        vm.connection = $stateParams.connection;
        vm.connection.edit = true;
        vm.connection.originalName = vm.connection.displayName;
      } else {
        vm.connection = {
          id: "KettleDatabaseRepository",
          displayName: "",
          databaseConnection: "None",
          description: "Database repository",
          isDefault: false
        };
      }
    }

    /**
     * Display an error message
     *
     * @param message
     */
    function error(message) {
      vm.errorMessage = {timestamp: Date.now(), message: message};
    }
  }

  return {
    name: "database",
    options: options
  };

});
