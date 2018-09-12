/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

/**
 * The Get Fields App component.
 *
 * This provides the main component for supporting the get fields functionality.
 * @module app.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "./services/data.service",
  "text!./app.html",
  "pentaho/i18n-osgi!get-fields.messages",
  "css!./app.css"
], function(dataService, template, i18n) {
  "use strict";

  var options = {
    bindings: {},
    template: template,
    controllerAs: "vm",
    controller: appController
  };

  appController.$inject = [dataService.name, "$location", "$scope", "$timeout"];

  function appController(dt, $location, $scope, $timeout) {
    var vm = this;
    vm.$onInit = onInit;
    vm.clearSelection = clearSelection;
    vm.ok = ok;
    vm.cancel = cancel;
    vm.loading = true;
    vm.loadingTitle = i18n.get('get-fields-plugin.loading.title');
    vm.loadingMessage = i18n.get('get-fields-plugin.loading.message');
    vm.selectFields = i18n.get('get-fields-plugin.app.header.title');
    vm.clearSelection = i18n.get('get-fields-plugin.app.clear-selection.label');
    vm.okLabel = i18n.get('get-fields-plugin.app.ok.button');
    vm.cancelLabel = i18n.get('get-fields-plugin.app.cancel.button');
    vm.searchPlaceHolder = i18n.get('get-fields-plugin.app.header.search-parsed-fields.placeholder');
    vm.longLoading = false;
    vm.doSearch = doSearch;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      var path = $location.search().path;
      $timeout(function() {
        if (vm.loading) {
          vm.longLoading = true;
        }
      }, 1000);
      dt.sample(path).then(function(response) {
        vm.tree = response.data;
        vm.loading = false;
      }, function() {
        console.log("An error has occurred");
      });
    }

    function doSearch(value) {
      if (value) {
        _findValue(vm.tree, value);
      } else {
        _clearSearch(vm.tree);
      }
    }

    function _clearSearch(node, value) {
      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          var child = node.children[i];
          child.hidden = false;
          if (child.children) {
            if (_clearSearch(child)) {
              return true;
            }
          }
        }
      }
      return false;
    }


    function _findValue(node, value) {
      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          var child = node.children[i];
          if (child.key && child.key.indexOf(value) !== -1) {
            child.hidden = false;
          } else {
            child.hidden = true;
          }
          if (child.children) {
            _findValue(child, value);
          }
        }
      }
    }

    function clearSelection() {
      $scope.$broadcast("clearSelection");
    }

    function ok() {
      $scope.$broadcast("ok");
    }

    function cancel() {
      window.close();
    }

  }

  return {
    name: "getFieldsApp",
    options: options
  };
});
