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
 * The Json Input Get Fields Tree component.
 *
 * This provides the component for the Tree screen.
 * @module components/tree/tree.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
    "text!./tree.html",
    "css!./tree.css"
], function(template) {
    "use strict";

    var options = {
        bindings: {
            content: '<'
        },
        controllerAs: "vm",
        template: template,
        controller: treeController
    };

    treeController.$inject = ["$scope"];

    /**
     * The Tree Controller
     */
    function treeController($scope) {
      var vm = this;
      vm.toggle = toggle;
      vm.hasChevron = hasChevron;
      vm.$onChanges = function(changes) {
        if (vm.content) {
          _setParent(null, vm.content);
        }
      };

      $scope.$on("clearSelection", function(e, data) {
        if (vm.content.children) {
          _clearChildren(vm.content);
        }
      });

      function _setParent(parent, node) {
        node.open = true;
        node.parent = parent;
        if (node.children) {
          for (var i = 0; i < node.children.length; i++) {
            var child = node.children[i];
            _setParent(node, child);
          }
        }
      }

      function _clearChildren(node) {
        node.checked = false;
        if (node.children) {
          for (var i = 0; i < node.children.length; i++) {
            var child = node.children[i];
            child.checked = false;
            _clearChildren(child);
          }
        }
      }

      function _getPaths() {
        var paths = [];
        _findPaths(vm.content, paths);
        return paths;
      }

      function _findPaths(node, paths) {
        if (node.children) {
          for (var i = 0; i < node.children.length; i++) {
            var child = node.children[i];
            if (child.checked && !_hasCheckedChildren(child)) {
              var output = child.key + (child.type === "Array" ? "[*]" : "");
              var parent = child.parent;
              while (parent) {
                if (parent.checked) {
                  if (parent.key === null) {
                    if (parent.type === "Array") {
                      output = "[*]" + output;
                    }
                  } else {
                    output = parent.key + (parent.type === "Array" ? "[*]" : "") + "." + output;
                  }
                } else {
                  if (!output.startsWith(".")) {
                    output = "." + output;
                  }
                }
                parent = parent.parent;
              }
              output = "$." + output;
              paths.push(child.key + ":" + output + ":" + child.type);
            }
            _findPaths(child, paths);
          }
        }
      }

      function _hasCheckedChildren(node) {
        if (node.children) {
          for (var i = 0; i < node.children.length; i++) {
            var child = node.children[i];
            if (child.checked) {
              return true;
            }
            if (child.children) {
              if (_hasCheckedChildren(child)) {
                return true;
              }
            }
          }
        }
        return false;
      }

      $scope.$on("ok", function(e, data) {
        var paths = _getPaths();
        try {
          window.ok(paths);
        } catch (err) {
          console.log(paths);
        }
      });

      function hasChevron(node) {
        return node.children && node.children.length > 0;
      }

      /**
       * Toggle a collection open/closed
       *
       * @param value
       */
      function toggle(node) {
        if (!node.open) {
          node.open = true;
        } else {
          node.open = false;
        }
      }
    }

    return {
        name: "tree",
        options: options
    };
});
