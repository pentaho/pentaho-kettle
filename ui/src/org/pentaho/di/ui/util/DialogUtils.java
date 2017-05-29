/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
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
    return objectWithTheSameNameExists( object, scope, false );
  }

  public static boolean objectWithTheSameNameExists( SharedObjectInterface object,
                                                     Collection<? extends SharedObjectInterface> scope,
                                                     boolean trimSpaces ) {
    String objectName = object.getName();
    if ( trimSpaces ) {
      objectName = StringUtils.trim( objectName );
    }
    for ( SharedObjectInterface element : scope ) {
      String elementName = element.getName();
      if ( trimSpaces ) {
        elementName = StringUtils.trim( elementName );
      }
      if ( elementName != null && elementName.equalsIgnoreCase( objectName ) && object != element ) {
        return true;
      }
    }
    return false;
  }

}
