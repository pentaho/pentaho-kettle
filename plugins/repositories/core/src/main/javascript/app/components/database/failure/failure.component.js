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
 * Defines the component for the database connection fail screen
 */
define([
  'text!./failure.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./failure.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {
      connection: "="
    },
    controllerAs: "vm",
    template: template,
    controller: failureController
  };

  failureController.$inject = ["$state"];

  /**
   * Controller for the database connection failure screen
   *
   * @param {Object} $state - object for controlling the app state
   */
  function failureController($state) {
    var vm = this;
    vm.change = change;
    vm.close = close;
    vm.manage = manage;

    /**
     * Redirect to the database connection details screen
     */
    function change() {
      vm.connection.modify = true;
      $state.go("database.details");
    }

    /**
     * Closes the repository manager
     */
    function close() {
      closeWindow();
    }

    /**
     * Redirect to the manager screen
     */
    function manage() {
      $state.go("manager");
    }
  }

  return {
    name: "database.failure",
    options: options
  };

});
