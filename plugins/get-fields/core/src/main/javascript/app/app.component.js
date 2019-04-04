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
    vm.doSearch = doSearch;
    vm.messageTitle = i18n.get("get-fields-plugin.message.title");
    vm.messageMessage = i18n.get("get-fields-plugin.message.message");
    vm.selectFields = i18n.get("get-fields-plugin.app.header.title");
    vm.clearSelectionLabel = i18n.get("get-fields-plugin.app.clear-selection.label");
    vm.okLabel = i18n.get("get-fields-plugin.app.ok.button");
    vm.cancelLabel = i18n.get("get-fields-plugin.app.cancel.button");
    vm.searchPlaceHolder = i18n.get("get-fields-plugin.app.header.search-parsed-fields.placeholder");
    vm.showMessage = true;
    vm.showMessageText = false;
    vm.showSpinner = true;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      var selections = $location.search().selections;
      var path = $location.search().path;
      vm.type = $location.search().type;
      vm.paths = $location.search().paths;
      vm.isClearDisabled = true;
      vm.onSelection = onSelection;
      if (path === "") { // No file path specified in step
        _showMessage(i18n.get("get-fields-plugin.app.unable-to-view.label", {dataType: vm.type}),
          i18n.get("get-fields.plugin.app.unable-to-view.no-input.message", {dataType: vm.type}));
        } else {
          $timeout(function() {
            if (vm.showMessage) {
              vm.showMessageText = true;
            }
          }, 1000);
          dt.sample(path).then(function(response) {
            vm.tree = response.data;
            vm.showMessage = false;
          }, function(response) {
            if (response.status === 404) { // Cannot find file OR no permissions
            _showMessage(i18n.get("get-fields-plugin.app.unable-to-access.label", {dataType: vm.type}),
              i18n.get("get-fields.plugin.app.unable-to-access.message"));
            } else { // Invalid JSON structure OR other issue
              _showMessage(i18n.get("get-fields-plugin.app.unable-to-view.label", {dataType: vm.type}),
              i18n.get("get-fields.plugin.app.unable-to-view.invalid.message"));
            }
          });
        }
    }

    function doSearch(value) {
      if (vm.tree) {
        _hideMessage();
        if (value) {
          var results = [];
          _findValue(vm.tree, value, results);
          for (var i = 0; i < results.length; i++) {
            if (results[i].parent) {
              var parent = results[i].parent;
              while (parent) {
                parent.hidden = false;
                parent.disabled = results.indexOf(parent) === -1;
                parent = parent.parent;
              }
            }
          }
          if (results.length === 0) {
            _showMessage(i18n.get("get-fields-plugin.app.unable-to-find.label", {searchField: value}),
              i18n.get("get-fields.plugin.app.unable-to-find.message"));
          }
        } else {
          _clearSearch(vm.tree);
        }
      }
    }

    function _clearSearch(node, value) {
      node.hidden = false;
      node.disabled = false;
      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          var child = node.children[i];
          child.hidden = false;
          child.disabled = false;
          if (child.children) {
            if (_clearSearch(child)) {
              return true;
            }
          }
        }
      }
      return false;
    }

    function _findValue(node, value, results) {
      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          var child = node.children[i];
          if (child.key && child.key.indexOf(value) !== -1) {
            results.push(child);
            child.hidden = false;
          } else {
            child.hidden = true;
          }
          if (child.children) {
            _findValue(child, value, results);
          }
        }
      }
    }

    function clearSelection() {
      $scope.$broadcast("clearSelection");
    }

    function ok() {
      if (vm.showMessage) {
        cancel();
      } else {
        $scope.$broadcast("ok");
      }
    }

    function cancel() {
      window.close();
    }

    function _showMessage(title, message) {
      vm.messageTitle = title;
      vm.messageMessage = message;
      vm.showSpinner = false;
      vm.showMessage = true;
      vm.showMessageText = true;
    }

    function _hideMessage() {
      vm.showMessage = false;
      vm.showMessageText = false;
    }

    function onSelection(count) {
      if (count > 0) {
        vm.isClearDisabled = false;
      } else {
        vm.isClearDisabled = true;
      }
    }
  }

  return {
    name: "getFieldsApp",
    options: options
  };
});
