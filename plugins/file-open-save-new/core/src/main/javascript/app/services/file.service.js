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
 * The File Service
 *
 * The File Service, a collection of endpoints used by the application
 *
 * @module services/file.service
 * @property {String} name The name of the module.
 */
define(
    [
      "pentaho/i18n-osgi!file-open-save-new.messages",
      "./services.service",
      "./helper.service",
      "./modal.service",
      "../components/utils"
    ],
    function (i18n, servicesService, helperService, modalService, utils) {
      "use strict";

      var factoryArray = [servicesService.name, helperService.name, modalService.name, "$q", "$interval", "$timeout", factory];
      var module = {
        name: "fileService",
        factory: factoryArray
      };

      return module;

      /**
       * The fileService factory
       *
       * @return {Object} The fileService api
       */
      function factory(ss, helperService, modalService, $q, $interval, $timeout) {
        var baseUrl = "/cxf/browser-new";
        return {
          deleteFiles: deleteFiles,
          renameFile: renameFile,
          moveFiles: moveFiles,
          copyFiles: copyFiles,
          isCopy: isCopy,
          open: open,
          save: save
        };

        /**
         *
         * @param folder
         * @param files
         * @returns {*}
         */
        function deleteFiles(folder, files) {
          return $q(function (resolve, reject) {
            ss.get(folder.provider).deleteFiles(files).then(function (response) {
              // TODO: Smart cleanup
              var deletedFolders = response.data.data;
              for (var i = 0; i < deletedFolders.length; i++) {
                for (var j = 0; j < folder.children.length; j++) {
                  if (deletedFolders[i].path === folder.children[j].path) {
                    folder.children.splice(j, 1);
                  }
                }
              }
              if (response.data.data.length === files.length) {
                resolve(response);
              } else {
                reject(response);
              }
            }, function (response) {
              reject(response);
            });
          });
        }

        /**
         *
         * @param file
         * @param newPath
         * @returns {*}
         */
        function renameFile(file, newPath) {
          return $q(function (resolve, reject) {
            ss.get(file.provider).renameFile(file, newPath).then(function (response) {
              var result = response.data;
              if (result.status === "SUCCESS") {
                resolve(result);
              } else {
                reject(result);
              }
            }, function (response) {
              reject(response.status);
            });
          });
        }

        function _getOverwrite(result) {
          var overwrite = "";
          if (result.values['apply-all']) {
            switch (result.button) {
              case "rename":
                overwrite = "rename_all";
                break;
              case "overwrite":
                overwrite = "all";
                break;
              case "skip":
                overwrite = "skip_all";
                break;
            }
          } else {
            switch (result.button) {
              case "rename":
                overwrite = "rename_one";
                break;
              case "overwrite":
                overwrite = "one";
                break;
              case "skip":
                overwrite = "skip";
                break;
            }
          }
          return overwrite;
        }

        function handleFileExistsCheck(to, newPath) {
          return $q(function (resolve, reject) {
            helperService.httpPost([baseUrl, "fileExists"].join("/") + "?newPath=" + newPath, to).then(function (result) {
              resolve(result.data);
            });
          });
        }

        function handleProgressModal(title, message) {
          modalService.open("file-progress", title, message).then(function (result) {
            // TODO: Handle cancelling (probably hold a cancel in the service and run it)
          });
        }

        function handleCollision(filename) {
          return $q(function (resolve, reject) {
            modalService.close("file-progress");
            modalService.open("overwrite-warning",
                i18n.get("file-open-save-plugin.error.file-exists.title"),
                i18n.get("file-open-save-plugin.error.file-exists.body", {name: filename})).then(function (result) {
              resolve(result);
            });
          });
        }

        /**
         * Copy files from one location to another
         * @param from
         * @param to
         * @returns {Promise}
         */
        function copyFiles(from, to) {
          return $q(function (resolve, reject) {
            startOperation(from, to, 0, copyFile, "", resolve, reject);
          });
        }


        /**
         * Copy a file from one location to another
         * @param from
         * @param to
         * @param path
         * @param overwrite
         * @returns {Promise}
         */
        function copyFile(from, to, path, overwrite) {
          handleProgressModal(i18n.get('file-open-save-plugin.copying.message'), "Copying file " + from.path + " to " + path);
          return helperService.httpPost([baseUrl, "copy"].join("/") + "?overwrite=" + overwrite + "&path=" + path, {
            from: from,
            to: to
          });
        }

        /**
         * Move files from one location to another
         * @param from
         * @param to
         * @returns {*}
         */
        function moveFiles(from, to) {
          return $q(function (resolve, reject) {
            startOperation(from, to, 0, moveFile, "", resolve, reject);
          });
        }

        /**
         * Move file from one location to another
         * @param from
         * @param to
         * @param path
         * @param overwrite
         * @returns {Promise}
         */
        function moveFile(from, to, path, overwrite) {
          handleProgressModal(i18n.get('file-open-save-plugin.moving.message'), "Moving file " + from.path + " to " + path);
          return helperService.httpPost([baseUrl, "move"].join("/") + "?overwrite=" + overwrite + "&path=" + path, {
            from: from,
            to: to
          });
        }

        function startOperation(from, to, index, operation, overwrite, resolve, reject) {
          if (index === from.length) {
            modalService.close("file-progress");
            resolve();
            return;
          } else if (index === -1) {
            modalService.close("file-progress");
            reject();
            return;
          }
          var filename = utils.getFilename(from[index].path);
          var newPath = to.path + "/" + filename;
          switch (overwrite) {
            case "one":
              console.log("one");
              overwrite = "";
            case "all":
              console.log("all");
              operation(from[index], to, newPath, true).then(function (result) {
                startOperation(from, to, ++index, operation, overwrite, resolve, reject);
              });
              break;
            case "rename_one":
              console.log("rename_one");
              overwrite = "";
            case "rename_all":
              console.log("rename_all");
              helperService.httpPost([baseUrl, "getNewName"].join("/") + "?newPath=" + newPath, to).then(function (result) {
                var renamedPath = result.data.data;
                operation(from[index], to, renamedPath, true).then(function (result) {
                  startOperation(from, to, ++index, operation, overwrite, resolve, reject);
                });
              });
              break;
            case "skip":
              overwrite = "";
            case "skip_all":
              startOperation(from, to, ++index, operation, overwrite, resolve, reject);
              break;
            case "":
              handleFileExistsCheck(to, newPath).then(function (exists) {
                if (!exists) {
                  operation(from[index], to, newPath, false).then(function (result) {
                    startOperation(from, to, ++index, operation, overwrite, resolve, reject);
                  });
                } else {
                  handleCollision(filename).then(function (result) {
                    overwrite = _getOverwrite(result);
                    startOperation(from, to, index, operation, overwrite, resolve, reject);
                  });
                }
              });
              break;
          }
        }

        /**
         *
         * @param from
         * @param to
         * @returns {*}
         */
        function isCopy(from, to) {
          return ss.get(from.provider).isCopy(from, to) && ss.get(to.provider).isCopy(from, to);
        }

        //TODO: Add a rename function to folders so I can traverse and rename

        /**
         * Calls vm.onError using the parameter errorType
         * @param {number} errorType - the number corresponding to the appropriate error
         * @private
         */
        function _doError(errorType, file) {
          vm.onError({errorType: errorType, file: file});
        }

        /**
         * Checks for a duplicate name
         *
         * @param {String} name - file name to check if it already exists within vm.files
         * @param {Object} file - File Object
         * @return {Boolean} true if it vm.files already has a file named "name", false otherwise
         * @private
         */
        function _hasDuplicate(name, file) {
          for (var i = 0; i < vm.files.length; i++) {
            var check = vm.files[i];
            if (check !== file) {
              if (check.name.toLowerCase() === name.toLowerCase() && check.type === file.type) {
                return true;
              }
            }
          }
          return false;
        }

        function open(file) {
          ss.get(file.provider).open(file);
        }

        function save(filename, folder, currentFilename, override) {
          helperService.httpPost([baseUrl, "clearCache"].join("/"), folder);
          return ss.get(folder.provider).save(filename, folder, currentFilename, override);
        }

        /**
         * Checks if the file name is valid or not. An invalid name contains forward or backward slashes
         * @returns {boolean} - true if the name is invalid, false otherwise
         * @private
         */
        function _hasInvalidChars(name) {
          return name.match(/[\\\/]/g) !== null;
        }
      }
    }
);
