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


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.trans.steps.fuzzymatch.FuzzyMatchMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ComboVar;
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
		
		// /////////////////////////////////
		// START OF Lookup  Fields GROUP
		// /////////////////////////////////

		wLookupGroup = new Group(shell, SWT.SHADOW_NONE);
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

		wMainStreamGroup = new Group(shell, SWT.SHADOW_NONE);
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

		wSettingsGroup = new Group(shell, SWT.SHADOW_NONE);
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
		
		// /////////////////////////////////
		// START OF OutputFields  Fields GROUP
		// /////////////////////////////////

		wOutputFieldsGroup = new Group(shell, SWT.SHADOW_NONE);
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
		

	
	
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wOutputFieldsGroup);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
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
		if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "FuzzyMatchDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
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
		
		StreamInterface infoStream = input.getStepIOMeta().getInfoStreams().get(0);
		wStep.setText( Const.NVL(infoStream.getStepname(), "") );
		
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
}
