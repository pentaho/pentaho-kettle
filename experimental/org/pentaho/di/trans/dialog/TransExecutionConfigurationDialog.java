package org.pentaho.di.trans.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.StepMeta;

import be.ibridge.kettle.core.ColumnInfo;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.GUIResource;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.widget.TableView;



public class TransExecutionConfigurationDialog extends Dialog
{
    private Display display;
    private Shell parent;
    private Shell shell;
    private Props props;
    private boolean retval;
    
    private Button wOK, wCancel;
    
    private Group gLocal;
    
    private TransExecutionConfiguration configuration;
    private TransMeta transMeta;
    private ArrayList executedSteps;

    private Button wExecLocal;
    private Button wExecRemote;
    private Button wExecCluster;
    private Button wPreview;
    private Button wSafeMode;
    private Button wPrepareExecution;
    private Button wPostTransformation;
    private Button wStartExecution;
    private Button wShowTransformations;
    private CCombo wRemoteHost;
    private Label wlRemoteHost;
    private TableView wPreviewSteps;
    private Text wReplayDate;
    private TableView wArguments;
    private Label wlArguments;
    private Label wlVariables;
    private TableView wVariables;
    private SimpleDateFormat simpleDateFormat;
    private Label wlReplayDate;
    private Label wlLogLevel;
    private CCombo wLogLevel;
    
    
    public TransExecutionConfigurationDialog(Shell parent, TransExecutionConfiguration configuration, TransMeta transMeta)
    {
        super(parent);
        this.parent = parent;
        this.configuration = configuration;
        this.transMeta  = transMeta;
        
        this.executedSteps = transMeta.getTransHopSteps(false);
        
        props = Props.getInstance();
        
        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    }
    
    public boolean open()
    {
        display = parent.getDisplay();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageSpoonGraph());
        
        FormLayout formLayout = new FormLayout ();
        formLayout.marginWidth  = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("TransExecutionConfigurationDialog.Shell.Title")); //$NON-NLS-1$

        int margin = Const.MARGIN;
        int tabsize = 5*margin;
        int rightMiddle = 2*props.getMiddlePct()/3;
        
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("TransExecutionConfigurationDialog.Button.Launch"));
        wOK.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { ok(); }});
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));
        wCancel.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { cancel(); }});
        
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);
        
        
        gLocal = new Group(shell, SWT.SHADOW_ETCHED_IN);
        gLocal.setText(Messages.getString("TransExecutionConfigurationDialog.LocalGroup.Label")); //$NON-NLS-1$;
        // The layout
        FormLayout localLayout = new FormLayout();
        localLayout.marginWidth  = Const.FORM_MARGIN;
        localLayout.marginHeight = Const.FORM_MARGIN;
        gLocal.setLayout(localLayout);
        // 
        FormData fdLocal=new FormData();
        fdLocal.left   = new FormAttachment(0, 0);
        fdLocal.right  = new FormAttachment(100, 0);
        gLocal.setBackground(shell.getBackground()); // the default looks ugly
        gLocal.setLayoutData(fdLocal);

        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Local execution
        //
        wExecLocal=new Button(gLocal, SWT.RADIO);
        wExecLocal.setText(Messages.getString("TransExecutionConfigurationDialog.ExecLocal.Label")); //$NON-NLS-1$
        wExecLocal.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.ExecLocal.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wExecLocal);
        FormData fdExecLocal = new FormData();
        fdExecLocal.left  = new FormAttachment(0, 0);
        fdExecLocal.right = new FormAttachment(33, 0);
        wExecLocal.setLayoutData(fdExecLocal);
        wExecLocal.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { enableFields(); }});
        
        /////////////////////////////////////////////////////////////////////////////////////////////////
        // remote execution
        //
        wExecRemote=new Button(gLocal, SWT.RADIO);
        wExecRemote.setText(Messages.getString("TransExecutionConfigurationDialog.ExecRemote.Label")); //$NON-NLS-1$
        wExecRemote.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.ExecRemote.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wExecRemote);
        FormData fdExecRemote = new FormData();
        fdExecRemote.left  = new FormAttachment(33, margin);
        fdExecRemote.right = new FormAttachment(66, 0);
        wExecRemote.setLayoutData(fdExecRemote);
        wExecRemote.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { enableFields(); }});

        wlRemoteHost = new Label(gLocal, SWT.LEFT);
        props.setLook(wlRemoteHost);
        wlRemoteHost.setText(Messages.getString("TransExecutionConfigurationDialog.RemoteHost.Label")); //$NON-NLS-1$
        wlRemoteHost.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.RemoteHost.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlRemoteHost = new FormData();
        fdlRemoteHost.left  = new FormAttachment(33, tabsize);
        fdlRemoteHost.top   = new FormAttachment(wExecRemote, margin*2);
        wlRemoteHost.setLayoutData(fdlRemoteHost);

        wRemoteHost = new CCombo(gLocal, SWT.READ_ONLY | SWT.BORDER);
        wRemoteHost.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.RemoteHost.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wRemoteHost);
        FormData fdRemoteHost = new FormData();
        fdRemoteHost.left  = new FormAttachment(wlRemoteHost, margin);
        fdRemoteHost.right = new FormAttachment(66, 0);
        fdRemoteHost.top   = new FormAttachment(wExecRemote, margin*2);
        wRemoteHost.setLayoutData(fdRemoteHost);
        for (int i=0;i<transMeta.getSlaveServers().size();i++)
        {
            SlaveServer slaveServer = (SlaveServer)transMeta.getSlaveServers().get(i);
            wRemoteHost.add(slaveServer.toString());
        }
        
        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Clustered execution
        //
        wExecCluster=new Button(gLocal, SWT.RADIO);
        wExecCluster.setText(Messages.getString("TransExecutionConfigurationDialog.ExecCluster.Label")); //$NON-NLS-1$
        wExecCluster.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.ExecCluster.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wExecCluster);
        FormData fdExecCluster = new FormData();
        fdExecCluster.left  = new FormAttachment(66, margin);
        fdExecCluster.right = new FormAttachment(100, 0);
        wExecCluster.setLayoutData(fdExecCluster);
        wExecCluster.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { enableFields(); }});

        wPostTransformation = new Button(gLocal, SWT.CHECK);
        wPostTransformation.setText(Messages.getString("TransExecutionConfigurationDialog.PostTransformation.Label")); //$NON-NLS-1$
        wPostTransformation.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.PostTransformation.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wPostTransformation);
        FormData fdPostTransformation = new FormData();
        fdPostTransformation.left  = new FormAttachment(66, tabsize);
        fdPostTransformation.right = new FormAttachment(100, 0);
        fdPostTransformation.top   = new FormAttachment(wExecCluster, margin*2);
        wPostTransformation.setLayoutData(fdPostTransformation);

        wPrepareExecution = new Button(gLocal, SWT.CHECK);
        wPrepareExecution.setText(Messages.getString("TransExecutionConfigurationDialog.PrepareExecution.Label")); //$NON-NLS-1$
        wPrepareExecution.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.PrepareExecution.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wPrepareExecution);
        FormData fdPrepareExecution = new FormData();
        fdPrepareExecution.left  = new FormAttachment(66, tabsize);
        fdPrepareExecution.right = new FormAttachment(100, 0);
        fdPrepareExecution.top   = new FormAttachment(wPostTransformation, margin);
        wPrepareExecution.setLayoutData(fdPrepareExecution);
        
        wStartExecution = new Button(gLocal, SWT.CHECK);
        wStartExecution.setText(Messages.getString("TransExecutionConfigurationDialog.StartExecution.Label")); //$NON-NLS-1$
        wStartExecution.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.StartExecution.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wStartExecution);
        FormData fdStartExecution = new FormData();
        fdStartExecution.left  = new FormAttachment(66, tabsize);
        fdStartExecution.right = new FormAttachment(100, 0);
        fdStartExecution.top   = new FormAttachment(wPrepareExecution, margin);
        wStartExecution.setLayoutData(fdStartExecution);

        wShowTransformations = new Button(gLocal, SWT.CHECK);
        wShowTransformations.setText(Messages.getString("TransExecutionConfigurationDialog.ShowTransformations.Label")); //$NON-NLS-1$
        wShowTransformations.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.ShowTransformations.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wShowTransformations);
        FormData fdShowTransformations = new FormData();
        fdShowTransformations.left  = new FormAttachment(66, tabsize);
        fdShowTransformations.right = new FormAttachment(100, 0);
        fdShowTransformations.top   = new FormAttachment(wStartExecution, margin);
        wShowTransformations.setLayoutData(fdShowTransformations);

        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Replay date, arguments & variables
        //

        wPreview = new Button(shell, SWT.CHECK);
        wPreview.setText(Messages.getString("TransExecutionConfigurationDialog.Preview.Label")); //$NON-NLS-1$
        wPreview.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.Preview.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wPreview);
        FormData fdPreview = new FormData();
        fdPreview.left  = new FormAttachment(  0, 0);
        fdPreview.right = new FormAttachment( 50, 0);
        fdPreview.top   = new FormAttachment(gLocal, margin*2);
        wPreview.setLayoutData(fdPreview);
        wPreview.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { enableFields(); }});
        
        ColumnInfo[] cPreviewSteps = {
              new ColumnInfo( Messages.getString("TransExecutionConfigurationDialog.PreviewColumn.Stepname"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ), //Stepname
              new ColumnInfo( Messages.getString("TransExecutionConfigurationDialog.PreviewColumn.PreviewSize"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //Preview size
            };
        cPreviewSteps[1].setValueType(ValueMetaInterface.TYPE_INTEGER);
                
        wPreviewSteps = new TableView(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, cPreviewSteps, executedSteps.size(), false, null, props);
        FormData fdPreviewSteps = new FormData();
        fdPreviewSteps.left   = new FormAttachment(  0, 0);
        fdPreviewSteps.top    = new FormAttachment(wPreview, margin);
        fdPreviewSteps.right  = new FormAttachment( 50, -margin);
        fdPreviewSteps.bottom = new FormAttachment( 60, 0);
        wPreviewSteps.setLayoutData(fdPreviewSteps);
        
        wSafeMode = new Button(shell, SWT.CHECK);
        wSafeMode.setText(Messages.getString("TransExecutionConfigurationDialog.SafeMode.Label")); //$NON-NLS-1$
        wSafeMode.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.SafeMode.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wSafeMode);
        FormData fdSafeMode = new FormData();
        fdSafeMode.left  = new FormAttachment( 50, margin);
        fdSafeMode.right = new FormAttachment(100, 0);
        fdSafeMode.top   = new FormAttachment(wPreview, margin);
        wSafeMode.setLayoutData(fdSafeMode);
        wSafeMode.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { enableFields(); }});

        wlLogLevel = new Label(shell, SWT.LEFT);
        props.setLook(wlLogLevel);
        wlLogLevel.setText(Messages.getString("TransExecutionConfigurationDialog.LogLevel.Label")); //$NON-NLS-1$
        wlLogLevel.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.LogLevel.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlLogLevel = new FormData();
        fdlLogLevel.left  = new FormAttachment(50, margin);
        fdlLogLevel.right = new FormAttachment(50+rightMiddle, 0);
        fdlLogLevel.top   = new FormAttachment(wSafeMode, margin*2);
        wlLogLevel.setLayoutData(fdlLogLevel);

        wLogLevel = new CCombo(shell, SWT.READ_ONLY | SWT.BORDER);
        wLogLevel.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.LogLevel.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wLogLevel);
        FormData fdLogLevel = new FormData();
        fdLogLevel.left  = new FormAttachment(50+rightMiddle, margin);
        fdLogLevel.right = new FormAttachment(100, 0);
        fdLogLevel.top   = new FormAttachment(wSafeMode, margin*2);
        wLogLevel.setLayoutData(fdLogLevel);
        wLogLevel.setItems( LogWriter.log_level_desc_long );

        // ReplayDate
        wlReplayDate = new Label(shell, SWT.LEFT);
        props.setLook(wlReplayDate);
        wlReplayDate.setText(Messages.getString("TransExecutionConfigurationDialog.ReplayDate.Label")); //$NON-NLS-1$
        wlReplayDate.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.ReplayDate.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlReplayDate = new FormData();
        fdlReplayDate.left   = new FormAttachment(50, margin);
        fdlReplayDate.right  = new FormAttachment(50+rightMiddle, 0);
        fdlReplayDate.top    = new FormAttachment(wLogLevel, margin*2);
        wlReplayDate.setLayoutData(fdlReplayDate);

        wReplayDate = new Text(shell, SWT.LEFT | SWT.BORDER | SWT.SINGLE);
        props.setLook(wReplayDate);
        wReplayDate.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.ReplayDate.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdReplayDate = new FormData();
        fdReplayDate.left   = new FormAttachment(50+rightMiddle, margin);
        fdReplayDate.right  = new FormAttachment(100, 0);
        fdReplayDate.top    = new FormAttachment(wLogLevel, margin*2);
        wReplayDate.setLayoutData(fdReplayDate);


        
        // Arguments
        wlArguments = new Label(shell, SWT.LEFT);
        props.setLook(wlArguments);
        wlArguments.setText(Messages.getString("TransExecutionConfigurationDialog.Arguments.Label")); //$NON-NLS-1$
        wlArguments.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.Arguments.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlArguments = new FormData();
        fdlArguments.left   = new FormAttachment(0, 0);
        fdlArguments.right  = new FormAttachment(50, 0);
        fdlArguments.top    = new FormAttachment(wPreviewSteps, margin*2);
        wlArguments.setLayoutData(fdlArguments);

        ColumnInfo[] cArguments = {
            new ColumnInfo( Messages.getString("TransExecutionConfigurationDialog.ArgumentsColumn.Argument"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ), //Stepname
            new ColumnInfo( Messages.getString("TransExecutionConfigurationDialog.ArgumentsColumn.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //Preview size
          };
              
        int nrArguments = configuration.getArguments() !=null ? configuration.getArguments().size() : 0; 
        wArguments = new TableView(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, cArguments, nrArguments, true, null, props);
        FormData fdArguments = new FormData();
        fdArguments.left   = new FormAttachment(0, 0);
        fdArguments.right  = new FormAttachment(50, -margin);
        fdArguments.top    = new FormAttachment(wlArguments, margin);
        fdArguments.bottom = new FormAttachment(wOK, -margin*2);
        wArguments.setLayoutData(fdArguments);

        // Variables
        wlVariables = new Label(shell, SWT.LEFT);
        props.setLook(wlVariables);
        wlVariables.setText(Messages.getString("TransExecutionConfigurationDialog.Variables.Label")); //$NON-NLS-1$
        wlVariables.setToolTipText(Messages.getString("TransExecutionConfigurationDialog.Variables.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlVariables = new FormData();
        fdlVariables.left   = new FormAttachment(50, margin);
        fdlVariables.right  = new FormAttachment(100, 0);
        fdlVariables.top    = new FormAttachment(wPreviewSteps, margin*2);
        wlVariables.setLayoutData(fdlVariables);

        ColumnInfo[] cVariables = {
            new ColumnInfo( Messages.getString("TransExecutionConfigurationDialog.VariablesColumn.Argument"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //Stepname
            new ColumnInfo( Messages.getString("TransExecutionConfigurationDialog.VariablesColumn.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //Preview size
          };
              
        int nrVariables = configuration.getVariables() !=null ? configuration.getVariables().size() : 0; 
        wVariables = new TableView(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, cVariables, nrVariables, true, null, props);
        FormData fdVariables = new FormData();
        fdVariables.left   = new FormAttachment(50, margin);
        fdVariables.right  = new FormAttachment(100, 0);
        fdVariables.top    = new FormAttachment(wlVariables, margin);
        fdVariables.bottom = new FormAttachment(wOK, -margin*2);
        wVariables.setLayoutData(fdVariables);

        getData();
        
        BaseStepDialog.setSize(shell);
        
        // Set the focus on the OK button
        wOK.setFocus();
        
        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        
        return retval;
    }

    private void getVariablesData()
    {
        wVariables.clearAll(false);
        for (int i=0;i<configuration.getVariables().size();i++)
        {
            ValueMetaInterface valueMeta = configuration.getVariables().getValueMeta(i);
            Object valueData = configuration.getVariables().getData()[i];
            
            TableItem tableItem = new TableItem(wVariables.table, SWT.NONE);
            tableItem.setText(1, valueMeta.getName());
            try
            {
                tableItem.setText(2, Const.NVL(valueMeta.getString(valueData), ""));;
            }
            catch (KettleValueException e)
            {
            }
            
            
        }
        wVariables.removeEmptyRows();
        wVariables.setRowNums();
        wVariables.optWidth(true);
    }

    private void getArgumentsData()
    {
        wArguments.clearAll(false);
        for (int i=0;i<configuration.getArguments().size();i++)
        {
            ValueMetaInterface valueMeta = configuration.getVariables().getValueMeta(i);
            Object valueData = configuration.getVariables().getData()[i];
           
            TableItem tableItem = new TableItem(wArguments.table, SWT.NONE);
            tableItem.setText(1, valueMeta.getName());
            try
            {
                tableItem.setText(2, Const.NVL(valueMeta.getString(valueData), ""));;
            }
            catch (KettleValueException e)
            {
            }
        }
        wArguments.removeEmptyRows();
        wArguments.setRowNums();
        wArguments.optWidth(true);
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */ 
    private void getPreviewStepsData()
    {
        String prSteps[] = props.getLastPreview();
        int    prSizes[] = props.getLastPreviewSize();
        
        boolean sizesSet=false;
        int selectedSteps = 0;
        for (int i=0;i<executedSteps.size();i++)
        {
            StepMeta stepMeta = (StepMeta) executedSteps.get(i);
            if (stepMeta.isSelected()) selectedSteps++;
        }
        
        if (selectedSteps==0)
        {
            for (int i=0;i<executedSteps.size();i++)
            {
                StepMeta stepMeta = (StepMeta) executedSteps.get(i);
                
                TableItem item = wPreviewSteps.table.getItem(i);
                String name = stepMeta.getName();
                item.setText(1, name);
                item.setText(2, "0");
    
                // Remember the last time...?
                for (int x=0;x<prSteps.length;x++)
                {
                    if (prSteps[x].equalsIgnoreCase(name)) 
                    {
                        item.setText(2, ""+prSizes[x]);
                        sizesSet=true;
                    } 
                }
            }
        }
        else
        {       
            // No previous selection: set the selected steps to the default preview size
            //
            for (int i=0;i<executedSteps.size();i++)
            {
                StepMeta stepMeta = (StepMeta) executedSteps.get(i);
                
                TableItem item = wPreviewSteps.table.getItem(i);
                String name = stepMeta.getName();
                item.setText(1, name);
                item.setText(2, "");
    
                // Is the step selected?
                if (stepMeta.isSelected())
                {
                    item.setText(2, ""+props.getDefaultPreviewSize());
                    sizesSet=true;
                }
            }
        }
        
        if (sizesSet)
        {
            wPreviewSteps.sortTable(2, true);
        }
        wPreviewSteps.setRowNums();
        wPreviewSteps.optWidth(true);
    }

    private void cancel()
    {
        dispose();
    }
    
    private void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
    }

    private void ok()
    {
        getInfo();
        retval=true;
        dispose();
    }
    
    public void getData()
    {
        wExecLocal.setSelection(configuration.isExecutingLocally());
        wExecRemote.setSelection(configuration.isExecutingRemotely());
        wExecCluster.setSelection(configuration.isExecutingClustered());
        wPreview.setSelection(configuration.isLocalPreviewing());
        wSafeMode.setSelection(configuration.isSafeModeEnabled());
        wPrepareExecution.setSelection(configuration.isClusterPreparing());
        wPostTransformation.setSelection(configuration.isClusterPosting());
        wStartExecution.setSelection(configuration.isClusterStarting());
        wShowTransformations.setSelection(configuration.isClusterShowingTransformation());
        wRemoteHost.setText( configuration.getRemoteServer()==null ? "" : configuration.getRemoteServer().toString() );
        wLogLevel.setText( LogWriter.getInstance().getLogLevelDesc() );
        if (configuration.getReplayDate()!=null) wReplayDate.setText(simpleDateFormat.format(configuration.getReplayDate()));
        getPreviewStepsData();
        getArgumentsData();
        getVariablesData();
        
        enableFields();
    }
    
    public void getInfo()
    {
        try
        {
            if (!Const.isEmpty(wReplayDate.getText()))
            {
                configuration.setReplayDate(simpleDateFormat.parse(wReplayDate.getText()));
            }
            else
            {
                configuration.setReplayDate(null);
            }
            configuration.setExecutingLocally(wExecLocal.getSelection());
            configuration.setExecutingRemotely(wExecRemote.getSelection());
            configuration.setExecutingClustered(wExecCluster.getSelection());
            
            // Local data
            configuration.setLocalPreviewing(wPreview.getSelection());
            if (wPreview.getSelection()) // only overwrite preview data if we selected to do so
            {
                getInfoPreview();
            }
            
            // Remote data
            if (wExecRemote.getSelection())
            {
                String serverName = wRemoteHost.getText();
                configuration.setRemoteServer(transMeta.findSlaveServer(serverName));
            }
            
            // Clustering data
            configuration.setClusterPosting(wPostTransformation.getSelection());
            configuration.setClusterPreparing(wPrepareExecution.getSelection());
            configuration.setClusterStarting(wStartExecution.getSelection());
            configuration.setClusterShowingTransformation(wShowTransformations.getSelection());
            
            configuration.setSafeModeEnabled(wSafeMode.getSelection() );
            configuration.setLogLevel( LogWriter.getLogLevel(wLogLevel.getText()) );
            
            // The lower part of the dialog...
            getInfoVariables();
            getInfoArguments();
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, "Error in settings", "There is an error in the dialog settings", e);
        }
    }
    
    private void getInfoVariables()
    {
        RowMetaAndData row = new RowMetaAndData();
        for (int i=0;i<wVariables.nrNonEmpty();i++)
        {
            TableItem tableItem = wVariables.getNonEmpty(i);
            String varName = tableItem.getText(1);
            String varValue = tableItem.getText(2);
            
            if (!Const.isEmpty(varName))
            {
                ValueMeta valueMeta = new ValueMeta(varName, ValueMetaInterface.TYPE_STRING);
                row.addValue(valueMeta, varValue);
            }
        }
        configuration.setVariables(row);
    }
    
    private void getInfoArguments()
    {
        RowMetaAndData row = new RowMetaAndData();
        for (int i=0;i<wArguments.nrNonEmpty();i++)
        {
            TableItem tableItem = wArguments.getNonEmpty(i);
            String varName = tableItem.getText(1);
            String varValue = tableItem.getText(2);
            
            if (!Const.isEmpty(varName))
            {
                ValueMeta value = new ValueMeta(varName, ValueMetaInterface.TYPE_STRING);
                row.addValue(value, varValue);
            }
        }
        configuration.setArguments(row);
    }

    
    private void getInfoPreview()
    {
        int sels=0;
        for (int i=0;i<wPreviewSteps.table.getItemCount();i++)
        {
            TableItem ti = wPreviewSteps.table.getItem(i);
            int size =  Const.toInt(ti.getText(2), 0);
            if (size > 0) 
            {
                sels++;
            } 
        }
        
        String[] previewSteps=new String[sels];
        int[]    previewSizes=new int   [sels];

        sels=0;     
        for (int i=0;i<wPreviewSteps.table.getItemCount();i++)
        {
            TableItem ti = wPreviewSteps.table.getItem(i);
            int size=Const.toInt(ti.getText(2), 0);

            if (size > 0) 
            {
                previewSteps[sels]=ti.getText(1);
                previewSizes[sels]=size;

                sels++;
            } 
        }
        
        configuration.setPreviewStepSizes(previewSteps, previewSizes);
        props.setLastPreview(previewSteps, previewSizes);
    }
    
    private void enableFields()
    {
        boolean enableLocal = wExecLocal.getSelection();
        boolean enablePreview = enableLocal && wPreview.getSelection();
        boolean enableRemote = wExecRemote.getSelection();
        boolean enableCluster = wExecCluster.getSelection();
        
        wPreview.setEnabled(enableLocal);
        wPreviewSteps.setEnabled(enablePreview);
        wPreviewSteps.table.setEnabled(enablePreview);
        
        // wlReplayDate.setEnabled(enableLocal);
        // wReplayDate.setEnabled(enableLocal);
        // wlArguments.setEnabled(enableLocal);
        // wArguments.setEnabled(enableLocal);
        // wArguments.table.setEnabled(enableLocal);
        // wlVariables.setEnabled(enableLocal);
        // wVariables.setEnabled(enableLocal);
        // wVariables.table.setEnabled(enableLocal);
        // wSafeMode.setEnabled(enableLocal);
        
        wRemoteHost.setEnabled(enableRemote);
        wlRemoteHost.setEnabled(enableRemote);
        
        wPostTransformation.setEnabled(enableCluster);
        wPrepareExecution.setEnabled(enableCluster);
        wStartExecution.setEnabled(enableCluster);
        wShowTransformations.setEnabled(enableCluster);
    }

    
    
    /**
     * @return the configuration
     */
    public TransExecutionConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(TransExecutionConfiguration configuration)
    {
        this.configuration = configuration;
    }

}
