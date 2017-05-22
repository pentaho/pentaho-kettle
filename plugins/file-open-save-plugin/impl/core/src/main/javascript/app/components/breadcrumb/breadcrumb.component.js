/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2017 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

/**
 * The File Open and Save Breadcrumb component.
 *
 * This provides the component for the Breadcrumbs for the Recents option.
 **/
define([
  "text!./breadcrumb.html",
  "css!./breadcrumb.css"
], function(breadcrumbTemplate) {
  "use strict";

  var options = {
    bindings: {
      path: '<',
      includeRoot: '<',
      onSelect: '&'
    },
    template: breadcrumbTemplate,
    controllerAs: "vm",
    controller: breadcrumbController
  };

  function breadcrumbController() {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;
    vm.select = select;
    vm.parts = [];
    vm.extras = [];

    function onInit() {
    }

    function onChanges(onChanges) {
      if (onChanges.path) {
        var path = onChanges.path.currentValue;
        if (path) {
          updatePath(path);
        }
      }
    }

    function updatePath(path) {
      if (path) {
        var parts = path.split("/");
        var set = [];
        if (vm.includeRoot && path !== "Recents") {
          set.push({path:"/", part:"/"});
        }
        for (var i = 0; i < parts.length; i++) {
          if (parts[i] !== "") {
            set.push({path:parts.slice(0, i+1).join("/"), part:parts[i]});
          }
        }
        if (set.length > 3) {
          vm.parts = set.splice(set.length-3, set.length);
          vm.extras = set;
        } else {
          vm.parts = set;
          vm.extras = [];
        }
      }
    }

    function select(path) {
      vm.showExtras = false;
      vm.onSelect({selectedPath:path});
    }
  }

  return {
    name: "breadcrumb",
    options: options
  };
});
