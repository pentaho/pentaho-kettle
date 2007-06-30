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
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.trans.steps.mapping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.dialog.EnterMappingDialog;
import org.pentaho.di.core.dialog.EnterSelectionDialog;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.widget.ColumnInfo;
import org.pentaho.di.core.widget.TableView;
import org.pentaho.di.core.widget.TextVar;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.dialog.SelectObjectDialog;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;


public class MappingDialog extends BaseStepDialog implements StepDialogInterface
{
	private MappingMeta mappingMeta;

    private Group       gTransGroup;
    
    // File
    private Button      wFileRadio;
    
    private Button       wbbFilename; // Browse: add file or directory
    private TextVar      wFilename;
    
    // Repository
    private Button      wRepRadio;
    
    private TextVar     wTransName, wTransDir;
    private Button      wbTrans;
    
    private Button      wEditTrans;
    
    private CTabFolder  wTabFolder;
    
    TransMeta mappingTransMeta = null;

    protected boolean transModified;

	private ModifyListener lsMod;

	private int middle;

	private int margin;
	
	private MappingParameters mappingParameters;
	private List<MappingIODefinition> inputMappings;
	private List<MappingIODefinition> outputMappings;

	private Button wAddInput;

	private Button wAddOutput;
    
	public MappingDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		mappingMeta=(MappingMeta)in;
        transModified=false;

        // Make a copy for our own purposes...
        // This allows us to change everything directly in the classes with listeners.
        // Later we need to copy it to the input class on ok()
        //
        mappingParameters = (MappingParameters) mappingMeta.getMappingParameters().clone();
        inputMappings = new ArrayList<MappingIODefinition>();
        outputMappings = new ArrayList<MappingIODefinition>();
        for (int i=0;i<mappingMeta.getInputMappings().size();i++) inputMappings.add((MappingIODefinition) mappingMeta.getInputMappings().get(i).clone());
        for (int i=0;i<mappingMeta.getOutputMappings().size();i++) outputMappings.add((MappingIODefinition) mappingMeta.getOutputMappings().get(i).clone());
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
        setShellImage(shell, mappingMeta);

		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				mappingMeta.setChanged();
			}
		};
		changed = mappingMeta.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("MappingDialog.Shell.Title")); //$NON-NLS-1$
		
		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("MappingDialog.Stepname.Label")); //$NON-NLS-1$
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

        // Show a group with 2 main options: a transformation in the repository or on file
        //
        
        ////////////////////////////////////////////////////
        // The key creation box
        ////////////////////////////////////////////////////
        //
        gTransGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        gTransGroup.setText(Messages.getString("MappingDialog.TransGroup.Label")); //$NON-NLS-1$;
        gTransGroup.setBackground(shell.getBackground()); // the default looks ugly
        FormLayout transGroupLayout = new FormLayout();
        transGroupLayout.marginLeft=margin*2;
        transGroupLayout.marginTop=margin*2;
        transGroupLayout.marginRight=margin*2;
        transGroupLayout.marginBottom=margin*2;
        gTransGroup.setLayout(transGroupLayout);
        
        // Radio button: The mapping is in a file 
        // 
        wFileRadio=new Button(gTransGroup, SWT.RADIO);
        props.setLook(wFileRadio);
        wFileRadio.setSelection(false);
        wFileRadio.setText(Messages.getString("MappingDialog.RadioFile.Label")); //$NON-NLS-1$
        wFileRadio.setToolTipText(Messages.getString("MappingDialog.RadioFile.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdFileRadio = new FormData();
        fdFileRadio.left   = new FormAttachment(0, 0);
        fdFileRadio.right  = new FormAttachment(100, 0);
        fdFileRadio.top    = new FormAttachment(0, 0); 
        wFileRadio.setLayoutData(fdFileRadio);
        
        wbbFilename=new Button(gTransGroup, SWT.PUSH| SWT.CENTER); // Browse
        props.setLook(wbbFilename);
        wbbFilename.setText(Messages.getString("System.Button.Browse"));
        wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
        FormData fdbFilename = new FormData();
        fdbFilename.right= new FormAttachment(100, 0);
        fdbFilename.top  = new FormAttachment(wFileRadio, margin);
        wbbFilename.setLayoutData(fdbFilename);
        wbbFilename.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { selectFileTrans(); }});

        wFilename=new TextVar(transMeta, gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFilename);
        wFilename.addModifyListener(lsMod);
        FormData fdFilename = new FormData();
        fdFilename.left = new FormAttachment(0, 25);
        fdFilename.right= new FormAttachment(wbbFilename, -margin);
        fdFilename.top  = new FormAttachment(wbbFilename, 0, SWT.CENTER);
        wFilename.setLayoutData(fdFilename);
        wFilename.addModifyListener(new ModifyListener() { public void modifyText(ModifyEvent e) { wFileRadio.setSelection(true); wRepRadio.setSelection(false); }});
        
        // Radio button: The mapping is in the repository
        // 
        wRepRadio=new Button(gTransGroup, SWT.RADIO);
        props.setLook(wRepRadio);
        wRepRadio.setSelection(false);
        wRepRadio.setText(Messages.getString("MappingDialog.RadioRep.Label")); //$NON-NLS-1$
        wRepRadio.setToolTipText(Messages.getString("MappingDialog.RadioRep.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdRepRadio = new FormData();
        fdRepRadio.left   = new FormAttachment(0, 0);
        fdRepRadio.right  = new FormAttachment(100, 0);
        fdRepRadio.top    = new FormAttachment(wbbFilename, 2*margin); 
        wRepRadio.setLayoutData(fdRepRadio);

        wbTrans=new Button(gTransGroup, SWT.PUSH| SWT.CENTER); // Browse
        props.setLook(wbTrans);
        wbTrans.setText(Messages.getString("MappingDialog.Select.Button"));
        wbTrans.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
        FormData fdbTrans = new FormData();
        fdbTrans.right= new FormAttachment(100, 0);
        fdbTrans.top  = new FormAttachment(wRepRadio, 2*margin);
        wbTrans.setLayoutData(fdbTrans);
        wbTrans.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { selectRepositoryTrans(); }});
        
        wTransDir=new TextVar(transMeta, gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTransDir);
        wTransDir.addModifyListener(lsMod);
        FormData fdTransDir = new FormData();
        fdTransDir.left = new FormAttachment(middle+(100-middle)/2, 0);
        fdTransDir.right= new FormAttachment(wbTrans, -margin);
        fdTransDir.top  = new FormAttachment(wbTrans, 0, SWT.CENTER);
        wTransDir.setLayoutData(fdTransDir);

        wTransName=new TextVar(transMeta, gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTransName);
        wTransName.addModifyListener(lsMod);
        FormData fdTransName = new FormData();
        fdTransName.left = new FormAttachment(0, 25);
        fdTransName.right= new FormAttachment(wTransDir, -margin);
        fdTransName.top  = new FormAttachment(wbTrans, 0, SWT.CENTER);
        wTransName.setLayoutData(fdTransName);
        
        wEditTrans=new Button(gTransGroup, SWT.PUSH| SWT.CENTER); // Browse
        props.setLook(wEditTrans);
        wEditTrans.setText(Messages.getString("MappingDialog.Edit.Button"));
        wEditTrans.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
        FormData fdEditTrans = new FormData();
        fdEditTrans.left = new FormAttachment(0,   0);
        fdEditTrans.right= new FormAttachment(100, 0);
        fdEditTrans.top  = new FormAttachment(wTransName, 3*margin);
        wEditTrans.setLayoutData(fdEditTrans);
        wEditTrans.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editTrans(); }});

        FormData fdTransGroup = new FormData();
        fdTransGroup.left   = new FormAttachment(0, 0);
        fdTransGroup.top    = new FormAttachment(wStepname, 2*margin); 
        fdTransGroup.right  = new FormAttachment(100, 0);
        // fdTransGroup.bottom = new FormAttachment(wStepname, 350);
        gTransGroup.setLayoutData(fdTransGroup);

        
        // 
        // Add a tab folder for the parameters and various input and output streams
        //
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
        wTabFolder.setSimple(false);
        wTabFolder.setUnselectedCloseVisible(true);
        
        FormData fdTabFolder = new FormData();
        fdTabFolder.left   = new FormAttachment(0, 0);
        fdTabFolder.right  = new FormAttachment(100, 0);
        fdTabFolder.top    = new FormAttachment(gTransGroup, margin*2);
        fdTabFolder.bottom = new FormAttachment(100, -75);
        wTabFolder.setLayoutData(fdTabFolder);
        
		// Now add buttons that will allow us to add or remove input or output tabs...
		wAddInput = new Button(shell, SWT.PUSH);
		props.setLook(wAddInput);
		wAddInput.setText(Messages.getString("MappingDialog.button.AddInput"));
		wAddInput.addSelectionListener(new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent event) {
				
				// Simply add a new MappingIODefinition object to the inputMappings
				MappingIODefinition definition = new MappingIODefinition();
				inputMappings.add(definition);
				int index = inputMappings.size()-1;
				addInputMappingDefinitionTab(definition, index);
			}
		
		});

		wAddOutput = new Button(shell, SWT.PUSH);
		props.setLook(wAddOutput);
		wAddOutput.setText(Messages.getString("MappingDialog.button.AddOutput"));
        
		setButtonPositions(new Button[] { wAddInput, wAddOutput}, margin, wTabFolder);
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
        
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		mappingMeta.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
    private void selectRepositoryTrans()
    {
        try
        {
            SelectObjectDialog sod = new SelectObjectDialog(shell, repository);
            String transName = sod.open();
            RepositoryDirectory repdir = sod.getDirectory();
            if (transName!=null && repdir!=null)
            {
                loadRepositoryTrans(transName, repdir);
                wTransName.setText(mappingTransMeta.getName());
                wTransDir.setText(mappingTransMeta.getDirectory().getPath());
                wFilename.setText("");
                wRepRadio.setSelection(true);
                wFileRadio.setSelection(false);
            }
        }
        catch(KettleException ke)
        {
            new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorSelectingObject.DialogTitle"), Messages.getString("MappingDialog.ErrorSelectingObject.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    private void loadRepositoryTrans(String transName, RepositoryDirectory repdir) throws KettleException
    {
        // Read the transformation...
        //
        mappingTransMeta = new TransMeta(repository, transMeta.environmentSubstitute(transName), repdir);
        mappingTransMeta.clearChanged();
    }

    private void selectFileTrans()
    {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(Const.STRING_TRANS_FILTER_EXT);
        dialog.setFilterNames(Const.getTransformationFilterNames());
        String fname = dialog.open();
        if (fname!=null)
        {
            try
            {
                loadFileTrans(fname);
                wFilename.setText(mappingTransMeta.getFilename());
                wTransName.setText(Const.NVL(mappingTransMeta.getName(), ""));
                wTransDir.setText("");
                wFileRadio.setSelection(true);
                wRepRadio.setSelection(false);
            }
            catch(KettleException e)
            {
                new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorLoadingTransformation.DialogTitle"), Messages.getString("MappingDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    private void loadFileTrans(String fname) throws KettleException
    {
        mappingTransMeta = new TransMeta(transMeta.environmentSubstitute(fname));
        mappingTransMeta.clearChanged();
    }

    private void editTrans()
    {
        // Load the transformation again to make sure it's still there and refreshed
        // It's an extra check to make sure it's still OK...
        
        try
        {
            loadTransformation();
            
            // If we're still here, mappingTransMeta is valid.
        	SpoonInterface spoon = SpoonFactory.getInstance();
        	if( spoon != null ) {
                spoon.addTransGraph(mappingTransMeta);
        	}

        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorShowingTransformation.Title"), Messages.getString("MappingDialog.ErrorShowingTransformation.Message"), e);
        }
    }

    private void loadTransformation() throws KettleException
    {
        if (wFileRadio.getSelection() && !Const.isEmpty(wFilename.getText())) // Read from file...
        {
            loadFileTrans(wFilename.getText());
        }
        else
        {
            if (wRepRadio.getSelection() && repository!=null && !Const.isEmpty(wTransName.getText()) && !Const.isEmpty(wTransDir.getText()) )
            {
                RepositoryDirectory repdir = repository.getDirectoryTree().findDirectory(wTransDir.getText());
                if (repdir==null)
                {
                    throw new KettleException(Messages.getString("MappingDialog.Exception.UnableToFindRepositoryDirectory)"));
                }
                loadRepositoryTrans(wTransName.getText(), repdir);
            }
            else
            {
                 throw new KettleException(Messages.getString("MappingDialog.Exception.NoValidMappingDetailsFound"));
            }
        }    
    }

    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
        
        wFilename.setText(Const.NVL(mappingMeta.getFileName(), ""));
        wTransName.setText(Const.NVL(mappingMeta.getTransName(), ""));
        wTransDir.setText(Const.NVL(mappingMeta.getDirectoryPath(), ""));

        // if we have a filename, then we use the filename, otherwise we go with the repository...
        if (!Const.isEmpty(mappingMeta.getFileName()))
        {
            wFileRadio.setSelection(true);
        }
        else
        {
            if (repository!=null && !Const.isEmpty(mappingMeta.getTransName()) && !Const.isEmpty(mappingMeta.getDirectoryPath()))
            {
                wRepRadio.setSelection(true);
            }
        }
        
        setFlags();
        
        // Add the parameters tab
        addParametersTab(mappingParameters);
        wTabFolder.setSelection(0);
        
        // Now add the input stream tabs: where is our data coming from?
		for (int i = 0; i < inputMappings.size(); i++) {
			addInputMappingDefinitionTab(inputMappings.get(i), i);
		}

		// Now add the output stream tabs: where is our data going to?
		for (int i = 0; i < outputMappings.size(); i++) {
			addOutputMappingDefinitionTab(outputMappings.get(i), i);
		}
		
		try 
		{
			loadTransformation();
		}
		catch(Throwable t) {
			
		}
	}
	
	private void addOutputMappingDefinitionTab(MappingIODefinition definition, int index) {
		addMappingDefinitionTab(outputMappings.get(index), index+1+inputMappings.size(), Messages.getString("MappingDialog.OutputTab.Title"), Messages
				.getString("MappingDialog.InputTab.Tooltip"), Messages
				.getString("MappingDialog.OutputTab.label.InputSourceStepName"), Messages
				.getString("MappingDialog.OutputTab.label.OutputTargetStepName"), Messages
				.getString("MappingDialog.OutputTab.label.Description"), Messages
				.getString("MappingDialog.OutputTab.column.SourceField"), Messages
				.getString("MappingDialog.OutputTab.column.TargetField"),
				false);
		
	}

	private void addInputMappingDefinitionTab(MappingIODefinition definition, int index) {
		addMappingDefinitionTab(definition, index+1, Messages.getString("MappingDialog.InputTab.Title"), Messages
				.getString("MappingDialog.InputTab.Tooltip"), Messages
				.getString("MappingDialog.InputTab.label.InputSourceStepName"), Messages
				.getString("MappingDialog.InputTab.label.OutputTargetStepName"), Messages
				.getString("MappingDialog.InputTab.label.Description"), Messages
				.getString("MappingDialog.InputTab.column.SourceField"), Messages
				.getString("MappingDialog.InputTab.column.TargetField"),
				true);

	}

	private void addParametersTab(final MappingParameters parameters) {
    	
    	CTabItem wParametersTab = new CTabItem(wTabFolder, SWT.NONE);
        wParametersTab.setText(Messages.getString("MappingDialog.Parameters.Title")); //$NON-NLS-1$
        wParametersTab.setToolTipText(Messages.getString("MappingDialog.Parameters.Tooltip")); //$NON-NLS-1$

        Composite wParametersComposite = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wParametersComposite);

        FormLayout parameterTabLayout = new FormLayout();
        parameterTabLayout.marginWidth = Const.FORM_MARGIN;
        parameterTabLayout.marginHeight = Const.FORM_MARGIN;
        wParametersComposite.setLayout(parameterTabLayout);

        // Add the parameters source step line...
        //
        Button wbParamStep = new Button(wParametersComposite, SWT.PUSH);
        props.setLook(wbParamStep);
        wbParamStep.setText(Messages.getString("MappingDialog.button.SourceStepName"));
        FormData fdbParamStep = new FormData();
        fdbParamStep.top = new FormAttachment(0, 0);
        fdbParamStep.right = new FormAttachment(100, 0); // First one in the left top corner
        wbParamStep.setLayoutData(fdbParamStep);

        Label wlParamStep = new Label(wParametersComposite, SWT.RIGHT);
        props.setLook(wlParamStep);
        wlParamStep.setText(Messages.getString("MappingDialog.Parameters.label.SourceStepName")); //$NON-NLS-1$
        FormData fdlParamStep = new FormData();
        fdlParamStep.top = new FormAttachment(wbParamStep, 0, SWT.CENTER);
        fdlParamStep.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlParamStep.right = new FormAttachment(middle, -margin);
        wlParamStep.setLayoutData(fdlParamStep);

        final Text wParamStep = new Text(wParametersComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wParamStep);
        wParamStep.setText(Const.NVL(parameters.getStepname(), ""));
        wParamStep.addModifyListener(lsMod);
        FormData fdParamStep = new FormData();
        fdParamStep.top = new FormAttachment(wbParamStep, 0, SWT.CENTER);
        fdParamStep.left = new FormAttachment(middle, 0); // To the right of the label
        fdParamStep.right = new FormAttachment(wbParamStep, -margin);
        wParamStep.setLayoutData(fdParamStep);
        wParamStep.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				parameters.setStepname(wParamStep.getText());
			}
		});
        
        wbParamStep.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// select a step from the parent transformation, NOT the mapping
				//
				String stepName = selectTransformationStepname(true, false); 
				if (stepName!=null)
				{
					wParamStep.setText(stepName);
					parameters.setStepname(stepName);
				}
			}
		});

        // Now add a tableview with the 2 columns to specify: input and output fields for the source and target steps.
        //
        ColumnInfo[] colinfo = new ColumnInfo[] {
				new ColumnInfo(Messages.getString("MappingDialog.Parameters.column.Variable"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("MappingDialog.Parameters.column.ValueOrField"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
		};
        colinfo[1].setUsingVariables(true);
        
		final TableView wMappingParameters = new TableView(transMeta, wParametersComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo,
				parameters.getVariable().length, lsMod, props);
		props.setLook(wMappingParameters);
		FormData fdMappings = new FormData();
		fdMappings.left = new FormAttachment(0, 0);
		fdMappings.right = new FormAttachment(100, 0);
		fdMappings.top = new FormAttachment(wParamStep, margin * 2);
		fdMappings.bottom = new FormAttachment(100, -20);
		wMappingParameters.setLayoutData(fdMappings);
        
        for (int i = 0; i < parameters.getVariable().length ; i++) {
			TableItem tableItem = wMappingParameters.table.getItem(i);
			tableItem.setText(1, parameters.getVariable()[i]);
			tableItem.setText(2, parameters.getInputField()[i]);
		}
        wMappingParameters.setRowNums();
        wMappingParameters.optWidth(true);
        
        wMappingParameters.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				int nrLines = wMappingParameters.nrNonEmpty();
				String variables[] = new String[nrLines];
				String inputFields[] = new String[nrLines];
				parameters.setVariable(variables);
				parameters.setInputField(inputFields);
				for (int i=0;i<nrLines;i++)
				{
					TableItem item = wMappingParameters.getNonEmpty(i);
					variables[i]=item.getText(1);
					inputFields[i]=item.getText(2);
				}
			}
		});
        
        FormData fdParametersComposite = new FormData();
        fdParametersComposite.left = new FormAttachment(0, 0);
        fdParametersComposite.top = new FormAttachment(0, 0);
        fdParametersComposite.right = new FormAttachment(100, 0);
        fdParametersComposite.bottom = new FormAttachment(100, 0);
        wParametersComposite.setLayoutData(fdParametersComposite);

        wParametersComposite.layout();
        wParametersTab.setControl(wParametersComposite);

	}
    
    protected String selectTransformationStepname(boolean getTransformationStep, boolean mappingInput) {
    	String dialogTitle = Messages.getString("MappingDialog.SelectTransStep.Title");
    	String dialogMessage = Messages.getString("MappingDialog.SelectTransStep.Message");
    	if (getTransformationStep) {
    		dialogTitle = Messages.getString("MappingDialog.SelectTransStep.Title");
    		dialogMessage = Messages.getString("MappingDialog.SelectTransStep.Message");
	    	String[] stepnames;
	    	if (mappingInput) {
	    		stepnames = transMeta.getPrevStepNames(stepMeta);
	    	}
	    	else {
	    		stepnames = transMeta.getNextStepNames(stepMeta);
	    	}
			EnterSelectionDialog dialog = new EnterSelectionDialog(shell, stepnames, dialogTitle, dialogMessage);
			return dialog.open();
    	}
    	else {
    		dialogTitle = Messages.getString("MappingDialog.SelectMappingStep.Title");
    		dialogMessage = Messages.getString("MappingDialog.SelectMappingStep.Message");

    		String[] stepnames = getMappingSteps(mappingTransMeta, mappingInput);
			EnterSelectionDialog dialog = new EnterSelectionDialog(shell, stepnames, dialogTitle, dialogMessage);
			return dialog.open();
    	}
	}
    
    public static String[] getMappingSteps(TransMeta mappingTransMeta, boolean mappingInput)
    {
		List<StepMeta> steps = new ArrayList<StepMeta>();
		for (StepMeta stepMeta : mappingTransMeta.getSteps()) {
			if (mappingInput && stepMeta.getStepID().equals("MappingInput")) {
				steps.add(stepMeta);
			}
			if (!mappingInput && stepMeta.getStepID().equals("MappingOutput")) {
				steps.add(stepMeta);
			}
		}
		String[] stepnames = new String[steps.size()];
		for (int i=0;i<stepnames.length;i++) stepnames[i] = steps.get(i).getName();
		
		return stepnames;

    }
    
	public RowMetaInterface getFieldsFromStep(String stepname, boolean getTransformationStep, boolean mappingInput) throws KettleException { 
		if (!(mappingInput^getTransformationStep)) {
			if (Const.isEmpty(stepname)) {
				// If we don't have a specified stepname we return the input row metadata
				//
				return transMeta.getPrevStepFields(this.stepname);
			}
			else {
				// OK, a fieldname is specified...
				// See if we can find it...
				StepMeta stepMeta = transMeta.findStep(stepname);
				if (stepMeta==null) {
					throw new KettleException(Messages.getString("MappingDialog.Exception.SpecifiedStepWasNotFound", stepname));
				}
				return transMeta.getStepFields(stepMeta);
			}
			
		} 
		else {
			if (Const.isEmpty(stepname)) {
				// If we don't have a specified stepname we select the one and only "mapping input" step.
				//
				String[] stepnames = getMappingSteps(mappingTransMeta, mappingInput);
				if (stepnames.length>1) {
					throw new KettleException(Messages.getString("MappingDialog.Exception.OnlyOneMappingInputStepAllowed", ""+stepnames.length));
				}
				if (stepnames.length==0) {
					throw new KettleException(Messages.getString("MappingDialog.Exception.OneMappingInputStepRequired", ""+stepnames.length));
				}
				return mappingTransMeta.getStepFields(stepnames[0]);
			}
			else {
				// OK, a fieldname is specified...
				// See if we can find it...
				StepMeta stepMeta = mappingTransMeta.findStep(stepname);
				if (stepMeta==null) {
					throw new KettleException(Messages.getString("MappingDialog.Exception.SpecifiedStepWasNotFound", stepname));
				}
				return mappingTransMeta.getStepFields(stepMeta);
			}
		}
	}

	private void addMappingDefinitionTab(final MappingIODefinition definition, int index,
    		final String tabTitle, final String tabTooltip, 
    		String inputStepLabel, String outputStepLabel, 
    		String descriptionLabel,
    		String sourceColumnLabel, String targetColumnLabel,
    		final boolean input
    		) {
		
		final CTabItem wTab;
		if (index>=wTabFolder.getItemCount())
		{
			wTab = new CTabItem(wTabFolder, SWT.CLOSE);
		}
		else
		{
			wTab = new CTabItem(wTabFolder, SWT.CLOSE, index);
		}
    	setMappingDefinitionTabNameAndToolTip(wTab, tabTitle, tabTooltip, definition, input);

        Composite wInputComposite = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wInputComposite);

        FormLayout tabLayout = new FormLayout();
        tabLayout.marginWidth = Const.FORM_MARGIN;
        tabLayout.marginHeight = Const.FORM_MARGIN;
        wInputComposite.setLayout(tabLayout);

        // What's the stepname to read from? (empty is OK too)
        //
        Button wbInputStep = new Button(wInputComposite, SWT.PUSH);
        props.setLook(wbInputStep);
        wbInputStep.setText(Messages.getString("MappingDialog.button.SourceStepName"));
        FormData fdbInputStep = new FormData();
        fdbInputStep.top = new FormAttachment(0, 0);
        fdbInputStep.right = new FormAttachment(100, 0); // First one in the left top corner
        wbInputStep.setLayoutData(fdbInputStep);

        Label wlInputStep = new Label(wInputComposite, SWT.RIGHT);
        props.setLook(wlInputStep);
        wlInputStep.setText(inputStepLabel); //$NON-NLS-1$
        FormData fdlInputStep = new FormData();
        fdlInputStep.top = new FormAttachment(wbInputStep, 0, SWT.CENTER);
        fdlInputStep.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlInputStep.right = new FormAttachment(middle, -margin);
        wlInputStep.setLayoutData(fdlInputStep);

        final Text wInputStep = new Text(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wInputStep);
        wInputStep.setText(Const.NVL(definition.getInputStepname(), ""));
        wInputStep.addModifyListener(lsMod);
        FormData fdInputStep = new FormData();
        fdInputStep.top = new FormAttachment(wbInputStep, 0, SWT.CENTER);
        fdInputStep.left = new FormAttachment(middle, 0); // To the right of the label
        fdInputStep.right = new FormAttachment(wbInputStep, -margin);
        wInputStep.setLayoutData(fdInputStep);
        wInputStep.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				definition.setInputStepname(wInputStep.getText());
				setMappingDefinitionTabNameAndToolTip(wTab, tabTitle, tabTooltip, definition, input);
			}
		});
        wbInputStep.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String stepName = selectTransformationStepname(input, false);
				if (stepName!=null)
				{
					wInputStep.setText(stepName);
					definition.setInputStepname(stepName);
					setMappingDefinitionTabNameAndToolTip(wTab, tabTitle, tabTooltip, definition, input);
				}
			}
		});
        
        
        // What's the stepname to read from? (empty is OK too)
        //
        Button wbOutputStep = new Button(wInputComposite, SWT.PUSH);
        props.setLook(wbOutputStep);
        wbOutputStep.setText(Messages.getString("MappingDialog.button.SourceStepName"));
        FormData fdbOutputStep = new FormData();
        fdbOutputStep.top = new FormAttachment(wbInputStep, margin);
        fdbOutputStep.right = new FormAttachment(100, 0);
        wbOutputStep.setLayoutData(fdbOutputStep);
        
        Label wlOutputStep = new Label(wInputComposite, SWT.RIGHT);
        props.setLook(wlOutputStep);
        wlOutputStep.setText(outputStepLabel); //$NON-NLS-1$
        FormData fdlOutputStep = new FormData();
        fdlOutputStep.top = new FormAttachment(wbOutputStep, 0, SWT.CENTER);
        fdlOutputStep.left = new FormAttachment(0, 0);
        fdlOutputStep.right = new FormAttachment(middle, -margin);
        wlOutputStep.setLayoutData(fdlOutputStep);

        final Text wOutputStep = new Text(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wOutputStep);
        wOutputStep.setText(Const.NVL(definition.getOutputStepname(), ""));
        wOutputStep.addModifyListener(lsMod);
        FormData fdOutputStep = new FormData();
        fdOutputStep.top = new FormAttachment(wbOutputStep, 0, SWT.CENTER);
        fdOutputStep.left = new FormAttachment(middle, 0); // To the right of the label
        fdOutputStep.right = new FormAttachment(wbOutputStep, -margin);
        wOutputStep.setLayoutData(fdOutputStep);
        wOutputStep.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				definition.setOutputStepname(wOutputStep.getText());
			}
		});
        wbOutputStep.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String stepName = selectTransformationStepname(!input, false);
				if (stepName!=null)
				{
					wOutputStep.setText(stepName);
					definition.setOutputStepname(stepName);
				}
			}
		});

        // Add a checkbox to indicate the main step to read from, the main data path...
        Label wlMainPath = new Label(wInputComposite, SWT.RIGHT);
        props.setLook(wlMainPath);
        wlMainPath.setText(Messages.getString("MappingDialog.input.MainDataPath")); //$NON-NLS-1$
        FormData fdlMainPath = new FormData();
        fdlMainPath.top = new FormAttachment(wbOutputStep, margin);
        fdlMainPath.left = new FormAttachment(0, 0);
        fdlMainPath.right = new FormAttachment(middle, -margin);
        wlMainPath.setLayoutData(fdlMainPath);

        Button wMainPath = new Button(wInputComposite, SWT.CHECK);
        props.setLook(wMainPath);
        FormData fdMainPath = new FormData();
        fdMainPath.top = new FormAttachment(wbOutputStep, margin);
        fdMainPath.left = new FormAttachment(middle, 0);
        // fdMainPath.right = new FormAttachment(100, 0); // who cares, it's a checkbox
        wMainPath.setLayoutData(fdMainPath);
        
        wMainPath.setSelection(definition.isMainDataPath());
        wMainPath.addSelectionListener(new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent event) {
				definition.setMainDataPath(!definition.isMainDataPath()); // flip the switch
			}
		
		});
        
        // Allow for a small description
        //
        Label wlDescription = new Label(wInputComposite, SWT.RIGHT);
        props.setLook(wlDescription);
        wlDescription.setText(descriptionLabel); //$NON-NLS-1$
        FormData fdlDescription = new FormData();
        fdlDescription.top = new FormAttachment(wMainPath, margin);
        fdlDescription.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlDescription.right = new FormAttachment(middle, -margin);
        wlDescription.setLayoutData(fdlDescription);

        final Text wDescription = new Text(wInputComposite, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wDescription);
        wDescription.setText(Const.NVL(definition.getDescription(), ""));
        wDescription.addModifyListener(lsMod);
        FormData fdDescription = new FormData();
        fdDescription.top = new FormAttachment(wMainPath, margin);
        fdDescription.bottom = new FormAttachment(wOutputStep, 100+margin);
        fdDescription.left = new FormAttachment(middle, 0); // To the right of the label
        fdDescription.right = new FormAttachment(wbOutputStep, -margin);
        wDescription.setLayoutData(fdDescription);
        wDescription.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				definition.setDescription(wDescription.getText());
			}
		});

        // Now add a tableview with the 2 columns to specify: input and output fields for the source and target steps.
        //
        Button wbEnterMapping = new Button(wInputComposite, SWT.PUSH);
        props.setLook(wbEnterMapping);
        wbEnterMapping.setText(Messages.getString("MappingDialog.button.EnterMapping"));
        FormData fdbEnterMapping = new FormData();
        fdbEnterMapping.top = new FormAttachment(wDescription, margin * 2);
        fdbEnterMapping.right = new FormAttachment(100, 0); // First one in the left top corner
        wbEnterMapping.setLayoutData(fdbEnterMapping);
        
        ColumnInfo[] colinfo = new ColumnInfo[] {
				new ColumnInfo(sourceColumnLabel, ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
				new ColumnInfo(targetColumnLabel, ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
		};
		final TableView wFieldMappings = new TableView(transMeta, wInputComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo,
				1, lsMod, props);
		props.setLook(wFieldMappings);
		FormData fdMappings = new FormData();
		fdMappings.left = new FormAttachment(0, 0);
		fdMappings.right = new FormAttachment(wbEnterMapping, -margin);
		fdMappings.top = new FormAttachment(wDescription, margin * 2);
		fdMappings.bottom = new FormAttachment(100, -20);
		wFieldMappings.setLayoutData(fdMappings);
        
        for (int i = 0; i < definition.getParentField().length; i++) {
			TableItem tableItem = new TableItem(wFieldMappings.table, SWT.NONE);
			tableItem.setText(1, definition.getParentField()[i]);
			tableItem.setText(2, definition.getMappingField()[i]);
		}
        wFieldMappings.removeEmptyRows();
        wFieldMappings.setRowNums();
        wFieldMappings.optWidth(true);
        
        // Reset content of definition *every* time the table loses focus.
        // This might not be very elegant resource wise, but the code is :-)
        // Typically the number of fields passes is very small anyway.
        //
        FocusListener focusListener = new FocusAdapter() {
        	
        	@Override
			public void focusLost(FocusEvent event) {
			
        		System.out.println("!!!!!!!!!!!!  Field mapping : focus lost !!!!!!!!!!");
    			
        		int nrLines = wFieldMappings.nrNonEmpty();
				definition.setParentField(new String[nrLines]);
				definition.setMappingField(new String[nrLines]);
				for (int i=0;i<nrLines;i++)
				{
					TableItem item = wFieldMappings.getNonEmpty(i);
					definition.getParentField()[i]=item.getText(1);
					definition.getMappingField()[i]=item.getText(2);
					System.out.println("#"+i+" --> parent="+definition.getParentField()[i]+", mapping="+definition.getMappingField()[i]);
				}
			}
		};
		// wFieldMappings.addFocusListener(focusListener);
		wFieldMappings.table.addFocusListener(focusListener);
        
        wbEnterMapping.addSelectionListener(new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try
				{
					RowMetaInterface sourceRowMeta = getFieldsFromStep(wInputStep.getText(), true, input);
					RowMetaInterface targetRowMeta = getFieldsFromStep(wOutputStep.getText(), false, input);
					String sourceFields[] = sourceRowMeta.getFieldNames();
					String targetFields[] = targetRowMeta.getFieldNames();
					
					EnterMappingDialog dialog = new EnterMappingDialog(shell, sourceFields, targetFields);
					List<SourceToTargetMapping> mappings = dialog.open();
					if (mappings!=null) {
						// first clear the dialog...
						wFieldMappings.clearAll(false);
						
						// 
						definition.setParentField(new String[mappings.size()]);
						definition.setMappingField(new String[mappings.size()]);
						
						// Now add the new values...
						for (int i=0;i<mappings.size();i++) {
							SourceToTargetMapping mapping = mappings.get(i);
							TableItem item = new TableItem(wFieldMappings.table, SWT.NONE);
							item.setText(1, mapping.getSourceString(sourceFields));
							item.setText(2, mapping.getTargetString(targetFields));
							
						    definition.getParentField()[i] = input ? item.getText(1) : item.getText(2); 
						    definition.getMappingField()[i] = input ? item.getText(2) : item.getText(1); 
						}
						wFieldMappings.removeEmptyRows();
						wFieldMappings.setRowNums();
				        wFieldMappings.optWidth(true);
					}
				}
				catch(KettleException e)
				{
					new ErrorDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages.getString("MappingDialog.Exception.ErrorGettingMappingSourceAndTargetFields", e.toString()), e);
				}
			}
		
		});
        
        FormData fdParametersComposite = new FormData();
        fdParametersComposite.left = new FormAttachment(0, 0);
        fdParametersComposite.top = new FormAttachment(0, 0);
        fdParametersComposite.right = new FormAttachment(100, 0);
        fdParametersComposite.bottom = new FormAttachment(100, 0);
        wInputComposite.setLayoutData(fdParametersComposite);
        
        wInputComposite.layout();
        wTab.setControl(wInputComposite);
        
        // OK, suppose for some weird reason the user wants to remove an input or output tab...
        wTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
		
			@Override
			public void close(CTabFolderEvent event) {
				if (event.item.equals(wTab)) {
					// The user has the audacity to try and close this mapping definition tab.
					// We really should warn him that this is a bad idea...
					MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO);
					box.setText(Messages.getString("MappingDialog.CloseDefinitionTabAreYouSure.Title"));
					box.setMessage(Messages.getString("MappingDialog.CloseDefinitionTabAreYouSure.Message"));
					int answer = box.open();
					if (answer!=SWT.YES) {
						event.doit = false;
					} 
					else {
						// Remove it from our list to make sure it's gone...
						if (input) inputMappings.remove(definition);
						else outputMappings.remove(definition);
					}
				}
			}
		
		});
	}
	


	private void setMappingDefinitionTabNameAndToolTip(CTabItem wTab, String tabTitle, String tabTooltip, MappingIODefinition definition, boolean input) {
		
		String stepname;
		if (input)
		{
			stepname = definition.getInputStepname();
		}
		else
		{
			stepname = definition.getOutputStepname();
		}
		String description = definition.getDescription();
		
		if (Const.isEmpty(stepname)) {
    		wTab.setText(tabTitle); //$NON-NLS-1$
    	} else {
    		wTab.setText(tabTitle+" : "+stepname); //$NON-NLS-1$ $NON-NLS-2$
    	}
    	String tooltip = tabTooltip; //$NON-NLS-1$
    	if (!Const.isEmpty(stepname)) {
    		tooltip+=Const.CR+Const.CR+stepname;
    	}if (!Const.isEmpty(description)) {
    		tooltip+=Const.CR+Const.CR+description;
    	}
        wTab.setToolTipText(tooltip); //$NON-NLS-1$
	}

	private void setFlags()
    {
        if (repository==null)
        {
            wRepRadio.setEnabled(false);
            wbTrans.setEnabled(false);
            wTransName.setEnabled(false);
            wTransDir.setEnabled(false);
        }
    }

	private void cancel()
	{
		stepname=null;
		mappingMeta.setChanged(changed);
		dispose();
	}
	
	private void ok()
    {
        try
        {
            stepname = wStepname.getText(); // return value

            loadTransformation();
            
            mappingMeta.setFileName(wFilename.getText());
            mappingMeta.setTransName(wTransName.getText());
            mappingMeta.setDirectoryPath(wTransDir.getText());

            // Load the information on the tabs, optionally do some verifications...
            // 
            mappingMeta.setMappingParameters(mappingParameters);
            mappingMeta.setInputMappings(inputMappings);
            mappingMeta.setOutputMappings(outputMappings);
            
            mappingMeta.setChanged(true);

            dispose();
        }
        catch (KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorLoadingSpecifiedTransformation.Title"), 
                    Messages.getString("MappingDialog.ErrorLoadingSpecifiedTransformation.Message"), e);
        }
    }
}
