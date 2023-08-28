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

define([
  'text!./help.html',
  'pentaho/i18n-osgi!connections.messages'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: helpController
  };

  helpController.$inject = ["dataService"];

  function helpController(dataService) {
    var vm = this;
    vm.$onInit = onInit;
    vm.click = click;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.helpLabel = i18n.get('connections.help.helpLabel');
    }

    /**
     * FIXME removing dependency on Spoon.java/SWT - SCENARIO HELP
     * dataService.help() is bringing up SWT UI
     * TODO properly implement a javascript callback similar to:
     *    https://github.com/pentaho/pentaho-kettle/blob/9.3.0.4/plugins/connections/ui/src/main/javascript/app/components/intro/intro.component.js#L176
     * and then in the Client whether Spoon or PUC provide the js function such as:
     *    https://github.com/pentaho/pentaho-kettle/blob/9.3.0.4/plugins/connections/ui/src/main/java/org/pentaho/di/connections/ui/dialog/ConnectionDialog.java#L84-L89
     * and return just HELP_URL or value of "ConnectionDialog.help.dialog.Help"
     * and have client implement how to handle it, i.e. ConnectionDialog.java for Spoon scenario
     */

    function click() {
      dataService.help();
    }
  }

  return {
    name: "help",
    options: options
  };

});
