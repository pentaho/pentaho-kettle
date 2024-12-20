/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.util;

import org.pentaho.di.core.Const;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.shared.SharedObjectInterface;

import java.util.Collection;
import java.util.Iterator;

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
    String objectName = object.getName().trim();
    for ( SharedObjectInterface element : scope ) {
      String elementName = element.getName().trim();
      if ( elementName != null && elementName.equalsIgnoreCase( objectName ) && object != element ) {
        return true;
      }
    }
    return false;
  }

  public static void removeMatchingObject( String nameToRemove, Collection<? extends SharedObjectInterface> objects ) {
    Iterator<? extends SharedObjectInterface> iter = objects.iterator();
    while ( iter.hasNext() ) {
      if ( nameToRemove.equals( iter.next().getName() ) ) {
        iter.remove();
      }
    }
  }

  public static String getPath( String parentPath, String path ) {
    if ( !parentPath.equals( "/" ) && path.startsWith( parentPath ) ) {
      path = path.replace( parentPath, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
    }
    return path;
  }

}
