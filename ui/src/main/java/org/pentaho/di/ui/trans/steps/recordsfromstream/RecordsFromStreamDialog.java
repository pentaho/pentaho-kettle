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
