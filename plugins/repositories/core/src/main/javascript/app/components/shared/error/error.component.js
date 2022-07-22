/*!
 * Copyright 2018-2020 Hitachi Vantara. All rights reserved.
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
  'text!./error.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'jquery',
  'css!./error.css'
], function (template, i18n, $) {

  'use strict';

  var options = {
    bindings: {
      error: "<"
    },
    controllerAs: "vm",
    template: template,
    controller: errorController
  };

  errorController.$inject = ["$timeout"];

  function errorController($timeout) {
    var vm = this;
    vm.$onChanges = onChanges;
    vm.reset = reset;
    vm.showError = false;
    vm.message = "";

    var timeout;
    var errorMessage = jQuery("#error-message");

    function onChanges(changes) {
      if (changes.error && changes.error.currentValue) {
        if (changes.error.currentValue.message === null) {
          if (vm.showError) {
            reset();
          }
        } else {
          vm.message = changes.error.currentValue.message;
          if (vm.showError) {
            reset(show);
          } else {
            show();
          }
        }
      }
    }

    function show() {
      if (timeout) {
        $timeout.cancel(timeout);
      }
      vm.showError = true;
      errorMessage.animate({
        opacity: 1,
        top: 20
      }, 600, "linear", function() {
        timeout = $timeout(function() {
          if (vm.showError) {
            reset();
          }
        }, 5000);
      });
    }

    function reset(callback) {
      if (timeout) {
        $timeout.cancel(timeout);
      }
      vm.showError = false;
      errorMessage.animate({
        opacity: 0
      }, 600, function() {
        if (callback) {
          callback();
        } else {
          errorMessage.css("top", "-63px");
        }
      });
    }
  }

  return {
    name: "error",
    options: options
  };

});
