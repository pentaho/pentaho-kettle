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
 * Defines the config for the connections UI
 */
define([], function() {
  'use strict';

  config.$inject = ['$stateProvider', '$urlRouterProvider'];

  /**
   * The config for the file open save app
   *
   * @param {Object} $stateProvider - Controls the state of the app
   */
  function config($stateProvider, $urlRouterProvider) {
    $stateProvider
      .state('intro', {
        url: "/intro",
        template: "<intro></intro>",
        params: {
          data: null,
          transition: null
        }
      })
      .state('summary', {
        url: "/summary",
        template: "<connection-summary></connection-summary>",
        params: {
          data: null,
          transition: null
        }
      })
      .state('creating', {
        url: "/creating",
        template: "<creating></creating>",
        params: {
          data: null,
          transition: null
        }
      })
      .state('success', {
        url: "/success",
        template: "<success></success>",
        params: {
          data: null,
          transition: null
        }
      })
      .state('failure', {
        url: "/failure",
        template: "<failure></failure>",
        params: {
          data: null,
          transition: null
        }
      });
    $urlRouterProvider.otherwise("/intro");
  }
  return config;
});
