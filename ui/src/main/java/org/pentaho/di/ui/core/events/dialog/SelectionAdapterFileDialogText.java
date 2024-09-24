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

import org.eclipse.swt.widgets.Text;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.core.events.dialog.extension.ExtensionPointWrapper;

public class SelectionAdapterFileDialogText extends SelectionAdapterFileDialog<Text> {

  SelectionAdapterFileDialogText( LogChannelInterface log, Text textUiWidget, AbstractMeta meta,
                                  SelectionAdapterOptions options, RepositoryUtility repositoryUtility,
                                  ExtensionPointWrapper extensionPointWrapper ) {
    super( log, textUiWidget, meta, options, repositoryUtility, extensionPointWrapper );
  }

  public SelectionAdapterFileDialogText( LogChannelInterface log, Text textUiWidget, AbstractMeta meta,
                                         SelectionAdapterOptions options ) {
    super( log, textUiWidget, meta, options );
  }

  @Override
  protected String getText() {
    return this.getTextWidget().getText();
  }

  @Override
  protected void setText( String text ) {
    this.getTextWidget().setText( text );
  }
}
