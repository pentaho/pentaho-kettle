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
 * Defines the component for the add connection screen
 */
define([
  'text!./add.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./add.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: addController
  };

  addController.$inject = ["$state"];

  /**
   * Controller for adding a new connection
   *
   * @param {Object} $state - Object for controlling app state
   */
  function addController($state) {
    var vm = this;
    vm.$onInit = onInit;
    vm.other = other;
    vm.getStarted = getStarted;
    vm.close = close;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.pentahoRepositoryLabel = i18n.get('repositories.pentaho.pentahorepository.label');
      vm.pentahoDescriptionLabel = i18n.get('repositories.pentaho.description.label');
      vm.pentahoClickStartedLabel = i18n.get('repositories.pentaho.clickstarted.label');
      vm.othersLabel = i18n.get('repositories.other.label');
      vm.closeLabel = i18n.get('repositories.close.label');
      vm.getStartedLabel = i18n.get('repositories.getstarted.label');
    }

    /**
     * Go to the state for other connections
     */
    function other() {
      $state.go("other");
    }

    /**
     * Go to the details for a pentaho connection
     */
    function getStarted() {
      $state.go("pentaho.details");
    }

    /**
     * Closes the window
     */
    function close() {
      closeWindow();
    }
  }

  return {
    name: "add",
    options: options
  };

});
