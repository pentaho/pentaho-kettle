/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjectsManagementInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TreeUtil;
import org.pentaho.di.ui.spoon.SharedObjectSyncUtil;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

public abstract class SpoonSharedObjectDelegate<T extends SharedObjectInterface<T> & RepositoryElementInterface & ChangedFlagInterface> extends SpoonDelegate {
  protected static final Class<?> PKG = Spoon.class;
  protected SharedObjectSyncUtil sharedObjectSyncUtil;

  public SpoonSharedObjectDelegate( Spoon spoon ) {
    super( spoon );
  }


  public void setSharedObjectSyncUtil( SharedObjectSyncUtil sharedObjectSyncUtil ) {
    this.sharedObjectSyncUtil = sharedObjectSyncUtil;
  }

  protected static <T extends SharedObjectInterface<T>> boolean isDuplicate( List<T> objects, T object ) {
    String newName = object.getName();
    for ( T soi : objects ) {
      if ( soi.getName().equalsIgnoreCase( newName ) ) {
        return true;
      }
    }
    return false;
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

  protected void moveCopy( SharedObjectsManagementInterface<T> srcManager,
      SharedObjectsManagementInterface<T> targetManager, T object, boolean deleteFromSource,
      String overWritePromptKey ) throws KettleException {
    try {
      // If object already exist, prompt for overwrite
      if ( findObject( targetManager.getAll(), object.getName() ) != null ) {
        if ( !shouldOverwrite( BaseMessages.getString( PKG, overWritePromptKey, object.getName() ) ) ) {
          return;
        }
      }
      // Add the object to target
      targetManager.add( object );
      if ( deleteFromSource ) {
        srcManager.remove( object );
      }
      refreshTree();
    } catch ( Exception exception ) {
      new ErrorDialog( spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingSlave.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message", object.getName() ), exception );
    }
  }

  @FunctionalInterface
  public interface ShowEditDialog<T> {
    void accept( T object, List<T> objects ) throws KettleException;
  }

  protected void dupeSharedObject( SharedObjectsManagementInterface<T> manager, T object, ShowEditDialog<T> showDialog ) {
    String originalName = object.getName().trim();

    try {
      List<T> objects = manager.getAll();
      Set<String> names = getNames( objects );

      //Clone the object
      T copy = object.makeClone();
      String newName = TreeUtil.findUniqueSuffix( originalName, names );
      copy.setName( newName );

      showDialog.accept( copy, objects );

      refreshTree();
      spoon.refreshGraph();
    } catch ( KettleException exception ) {
      new ErrorDialog( spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingSlave.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message", object.getName() ), exception );
    }
  }

  private Set<String> getNames( List<T> objects ) {
    return objects.stream().map( SharedObjectInterface::getName ).collect( Collectors.toSet() );
  }

  protected T findObject( List<T> objects, String name ) {
    for ( T object : objects ) {
      if ( object.getName() != null && object.getName().equalsIgnoreCase( name ) ) {
        return object;
      }
    }
    return null;
  }

  protected boolean shouldOverwrite( String message ) {
    MessageBox mb = new MessageBox( spoon.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION );
    mb.setMessage( message );
    mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.PromptOverwriteTransformation.Title" ) );
    int response = mb.open();

    if ( response != SWT.YES ) {
      return false;
    }
    return true;
  }

  protected abstract void refreshTree();

  protected static String getMessage( String key ) {
    return BaseMessages.getString( PKG, key );
  }

  protected static String getMessage( String key, Object... params ) {
    return BaseMessages.getString( PKG, key, params );
  }

}
