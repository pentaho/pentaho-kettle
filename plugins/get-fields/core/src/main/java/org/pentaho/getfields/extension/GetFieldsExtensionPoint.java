/*
 * Copyright 2018 Hitachi Vantara. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */

package org.pentaho.getfields.extension;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.core.GetFieldsDialogOperation;
import org.pentaho.getfields.dialog.GetFieldsDialog;

/**
 * Created by ddiroma on 08/13/18.
 */

@ExtensionPoint(
  id = "GetFieldsExtensionPoint",
  extensionPointId = "GetFieldsExtension",
  description = "Get Fields dialog"
)
public class GetFieldsExtensionPoint implements ExtensionPointInterface {

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    GetFieldsDialogOperation getFieldsDialogOperation = (GetFieldsDialogOperation) o;
    GetFieldsDialog getFieldsDialog = new GetFieldsDialog( getFieldsDialogOperation.getShell(),
            getFieldsDialogOperation.getWidth(), getFieldsDialogOperation.getHeight(), getFieldsDialogOperation
            .getFilename() );
    getFieldsDialog.setTitle( getFieldsDialogOperation.getStepMeta().getName() );
    getFieldsDialog.open();
    getFieldsDialogOperation.setPaths( getFieldsDialog.getPaths() );
  }
}
