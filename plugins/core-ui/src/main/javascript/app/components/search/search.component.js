/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2017 Hitachi Vantara. All rights reserved.
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
 * The File Open and Save Card component.
 *
 * This provides the component for the Cards for the Recents option.
 * @module components/card/card.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 */
define([
  "text!./search.html",
  "../utils",
  "css!./search.css"
], function(searchTemplate, utils) {
  "use strict";

  var options = {
    bindings: {
      searching: '<',
      searchValue: '<',
      autofill: '<',
      placeholder: '<',
      onSearch: '&',
      onBlur: '&',
      onFocus: '&',
      liveSearch: '<'
    },
    template: searchTemplate,
    controllerAs: "vm",
    controller: searchController
  };

  /**
   * The Card Controller.
   *
   * This provides the controller for the card component.
   */
  function searchController() {
    var vm = this;
    vm.searchValue = "";
    vm.inSearch = false;
    vm.getTooltip = getTooltip;
    vm.clearSearch = clearSearch;
    vm.selectAutofill = selectAutofill;
    vm.showAutofill = showAutofill;
    vm.doSearch = doSearch;
    vm.blur = blur;
    vm.focus = focus;

    function doSearch(e) {
      if (vm.liveSearch) {
        vm.onSearch({searchValue:vm.searchValue});
      } else if (e.keyCode === 13) {
        vm.onSearch({searchValue:vm.searchValue});
      }
    }

    /**
     * Clears the search value
     */
    function clearSearch() {
      vm.blur();
      vm.searchValue = "";
      vm.onSearch({searchValue:""});
    }

    /**
     * Selects an autofill value
     * @param {String} value - the text value to be searched
     */
    function selectAutofill(value) {
      vm.searchValue = value;
      vm.onSearch({searchValue:value});
    }

    /**
     * A boolean as to whether or not to show the autofill
     */
    function showAutofill() {
      if (!vm.autofill) {
        return false;
      }
      return vm.inSearch && !vm.searchValue && vm.autofill.length > 0;
    }

    /**
     * Method to call when the blur event happens on the search box
     */
    function blur() {
      vm.inSearch = false;
      vm.onBlur();
    }

    /**
     * Method to call when the focus event happens on the search box
     */
    function focus() {
      vm.inSearch = true;
      vm.onFocus();
    }

    /**
     * Sets the tooltip of a recent search in the search dropdown if it is wider than its container
     * @param {String} text - the tooltip to add if necessary
     */
    function getTooltip(text) {
      if (utils.getTextWidth(text) - 1 > 247) {
        return text;
      }
    }
  }

  return {
    name: "search",
    options: options
  };
});
