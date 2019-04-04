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
 * Defines the component for the database repository type configuration
 */
define([], function() {
  'use strict';

  config.$inject = ['$stateProvider'];

  /**
   * The configuration for the database repository type
   *
   * @param {Object} $stateProvider - manages the state
   */
  function config($stateProvider) {
    $stateProvider
        .state('database.details', {
          url: "/details",
          template: "<database.details connection='vm.connection' on-error='vm.error(message)'></database.details>",
          params: {
            connection: null
          }
        })
        .state('database.select', {
          url: "/select",
          template: "<database.select connection='vm.connection'></database.select>"
        })
        .state('database.loading', {
          url: "/loading",
          template: "<database.loading connection='vm.connection'></database.loading>"
        })
        .state('database.success', {
          url: "/success",
          template: "<database.success connection='vm.connection'></database.success>"
        })
        .state('database.failure', {
          url: "/failure",
          template: "<database.failure connection='vm.connection'></database.failure>"
        });
  }
  return config;
});
