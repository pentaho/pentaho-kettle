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
