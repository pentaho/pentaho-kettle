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

define([], function() {
  'use strict';

  config.$inject = ['$stateProvider'];

  function config($stateProvider) {
    $stateProvider
        .state('pentaho.details', {
          url: "/details",
          template: "<pentaho.details connection='vm.connection' on-error='vm.error(message)'></pentaho.details>",
          params: {
            connection: null
          }
        })
        .state('pentaho.loading', {
          url: "/loading",
          template: "<pentaho.loading connection='vm.connection'></pentaho.loading>"
        })
        .state('pentaho.success', {
          url: "/success",
          template: "<pentaho.success connection='vm.connection'></pentaho.success>"
        })
        .state('pentaho.failure', {
          url: "/failure",
          template: "<pentaho.failure connection='vm.connection'></pentaho.failure>"
        });
  }
  return config;
});
