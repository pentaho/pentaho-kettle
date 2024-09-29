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
package org.pentaho.di.ui.core.events.dialog;

import org.eclipse.swt.events.SelectionEvent;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.core.events.dialog.extension.ExtensionPointWrapper;
import org.pentaho.di.ui.core.events.dialog.extension.SpoonOpenExtensionPointWrapper;
import org.pentaho.di.ui.core.widget.TextVar;

/**
 *  This Adapter class extends the SelectionAdapterFileDialogTextVar but includes (DetermineSelectionOperationOp) Interface to
 *  allow the dialog to determine which SelectionOperation (File or Folder) when the widget is selected. This is useful
 *  for steps that include "File Or Directory" option based on the dialog settings.
 *
 *  Example:
 *     SelectionAdapterOptions options = new SelectionAdapterOptions( SelectionOperation.FILE,
 *          new FilterType[] { FilterType.ALL, FilterType.XML }, FilterType.XML );
 *     adapter = new ConditionSelectionAdapterFileDialogTextVar( log, wFilename, transMeta, options,
 *          () -> ( [ Is Folder Condition ] ) ? SelectionOperation.FOLDER : SelectionOperation.FILE );
 *  Note:
 *    The class only updates SelectionOperation to Folder the other options remain the same.
 */
public class ConditionSelectionAdapterFileDialogTextVar extends SelectionAdapterFileDialogTextVar {
  private DetermineSelectionOperationOp operator;

  ConditionSelectionAdapterFileDialogTextVar( LogChannelInterface log, TextVar textUiWidget, AbstractMeta meta,
                                              SelectionAdapterOptions options, RepositoryUtility repositoryUtility,
                                              ExtensionPointWrapper extensionPointWrapper, DetermineSelectionOperationOp operator ) {
    super( log, textUiWidget, meta, options, repositoryUtility, extensionPointWrapper );
    this.operator = operator;
  }

  public ConditionSelectionAdapterFileDialogTextVar( LogChannelInterface log, TextVar textUiWidget, AbstractMeta meta,
                                                    SelectionAdapterOptions options, DetermineSelectionOperationOp operator ) {
    this( log, textUiWidget, meta, options, new RepositoryUtility(), new SpoonOpenExtensionPointWrapper(), operator );
  }

  @Override
  public void widgetSelected( SelectionEvent selectionEvent ) {

    // Replace the SelectionOperation  based on the operator result.
    SelectionOperation result = operator.op();
    if ( result != null ) {
      this.getSelectionOptions().setSelectionOperation( result );
    }

    super.widgetSelected( selectionEvent );
  }

}
