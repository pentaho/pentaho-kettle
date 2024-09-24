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
  "module",
  "./environment/impl/Environment"
], function(module, Environment) {

  "use strict";

  // This singleton instance module does not use an intermediary interface to be resolved,
  // as it may be used in a bootstrapping phase.

  /**
   * The _main_ environment of the JavaScript Pentaho Platform.
   *
   * This instance is initialized with the environment specification
   * which is the value of this module's AMD configuration.
   *
   * @name pentaho.environment.main
   * @type pentaho.environment.IEnvironment
   * @amd pentaho/environment
   * @see pentaho.environment.spec.IEnvironment
   */
  return new Environment(module.config());
});
