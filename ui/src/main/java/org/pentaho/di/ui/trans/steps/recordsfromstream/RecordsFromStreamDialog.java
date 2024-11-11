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

package org.pentaho.di.ui.trans.steps.recordsfromstream;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.recordsfromstream.RecordsFromStreamMeta;
import org.pentaho.di.ui.trans.steps.rowsfromresult.RowsFromResultDialog;

public class RecordsFromStreamDialog extends RowsFromResultDialog {
  private static Class<?> PKG = RecordsFromStreamMeta.class; // for i18n purposes, needed by Translator2!!

  @Override public String getTitle() {
    return BaseMessages.getString( PKG, "RecordsFromStreamDialog.Shell.Title" );
  }

  public RecordsFromStreamDialog( final Shell parent, final Object in,
                                  final TransMeta transMeta,
                                  final String sname ) {
    super( parent, in, transMeta, sname );
  }
}
