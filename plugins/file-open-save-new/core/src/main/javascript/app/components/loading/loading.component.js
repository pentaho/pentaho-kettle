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
 * The File Open and Save Loading component.
 *
 * This provides the component for the Loading screen while the directory structure loads.
 * @module components/loading/loading.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "text!./loading.html",
  "pentaho/i18n-osgi!file-open-save-new.messages",
  "css!./loading.css"
], function(template, i18n) {
  "use strict";

  var options = {
    bindings: {
      showText: '<',
      title: '<',
      message: '<'
    },
    controllerAs: "vm",
    template: template,
    controller: loadingController
  };

  /**
   * The Loading Controller
   */
  function loadingController() {
    var vm = this;
  }

  return {
    name: "loading",
    options: options
  };
});
