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
  'text!./file.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./file.css'
], function (template, i18n, $) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: pentahoController
  };

  pentahoController.$inject = ["$stateParams"];

  function pentahoController($stateParams) {
    var vm = this;
    vm.error = error;

    if ($stateParams.connection) {
      vm.connection = $stateParams.connection;
      vm.connection.edit = true;
      vm.connection.originalName = vm.connection.displayName;
    } else {
      vm.connection = {
        id: "KettleFileRepository",
        displayName: "",
        location: "",
        doNotModify: false,
        showHiddenFolders: false,
        description: "File repository",
        isDefault: false
      };
    }

    function error(message) {
      vm.errorMessage = {timestamp: Date.now(), message: message};
    }
  }

  return {
    name: "file",
    options: options
  };

});
