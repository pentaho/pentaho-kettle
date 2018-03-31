/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.ui.core.FileDialogOperation;

/**
 * Created by bmorrise on 8/17/17.
 */
public class DialogHelper {

  public static RepositoryObject selectRepositoryObject( String filter, LogChannel log ) {
    try {
      FileDialogOperation fileDialogOperation =
        new FileDialogOperation( FileDialogOperation.OPEN, FileDialogOperation.ORIGIN_OTHER );
      if ( !Utils.isEmpty( filter ) ) {
        fileDialogOperation.setFilter( filter );
      }
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonOpenSaveRepository.id,
        fileDialogOperation );
      return (RepositoryObject) fileDialogOperation.getRepositoryObject();
    } catch ( KettleException ke ) {
      // Ignore
    }

    return null;
  }

}
