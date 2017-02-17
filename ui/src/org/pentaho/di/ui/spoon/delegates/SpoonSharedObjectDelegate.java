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

package org.pentaho.di.ui.spoon.delegates;


import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.changed.ChangedFlagInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.ui.spoon.SharedObjectSyncUtil;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.List;

public abstract class SpoonSharedObjectDelegate extends SpoonDelegate {
  protected static final Class<?> PKG = Spoon.class;
  protected SharedObjectSyncUtil sharedObjectSyncUtil;

  public SpoonSharedObjectDelegate( Spoon spoon ) {
    super( spoon );
  }


  public void setSharedObjectSyncUtil( SharedObjectSyncUtil sharedObjectSyncUtil ) {
    this.sharedObjectSyncUtil = sharedObjectSyncUtil;
  }

  protected static boolean isDuplicate( List<? extends SharedObjectInterface> objects, SharedObjectInterface object ) {
    String newName = object.getName();
    for ( SharedObjectInterface soi : objects ) {
      if ( soi.getName().equalsIgnoreCase( newName ) ) {
        return true;
      }
    }
    return false;
  }

  protected <T extends SharedObjectInterface & RepositoryElementInterface & ChangedFlagInterface>
      void saveSharedObjectToRepository( T sharedObject, String versionComment ) throws KettleException {
    Repository rep = spoon.getRepository();
    if ( rep != null  ) {
      if ( !rep.getSecurityProvider().isReadOnly() ) {
        rep.save( sharedObject, versionComment, null );
        sharedObject.clearChanged();
      } else {
        throw new KettleException( BaseMessages.getString(
            PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser" ) );
      }
    }
  }

  protected void saveSharedObjects() {
    try {
      // flush to file for newly opened
      EngineMetaInterface meta = spoon.getActiveMeta();
      if ( meta != null ) {
        meta.saveSharedObjects();
      }
    } catch ( KettleException e ) {
      spoon.getLog().logError( e.getLocalizedMessage(), e );
    }
  }

  protected static String getMessage( String key ) {
    return BaseMessages.getString( PKG, key );
  }

  protected static String getMessage( String key, Object... params ) {
    return BaseMessages.getString( PKG, key, params );
  }

}
