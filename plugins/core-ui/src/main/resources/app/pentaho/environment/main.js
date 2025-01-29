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
  "pentaho/util/requireJS",
  "./impl/Environment"
], function(requireJSUtil, Environment) {

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
   * @type ?pentaho.environment.IEnvironment
   * @amd pentaho/environment
   * @see pentaho.environment.spec.IEnvironment
   */

  var environmentConfig = requireJSUtil.config().config["pentaho/environment"] || null;

  return new Environment(environmentConfig);
});
