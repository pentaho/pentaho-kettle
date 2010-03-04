package org.pentaho.di.core.database;

import org.pentaho.di.core.plugins.DatabaseMetaPlugin;


@DatabaseMetaPlugin( type="INFOBRIGHT", typeDescription="Infobright" )
public class InfobrightDatabaseMeta extends MySQLDatabaseMeta implements DatabaseInterface {

	// Only has a different ID to catch exceptions here and there.

}
