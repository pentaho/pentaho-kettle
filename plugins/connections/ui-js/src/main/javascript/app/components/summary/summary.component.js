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
  'text!./summary.html',
  'pentaho/i18n-osgi!connections.messages',
  "pentaho/module/instancesOf!IPenConnectionProvider",
  'css!./summary.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {},
    controllerAs: "vm",
    template: template,
    controller: summaryController
  };

  summaryController.$inject = ["$state", "$stateParams", "$location", "dataService", "vfsTypes", "vfsSummaries"];

  function summaryController($state, $stateParams, $location, dataService, vfsTypes, vfsSummaries) {
    var vm = this;
    vm.$onInit = onInit;
    vm.getLabel = getLabel;
    vm.onEditClick = onEditClick;
    var loaded = false;

    function onInit() {
      vm.connectionName = i18n.get('connections.intro.connectionName');
      vm.connectionType = i18n.get('connections.intro.connectionType');
      vm.connectionSummary = i18n.get('connections.summary.connectionSummary');
      vm.generalSettings = i18n.get('connections.summary.generalSettings');
      vm.description = i18n.get('connections.summary.description');
      vm.finishLabel = i18n.get('connections.summary.finishLabel');
      vm.data = $stateParams.data;
      vm.buttons = getButtons();
      vm.summary = [];

      if (!vm.data) {
        vm.data = {
          model: null
        }
      }

      var connection = $location.search().connection;
      if (connection) {
        vm.title = i18n.get('connections.intro.edit.label');
        dataService.getConnection(connection).then(function (res) {
          loaded = true;
          if (res.data !== "") {
            setDialogTitle(vm.title);
            var model = res.data;
            vm.type = model.type;
            vm.data.model = model;
            vm.next = vm.data.model.type + "step1";
            vm.data.state = "edit";
            vm.data.isSaved = true;
            vm.name = vm.data.model.name;
            vm.summary = vfsSummaries[model.type];
          } else {
            vm.title = i18n.get('connections.intro.new.label');
            setDialogTitle(vm.title);
          }
        });
      } else {
        vm.summary = vfsSummaries[vm.data.model.type];
        loaded = true;
      }
    }

    function getLabel(scheme) {
      for (var i = 0; i < vfsTypes.length; i++) {
        if (vfsTypes[i].value === scheme) {
          return vfsTypes[i].label;
        }
      }
      return "";
    }

    function onEditClick(destination) {
      vm.data.state = "modify";
      $state.go(destination, {data: vm.data, transition: "slideRight"});
    }

    function getButtons() {
      return [{
        label: i18n.get('connections.controls.finishLabel'),
        class: "primary",
        position: "right",
        onClick: function() {
          $state.go( "creating", {data: vm.data, transition: "slideLeft"});
        }
      }];
    }

    function setDialogTitle(title) {
      if (loaded === true) {
        try {
          setTitle(title);
        } catch (e) {
          console.log(title);
        }
      }
    }
  }

  return {
    name: "connectionSummary",
    options: options
  };

});
