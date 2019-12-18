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
 * The Recents Service
 *
 * The Recents Service, a collection of endpoints used by the application
 *
 * @module providers/recents.service
 * @property {String} name The name of the module.
 */
define(
    [
      "./fileutil",
      "../message.service",
      "pentaho/i18n-osgi!file-open-save-new.messages"
    ],
    function(fileutil, messageService, i18n) {
      "use strict";

      var factoryArray = ["helperService", "$http", "$q", messageService.name, factory];
      var module = {
        name: "recentsService",
        factory: factoryArray
      };

      return module;

      /**
       * The dataService factory
       *
       * @param {Object} $http - The $http angular helper service
       *
       * @return {Object} The dataService api
       */
      function factory(helperService, $http, $q, messageService) {
        var baseUrl = "/cxf/browser-new";
        return {
          provider: "recents",
          order: 0,
          root: "Recents",
          getBreadcrumbPath: getBreadcrumbPath,
          open: open,
          save: save,
          getPath: getPath,
          selectFolder: selectFolder
        };

        function getPath(file) {
          return file.root ? _getTreePath(file) : file.path;
        }

        function getBreadcrumbPath(file) {
          return {
            type: "recents",
            fileType: file.type ? file.type : "folder",
            prefix: null,
            uri: _getFilePath(file),
            path: _getTreePath(file)
          };
        }

        function _getTreePath(file) {
          if (!file.path) {
            return file.root ? file.root + "/" + file.name : file.name;
          }
          return file.root + "/" + file.name;
        }

        function _getFilePath(file) {
          return file.path ? file.path : null;
        }

        function selectFolder(folder, filters, useCache) {
          return $q(function(resolve) {
            if (folder.children && folder.children.length > 0) {
              messageService.set("file", null);
            } else {
              messageService.set("file", i18n.get('file-open-save-plugin.app.middle.no-recents.message'));
            }
            resolve();
          });
        }

        function open(file) {
          select(JSON.stringify({
            name: file.name,
            path: file.path,
            parent: file.parent,
            connection: file.connection,
            provider: file.provider
          }));
        }

        function save(filename, folder) {
          select(JSON.stringify({
            name: filename,
            path: folder.path,
            parent: folder.parent,
            provider: folder.provider
          }));

          return $q.resolve();
        }
      }
    });
