/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

define([
  "./Core",
  "pentaho/environment"
], function(Core, environment) {

  var corePromise = null;

  return {
    load: function(name, require, onLoad, config) {

      if(config.isBuild) {
        // Don't create when building.
        onLoad({moduleMetaService: {}, moduleService: {}});
      } else {
        if(corePromise === null) {
          corePromise = Core.createAsync(environment);
        }

        corePromise.then(onLoad, onLoad.error);
      }
    }
  };
});
