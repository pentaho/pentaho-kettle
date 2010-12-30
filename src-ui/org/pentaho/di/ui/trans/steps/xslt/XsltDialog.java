 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
/*
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.trans.steps.xslt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.xslt.XsltMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class XsltDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = XsltMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private FormData    fdResultField,fdlField, fdField,fdTabFolder
						,fdlXSLFilename, fdbXSLFilename,fdXSLFilename,fdXSLField,fdlXSLField;	
	private LabelTextVar wResultField;
	private CCombo       wField,wXSLField;
    private FormData fdlXSLFileField,fdXSLFileField;  
 
	private Label wlField,wlFilename,wlXSLField,wlXSLFileField;
    
	private Button wbbFilename,wXSLFileField; 

	private XsltMeta input;
	
	private Group wOutputField,wXSLFileGroup;
	private FormData fdOutputField,fdXSLFileGroup;
	
	private TextVar wXSLFilename;

	
	private CTabFolder   wTabFolder;
	
	private CTabItem     wGeneralTab;
	private Composite    wGeneralComp;
	private FormData     fdGeneralComp;
	
    private Label        wlXSLTFactory;
    private CCombo       wXSLTFactory;
    private FormData     fdlXSLTFactory, fdXSLTFactory;
	
	public XsltDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(XsltMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "XsltDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "XsltDialog.Stepname.Label")); //$NON-NLS-1$
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

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "XsltDialog.GeneralTab.TabTitle"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		

		
		
		// FieldName to evaluate
		wlField=new Label(wGeneralComp, SWT.RIGHT);
        wlField.setText(BaseMessages.getString(PKG, "XsltDialog.Field.Label"));
        props.setLook(wlField);
        fdlField=new FormData();
        fdlField.left = new FormAttachment(0, 0);
        fdlField.top  = new FormAttachment(wStepname, margin);
        fdlField.right= new FormAttachment(middle, -margin);
        wlField.setLayoutData(fdlField);
        wField=new CCombo(wGeneralComp, SWT.BORDER | SWT.READ_ONLY);
        wField.setEditable(true);
        props.setLook(wField);
        wField.addModifyListener(lsMod);
        fdField=new FormData();
        fdField.left = new FormAttachment(middle, margin);
        fdField.top  = new FormAttachment(wStepname, margin);
        fdField.right= new FormAttachment(100, -margin);
        wField.setLayoutData(fdField);
        wField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    PopulateFields(wField);
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );

		
    	// Step Ouput field grouping?
		// ////////////////////////
		// START OF Output Field GROUP
		// 

		wOutputField = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wOutputField);
		wOutputField.setText(BaseMessages.getString(PKG, "XsltDialog.ResultField.Group.Label"));
		
		FormLayout outputfieldgroupLayout = new FormLayout();
		outputfieldgroupLayout.marginWidth = 10;
		outputfieldgroupLayout.marginHeight = 10;
		wOutputField.setLayout(outputfieldgroupLayout);
        
	      // Output Fieldame
        wResultField = new LabelTextVar(transMeta,wOutputField, BaseMessages.getString(PKG, "XsltDialog.ResultField.Label"), 
        		BaseMessages.getString(PKG, "XsltDialog.ResultField.Tooltip"));
        props.setLook(wResultField);
        wResultField .addModifyListener(lsMod);
        fdResultField  = new FormData();
        fdResultField .left = new FormAttachment(0, 0);
        fdResultField .top = new FormAttachment(wField, margin);
        fdResultField .right = new FormAttachment(100, 0);
        wResultField .setLayoutData(fdResultField );
        
         
		fdOutputField = new FormData();
		fdOutputField.left = new FormAttachment(0, margin);
		fdOutputField.top = new FormAttachment(wField, margin);
		fdOutputField.right = new FormAttachment(100, -margin);
		wOutputField.setLayoutData(fdOutputField);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Output Field GROUP
		// ///////////////////////////////////////////////////////////	
		
		
    	// XSL File grouping
		// ////////////////////////
		// START OF XSL File GROUP
		// 

		wXSLFileGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wXSLFileGroup);
		wXSLFileGroup.setText(BaseMessages.getString(PKG, "XsltDialog.XSL.Group.Label"));
		
		FormLayout XSLFileGroupLayout = new FormLayout();
		XSLFileGroupLayout.marginWidth = 10;
		XSLFileGroupLayout.marginHeight = 10;
		wXSLFileGroup.setLayout(XSLFileGroupLayout);


		// Is XSL filename defined in a Field?
		wlXSLFileField = new Label(wXSLFileGroup, SWT.RIGHT);
		wlXSLFileField.setText(BaseMessages.getString(PKG, "XsltDialog.XSLFilenameFileField.Label"));
		props.setLook(wlXSLFileField);
		fdlXSLFileField = new FormData();
		fdlXSLFileField.left = new FormAttachment(0, 0);
		fdlXSLFileField.top = new FormAttachment(wResultField, margin);
		fdlXSLFileField.right = new FormAttachment(middle, -margin);
		wlXSLFileField.setLayoutData(fdlXSLFileField);
		wXSLFileField = new Button(wXSLFileGroup, SWT.CHECK);
		props.setLook(wXSLFileField);
		wXSLFileField.setToolTipText(BaseMessages.getString(PKG, "XsltDialog.XSLFilenameFileField.Tooltip"));
		fdXSLFileField = new FormData();
		fdXSLFileField.left = new FormAttachment(middle, margin);
		fdXSLFileField.top = new FormAttachment(wResultField, margin);
		wXSLFileField.setLayoutData(fdXSLFileField);
		
		SelectionAdapter lsXslFile = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActivewlXSLField();
            	input.setChanged();
            }
        };
        wXSLFileField.addSelectionListener(lsXslFile);
		
		
		
		// If XSL File name defined in a Field
		wlXSLField=new Label(wXSLFileGroup, SWT.RIGHT);
        wlXSLField.setText(BaseMessages.getString(PKG, "XsltDialog.XSLFilenameField.Label"));
        props.setLook(wlXSLField);
        fdlXSLField=new FormData();
        fdlXSLField.left = new FormAttachment(0, 0);
        fdlXSLField.top  = new FormAttachment(wXSLFileField, margin);
        fdlXSLField.right= new FormAttachment(middle, -margin);
        wlXSLField.setLayoutData(fdlXSLField);
        wXSLField=new CCombo(wXSLFileGroup, SWT.BORDER | SWT.READ_ONLY);
        wXSLField.setEditable(true);
        props.setLook(wXSLField);
        wXSLField.addModifyListener(lsMod);
        fdXSLField=new FormData();
        fdXSLField.left = new FormAttachment(middle, margin);
        fdXSLField.top  = new FormAttachment(wXSLFileField, margin);
        fdXSLField.right= new FormAttachment(100, -margin);
        wXSLField.setLayoutData(fdXSLField);
        wXSLField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    PopulateFields(wXSLField);
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );
		       
        
		// XSL Filename
		wlFilename = new Label(wXSLFileGroup, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "XsltDialog.XSLFilename.Label"));
		props.setLook(wlFilename);
		fdlXSLFilename = new FormData();
		fdlXSLFilename.left = new FormAttachment(0, 0);
		fdlXSLFilename.top = new FormAttachment(wXSLField, 2*margin);
		fdlXSLFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlXSLFilename);

		wbbFilename = new Button(wXSLFileGroup, SWT.PUSH | SWT.CENTER);
		props.setLook(wbbFilename);
		wbbFilename.setText(BaseMessages.getString(PKG, "XsltDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbXSLFilename = new FormData();
		fdbXSLFilename.right = new FormAttachment(100, 0);
		fdbXSLFilename.top = new FormAttachment(wXSLField, 2*margin);
		wbbFilename.setLayoutData(fdbXSLFilename);

		wXSLFilename = new TextVar(transMeta,wXSLFileGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wXSLFilename);
		wXSLFilename.addModifyListener(lsMod);
		fdXSLFilename = new FormData();
		fdXSLFilename.left = new FormAttachment(middle, margin);
		fdXSLFilename.right = new FormAttachment(wbbFilename, -margin);
		fdXSLFilename.top = new FormAttachment(wXSLField, 2*margin);
		wXSLFilename.setLayoutData(fdXSLFilename);
				
		 // XSLTFactory
        wlXSLTFactory=new Label(wXSLFileGroup, SWT.RIGHT);
        wlXSLTFactory.setText(BaseMessages.getString(PKG, "XsltDialog.XSLTFactory.Label"));
        props.setLook(wlXSLTFactory);
        fdlXSLTFactory=new FormData();
        fdlXSLTFactory.left = new FormAttachment(0, 0);
        fdlXSLTFactory.top  = new FormAttachment(wXSLFilename, 2*margin);
        fdlXSLTFactory.right= new FormAttachment(middle, -margin);
        wlXSLTFactory.setLayoutData(fdlXSLTFactory);
        wXSLTFactory=new CCombo(wXSLFileGroup, SWT.BORDER | SWT.READ_ONLY);
        wXSLTFactory.setEditable(true);
        props.setLook(wXSLTFactory);
        wXSLTFactory.addModifyListener(lsMod);
        fdXSLTFactory=new FormData();
        fdXSLTFactory.left = new FormAttachment(middle, margin);
        fdXSLTFactory.top  = new FormAttachment(wXSLFilename,2*margin);
        fdXSLTFactory.right= new FormAttachment(100, 0);
        wXSLTFactory.setLayoutData(fdXSLTFactory);
        wXSLTFactory.add("JAXP");
        wXSLTFactory.add("SAXON");
		        
		
		fdXSLFileGroup = new FormData();
		fdXSLFileGroup.left = new FormAttachment(0, margin);
		fdXSLFileGroup.top = new FormAttachment(wOutputField, margin);
		fdXSLFileGroup.right = new FormAttachment(100, -margin);
		wXSLFileGroup.setLayoutData(fdXSLFileGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF XSL File GROUP
		// ///////////////////////////////////////////////////////////	
	
	        
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(wField, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
 		
 		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);


		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();          } };
		
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();              } };
		
		wCancel.addListener(SWT.Selection, lsCancel);


		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );

		// Whenever something changes, set the tooltip to the expanded version
		// of the filename:
		wXSLFilename.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wXSLFilename.setToolTipText(transMeta.environmentSubstitute(wXSLFilename.getText()));
			}
		});

		// Listen to the Browse... button
		wbbFilename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] { "*.xsl;*.XSL",
							"*.xslt;.*XSLT",
							"*" });
					if (wXSLFilename.getText() != null) {
						String fname = transMeta.environmentSubstitute(wXSLFilename.getText());
						dialog.setFileName(fname);
					}

					dialog.setFilterNames(new String[] {
							BaseMessages.getString(PKG, "XsltDialog.FileType"),
							BaseMessages.getString(PKG, "System.FileType.AllFiles") });

					if (dialog.open() != null) {
						String str = dialog.getFilterPath()
								+ System.getProperty("file.separator")
								+ dialog.getFileName();
						wXSLFilename.setText(str);
					}
				}			
		});
		
		
				
		wTabFolder.setSelection(0);
		
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		ActivewlXSLField();

		
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	
	
	
		
	
	private void ActivewlXSLField()
	{
       
		wXSLField.setEnabled(wXSLFileField.getSelection());
		wlXSLField.setEnabled(wXSLFileField.getSelection());
		
		wXSLFilename.setEnabled(!wXSLFileField.getSelection());
		wlFilename.setEnabled(!wXSLFileField.getSelection());
		wbbFilename.setEnabled(!wXSLFileField.getSelection());
	
	}
	 private void PopulateFields(CCombo cc)
	 {
		 try{
	           
				cc.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
		             r.getFieldNames();
		             
		             for (int i=0;i<r.getFieldNames().length;i++)
						{	
							cc.add(r.getFieldNames()[i]);					
							
						}
				}

		 }catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "XsltDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "XsltDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}

	 }
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
	
		if (input.getXslFilename() != null) wXSLFilename.setText( input.getXslFilename() );
		if (input.getResultfieldname() != null) wResultField.setText( input.getResultfieldname() );
		if (input.getFieldname() != null) wField.setText( input.getFieldname() );

		if (input.getXSLFileField() != null) wXSLField.setText( input.getXSLFileField() );
		
		wXSLFileField.setSelection(input.useXSLFileFieldUse());
		if (input.getXSLFactory() != null) 
			wXSLTFactory.setText( input.getXSLFactory() );
		else
			wXSLTFactory.setText( "JAXP");
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value

		input.setXslFilename( wXSLFilename.getText() );
		input.setResultfieldname(wResultField.getText() );
		input.setFieldname(wField.getText() );
		input.setXSLFileField(wXSLField.getText() );
		input.setXSLFactory(wXSLTFactory.getText() );
		
		input.setXSLFileFieldUse(wXSLFileField.getSelection());
						
		dispose();
	}
}