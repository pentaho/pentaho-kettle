/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.mongodbinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class MongoDbInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = MongoDbInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private TextVar      wHostname;
  private TextVar      wPort;
  private TextVar      wDbName;
  private TextVar      wCollection;
  private TextVar      wJsonField;
  private TextVar      wJsonQuery;
	
  private TextVar wAuthUser;
  private TextVar wAuthPass;

	private MongoDbInputMeta input;

	public MongoDbInputDialog(Shell parent,  Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MongoDbInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		Control lastControl = wStepname; 

		// Hostname input ...
		//
		Label wlHostname = new Label(shell, SWT.RIGHT);
		wlHostname.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.Hostname.Label")); //$NON-NLS-1$
 		props.setLook(wlHostname);
		FormData fdlHostname = new FormData();
		fdlHostname.left = new FormAttachment(0, 0);
		fdlHostname.right= new FormAttachment(middle, -margin);
		fdlHostname.top  = new FormAttachment(lastControl, margin);
		wlHostname.setLayoutData(fdlHostname);
		wHostname=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wHostname);
		wHostname.addModifyListener(lsMod);
		FormData fdHostname = new FormData();
		fdHostname.left = new FormAttachment(middle, 0);
		fdHostname.top  = new FormAttachment(lastControl, margin);
		fdHostname.right= new FormAttachment(100, 0);
		wHostname.setLayoutData(fdHostname);
		lastControl = wHostname;

    // Port input ...
    //
    Label wlPort = new Label(shell, SWT.RIGHT);
    wlPort.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.Port.Label")); //$NON-NLS-1$
    props.setLook(wlPort);
    FormData fdlPort = new FormData();
    fdlPort.left = new FormAttachment(0, 0);
    fdlPort.right= new FormAttachment(middle, -margin);
    fdlPort.top  = new FormAttachment(lastControl, margin);
    wlPort.setLayoutData(fdlPort);
    wPort=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wPort);
    wPort.addModifyListener(lsMod);
    FormData fdPort = new FormData();
    fdPort.left = new FormAttachment(middle, 0);
    fdPort.top  = new FormAttachment(lastControl, margin);
    fdPort.right= new FormAttachment(100, 0);
    wPort.setLayoutData(fdPort);
    lastControl = wPort;

    // DbName input ...
    //
    Label wlDbName = new Label(shell, SWT.RIGHT);
    wlDbName.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.DbName.Label")); //$NON-NLS-1$
    props.setLook(wlDbName);
    FormData fdlDbName = new FormData();
    fdlDbName.left = new FormAttachment(0, 0);
    fdlDbName.right= new FormAttachment(middle, -margin);
    fdlDbName.top  = new FormAttachment(lastControl, margin);
    wlDbName.setLayoutData(fdlDbName);
    wDbName=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wDbName);
    wDbName.addModifyListener(lsMod);
    FormData fdDbName = new FormData();
    fdDbName.left = new FormAttachment(middle, 0);
    fdDbName.top  = new FormAttachment(lastControl, margin);
    fdDbName.right= new FormAttachment(100, 0);
    wDbName.setLayoutData(fdDbName);
    lastControl = wDbName;

    // Collection input ...
    //
    Label wlCollection = new Label(shell, SWT.RIGHT);
    wlCollection.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.Collection.Label")); //$NON-NLS-1$
    props.setLook(wlCollection);
    FormData fdlCollection = new FormData();
    fdlCollection.left = new FormAttachment(0, 0);
    fdlCollection.right= new FormAttachment(middle, -margin);
    fdlCollection.top  = new FormAttachment(lastControl, margin);
    wlCollection.setLayoutData(fdlCollection);
    wCollection=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wCollection);
    wCollection.addModifyListener(lsMod);
    FormData fdCollection = new FormData();
    fdCollection.left = new FormAttachment(middle, 0);
    fdCollection.top  = new FormAttachment(lastControl, margin);
    fdCollection.right= new FormAttachment(100, 0);
    wCollection.setLayoutData(fdCollection);
    lastControl = wCollection;

    // JsonField input ...
    //
    Label wlJsonField = new Label(shell, SWT.RIGHT);
    wlJsonField.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.JsonField.Label")); //$NON-NLS-1$
    props.setLook(wlJsonField);
    FormData fdlJsonField = new FormData();
    fdlJsonField.left = new FormAttachment(0, 0);
    fdlJsonField.right= new FormAttachment(middle, -margin);
    fdlJsonField.top  = new FormAttachment(lastControl, margin);
    wlJsonField.setLayoutData(fdlJsonField);
    wJsonField=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wJsonField);
    wJsonField.addModifyListener(lsMod);
    FormData fdJsonField = new FormData();
    fdJsonField.left = new FormAttachment(middle, 0);
    fdJsonField.top  = new FormAttachment(lastControl, margin);
    fdJsonField.right= new FormAttachment(100, 0);
    wJsonField.setLayoutData(fdJsonField);
    lastControl = wJsonField;

    // JSON Query input ...
    //
    Label wlJsonQuery = new Label(shell, SWT.RIGHT);
    wlJsonQuery.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.JsonQuery.Label")); //$NON-NLS-1$
    props.setLook(wlJsonQuery);
    FormData fdlJsonQuery = new FormData();
    fdlJsonQuery.left = new FormAttachment(0, 0);
    fdlJsonQuery.right= new FormAttachment(middle, -margin);
    fdlJsonQuery.top  = new FormAttachment(lastControl, margin);
    wlJsonQuery.setLayoutData(fdlJsonQuery);
    wJsonQuery=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wJsonQuery);
    wJsonQuery.addModifyListener(lsMod);
    FormData fdJsonQuery = new FormData();
    fdJsonQuery.left = new FormAttachment(middle, 0);
    fdJsonQuery.top  = new FormAttachment(lastControl, margin);
    fdJsonQuery.right= new FormAttachment(100, 0);
    wJsonQuery.setLayoutData(fdJsonQuery);
    lastControl = wJsonQuery;

    // Authentication...
    //
    // AuthUser line
    Label wlAuthUser = new Label(shell, SWT.RIGHT);
    wlAuthUser.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.AuthenticationUser.Label"));
    props.setLook(wlAuthUser);
    FormData fdlAuthUser = new FormData();
    fdlAuthUser.left = new FormAttachment(0, -margin);
    fdlAuthUser.top = new FormAttachment(lastControl, margin);
    fdlAuthUser.right = new FormAttachment(middle, -margin);
    wlAuthUser.setLayoutData(fdlAuthUser);

    wAuthUser = new TextVar(transMeta, shell, SWT.BORDER | SWT.READ_ONLY);
    wAuthUser.setEditable(true);
    props.setLook(wAuthUser);
    wAuthUser.addModifyListener(lsMod);
    FormData fdAuthUser = new FormData();
    fdAuthUser.left = new FormAttachment(middle, 0);
    fdAuthUser.top = new FormAttachment(lastControl, margin);
    fdAuthUser.right = new FormAttachment(100, 0);
    wAuthUser.setLayoutData(fdAuthUser);
    lastControl = wAuthUser;

    // AuthPass line
    Label wlAuthPass = new Label(shell, SWT.RIGHT);
    wlAuthPass.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.AuthenticationPassword.Label"));
    props.setLook(wlAuthPass);
    FormData fdlAuthPass = new FormData();
    fdlAuthPass.left = new FormAttachment(0, -margin);
    fdlAuthPass.top = new FormAttachment(lastControl, margin);
    fdlAuthPass.right = new FormAttachment(middle, -margin);
    wlAuthPass.setLayoutData(fdlAuthPass);
        
    wAuthPass = new TextVar(transMeta, shell, SWT.BORDER | SWT.READ_ONLY);
    wAuthPass.setEditable(true);
    wAuthPass.setEchoChar('*');
    props.setLook(wAuthPass);
    wAuthPass.addModifyListener(lsMod);
    FormData fdAuthPass = new FormData();
    fdAuthPass.left = new FormAttachment(middle, 0);
    fdAuthPass.top = new FormAttachment(wAuthUser, margin);
    fdAuthPass.right = new FormAttachment(100, 0);
    wAuthPass.setLayoutData(fdAuthPass);
    lastControl = wAuthPass;
    
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
    wPreview=new Button(shell, SWT.PUSH);
    wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, lastControl);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
    lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
    wPreview.addListener(SWT.Selection, lsPreview);
		wOK.addListener    (SWT.Selection, lsOK    );

		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wHostname.addSelectionListener( lsDef );
		
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wHostname.setText(Const.NVL(input.getHostname(), "")); //$NON-NLS-1$
    wPort.setText(Const.NVL(input.getPort(), "")); //$NON-NLS-1$
    wDbName.setText(Const.NVL(input.getDbName(), "")); //$NON-NLS-1$
    wCollection.setText(Const.NVL(input.getCollection(), "")); //$NON-NLS-1$
    wJsonField.setText(Const.NVL(input.getJsonFieldName(), "")); //$NON-NLS-1$
    wJsonQuery.setText(Const.NVL(input.getJsonQuery(), "")); //$NON-NLS-1$
    
    wAuthUser.setText(Const.NVL(input.getAuthenticationUser(), "")); // $NON-NLS-1$
    wAuthPass.setText(Const.NVL(input.getAuthenticationPassword(), "")); // $NON-NLS-1$

    wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}

  private void getInfo(MongoDbInputMeta meta) {

    meta.setHostname(wHostname.getText());
    meta.setPort(wPort.getText());
    meta.setDbName(wDbName.getText());
    meta.setCollection(wCollection.getText());
    meta.setJsonFieldName(wJsonField.getText());
    meta.setJsonQuery(wJsonQuery.getText());
    
    meta.setAuthenticationUser(wAuthUser.getText());
    meta.setAuthenticationPassword(wAuthPass.getText());
  }

	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value

		getInfo(input);
    
		dispose();
	}
	
	 // Preview the data
  private void preview()
  {
      // Create the XML input step
      MongoDbInputMeta oneMeta = new MongoDbInputMeta();
      getInfo(oneMeta);
      
      TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
      
      EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), 
          BaseMessages.getString(PKG, "MongoDbInputDialog.PreviewSize.DialogTitle"), 
          BaseMessages.getString(PKG, "MongoDbInputDialog.PreviewSize.DialogMessage")
        );
      int previewSize = numberDialog.open();
      if (previewSize>0)
      {
          TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
          progressDialog.open();

          Trans trans = progressDialog.getTrans();
          String loggingText = progressDialog.getLoggingText();

          if (!progressDialog.isCancelled())
          {
              if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
              {
                EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                    BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
                etd.setReadOnly();
                etd.open();
              }
          }
          
          PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
          prd.open();
      }
  }

}
