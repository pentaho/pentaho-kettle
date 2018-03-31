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
 * The File Open and Save Loading component.
 *
 * This provides the component for the Loading screen while the directory structure loads.
 * @module components/loading/loading.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "text!./loading.html",
  "pentaho/i18n-osgi!file-open-save.messages",
  "css!./loading.css"
], function(template, i18n) {
  "use strict";

  var options = {
    bindings: {
      showText: '<',
      title: '<',
      message: '<'
    },
    controllerAs: "vm",
    template: template,
    controller: loadingController
  };

  /**
   * The Loading Controller
   */
  function loadingController() {
    var vm = this;
  }

  return {
    name: "loading",
    options: options
  };
});
