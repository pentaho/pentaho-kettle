/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.msaccess;

import java.io.File;
import java.io.IOException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.util.LinkResolver;

/**
 * Opens and creates Access databases with linked table resolution disabled.
 * <p>
 * Jackcess's default {@link LinkResolver} opens whatever file path a linked table points to. That path comes from
 * the Access file itself, so a crafted file could make PDI open an arbitrary local file or a remote (UNC) path.
 * Every Access database used by this plugin must be obtained through this class.
 */
public final class MSAccessDatabaseUtil {
  private static final Class<?> PKG = MSAccessDatabaseUtil.class; // for i18n purposes, needed by Translator2!!

  static final LinkResolver LINKED_DATABASES_DISABLED = ( linkerDb, linkeeFileName ) -> {
    throw new IOException(
      BaseMessages.getString( PKG, "MSAccessDatabaseUtil.Error.LinkedTableNotAllowed", linkeeFileName ) );
  };

  private MSAccessDatabaseUtil() {
  }

  public static Database open( File file, boolean readOnly ) throws IOException {
    return disableLinkedDatabases( new DatabaseBuilder( file ).setReadOnly( readOnly ).open() );
  }

  public static Database create( File file, Database.FileFormat fileFormat ) throws IOException {
    return disableLinkedDatabases( new DatabaseBuilder( file ).setFileFormat( fileFormat ).create() );
  }

  public static Table getTable( Database db, String tableName ) throws KettleException {
    try {
      return db.getTable( tableName );
    } catch ( IOException e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "MSAccessDatabaseUtil.Error.UnableToReadTable", tableName ), e );
    }
  }

  private static Database disableLinkedDatabases( Database db ) {
    db.setLinkResolver( LINKED_DATABASES_DISABLED );

    return db;
  }
}
