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


package org.pentaho.di.core.database;

import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

/**
 * Centralized factory for instantiating {@link DatabaseInterface} instances based on the passed database type.
 * <p>
 * This is the single place where the database type is inspected to decide which {@link DatabaseInterface}
 * implementation to create. Keeping the type-detection logic here avoids the scattered {@code instanceof} checks and
 * conditional branches that would otherwise leak into business logic across the codebase: callers ask the factory for
 * an instance and never inspect the type themselves.
 * <p>
 * Resolution rules:
 * <ul>
 *   <li>A blank type identifies a Connection Management Service connection, which carries no local database type and
 *       is resolved at runtime by its connection id. A {@link ConnectionManagementServiceMeta} placeholder is
 *       returned.</li>
 *   <li>Any other value is treated as a database plugin id or name and resolved through the {@link PluginRegistry}.</li>
 * </ul>
 */
public final class DatabaseInterfaceFactory {

  private DatabaseInterfaceFactory() {
    // Utility class: no instances.
  }

  /**
   * Creates the {@link DatabaseInterface} for the given database type.
   *
   * @param databaseTypeDesc
   *          the database type to instantiate (a plugin id or description), or blank for a Connection Management
   *          Service connection
   * @return the {@link DatabaseInterface} matching the requested type
   * @throws KettleDatabaseException
   *           when the type could not be found or referenced
   */
  public static DatabaseInterface create( String databaseTypeDesc ) throws KettleDatabaseException {
    if ( isConnectionManagementServiceType( databaseTypeDesc ) ) {
      return new ConnectionManagementServiceMeta();
    }

    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.getPlugin( DatabasePluginType.class, databaseTypeDesc );
    if ( plugin == null ) {
      plugin = registry.findPluginWithName( DatabasePluginType.class, databaseTypeDesc );
    }

    if ( plugin == null ) {
      throw new KettleDatabaseException( "database type with plugin id ["
        + databaseTypeDesc + "] couldn't be found!" );
    }

    return DatabaseMeta.getDatabaseInterfacesMap().get( plugin.getIds()[0] );
  }

  /**
   * Tells whether the given database type denotes a Connection Management Service connection. This is the single point
   * of truth for that decision so callers do not need to perform their own {@code instanceof} or string checks.
   *
   * @param databaseTypeDesc
   *          the database type to inspect
   * @return {@code true} when the type identifies a Connection Management Service connection
   */
  public static boolean isConnectionManagementServiceType( String databaseTypeDesc ) {
    return StringUtils.isBlank( databaseTypeDesc );
  }
}
