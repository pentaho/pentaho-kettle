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

package be.ibridge.kettle.trans.step.xslt;

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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.LabelTextVar;
import be.ibridge.kettle.core.widget.TextVar;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;



public class XsltDialog extends BaseStepDialog implements StepDialogInterface
{
	

	private FormData    fdResultField,fdlField, fdField,fdTabFolder
						,fdlXSDFilename, fdbXSDFilename,fdXSDFilename,fdXSDField,fdlXSDField;//fdXsdValideText,fdXsdNoValideText,,fdInvalidMsgField	
	private LabelTextVar wResultField;//,wXsdValideText,wXsdNoValideText,wInvalidMsgField
	private CCombo       wField,wXSDField;
    private FormData fdlXSDFileField,fdXSDFileField;  //fdlInterneXsd, fdInterneXsd,fdlInvalidMsg,fdInvalidMsg,
    
 
	private Label wlField,wlFilename,wlXSDField,wlXSDFileField;//wlInterneXsd,wlInvalidMsg,
    
	private Button wbbFilename,wXSDFileField; //,wInterneXsd,wInvalidMsg

	private XsltMeta input;
	
	private Group wOutputField,wXSDFileGroup;
	private FormData fdOutputField,fdXSDFileGroup;
	
	private TextVar wXSDFilename;

	
	private CTabFolder   wTabFolder;
	
	private CTabItem     wGeneralTab;
	private Composite    wGeneralComp;
	private FormData     fdGeneralComp;
	
   // private Label        wlFormat;
   // private CCombo       wFormat;
   // private FormData     fdlFormat, fdFormat;
	
	
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
		shell.setText(Messages.getString("XsltDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("XsltDialog.Stepname.Label")); //$NON-NLS-1$
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
		wGeneralTab.setText(Messages.getString("XsltDialog.GeneralTab.TabTitle"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		

		
		
		// FieldName to evaluate
		wlField=new Label(wGeneralComp, SWT.RIGHT);
        wlField.setText(Messages.getString("XsltDialog.Field.Label"));
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
                    setFieldname();
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
		wOutputField.setText(Messages.getString("XsltDialog.ResultField.Group.Label"));
		
		FormLayout outputfieldgroupLayout = new FormLayout();
		outputfieldgroupLayout.marginWidth = 10;
		outputfieldgroupLayout.marginHeight = 10;
		wOutputField.setLayout(outputfieldgroupLayout);
        
	      // Output Fieldame
        wResultField = new LabelTextVar(wOutputField, Messages.getString("XsltDialog.ResultField.Label"), Messages
            .getString("XsltDialog.ResultField.Tooltip"));
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
		
		
    	// XSD File grouping?
		// ////////////////////////
		// START OF XSD File GROUP
		// 

		wXSDFileGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wXSDFileGroup);
		wXSDFileGroup.setText(Messages.getString("XsltDialog.XSD.Group.Label"));
		
		FormLayout XSDFileGroupLayout = new FormLayout();
		XSDFileGroupLayout.marginWidth = 10;
		XSDFileGroupLayout.marginHeight = 10;
		wXSDFileGroup.setLayout(XSDFileGroupLayout);


		// Is XSD filename defined in a Field?
		wlXSDFileField = new Label(wXSDFileGroup, SWT.RIGHT);
		wlXSDFileField.setText(Messages.getString("XsltDialog.XSDFilenameFileField.Label"));
		props.setLook(wlXSDFileField);
		fdlXSDFileField = new FormData();
		fdlXSDFileField.left = new FormAttachment(0, 0);
		fdlXSDFileField.top = new FormAttachment(wResultField, margin);
		fdlXSDFileField.right = new FormAttachment(middle, -margin);
		wlXSDFileField.setLayoutData(fdlXSDFileField);
		wXSDFileField = new Button(wXSDFileGroup, SWT.CHECK);
		props.setLook(wXSDFileField);
		wXSDFileField.setToolTipText(Messages.getString("XsltDialog.XSDFilenameFileField.Tooltip"));
		fdXSDFileField = new FormData();
		fdXSDFileField.left = new FormAttachment(middle, margin);
		fdXSDFileField.top = new FormAttachment(wResultField, margin);
		wXSDFileField.setLayoutData(fdXSDFileField);
		
		SelectionAdapter lsXsdFile = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActivewlXSDField();
            	input.setChanged();
            }
        };
        wXSDFileField.addSelectionListener(lsXsdFile);
		
		
		
		// If XSD File name defined in a Field
		wlXSDField=new Label(wXSDFileGroup, SWT.RIGHT);
        wlXSDField.setText(Messages.getString("XsltDialog.XSDFilenameField.Label"));
        props.setLook(wlXSDField);
        fdlXSDField=new FormData();
        fdlXSDField.left = new FormAttachment(0, 0);
        fdlXSDField.top  = new FormAttachment(wXSDFileField, margin);
        fdlXSDField.right= new FormAttachment(middle, -margin);
        wlXSDField.setLayoutData(fdlXSDField);
        wXSDField=new CCombo(wXSDFileGroup, SWT.BORDER | SWT.READ_ONLY);
        wXSDField.setEditable(true);
        props.setLook(wXSDField);
        wXSDField.addModifyListener(lsMod);
        fdXSDField=new FormData();
        fdXSDField.left = new FormAttachment(middle, margin);
        fdXSDField.top  = new FormAttachment(wXSDFileField, margin);
        fdXSDField.right= new FormAttachment(100, -margin);
        wXSDField.setLayoutData(fdXSDField);
        wXSDField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setXSDFieldname();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );
		       
        
		// XSD Filename
		wlFilename = new Label(wXSDFileGroup, SWT.RIGHT);
		wlFilename.setText(Messages.getString("XsltDialog.XSDFilename.Label"));
		props.setLook(wlFilename);
		fdlXSDFilename = new FormData();
		fdlXSDFilename.left = new FormAttachment(0, 0);
		fdlXSDFilename.top = new FormAttachment(wXSDField, 2*margin);
		fdlXSDFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlXSDFilename);

		wbbFilename = new Button(wXSDFileGroup, SWT.PUSH | SWT.CENTER);
		props.setLook(wbbFilename);
		wbbFilename.setText(Messages
				.getString("XsltDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(Messages
				.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbXSDFilename = new FormData();
		fdbXSDFilename.right = new FormAttachment(100, 0);
		fdbXSDFilename.top = new FormAttachment(wXSDField, 2*margin);
		wbbFilename.setLayoutData(fdbXSDFilename);

		wXSDFilename = new TextVar(wXSDFileGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wXSDFilename);
		wXSDFilename.addModifyListener(lsMod);
		fdXSDFilename = new FormData();
		fdXSDFilename.left = new FormAttachment(middle, margin);
		fdXSDFilename.right = new FormAttachment(wbbFilename, -margin);
		fdXSDFilename.top = new FormAttachment(wXSDField, 2*margin);
		wXSDFilename.setLayoutData(fdXSDFilename);
				
		        
		
		
		
		fdXSDFileGroup = new FormData();
		fdXSDFileGroup.left = new FormAttachment(0, margin);
		fdXSDFileGroup.top = new FormAttachment(wOutputField, margin);
		fdXSDFileGroup.right = new FormAttachment(100, -margin);
		wXSDFileGroup.setLayoutData(fdXSDFileGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF XSD File GROUP
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
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

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
		wXSDFilename.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wXSDFilename.setToolTipText(StringUtil
						.environmentSubstitute(wXSDFilename.getText()));
			}
		});

		// Listen to the Browse... button
		wbbFilename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] { "*.xsl;*.XSL",
							"*.xslt;.*XSLT",
							"*" });
					if (wXSDFilename.getText() != null) {
						String fname = StringUtil
								.environmentSubstitute(wXSDFilename.getText());
						dialog.setFileName(fname);
					}

					dialog.setFilterNames(new String[] {
							Messages.getString("XsltDialog.FileType"),
							Messages.getString("XslTValidatorDialog.FileType"),
							Messages.getString("System.FileType.AllFiles") });

					if (dialog.open() != null) {
						String str = dialog.getFilterPath()
								+ System.getProperty("file.separator")
								+ dialog.getFileName();
						wXSDFilename.setText(str);
					}
				}			
		});
		
		
				
		wTabFolder.setSelection(0);
		
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		ActivewlXSDField();

		
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	
	
	
		
	
	private void ActivewlXSDField()
	{
       
		wXSDField.setEnabled(wXSDFileField.getSelection());
		wlXSDField.setEnabled(wXSDFileField.getSelection());
		
		wXSDFilename.setEnabled(!wXSDFileField.getSelection());
		wlFilename.setEnabled(!wXSDFileField.getSelection());
		wbbFilename.setEnabled(!wXSDFileField.getSelection());
		
	}
	
	
 	 private void setXSDFieldname()
	 {
		 try{
	           
			 wField.removeAll();
				
				Row r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					for (int i=0;i<r.size();i++)
					{
						Value v = r.getValue(i);	
						wXSDField.add(v.getName());										
					}
				}
		    

		 }catch(KettleException ke){
				new ErrorDialog(shell, Messages.getString("XsltDialog.FailedToGetFields.DialogTitle"), Messages.getString("XsltDialogMod.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
	


	 }
	 private void setFieldname()
	 {
		 try{
	           
			 wField.removeAll();
				
				Row r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					for (int i=0;i<r.size();i++)
					{
						Value v = r.getValue(i);	
						wField.add(v.getName());
						
						
					}
				}
		    

		 }catch(KettleException ke){
				new ErrorDialog(shell, Messages.getString("XsltDialog.FailedToGetFields.DialogTitle"), Messages.getString("XsltDialogMod.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
	


	 }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
	
		if (input.getXsdFilename() != null) wXSDFilename.setText( input.getXsdFilename() );
		if (input.getResultfieldname() != null) wResultField.setText( input.getResultfieldname() );
		if (input.getFieldname() != null) wField.setText( input.getFieldname() );

		
		
		if (input.getXSDFileField() != null) wXSDField.setText( input.getXSDFileField() );
		
		
		wXSDFileField.setSelection(input.useXSDFileFieldUse());
		
			
		
					

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

		input.setXsdFilename( wXSDFilename.getText() );
		input.setResultfieldname(wResultField.getText() );
		input.setFieldname(wField.getText() );

		
		input.setXSDFileField(wXSDField.getText() );
		

		input.setXSDFileFieldUse(wXSDFileField.getSelection());
						
		dispose();
	}
	
	
	
		
	public String toString()
	{
		return this.getClass().getName();
	}
}