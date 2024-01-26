/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.mondrianinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.mondrianinput.MondrianInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.core.annotations.PluginDialog;

@PluginDialog( id = "MondrianInput", image = "MON.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "mk-95pdia003/pdi-transformation-steps/mondrian-input" )
public class MondrianInputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = MondrianInputMeta.class; // for i18n purposes, needed by Translator2!!

  private CCombo wConnection;

  private Label wlSQL;
  private StyledTextComp wSQL;
  private FormData fdlSQL, fdSQL;

  private Label wlCatalog;
  private TextVar wCatalog;
  private Button wbbFilename; // Browse for a file
  private FormData fdlCatalog, fdCatalog;

  private MondrianInputMeta input;

  private Label wlPosition;
  private FormData fdlPosition;

  private Label wlVariables;
  private Button wVariables;
  private FormData fdlVariables, fdVariables;

  private TextVar wRole;

  public MondrianInputDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (MondrianInputMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "MondrianInputDialog.MondrianInput" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    //
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "MondrianInputDialog.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    // Connection line
    //
    wConnection = addConnectionLine( shell, wStepname, middle, margin );
    if ( input.getDatabaseMeta() == null && transMeta.nrDatabases() == 1 ) {
      wConnection.select( 0 );
    }
    wConnection.addModifyListener( lsMod );

    // Some buttons
    //
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "System.Button.Preview" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, null );

    // Mondrian Role
    Label wlRole = new Label( shell, SWT.RIGHT );
    wlRole.setText( BaseMessages.getString( PKG, "MondrianInputDialog.Role" ) );
    props.setLook( wlRole );
    FormData fdlRole = new FormData();
    fdlRole.left = new FormAttachment( 0, 0 );
    fdlRole.right = new FormAttachment( middle, -margin );
    fdlRole.bottom = new FormAttachment( wOK, -2 * margin );
    wlRole.setLayoutData( fdlRole );
    wRole = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wRole );
    wRole.addModifyListener( lsMod );
    wRole.setToolTipText( BaseMessages.getString( PKG, "MondrianInputDialog.Role.Tooltip" ) );
    FormData fdRole = new FormData();
    fdRole.left = new FormAttachment( middle, 0 );
    fdRole.right = new FormAttachment( 100, -margin );
    fdRole.bottom = new FormAttachment( wOK, -2 * margin );
    wRole.setLayoutData( fdRole );

    // Catalog location...
    //
    wbbFilename = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbbFilename );
    wbbFilename.setText( BaseMessages.getString( "System.Button.Browse" ) );
    wbbFilename.setToolTipText( BaseMessages.getString( "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
    FormData fdbFilename = new FormData();
    fdbFilename.right = new FormAttachment( 100, 0 );
    fdbFilename.bottom = new FormAttachment( wRole, -2 * margin );
    wbbFilename.setLayoutData( fdbFilename );

    wlCatalog = new Label( shell, SWT.RIGHT );
    wlCatalog.setText( BaseMessages.getString( PKG, "MondrianInputDialog.Catalog" ) );
    props.setLook( wlCatalog );
    fdlCatalog = new FormData();
    fdlCatalog.left = new FormAttachment( 0, 0 );
    fdlCatalog.right = new FormAttachment( middle, -margin );
    fdlCatalog.bottom = new FormAttachment( wRole, -2 * margin );
    wlCatalog.setLayoutData( fdlCatalog );
    wCatalog = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wCatalog );
    wCatalog.addModifyListener( lsMod );
    fdCatalog = new FormData();
    fdCatalog.left = new FormAttachment( middle, 0 );
    fdCatalog.right = new FormAttachment( wbbFilename, -margin );
    fdCatalog.bottom = new FormAttachment( wRole, -2 * margin );
    wCatalog.setLayoutData( fdCatalog );

    // Replace variables in MDX?
    //
    wlVariables = new Label( shell, SWT.RIGHT );
    wlVariables.setText( BaseMessages.getString( PKG, "MondrianInputDialog.ReplaceVariables" ) );
    props.setLook( wlVariables );
    fdlVariables = new FormData();
    fdlVariables.left = new FormAttachment( 0, 0 );
    fdlVariables.right = new FormAttachment( middle, -margin );
    fdlVariables.bottom = new FormAttachment( wCatalog, -margin );
    wlVariables.setLayoutData( fdlVariables );
    wVariables = new Button( shell, SWT.CHECK );
    props.setLook( wVariables );
    wVariables.setToolTipText( BaseMessages.getString( PKG, "MondrianInputDialog.ReplaceVariables.Tooltip" ) );
    fdVariables = new FormData();
    fdVariables.left = new FormAttachment( middle, 0 );
    fdVariables.right = new FormAttachment( 100, 0 );
    fdVariables.bottom = new FormAttachment( wCatalog, -margin );
    wVariables.setLayoutData( fdVariables );
    wVariables.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        setSQLToolTip();
        input.setChanged();
      }
    } );

    wlPosition = new Label( shell, SWT.NONE );
    props.setLook( wlPosition );
    fdlPosition = new FormData();
    fdlPosition.left = new FormAttachment( 0, 0 );
    fdlPosition.right = new FormAttachment( 100, 0 );
    fdlPosition.bottom = new FormAttachment( wVariables, -margin );
    wlPosition.setLayoutData( fdlPosition );

    // Table line...
    //
    wlSQL = new Label( shell, SWT.NONE );
    wlSQL.setText( BaseMessages.getString( PKG, "MondrianInputDialog.SQL" ) );
    props.setLook( wlSQL );
    fdlSQL = new FormData();
    fdlSQL.left = new FormAttachment( 0, 0 );
    fdlSQL.top = new FormAttachment( wConnection, margin * 2 );
    wlSQL.setLayoutData( fdlSQL );

    wSQL =
      new StyledTextComp( transMeta, shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "" );
    props.setLook( wSQL, Props.WIDGET_STYLE_FIXED );
    wSQL.addModifyListener( lsMod );
    fdSQL = new FormData();
    fdSQL.left = new FormAttachment( 0, 0 );
    fdSQL.top = new FormAttachment( wlSQL, margin );
    fdSQL.right = new FormAttachment( 100, -2 * margin );
    fdSQL.bottom = new FormAttachment( wlPosition, -margin );
    wSQL.setLayoutData( fdSQL );

    wSQL.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        setSQLToolTip();
        setPosition();
      }
    } );

    wSQL.addKeyListener( new KeyAdapter() {
      public void keyPressed( KeyEvent e ) {
        setPosition();
      }

      public void keyReleased( KeyEvent e ) {
        setPosition();
      }
    } );
    wSQL.addFocusListener( new FocusAdapter() {
      public void focusGained( FocusEvent e ) {
        setPosition();
      }

      public void focusLost( FocusEvent e ) {
        setPosition();
      }
    } );
    wSQL.addMouseListener( new MouseAdapter() {
      public void mouseDoubleClick( MouseEvent e ) {
        setPosition();
      }

      public void mouseDown( MouseEvent e ) {
        setPosition();
      }

      public void mouseUp( MouseEvent e ) {
        setPosition();
      }
    } );

    // Text Higlighting
    wSQL.addLineStyleListener( new MDXValuesHighlight() );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wPreview.addListener( SWT.Selection, lsPreview );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wCatalog.addSelectionListener( lsDef );

      // Listen to the browse button next to the file name
    wbbFilename.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wCatalog, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FILE,
        new FilterType[] { FilterType.XML, FilterType.ALL }, FilterType.XML ) ) );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    input.setChanged( changed );

    // Set the shell size, based upon previous time...
    setSize();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  public void setPosition() {

    String scr = wSQL.getText();
    int linenr = wSQL.getLineAtOffset( wSQL.getCaretOffset() ) + 1;
    int posnr = wSQL.getCaretOffset();

    // Go back from position to last CR: how many positions?
    int colnr = 0;
    while ( posnr > 0 && scr.charAt( posnr - 1 ) != '\n' && scr.charAt( posnr - 1 ) != '\r' ) {
      posnr--;
      colnr++;
    }
    wlPosition.setText( BaseMessages
      .getString( PKG, "MondrianInputDialog.Position.Label", "" + linenr, "" + colnr ) );

  }

  protected void setSQLToolTip() {
    if ( wVariables.getSelection() ) {
      wSQL.setToolTipText( transMeta.environmentSubstitute( wSQL.getText() ) );
    }
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( input.getSQL() != null ) {
      wSQL.setText( input.getSQL() );
    }
    if ( input.getDatabaseMeta() != null ) {
      wConnection.setText( input.getDatabaseMeta().getName() );
    }
    if ( input.getCatalog() != null ) {
      wCatalog.setText( input.getCatalog() );
    }
    if ( input.getRole() != null ) {
      wRole.setText( input.getRole() );
    }
    wVariables.setSelection( input.isVariableReplacementActive() );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void getInfo( MondrianInputMeta meta ) {
    meta.setSQL( wSQL.getText() );
    meta.setDatabaseMeta( transMeta.findDatabase( wConnection.getText() ) );
    meta.setCatalog( wCatalog.getText() );
    meta.setVariableReplacementActive( wVariables.getSelection() );
    meta.setRole( wRole.getText() );
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    stepname = wStepname.getText(); // return value
    // copy info to TextFileInputMeta class (input)

    getInfo( input );

    if ( input.getDatabaseMeta() == null ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "MondrianInputDialog.SelectValidConnection" ) );
      mb.setText( BaseMessages.getString( PKG, "MondrianInputDialog.DialogCaptionError" ) );
      mb.open();
      return;
    }

    dispose();
  }

  /**
   * Preview the data generated by this step. This generates a transformation using this step & a dummy and previews it.
   *
   */
  private void preview() {
    // Create the table input reader step...
    MondrianInputMeta oneMeta = new MondrianInputMeta();
    getInfo( oneMeta );

    TransMeta previewMeta =
      TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

    EnterNumberDialog numberDialog = new EnterNumberDialog( shell, props.getDefaultPreviewSize(),
      BaseMessages.getString( PKG, "MondrianInputDialog.EnterPreviewSize" ),
      BaseMessages.getString( PKG, "MondrianInputDialog.NumberOfRowsToPreview" ) );
    int previewSize = numberDialog.open();
    if ( previewSize > 0 ) {
      TransPreviewProgressDialog progressDialog =
        new TransPreviewProgressDialog(
          shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
      progressDialog.open();

      Trans trans = progressDialog.getTrans();
      String loggingText = progressDialog.getLoggingText();

      if ( !progressDialog.isCancelled() ) {
        if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
          EnterTextDialog etd =
            new EnterTextDialog(
              shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
                .getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
          etd.setReadOnly();
          etd.open();
        }
      }

      PreviewRowsDialog prd =
        new PreviewRowsDialog(
          shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname
            .getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
      prd.open();
    }
  }
}
