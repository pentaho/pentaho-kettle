/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.delegates;


import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.List;

public abstract class SpoonSharedObjectDelegate extends SpoonDelegate {
  protected static final Class<?> PKG = Spoon.class;

  public SpoonSharedObjectDelegate( Spoon spoon ) {
    super( spoon );
  }


  protected static boolean isDuplicate( List<? extends SharedObjectInterface> objects, SharedObjectInterface object ) {
    String newName = object.getName();
    for ( SharedObjectInterface cluster : objects ) {
      if ( cluster.getName().equalsIgnoreCase( newName ) ) {
        return true;
      }
    }
    return false;
  }


  protected static String getMessage( String key ) {
    return BaseMessages.getString( PKG, key );
  }

  protected static String getMessage( String key, Object... params ) {
    return BaseMessages.getString( PKG, key, params );
  }
}
