/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/


/**
 * Internal class for handling dynamic properties.
 *
 * WARNING: This is a legacy class to support the old-style dynamic properties
 * in 0.6.x. Its much improved successor is {@link rwt.qx.Property}.
 *
 * @deprecated This class is supposed to be removed in qooxdoo 0.7
 */
/*jshint boss: true */
rwt.qx.Class.define( "rwt.qx.LegacyProperty", {

  statics : {

    /**
     * Adds a so-named fast property to a prototype.
     *
     * config fields:
     * - name
     * - defaultValue
     * - noCompute
     * - setOnlyOnce
     *
     * @param config {Map} Configuration structure
     * @param proto {Object} Prototype where the methods should be attached
     */
    addFastProperty : function( config, proto ) {
      var vName = config.name;
      var vUpName = rwt.util.Strings.toFirstUp( vName );
      var vStorageField = "_value" + vUpName;
      var vGetterName = "get" + vUpName;
      var vSetterName = "set" + vUpName;
      var vComputerName = "_compute" + vUpName;
      proto[ vStorageField ] = typeof config.defaultValue !== "undefined" ? config.defaultValue
                                                                          : null;
      if( config.noCompute ) {
        proto[ vGetterName ] = function() {
          return this[ vStorageField ];
        };
      } else {
        proto[ vGetterName ] = function() {
          return this[ vStorageField ] == null ? this[ vStorageField ] = this[ vComputerName ]()
                                               : this[ vStorageField ];
        };
      }
      proto[ vGetterName ].self = proto.constructor;
      if( config.setOnlyOnce ) {
        proto[ vSetterName ] = function( vValue ) {
          this[ vStorageField ] = vValue;
          this[ vSetterName ] = null;
          return vValue;
        };
      } else {
        proto[ vSetterName ] = function( vValue ) {
          return this[ vStorageField ] = vValue;
        };
      }
      proto[ vSetterName ].self = proto.constructor;
      if( !config.noCompute ) {
        proto[ vComputerName ] = function() {
          return null;
        };
        proto[ vComputerName ].self = proto.constructor;
      }
    },

    /**
     * Adds a so-named cached property to a prototype.
     *
     * config fields:
     * - name
     * - defaultValue
     * - addToQueueRuntime
     *
     * @param config {Map} Configuration structure
     * @param proto {Object} Prototype where the methods should be attached
     */
    addCachedProperty : function( config, proto ) {
      var vName = config.name;
      var vUpName = rwt.util.Strings.toFirstUp( vName );
      var vStorageField = "_cached" + vUpName;
      var vComputerName = "_compute" + vUpName;
      var vChangeName = "_change" + vUpName;
      if( typeof config.defaultValue !== "undefined" ) {
        proto[ vStorageField ] = config.defaultValue;
      }
      proto[ "get" + vUpName ] = function() {
        if( this[ vStorageField ] == null ) {
          this[ vStorageField ] = this[ vComputerName ]();
        }
        return this[ vStorageField ];
      };
      proto[ "_invalidate" + vUpName ] = function() {
        if( this[ vStorageField ] != null ) {
          this[ vStorageField ] = null;
          if( config.addToQueueRuntime ) {
            this.addToQueueRuntime( config.name );
          }
        }
      };
      proto[ "_recompute" + vUpName ] = function() {
        var vOld = this[ vStorageField ];
        var vNew = this[ vComputerName ]();
        if( vNew != vOld ) {
          this[ vStorageField ] = vNew;
          this[ vChangeName ]( vNew, vOld );
          return true;
        }
        return false;
      };
      proto[ vChangeName ] = function( /* vNew, vOld */ ) {};
      proto[ vComputerName ] = function() {
        return null;
      };
      proto[ "get" + vUpName ].self = proto.constructor;
      proto[ "_invalidate" + vUpName ].self = proto.constructor;
      proto[ "_recompute" + vUpName ].self = proto.constructor;
    }

  }

} );
