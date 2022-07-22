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
 * The File Open and Save Files component.
 *
 * This is a file list component.
 * @module components/files/files.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "text!./modals.html",
  "pentaho/i18n-osgi!file-open-save-new.messages",
  "css!./modals.css"
], function (modalsTemplate, i18n) {
  "use strict";

  var options = {
    bindings: {
    },
    template: modalsTemplate,
    controllerAs: "vm",
    controller: modalsController
  };

  modalsController.$inject = [];

  /**
   * The Modals Controller.
   *
   * This provides the controller for the modals component.
   *
   * @param {Function} $timeout - Angular wrapper for window.setTimeout.
   * @param $filter
   */
  function modalsController() {
    var vm = this;
    vm.$onInit = onInit;

    function onInit() {
      vm.closeLabel = i18n.get("file-open-save-plugin.close.button");
    }
  }

  return {
    name: "modals",
    options: options
  };
});
