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

package org.pentaho.di.ui.util;

import java.util.Collection;

import org.pentaho.di.core.Const;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.shared.SharedObjectInterface;

/**
 * @author Andrey Khayrutdinov
 */
public class DialogUtils {

  public static String getPathOf( RepositoryElementMetaInterface object ) {
    if ( object != null && !object.isDeleted() ) {
      RepositoryDirectoryInterface directory = object.getRepositoryDirectory();
      if ( directory != null ) {
        String path = directory.getPath();
        if ( path != null ) {
          if ( !path.endsWith( "/" ) ) {
            path += "/";
          }
          path += object.getName();

          return path;
        }
      }
    }
    return null;
  }

  public static boolean objectWithTheSameNameExists( SharedObjectInterface object,
      Collection<? extends SharedObjectInterface> scope ) {
    for ( SharedObjectInterface element : scope ) {
      String elementName = element.getName();
      if ( elementName != null && elementName.equalsIgnoreCase( object.getName() ) && object != element ) {
        return true;
      }
    }
    return false;
  }

  public static String getPath( String parentPath, String path ) {
    if ( !parentPath.equals( "/" ) && path.startsWith( parentPath ) ) {
      path = path.replace( parentPath, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
    }
    return path;
  }

}
