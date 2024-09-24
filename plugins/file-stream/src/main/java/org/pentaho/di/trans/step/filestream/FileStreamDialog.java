/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.filestream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.di.ui.core.widget.VFSFileSelection;
import org.pentaho.di.ui.trans.step.BaseStreamingDialog;

public class FileStreamDialog extends BaseStreamingDialog implements StepDialogInterface {

  private static Class<?> PKG = FileStreamMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private FileStreamMeta meta;

  private VFSFileSelection wFileSelection;

  public FileStreamDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, in, tr, sname );
    meta = (FileStreamMeta) in;
  }

  @Override protected String getDialogTitle() {
    return BaseMessages.getString( PKG, "FileStreamDialog.Shell.Title" );
  }

  @Override protected void buildSetup( Composite wSetupComp ) {
    Label wlSourcePath = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlSourcePath );
    wlSourcePath.setText( BaseMessages.getString( PKG, "FileStreamDialog.SourcePath" ) );
    FormData fdlTransPath = new FormData();
    fdlTransPath.left = new FormAttachment( 0, 0 );
    fdlTransPath.top = new FormAttachment( 0, 0 );
    fdlTransPath.right = new FormAttachment( 50, 0 );
    wlSourcePath.setLayoutData( fdlTransPath );

    wFileSelection = new VFSFileSelection(
      wSetupComp,
      SWT.NONE,
      new String[]{ "*" },
      new String[] { BaseMessages.getString( Const.class, "Const.FileFilter.All" ) },
      transMeta );
    FormData fdFileSelection = new FormData();
    fdFileSelection.left = new FormAttachment( 0, 0 );
    fdFileSelection.top = new FormAttachment( wlSourcePath, 5 );
  }

  @Override protected void additionalOks( BaseStreamStepMeta meta ) {
    ( (FileStreamMeta) meta ).setSourcePath( wFileSelection.wFileName.getText() );
  }

  @Override protected void createAdditionalTabs() {
    shell.setText( BaseMessages.getString( PKG, "FileStreamDialog.Shell.Title" ) );
  }

  @Override protected int[] getFieldTypes() {
    return new int[]{ ValueMetaInterface.TYPE_STRING };
  }

  @Override protected String[] getFieldNames() {
    return new String[]{ "line" };
  }

  @Override protected void getData() {
    super.getData();
    if ( meta.getSourcePath() != null ) {
      wFileSelection.wFileName.setText( meta.getSourcePath() );
    }
  }
}
