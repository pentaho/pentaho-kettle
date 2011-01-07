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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.xslt.XsltMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TableView;
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

	private Label wlXSLFieldIsAFile;
	private FormData fdlXSLFieldIsAFile;
	private Button wXSLFieldIsAFile;
	private FormData fdXSLFieldIsAFile;
	
	private CTabFolder   wTabFolder;
	
	private CTabItem     wGeneralTab;
	private Composite    wGeneralComp;
	private FormData     fdGeneralComp;
	
	private CTabItem     wAdditionalTab;

	private Composite    wAdditionalComp;
	private FormData     fdAdditionalComp;
	
    private Label        wlXSLTFactory;
    private CCombo       wXSLTFactory;
    private FormData     fdlXSLTFactory, fdXSLTFactory;
    
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private Label wlOutputProperties;
	private FormData fdlOutputProperties;
	
	private TableView wOutputProperties;
	private FormData fdOutputProperties;
	
	private ColumnInfo[] colinf;
    
    private Map<String, Integer> inputFields;
	
	public XsltDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(XsltMeta)in;
        inputFields =new HashMap<String, Integer>();
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
        fdlField.top  = new FormAttachment(wStepname, 2*margin);
        fdlField.right= new FormAttachment(middle, -margin);
        wlField.setLayoutData(fdlField);
        wField=new CCombo(wGeneralComp, SWT.BORDER | SWT.READ_ONLY);
        wField.setEditable(true);
        props.setLook(wField);
        wField.addModifyListener(lsMod);
        fdField=new FormData();
        fdField.left = new FormAttachment(middle, margin);
        fdField.top  = new FormAttachment(wStepname, 2*margin);
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


		// Is XSL source defined in a Field?
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
		       
        
    	// Is XSL field defined in a Field is a file?
		wlXSLFieldIsAFile = new Label(wXSLFileGroup, SWT.RIGHT);
		wlXSLFieldIsAFile.setText(BaseMessages.getString(PKG, "XsltDialog.XSLFieldIsAFile.Label"));
		props.setLook(wlXSLFieldIsAFile);
		fdlXSLFieldIsAFile = new FormData();
		fdlXSLFieldIsAFile.left = new FormAttachment(0, 0);
		fdlXSLFieldIsAFile.top = new FormAttachment(wXSLField, margin);
		fdlXSLFieldIsAFile.right = new FormAttachment(middle, -margin);
		wlXSLFieldIsAFile.setLayoutData(fdlXSLFieldIsAFile);
		wXSLFieldIsAFile = new Button(wXSLFileGroup, SWT.CHECK);
		props.setLook(wXSLFieldIsAFile);
		wXSLFieldIsAFile.setToolTipText(BaseMessages.getString(PKG, "XsltDialog.XSLFieldIsAFile.Tooltip"));
		fdXSLFieldIsAFile = new FormData();
		fdXSLFieldIsAFile.left = new FormAttachment(middle, margin);
		fdXSLFieldIsAFile.top = new FormAttachment(wXSLField, margin);
		wXSLFieldIsAFile.setLayoutData(fdXSLFieldIsAFile);
		wXSLFieldIsAFile.addSelectionListener(
			 new SelectionAdapter()
	        {
	            public void widgetSelected(SelectionEvent arg0)
	            {
	            	input.setChanged();
	            }
	        }
	    );
        
		// XSL Filename
		wlFilename = new Label(wXSLFileGroup, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "XsltDialog.XSLFilename.Label"));
		props.setLook(wlFilename);
		fdlXSLFilename = new FormData();
		fdlXSLFilename.left = new FormAttachment(0, 0);
		fdlXSLFilename.top = new FormAttachment(wXSLFieldIsAFile, 2*margin);
		fdlXSLFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlXSLFilename);

		wbbFilename = new Button(wXSLFileGroup, SWT.PUSH | SWT.CENTER);
		props.setLook(wbbFilename);
		wbbFilename.setText(BaseMessages.getString(PKG, "XsltDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbXSLFilename = new FormData();
		fdbXSLFilename.right = new FormAttachment(100, 0);
		fdbXSLFilename.top = new FormAttachment(wXSLFieldIsAFile, 2*margin);
		wbbFilename.setLayoutData(fdbXSLFilename);

		wXSLFilename = new TextVar(transMeta,wXSLFileGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wXSLFilename);
		wXSLFilename.addModifyListener(lsMod);
		fdXSLFilename = new FormData();
		fdXSLFilename.left = new FormAttachment(middle, margin);
		fdXSLFilename.right = new FormAttachment(wbbFilename, -margin);
		fdXSLFilename.top = new FormAttachment(wXSLFieldIsAFile, 2*margin);
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
 		
 		// Additional tab...
		//
		wAdditionalTab = new CTabItem(wTabFolder, SWT.NONE);
		wAdditionalTab.setText(BaseMessages.getString(PKG, "XsltDialog.AdvancedTab.Title"));
		
		FormLayout addLayout = new FormLayout ();
		addLayout.marginWidth  = Const.FORM_MARGIN;
		addLayout.marginHeight = Const.FORM_MARGIN;
		
		wAdditionalComp = new Composite(wTabFolder, SWT.NONE);
		wAdditionalComp.setLayout(addLayout);
 		props.setLook(wAdditionalComp);
 		
 		
 		 // Output properties
		 wlOutputProperties=new Label(wAdditionalComp, SWT.NONE);
	     wlOutputProperties.setText(BaseMessages.getString(PKG, "XsltDialog.OutputProperties.Label")); //$NON-NLS-1$
	     props.setLook(wlOutputProperties);
	     fdlOutputProperties=new FormData();
	     fdlOutputProperties.left = new FormAttachment(0, 0);
	     fdlOutputProperties.top  = new FormAttachment(0, margin);
	     wlOutputProperties.setLayoutData(fdlOutputProperties);

	     final int OutputPropertiesRows=input.getParameterField().length;
		
		 colinf=new ColumnInfo[] { 
		  new ColumnInfo(BaseMessages.getString(PKG, "XsltDialog.ColumnInfo.OutputProperties.Name"),      ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
		  new ColumnInfo(BaseMessages.getString(PKG, "XsltDialog.ColumnInfo.OutputProperties.Value"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
	       };
		 colinf[0].setComboValues(XsltMeta.outputProperties);
		 colinf[1].setUsingVariables(true);
		
		 wOutputProperties=new TableView(transMeta, wAdditionalComp, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  OutputPropertiesRows,  
							  lsMod,
							  props
							  );
		fdOutputProperties=new FormData();
		fdOutputProperties.left  = new FormAttachment(0, 0);
		fdOutputProperties.top   = new FormAttachment(wlOutputProperties, margin);
		fdOutputProperties.right = new FormAttachment(100, -margin);
		fdOutputProperties.bottom= new FormAttachment(wlOutputProperties, 200);
		wOutputProperties.setLayoutData(fdOutputProperties);
		
		// Parameters
 		
		 wlFields=new Label(wAdditionalComp, SWT.NONE);
	     wlFields.setText(BaseMessages.getString(PKG, "XsltDialog.Parameters.Label")); //$NON-NLS-1$
	     props.setLook(wlFields);
	     fdlFields=new FormData();
	     fdlFields.left = new FormAttachment(0, 0);
	     fdlFields.top  = new FormAttachment(wOutputProperties, 2*margin);
	     wlFields.setLayoutData(fdlFields);
	       
		wGet=new Button(wAdditionalComp, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "XsltDialog.GetFields.Button")); //$NON-NLS-1$
		FormData fdGet = new FormData();
		fdGet.top = new FormAttachment(wlFields, margin);
		fdGet.right = new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
			
		final int FieldsRows=input.getParameterField().length;
		
		 colinf=new ColumnInfo[] { 
		  new ColumnInfo(BaseMessages.getString(PKG, "XsltDialog.ColumnInfo.Name"),      ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
		  new ColumnInfo(BaseMessages.getString(PKG, "XsltDialog.ColumnInfo.Parameter"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
	       };
		colinf[1].setUsingVariables(true);
		
		wFields=new TableView(transMeta, wAdditionalComp, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );
		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(wGet, -margin);
		fdFields.bottom= new FormAttachment(100, -margin);
		wFields.setLayoutData(fdFields);
		
		

 	        // Search the fields in the background
 			
 	        final Runnable runnable = new Runnable()
 	        {
 	            public void run()
 	            {
 	                StepMeta stepMeta = transMeta.findStep(stepname);
 	                if (stepMeta!=null)
 	                {
 	                    try
 	                    {
 	                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
 	                       
 	                        // Remember these fields...
 	                        for (int i=0;i<row.size();i++)
 	                        {
 	                            inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
 	                        }
 	                        setComboBoxes();
 	                    }
 	                    catch(KettleException e)
 	                    {
 	                    	logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
 	                    }
 	                }
 	            }
 	        };
 	        new Thread(runnable).start();

 		
 		
 	    fdAdditionalComp=new FormData();
 		fdAdditionalComp.left  = new FormAttachment(0, 0);
		fdAdditionalComp.top   = new FormAttachment(wStepname, margin);
		fdAdditionalComp.right = new FormAttachment(100, 0);
		fdAdditionalComp.bottom= new FormAttachment(100, 0);
		wAdditionalComp.setLayoutData(fdAdditionalComp);
		
		wAdditionalComp.layout();
		wAdditionalTab.setControl(wAdditionalComp);
		//////// END of Additional Tab
 		
 		
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
		lsGet      = new Listener() { public void handleEvent(Event e) { get();        } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();              } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wGet.addListener   (SWT.Selection, lsGet   );

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
		wlXSLFieldIsAFile.setEnabled(wXSLFileField.getSelection());
		wXSLFieldIsAFile.setEnabled(wXSLFileField.getSelection());
		if(!wXSLFileField.getSelection()) {
			wXSLFieldIsAFile.setSelection(false);
		}
	
	}
	 private void PopulateFields(CCombo cc)
	 {
		 if(cc.isDisposed()) return;
		 try{
			String initValue=cc.getText();
			cc.removeAll();
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null) {
	             cc.setItems(r.getFieldNames());
			}
			if(!Const.isEmpty(initValue)) cc.setText(initValue);
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
		
		wXSLFileField.setSelection(input.useXSLField());
		wXSLFieldIsAFile.setSelection(input.isXSLFieldIsAFile());
		
		if (input.getXSLFactory() != null) 
			wXSLTFactory.setText( input.getXSLFactory() );
		else
			wXSLTFactory.setText( "JAXP");
		
		
		if (input.getParameterName()!=null) {
			for (int i=0;i<input.getParameterName().length;i++)
			{
				TableItem item = wFields.table.getItem(i);
				item.setText(1, Const.NVL(input.getParameterField()[i], ""));
				item.setText(2, Const.NVL(input.getParameterName()[i], ""));
			}
		}
		
		
		if (input.getOutputPropertyName()!=null) {
			for (int i=0;i<input.getOutputPropertyName().length;i++)
			{
				TableItem item = wOutputProperties.table.getItem(i);
				item.setText(1, Const.NVL(input.getOutputPropertyName()[i], ""));
				item.setText(2, Const.NVL(input.getOutputPropertyValue()[i], ""));
			}
		}
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
		
		input.setXSLField(wXSLFileField.getSelection());
		input.setXSLFieldIsAFile(wXSLFieldIsAFile.getSelection());
		int nrparams = wFields.nrNonEmpty();
		int nroutputprops = wOutputProperties.nrNonEmpty();
		input.allocate(nrparams, nroutputprops);

		if(isDebug()) logDebug(BaseMessages.getString(PKG, "HTTPDialog.Log.FoundArguments",String.valueOf(nrparams))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrparams;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getParameterField()[i]       = item.getText(1);
			input.getParameterName()[i]    = item.getText(2);
		}
		for (int i=0;i<nroutputprops;i++)
		{
			TableItem item = wOutputProperties.getNonEmpty(i);
			input.getOutputPropertyName()[i]       = item.getText(1);
			input.getOutputPropertyValue()[i]    = item.getText(2);
		}				
		dispose();
	}
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);

        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[0].setComboValues(fieldNames);
        //colinfHeaders[0].setComboValues(fieldNames);
    }
	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1, 2 }, new int[] { 3 }, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "XsltDialog.FailedToGetFields.DialogTitle"), 
					BaseMessages.getString(PKG, "XsltDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}