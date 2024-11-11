/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

define([
  "../lang/Base",
  "../util/arg",
  "../util/object"
], function(Base, arg, O) {

  return Base.extend("pentaho.lang._Annotatable", {

    constructor: function(spec) {
      this._annots = arg.optional(spec, "p");
    },

    property: function(name, value) {
      var annots = this._annots;
      if(arguments.length < 2) {
        // Get
        return annots ? O.getOwn(annots, name) : undefined;
      }

      // Set
      if(!annots) this._annots = annots = {};
      annots[name] = value;
      return this;
    }
  }, {
    configure: function(inst, config) {
      var ps = config.p;
      if(ps) {
        var annots = inst._annots;
        Object.keys(ps).forEach(function(name) {
          if(!annots) inst._annots = annots = {};
          annots[name] = ps[name];
        });
      }
    },

    // region ISpecifiable implementation helper
    toSpec: function(inst, json) {
      if(!json) json = {};
      if(inst._annots) json.p = O.cloneShallow(inst._annots);
      return json;
    }
    // endregion
  });
});
