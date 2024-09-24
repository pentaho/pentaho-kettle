/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.core.events.dialog.extension;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.core.FileDialogOperation;

public class SpoonOpenExtensionPointWrapper implements ExtensionPointWrapper {

  public SpoonOpenExtensionPointWrapper() {
    //empty constructor
  }

  @Override
  public void callExtensionPoint( LogChannelInterface log, String id, Object object ) throws KettleException {
    FileDialogOperation fileDialogOperation = (FileDialogOperation) object;
    ExtensionPointHandler.callExtensionPoint( log, id, fileDialogOperation );
  }
}
