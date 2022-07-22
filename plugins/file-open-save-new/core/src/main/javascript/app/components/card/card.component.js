/*!
 * Copyright 2019 Hitachi Vantara. All rights reserved.
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
 * The File Open and Save Card component.
 *
 * This provides the component for the Cards for the Recents option.
 * @module components/card/card.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 */
define([
  "text!./card.html",
  "css!./card.css"
], function(cardTemplate) {
  "use strict";

  var options = {
    bindings: {
      recentFiles: "<",
      folder: "<",
      onClick: "&",
      onSelect: "&"
    },
    template: cardTemplate,
    controllerAs: "vm",
    controller: cardController
  };

  /**
   * The Card Controller.
   *
   * This provides the controller for the card component.
   */
  function cardController() {
    var vm = this;
    vm.selectFile = selectFile;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
    }

    /**
    * Called whenever one-way bindings are updated.
    *
    * @param {Object} changes - hash whose keys are the names of the bound properties
    * that have changed, and the values are an object of the form
    */
    function onChanges(changes) {
      if (changes.folder) {
        vm.selectedFile = null;
      }
    }

    /**
     * Sets the selected file and passes it to the app component
     *
     * @param {Object} file - A file object that the user clicked
     */
    function selectFile(file) {
      vm.selectedFile = file;
      vm.onSelect({selectedFile: [file]});
    }
  }

  return {
    name: "card",
    options: options
  };
});
