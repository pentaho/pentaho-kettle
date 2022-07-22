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
 * Defines the component for the success screen for database repository connection
 */
define([
  'text!./success.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./success.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {
      connection: "<"
    },
    controllerAs: "vm",
    template: template,
    controller: successController
  };

  successController.$inject = ["$state"];

  /**
   * Controller for the database repository success screen
   *
   * @param {Object} $state - Object for changing app state
   */
  function successController($state) {
    var vm = this;
    vm.close = close;
    vm.manage = manage;
    vm.connect = connect;

    /**
     * Method for closing the repository manager dialog
     */
    function close() {
      closeWindow();
    }

    /**
     * Transition to the repository manager screen
     */
    function manage() {
      $state.go("manager");
    }

    /**
     * Transition to the connect screen
     */
    function connect() {
      $state.go("connect", {connection: vm.connection});
    }
  }

  return {
    name: "database.success",
    options: options
  };

});
