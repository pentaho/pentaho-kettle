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

/**
 * The Repository Manager Config
 *
 * The main config for supporting the repository manager
 */
define([], function() {
  'use strict';

  config.$inject = ['$stateProvider', '$urlRouterProvider'];

  /**
   * Defines the config for the repository manager
   *
   * @param $stateProvider
   * @param $urlRouterProvider
   */
  function config($stateProvider, $urlRouterProvider) {
    $stateProvider
        .state('manager', {
          url: "/",
          template: "<manager></manager>"
        })
        .state('add', {
          url: "/add",
          template: "<add></add>"
        })
        .state('connect', {
          url: "/connect/:repo",
          template: "<connect></connect>",
          params: {
            credentials: null,
            connection: null,
            error: null,
            repo: null
          }
        })
        .state('connecting', {
          url: "/connecting",
          template: "<connecting></connecting>",
          params: {
            credentials: null,
            connection: null
          }
        })
        .state('other', {
          url: "/other",
          template: "<other></other>",
          params: {
            type: null
          }
        })
        .state('pentaho', {
          url: "/pentaho",
          template: "<pentaho></pentaho>"
        })
        .state('database', {
          url: "/database",
          template: "<database></database>"
        })
        .state('file', {
          url: "/file",
          template: "<file></file>"
        });

    $urlRouterProvider.otherwise("/");
  }
  return config;
});
