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

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.core.events.dialog.extension.ExtensionPointWrapper;
import org.pentaho.di.ui.core.widget.TextVar;

public class SelectionAdapterFileDialogTextVar extends SelectionAdapterFileDialog<TextVar> {

  SelectionAdapterFileDialogTextVar( LogChannelInterface log, TextVar textUiWidget, AbstractMeta meta,
                                     SelectionAdapterOptions options, RepositoryUtility repositoryUtility,
                                     ExtensionPointWrapper extensionPointWrapper ) {
    super( log, textUiWidget, meta, options, repositoryUtility, extensionPointWrapper );
  }

  public SelectionAdapterFileDialogTextVar( LogChannelInterface log, TextVar textUiWidget, AbstractMeta meta,
                                            SelectionAdapterOptions options ) {
    super( log, textUiWidget, meta, options );
  }

  @Override protected String getText() {
    return this.getTextWidget().getText();
  }

  @Override protected void setText( String text ) {
    this.getTextWidget().setText( text );
  }
}
