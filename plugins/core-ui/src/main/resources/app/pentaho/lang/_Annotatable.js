/*!
 * Copyright 2010 - 2017 Hitachi Vantara. All rights reserved.
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
