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
 * Created on 2-jul-2003
 *
 */

package org.pentaho.di.ui.trans.steps.fuzzymatch;


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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.trans.steps.fuzzymatch.FuzzyMatchMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class FuzzyMatchDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = FuzzyMatchMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlStep;
	private CCombo       wStep;
	private FormData     fdlStep, fdStep;

	
	private Label 		wlAlgorithm;
	private CCombo 		wAlgorithm;
	private FormData    fdlAlgorithm;
	private FormData    fdAlgorithm;
	
	private ComboVar wMainStreamField;
	private FormData fdMainStreamField;
	private Label wlMainStreamField;
	private FormData fdlMainStreamField;
	
	private ComboVar wLookupField;
	private FormData fdLookupField;
	private Label wlLookupField;
	private FormData fdlLookupField;
	
	private Group wLookupGroup ;
	private FormData fdLookupGroup ;
	
	private Group wMainStreamGroup ;
	private FormData fdMainStreamGroup ;
	
	private Group wSettingsGroup ;
	private FormData fdSettingsGroup ;
	
	private Group wOutputFieldsGroup ;
	private FormData fdOutputFieldsGroup ;
	
    private ColumnInfo[] ciReturn;
	private Label        wlReturn;
	private TableView    wReturn;
	private FormData     fdlReturn, fdReturn;
	
	private Label wlmatchField;
	private TextVar wmatchField;
	private FormData fdlmatchField;
	private FormData fdmatchField;
	
	private Label wlvalueField;
	private TextVar wvalueField;
	private FormData fdlvalueField;
	private FormData fdvalueField;
	
    private Label wlcaseSensitive;
    private Button wcaseSensitive ;
    private FormData fdlcaseSensitive,fdcaseSensitive;	
    
    private Label wlgetCloserValue;
    private Button wgetCloserValue ;
    private FormData fdlgetCloserValue,fdgetCloserValue;	
    
    private Label wlminValue;
    private TextVar wminValue ;
    private FormData fdminValue,fdlminValue;	
    
    private Label wlmaxValue;
    private TextVar wmaxValue ;
    private FormData fdmaxValue,fdlmaxValue;
    
    private Label wlseparator;
    private TextVar wseparator;
    private FormData fdseparator,fdlseparator;
    
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private Composite    wGeneralComp;
	private FormData     fdGeneralComp;	
	private CTabItem wGeneralTab, wFieldsTab;
	
	private Composite    wFieldsComp;
	private FormData     fdFieldsComp;	
	
    private Button       wGetLU;
    private Listener     lsGetLU;
    
	private FuzzyMatchMeta input;
	private boolean gotPreviousFields=false;
	private boolean gotLookupFields=false;

	public FuzzyMatchDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(FuzzyMatchMeta)in;
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
		SelectionListener lsSelection = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) 
			{
				input.setChanged();
				setComboBoxesLookup();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.Stepname.Label")); //$NON-NLS-1$
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
		// START OF General TAB   ///
		//////////////////////////
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.General.Tab"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout GeneralLayout = new FormLayout();
		GeneralLayout.marginWidth  = 3;
		GeneralLayout.marginHeight = 3;
		wGeneralComp.setLayout(GeneralLayout);
		
		// /////////////////////////////////
		// START OF Lookup  Fields GROUP
		// /////////////////////////////////

		wLookupGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wLookupGroup);
		wLookupGroup.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.Group.Lookup.Label"));
		
		FormLayout LookupgroupLayout = new FormLayout();
		LookupgroupLayout.marginWidth = 10;
		LookupgroupLayout.marginHeight = 10;
		wLookupGroup.setLayout(LookupgroupLayout);

		// Source step line...
		wlStep=new Label(wLookupGroup, SWT.RIGHT);
		wlStep.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.SourceStep.Label")); //$NON-NLS-1$
 		props.setLook(wlStep);
		fdlStep=new FormData();
		fdlStep.left = new FormAttachment(0, 0);
		fdlStep.right= new FormAttachment(middle, -margin);
		fdlStep.top  = new FormAttachment(wStepname, margin);
		wlStep.setLayoutData(fdlStep);
		wStep=new CCombo(wLookupGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStep);
 		
		for (int i=0;i<transMeta.findNrPrevSteps(stepname, true);i++)
		{
			StepMeta stepMeta = transMeta.findPrevStep(stepname, i, true);
			wStep.add(stepMeta.getName());
		}
		
		wStep.addModifyListener(lsMod);
		wStep.addSelectionListener(lsSelection);
		
		fdStep=new FormData();
		fdStep.left = new FormAttachment(middle, 0);
		fdStep.top  = new FormAttachment(wStepname, margin);
		fdStep.right= new FormAttachment(100, 0);
		wStep.setLayoutData(fdStep);
		
		// LookupField
		wlLookupField=new Label(wLookupGroup, SWT.RIGHT);
        wlLookupField.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.wlLookupField.Label"));
        props.setLook(wlLookupField);
        fdlLookupField=new FormData();
        fdlLookupField.left = new FormAttachment(0, 0);
        fdlLookupField.top  = new FormAttachment(wStep, margin);
        fdlLookupField.right= new FormAttachment(middle, -2*margin);
        wlLookupField.setLayoutData(fdlLookupField);
        
        
        wLookupField=new ComboVar(transMeta, wLookupGroup, SWT.BORDER | SWT.READ_ONLY);
        wLookupField.setEditable(true);
        props.setLook(wLookupField);
        wLookupField.addModifyListener(lsMod);
        fdLookupField=new FormData();
        fdLookupField.left = new FormAttachment(middle, 0);
        fdLookupField.top  = new FormAttachment(wStep, margin);
        fdLookupField.right= new FormAttachment(100, -margin);
        wLookupField.setLayoutData(fdLookupField);
        wLookupField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    setLookupField();
                }
            }
        );           	
        
		
		fdLookupGroup = new FormData();
		fdLookupGroup.left = new FormAttachment(0, margin);
		fdLookupGroup.top = new FormAttachment(wStepname, margin);
		fdLookupGroup.right = new FormAttachment(100, -margin);
		wLookupGroup.setLayoutData(fdLookupGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Lookup  GROUP
		// ///////////////////////////////////////////////////////////

		// /////////////////////////////////
		// START OF MainStream  Fields GROUP
		// /////////////////////////////////

		wMainStreamGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wMainStreamGroup);
		wMainStreamGroup.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.Group.MainStreamGroup.Label"));
		
		FormLayout MainStreamgroupLayout = new FormLayout();
		MainStreamgroupLayout.marginWidth = 10;
		MainStreamgroupLayout.marginHeight = 10;
		wMainStreamGroup.setLayout(MainStreamgroupLayout);

		
		
		// MainStreamFieldname field
		wlMainStreamField=new Label(wMainStreamGroup, SWT.RIGHT);
        wlMainStreamField.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.wlMainStreamField.Label"));
        props.setLook(wlMainStreamField);
        fdlMainStreamField=new FormData();
        fdlMainStreamField.left = new FormAttachment(0, 0);
        fdlMainStreamField.top  = new FormAttachment(wLookupGroup, margin);
        fdlMainStreamField.right= new FormAttachment(middle, -2*margin);
        wlMainStreamField.setLayoutData(fdlMainStreamField);
        
        wMainStreamField=new ComboVar(transMeta, wMainStreamGroup, SWT.BORDER | SWT.READ_ONLY);
        wMainStreamField.setEditable(true);
        props.setLook(wMainStreamField);
        wMainStreamField.addModifyListener(lsMod);
        fdMainStreamField=new FormData();
        fdMainStreamField.left = new FormAttachment(middle, 0);
        fdMainStreamField.top  = new FormAttachment(wLookupGroup, margin);
        fdMainStreamField.right= new FormAttachment(100, -margin);
        wMainStreamField.setLayoutData(fdMainStreamField);
        wMainStreamField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    setMainStreamField();
                }
            }
        );           	
        
		
		

		fdMainStreamGroup = new FormData();
		fdMainStreamGroup.left = new FormAttachment(0, margin);
		fdMainStreamGroup.top = new FormAttachment(wLookupGroup, margin);
		fdMainStreamGroup.right = new FormAttachment(100, -margin);
		wMainStreamGroup.setLayoutData(fdMainStreamGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF MainStream  GROUP
		// ///////////////////////////////////////////////////////////
		
		// /////////////////////////////////
		// START OF Settings  Fields GROUP
		// /////////////////////////////////

		wSettingsGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wSettingsGroup);
		wSettingsGroup.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.Group.SettingsGroup.Label"));
		
		FormLayout SettingsgroupLayout = new FormLayout();
		SettingsgroupLayout.marginWidth = 10;
		SettingsgroupLayout.marginHeight = 10;
		wSettingsGroup.setLayout(SettingsgroupLayout);

		// Algorithm
		wlAlgorithm=new Label(wSettingsGroup, SWT.RIGHT);
		wlAlgorithm.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.Algorithm.Label")); //$NON-NLS-1$
 		props.setLook(wlAlgorithm);
		fdlAlgorithm=new FormData();
		fdlAlgorithm.left = new FormAttachment(0, 0);
		fdlAlgorithm.right= new FormAttachment(middle, -margin);
		fdlAlgorithm.top  = new FormAttachment(wMainStreamGroup, margin);
		wlAlgorithm.setLayoutData(fdlAlgorithm);
		
		wAlgorithm=new CCombo(wSettingsGroup, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wAlgorithm);
 		wAlgorithm.addModifyListener(lsMod);
		fdAlgorithm=new FormData();
		fdAlgorithm.left = new FormAttachment(middle, 0);
		fdAlgorithm.top  = new FormAttachment(wMainStreamGroup, margin);
		fdAlgorithm.right= new FormAttachment(100, -margin);
		wAlgorithm.setLayoutData(fdAlgorithm);
		wAlgorithm.setItems(FuzzyMatchMeta.algorithmDesc);
		wAlgorithm.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeAlgorithm();
				
			}
		});
 
		//Is case sensitive		
		wlcaseSensitive = new Label(wSettingsGroup, SWT.RIGHT);
		wlcaseSensitive.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.caseSensitive.Label"));
		props.setLook(wlcaseSensitive);
		fdlcaseSensitive = new FormData();
		fdlcaseSensitive.left = new FormAttachment(0, 0);
		fdlcaseSensitive.top = new FormAttachment(wAlgorithm, margin);
		fdlcaseSensitive.right = new FormAttachment(middle, -2*margin);
		wlcaseSensitive.setLayoutData(fdlcaseSensitive);
		
		wcaseSensitive = new Button(wSettingsGroup, SWT.CHECK);
		props.setLook(wcaseSensitive);
		wcaseSensitive.setToolTipText(BaseMessages.getString(PKG, "FuzzyMatchDialog.caseSensitive.Tooltip"));
		fdcaseSensitive = new FormData();
		fdcaseSensitive.left = new FormAttachment(middle, 0);
		fdcaseSensitive.top = new FormAttachment(wAlgorithm, margin);
		wcaseSensitive.setLayoutData(fdcaseSensitive);		
		SelectionAdapter lcaseSensitive = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	input.setChanged();
            }
        };

        wcaseSensitive.addSelectionListener(lcaseSensitive);
		
        //Is get closer value		
		wlgetCloserValue = new Label(wSettingsGroup, SWT.RIGHT);
		wlgetCloserValue.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.getCloserValue.Label"));
		props.setLook(wlgetCloserValue);
		fdlgetCloserValue = new FormData();
		fdlgetCloserValue.left = new FormAttachment(0, 0);
		fdlgetCloserValue.top = new FormAttachment(wcaseSensitive, margin);
		fdlgetCloserValue.right = new FormAttachment(middle, -2*margin);
		wlgetCloserValue.setLayoutData(fdlgetCloserValue);
		
		
		wgetCloserValue = new Button(wSettingsGroup, SWT.CHECK);
		props.setLook(wgetCloserValue);
		wgetCloserValue.setToolTipText(BaseMessages.getString(PKG, "FuzzyMatchDialog.getCloserValue.Tooltip"));
		fdgetCloserValue = new FormData();
		fdgetCloserValue.left = new FormAttachment(middle, 0);
		fdgetCloserValue.top = new FormAttachment(wcaseSensitive, margin);
		wgetCloserValue.setLayoutData(fdgetCloserValue);		
		SelectionAdapter lgetCloserValue = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	activegetCloserValue();
            	input.setChanged();
            }
        };
        wgetCloserValue.addSelectionListener(lgetCloserValue);
        
		wlminValue=new Label(wSettingsGroup, SWT.RIGHT);
		wlminValue.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.minValue.Label"));
 		props.setLook(wlminValue);
		fdlminValue=new FormData();
		fdlminValue.left = new FormAttachment(0, 0);
		fdlminValue.top  = new FormAttachment(wgetCloserValue, margin);
		fdlminValue.right= new FormAttachment(middle, -margin);
		wlminValue.setLayoutData(fdlminValue);
		wminValue=new TextVar(transMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wminValue);
 		wminValue.setToolTipText(BaseMessages.getString(PKG, "FuzzyMatchDialog.minValue.Tooltip"));
		wminValue.addModifyListener(lsMod);
		fdminValue=new FormData();
		fdminValue.left = new FormAttachment(middle, 0);
		fdminValue.top  = new FormAttachment(wgetCloserValue, margin);
		fdminValue.right= new FormAttachment(100, 0);
		wminValue.setLayoutData(fdminValue);

		wlmaxValue=new Label(wSettingsGroup, SWT.RIGHT);
		wlmaxValue.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.maxValue.Label"));
 		props.setLook(wlmaxValue);
		fdlmaxValue=new FormData();
		fdlmaxValue.left = new FormAttachment(0, 0);
		fdlmaxValue.top  = new FormAttachment(wminValue, margin);
		fdlmaxValue.right= new FormAttachment(middle, -margin);
		wlmaxValue.setLayoutData(fdlmaxValue);
		wmaxValue=new TextVar(transMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wmaxValue);
 		wmaxValue.setToolTipText(BaseMessages.getString(PKG, "FuzzyMatchDialog.maxValue.Tooltip"));
		wmaxValue.addModifyListener(lsMod);
		fdmaxValue=new FormData();
		fdmaxValue.left = new FormAttachment(middle, 0);
		fdmaxValue.top  = new FormAttachment(wminValue, margin);
		fdmaxValue.right= new FormAttachment(100, 0);
		wmaxValue.setLayoutData(fdmaxValue);
		

		wlseparator=new Label(wSettingsGroup, SWT.RIGHT);
		wlseparator.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.separator.Label"));
 		props.setLook(wlseparator);
		fdlseparator=new FormData();
		fdlseparator.left = new FormAttachment(0, 0);
		fdlseparator.top  = new FormAttachment(wmaxValue, margin);
		fdlseparator.right= new FormAttachment(middle, -margin);
		wlseparator.setLayoutData(fdlseparator);
		wseparator=new TextVar(transMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wseparator);
		wseparator.addModifyListener(lsMod);
		fdseparator=new FormData();
		fdseparator.left = new FormAttachment(middle, 0);
		fdseparator.top  = new FormAttachment(wmaxValue, margin);
		fdseparator.right= new FormAttachment(100, 0);
		wseparator.setLayoutData(fdseparator);
        
		fdSettingsGroup = new FormData();
		fdSettingsGroup.left = new FormAttachment(0, margin);
		fdSettingsGroup.top = new FormAttachment(wMainStreamGroup, margin);
		fdSettingsGroup.right = new FormAttachment(100, -margin);
		wSettingsGroup.setLayoutData(fdSettingsGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Settings  GROUP
		// ///////////////////////////////////////////////////////////
		
		
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
	
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF General TAB
		/////////////////////////////////////////////////////////////
		
		
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);
		
		//////////////////////////
		// START OF Fields TAB   ///
		//////////////////////////
		wFieldsTab=new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.Fields.Tab"));
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFieldsComp);

		FormLayout FieldsLayout = new FormLayout();
		FieldsLayout.marginWidth  = 3;
		FieldsLayout.marginHeight = 3;
		wFieldsComp.setLayout(FieldsLayout);
		
		
		// /////////////////////////////////
		// START OF OutputFields  Fields GROUP
		// /////////////////////////////////

		wOutputFieldsGroup = new Group(wFieldsComp, SWT.SHADOW_NONE);
		props.setLook(wOutputFieldsGroup);
		wOutputFieldsGroup.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.Group.OutputFieldsGroup.Label"));
		
		FormLayout OutputFieldsgroupLayout = new FormLayout();
		OutputFieldsgroupLayout.marginWidth = 10;
		OutputFieldsgroupLayout.marginHeight = 10;
		wOutputFieldsGroup.setLayout(OutputFieldsgroupLayout);

		wlmatchField=new Label(wOutputFieldsGroup, SWT.RIGHT);
		wlmatchField.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.MatchField.Label"));
 		props.setLook(wlmatchField);
		fdlmatchField=new FormData();
		fdlmatchField.left = new FormAttachment(0, 0);
		fdlmatchField.top  = new FormAttachment(wSettingsGroup, margin);
		fdlmatchField.right= new FormAttachment(middle, -margin);
		wlmatchField.setLayoutData(fdlmatchField);
		wmatchField=new TextVar(transMeta, wOutputFieldsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wmatchField);
		wmatchField.addModifyListener(lsMod);
		fdmatchField=new FormData();
		fdmatchField.left = new FormAttachment(middle, 0);
		fdmatchField.top  = new FormAttachment(wSettingsGroup, margin);
		fdmatchField.right= new FormAttachment(100, 0);
		wmatchField.setLayoutData(fdmatchField);

		wlvalueField=new Label(wOutputFieldsGroup, SWT.RIGHT);
		wlvalueField.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.valueField.Label"));
 		props.setLook(wlvalueField);
		fdlvalueField=new FormData();
		fdlvalueField.left = new FormAttachment(0, 0);
		fdlvalueField.top  = new FormAttachment(wmatchField, margin);
		fdlvalueField.right= new FormAttachment(middle, -margin);
		wlvalueField.setLayoutData(fdlvalueField);
		wvalueField=new TextVar(transMeta, wOutputFieldsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wvalueField);
		wvalueField.setToolTipText(BaseMessages.getString(PKG, "FuzzyMatchDialog.valueField.Tooltip"));
		wvalueField.addModifyListener(lsMod);
		fdvalueField=new FormData();
		fdvalueField.left = new FormAttachment(middle, 0);
		fdvalueField.top  = new FormAttachment(wmatchField, margin);
		fdvalueField.right= new FormAttachment(100, 0);
		wvalueField.setLayoutData(fdvalueField);

		fdOutputFieldsGroup = new FormData();
		fdOutputFieldsGroup.left = new FormAttachment(0, margin);
		fdOutputFieldsGroup.top = new FormAttachment(wSettingsGroup, margin);
		fdOutputFieldsGroup.right = new FormAttachment(100, -margin);
		wOutputFieldsGroup.setLayoutData(fdOutputFieldsGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF OutputFields  GROUP
		// ///////////////////////////////////////////////////////////
		

		// THE UPDATE/INSERT TABLE
		wlReturn=new Label(wFieldsComp, SWT.NONE);
		wlReturn.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.ReturnFields.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn=new FormData();
		fdlReturn.left  = new FormAttachment(0, 0);
		fdlReturn.top   = new FormAttachment(wOutputFieldsGroup, margin);
		wlReturn.setLayoutData(fdlReturn);
		
		wGetLU=new Button(wFieldsComp, SWT.PUSH);
		wGetLU.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.GetLookupFields.Button")); //$NON-NLS-1$
		FormData fdlu = new FormData();
		fdlu.top = new FormAttachment(wlReturn, margin);
		fdlu.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdlu);
		
		
		int UpInsCols=2;
		int UpInsRows= (input.getValue()!=null?input.getValue().length:1);
		
		ciReturn=new ColumnInfo[UpInsCols];
		ciReturn[0]=new ColumnInfo(BaseMessages.getString(PKG, "FuzzyMatchDialog.ColumnInfo.FieldReturn"),    ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1]=new ColumnInfo(BaseMessages.getString(PKG, "FuzzyMatchDialog.ColumnInfo.NewName"), ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
		
		wReturn=new TableView(transMeta, wFieldsComp, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
							  ciReturn, 
							  UpInsRows,  
							  lsMod,
							  props
							  );

		fdReturn=new FormData();
		fdReturn.left  = new FormAttachment(0, 0);
		fdReturn.top   = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wGetLU, -margin);
		fdReturn.bottom= new FormAttachment(100, -3*margin);
		wReturn.setLayoutData(fdReturn);


		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);
	
		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF Fields TAB
		/////////////////////////////////////////////////////////////
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(wOK, -margin);
		wTabFolder.setLayoutData(fdTabFolder);


		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		lsGetLU    = new Listener() { public void handleEvent(Event e) { getlookup(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		wGetLU.addListener (SWT.Selection, lsGetLU );
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
        setComboBoxesLookup();
		activeAlgorithm();
		activegetCloserValue();
		input.setChanged(changed);
		
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
		if(isDebug()) logDebug( BaseMessages.getString(PKG, "FuzzyMatchDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		wAlgorithm.setText(FuzzyMatchMeta.getAlgorithmTypeDesc(input.getAlgorithmType()));

		if(input.getMainStreamField()!=null) wMainStreamField.setText(input.getMainStreamField());
		if(input.getLookupField()!=null) wLookupField.setText(input.getLookupField());
		wcaseSensitive.setSelection(input.isCaseSensitive());
		wgetCloserValue.setSelection(input.isGetCloserValue());
		if(input.getMinimalValue()!=null) wminValue.setText(input.getMinimalValue());
		if(input.getMaximalValue()!=null) wmaxValue.setText(input.getMaximalValue());
		if(input.getOutputMatchField()!=null) wmatchField.setText(input.getOutputMatchField());
		if(input.getOutputValueField()!=null) wvalueField.setText(input.getOutputValueField());
		if(input.getSeparator()!=null) wseparator.setText(input.getSeparator());
		
		if (input.getValue()!=null)
			for (int i=0;i<input.getValue().length;i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getValue()[i]!=null     ) item.setText(1, input.getValue()[i]);
				if (input.getValueName()[i]!=null && !input.getValueName()[i].equals(input.getValue()[i]))
					item.setText(2, input.getValueName()[i]);
			}
		
		
		StreamInterface infoStream = input.getStepIOMeta().getInfoStreams().get(0);
		wStep.setText( Const.NVL(infoStream.getStepname(), "") );
		
		wStepname.selectAll();
		wReturn.setRowNums();
		wReturn.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		input.setMainStreamField(wMainStreamField.getText());
		input.setLookupField(wLookupField.getText());
		
		input.setAlgorithmType(FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText()));
		input.setCaseSensitive(wcaseSensitive.getSelection());
		input.setGetCloserValue(wgetCloserValue.getSelection());
		input.setMaximalValue(wmaxValue.getText());
		input.setMinimalValue(wminValue.getText());
		
		input.setOutputMatchField(wmatchField.getText());
		input.setOutputValueField(wvalueField.getText());
		input.setSeparator(wseparator.getText());
		
		
		int nrvalues           = wReturn.nrNonEmpty();
		input.allocate(nrvalues);
		if(isDebug()) logDebug(BaseMessages.getString(PKG, "FuzzyMatchDialog.Log.FoundFields",nrvalues+"")); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrvalues;i++)
		{
			TableItem item        = wReturn.getNonEmpty(i);
			input.getValue()[i]        = item.getText(1);
			input.getValueName()[i]    = item.getText(2);
			if (input.getValueName()[i]==null || input.getValueName()[i].length()==0)
				input.getValueName()[i] = input.getValue()[i];
		}
		
		StreamInterface infoStream = input.getStepIOMeta().getInfoStreams().get(0);
		infoStream.setStepMeta( transMeta.findStep( wStep.getText() ) );
		if (infoStream.getStepMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			if(Const.isEmpty(wStep.getText()))
				mb.setMessage(BaseMessages.getString(PKG, "FuzzyMatchDialog.NotStepSpecified.DialogMessage",wStep.getText())); 
			else
				mb.setMessage(BaseMessages.getString(PKG, "FuzzyMatchDialog.StepCanNotFound.DialogMessage",wStep.getText())); //$NON-NLS-1$ //$NON-NLS-2$
				
			mb.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.StepCanNotFound.DialogTitle")); //$NON-NLS-1$
			mb.open(); 
		}
		
		stepname = wStepname.getText(); // return value
		
		dispose();
	}

	 private void setMainStreamField()
	 {
		 if(!gotPreviousFields)
		 {
	          String field=  wMainStreamField.getText();
			 try{
				 wMainStreamField.removeAll();
					
				  RowMetaInterface r = transMeta.getPrevStepFields(stepname);
					if (r!=null)
					{
						wMainStreamField.setItems(r.getFieldNames());
					}
			 }catch(KettleException ke){
					new ErrorDialog(shell, BaseMessages.getString(PKG, "FuzzyMatchDialog.FailedToGetFields.DialogTitle"), 
							BaseMessages.getString(PKG, "FuzzyMatchDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			 }
			 if(field!=null) wMainStreamField.setText(field);
			 gotPreviousFields=true;
		 }
	 }
	 private void setLookupField()
	 {
		 if(!gotLookupFields)
		 {
	          String field=  wLookupField.getText();
			 try{
				 wLookupField.removeAll();
					
				  RowMetaInterface r = transMeta.getStepFields(wStep.getText());
					if (r!=null)
					{
						wLookupField.setItems(r.getFieldNames());
					}
			 }catch(KettleException ke){
					new ErrorDialog(shell, BaseMessages.getString(PKG, "FuzzyMatchDialog.FailedToGetLookupFields.DialogTitle"), 
							BaseMessages.getString(PKG, "FuzzyMatchDialog.FailedToGetLookupFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			 }
			 if(field!=null) wLookupField.setText(field);
			 gotLookupFields=true;
		 }
	 }
	 
	 private void activegetCloserValue()
	 {
		 boolean enableRange=(FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_LEVENSHTEIN
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_NEEDLEMAN_WUNSH
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_DAMERAU_LEVENSHTEIN
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_JARO
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_JARO_WINKLER
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_PAIR_SIMILARITY)
		 	&& !wgetCloserValue.getSelection();

		 wlseparator.setEnabled(enableRange);
		 wseparator.setEnabled(enableRange);
		 wlvalueField.setEnabled(wgetCloserValue.getSelection());
		 wvalueField.setEnabled(wgetCloserValue.getSelection());

		 activeAddFields();
	 }
	 private void activeAddFields()
	 {
		 boolean activate = wgetCloserValue.getSelection() || 
		 (FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_DOUBLE_METAPHONE)
		  ||(FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_SOUNDEX)
		  ||(FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_REFINED_SOUNDEX)
		  ||(FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_METAPHONE);
		 
		 wlReturn.setEnabled(activate);
		 wReturn.setEnabled(activate);
		 wGetLU.setEnabled(activate);
	 }
	private void activeAlgorithm()
	{
		 boolean enable=(FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_LEVENSHTEIN
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_NEEDLEMAN_WUNSH
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_DAMERAU_LEVENSHTEIN
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_JARO
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_JARO_WINKLER
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_PAIR_SIMILARITY);

		 wlgetCloserValue.setEnabled(enable);
		 wgetCloserValue.setEnabled(enable);
		 wlminValue.setEnabled(enable);
		 wminValue.setEnabled(enable);
		 wlmaxValue.setEnabled(enable);
		 wmaxValue.setEnabled(enable);
		 
		if( FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_JARO
		 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_JARO_WINKLER
		 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_PAIR_SIMILARITY) {
			 if(Const.toDouble(transMeta.environmentSubstitute(wminValue.getText()), 0)>1) {
				 	wminValue.setText(String.valueOf(1));
			 }
			 if(Const.toDouble(transMeta.environmentSubstitute(wmaxValue.getText()), 0)>1) {
				 	wmaxValue.setText(String.valueOf(1));
			 }
		}
	 
		 
		 boolean enableCaseSensitive =(FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_LEVENSHTEIN
				 || FuzzyMatchMeta.getAlgorithmTypeByDesc(wAlgorithm.getText())==FuzzyMatchMeta.OPERATION_TYPE_DAMERAU_LEVENSHTEIN);
		 wlcaseSensitive.setEnabled(enableCaseSensitive);
		 wcaseSensitive.setEnabled(enableCaseSensitive);
		 activegetCloserValue();
	}
	private void getlookup()
	{
		try
		{
			String stepFrom = wStep.getText();
			if (!Const.isEmpty(stepFrom))
			{
				RowMetaInterface r = transMeta.getStepFields(stepFrom);
				if (r!=null && !r.isEmpty())
				{
                    BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1 }, new int[] { 4 }, -1, -1, null);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage(BaseMessages.getString(PKG, "FuzzyMatchDialog.CouldNotFindFields.DialogMessage")); //$NON-NLS-1$
					mb.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.CouldNotFindFields.DialogTitle")); //$NON-NLS-1$
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(BaseMessages.getString(PKG, "FuzzyMatchDialog.StepNameRequired.DialogMessage")); //$NON-NLS-1$
				mb.setText(BaseMessages.getString(PKG, "FuzzyMatchDialog.StepNameRequired.DialogTitle")); //$NON-NLS-1$
				mb.open(); 
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "FuzzyMatchDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "FuzzyMatchDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	protected void setComboBoxesLookup()
    {
		Runnable fieldLoader = new Runnable() {
			public void run() {
		        StepMeta lookupStepMeta = transMeta.findStep(wStep.getText());
                if (lookupStepMeta!=null)
                {
                    try
                    {
                        RowMetaInterface row = transMeta.getStepFields(lookupStepMeta);
                        Map<String, Integer> lookupFields =new HashMap<String, Integer>();
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                            lookupFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        
                        // Something was changed in the row.
        		        //
        				final Map<String, Integer> fields = new HashMap<String, Integer>();
        		        
        		        // Add the currentMeta fields...
        		        fields.putAll(lookupFields);
        		        
        		        Set<String> keySet = fields.keySet();
        		        List<String> entries = new ArrayList<String>(keySet);
        		        
        		        String[] fieldNames= (String[]) entries.toArray(new String[entries.size()]);
        		        Const.sortStrings(fieldNames);
        		        // return fields
        		        ciReturn[0].setComboValues(fieldNames);
                    }
                    catch(KettleException e)
                    {
                    	 logError("It was not possible to retrieve the list of fields for step [" + wStep.getText() + "]!");
                    }
                }
			}
		};
		shell.getDisplay().asyncExec(fieldLoader);
    }
}
