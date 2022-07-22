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
  'text!./details.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./details.css'
], function (template, i18n) {

  'use strict';

  var options = {
    bindings: {
      onError: "&",
      connection: "="
    },
    controllerAs: "vm",
    template: template,
    controller: detailsController
  };

  detailsController.$inject = ["file.detailsService", "$state", "$stateParams"];

  function detailsController(detailsService, $state) {
    var vm = this;
    vm.$onInit = onInit;
    vm.browse = browse;
    vm.canFinish = canFinish;
    vm.setDefaultConn = setDefaultConn;
    vm.setDoNotModify = setDoNotModify;
    vm.setShowHiddenFolders = setShowHiddenFolders;
    vm.back = back;
    vm.finish = finish;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.detailsLabel = i18n.get('repositories.file.repositoryDetails.label');
      vm.displayNameLabel = i18n.get('repositories.displayName.label');
      vm.fileLocationLabel = i18n.get('repositories.file.location.label');
      vm.fileBrowseLabel = i18n.get('repositories.file.browse.label');
      vm.noModifyLabel = i18n.get('repositories.file.nomodify.label');
      vm.showHiddenLabel = i18n.get('repositories.file.showHidden.label');
      vm.descriptionLabel = i18n.get('repositories.description.label');
      vm.launchLabel = i18n.get('repositories.launch.label');
      vm.finishLabel = i18n.get('repositories.finish.label');
      vm.backLabel = i18n.get('repositories.back.label');
      vm.existsMessage = i18n.get('repositories.error.exists.label');
    }

    function browse() {
      detailsService.browse().then(function(res) {
        vm.connection.location = res.data.path;
      });
    }

    function canFinish() {
      return vm.connection.displayName && vm.connection.location;
    }

    function setDoNotModify(doNotModify) {
      vm.connection.doNotModify = doNotModify;
    }

    function setShowHiddenFolders(showHiddenFolders) {
      vm.connection.showHiddenFolders = showHiddenFolders;
    }

    function setDefaultConn(isDefault) {
      vm.connection.isDefault = isDefault;
    }

    function finish() {
      if (vm.connection.edit || vm.connection.modify) {
        $state.go("file.loading");
      } else {
        detailsService.checkDuplicate(vm.connection).then(function(res) {
          if (res.data === true) {
            vm.onError({message:vm.existsMessage});
          } else {
            $state.go("file.loading");
          }
        });
      }
    }

    function back() {
      if (vm.connection.edit) {
        $state.go("manager");
      } else {
        $state.go("other", {type: "KettleFileRepository"});
      }
    }
  }

  return {
    name: "file.details",
    options: options
  };

});
