package org.pentaho.di.partition.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;
import org.pentaho.di.core.dialog.EnterSelectionDialog;
import org.pentaho.di.core.gui.GUIResource;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.step.BaseStepDialog;

import org.pentaho.di.core.widget.ColumnInfo;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.gui.WindowProperty;
import org.pentaho.di.core.widget.TableView;




/**
 * 
 * Dialog that allows you to edit the settings of the cluster schema
 * 
 * @see ClusterSchema
 * @author Matt
 * @since 17-11-2006
 *
 */

public class PartitionSchemaDialog extends Dialog 
{
	private PartitionSchema partitionSchema;
	
	private Shell     shell;

    // Name
	private Text     wName;

    // Partitions
    private TableView wPartitions;
    
	private Button    wOK, wGet, wCancel;
	
    private ModifyListener lsMod;

	private Props     props;

    private int middle;
    private int margin;

    private PartitionSchema originalSchema;
    private boolean ok;

    private List databases;
    
	public PartitionSchemaDialog(Shell par, PartitionSchema partitionSchema, List databases)
	{
		super(par, SWT.NONE);
		this.partitionSchema=(PartitionSchema) partitionSchema.clone();
        this.originalSchema=partitionSchema;
        this.databases = databases;
        
		props=Props.getInstance();
        ok=false;
	}
	
	public boolean open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);
		shell.setImage((Image) GUIResource.getInstance().getImageConnection());
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				partitionSchema.setChanged();
			}
		};

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText(Messages.getString("PartitionSchemaDialog.Shell.Title"));
		shell.setLayout (formLayout);
 		
		// First, add the buttons...
		
		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(" &OK ");

        wGet    = new Button(shell, SWT.PUSH); 
        wGet.setText(Messages.getString("PartitionSchema.ImportPartitions"));

		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(" &Cancel ");

		Button[] buttons = new Button[] { wOK, wGet, wCancel };
		BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);
		
		// The rest stays above the buttons, so we added those first...
        
        // What's the schema name??
        Label wlName = new Label(shell, SWT.RIGHT); 
        props.setLook(wlName);
        wlName.setText(Messages.getString("PartitionSchemaDialog.PartitionName.Label"));
        FormData fdlServiceURL = new FormData();
        fdlServiceURL.top   = new FormAttachment(0, 0);
        fdlServiceURL.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlServiceURL.right = new FormAttachment(middle, 0);
        wlName.setLayoutData(fdlServiceURL);

        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        FormData fdServiceURL = new FormData();
        fdServiceURL.top  = new FormAttachment(0, 0);
        fdServiceURL.left = new FormAttachment(middle, margin); // To the right of the label
        fdServiceURL.right= new FormAttachment(95, 0);
        wName.setLayoutData(fdServiceURL);

        // Schema list:
        Label wlPartitions = new Label(shell, SWT.RIGHT);
        wlPartitions.setText(Messages.getString("PartitionSchemaDialog.Partitions.Label"));
        props.setLook(wlPartitions);
        FormData fdlPartitions=new FormData();
        fdlPartitions.left  = new FormAttachment(0, 0);
        fdlPartitions.right = new FormAttachment(middle, 0);
        fdlPartitions.top   = new FormAttachment(wName, margin);
        wlPartitions.setLayoutData(fdlPartitions);
        
        ColumnInfo[] partitionColumns=new ColumnInfo[] 
            {
                new ColumnInfo(Messages.getString("PartitionSchemaDialog.PartitionID.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
            };
        wPartitions=new TableView(shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              partitionColumns, 
                              1,  
                              lsMod,
                              props
                              );
        props.setLook(wPartitions);
        FormData fdPartitions=new FormData();
        fdPartitions.left   = new FormAttachment(middle, margin);
        fdPartitions.right  = new FormAttachment(100, 0);
        fdPartitions.top    = new FormAttachment(wName, margin);
        fdPartitions.bottom = new FormAttachment(wOK, -margin*2);
        wPartitions.setLayoutData(fdPartitions);
        
		// Add listeners
		wOK.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { ok(); } } );
        wGet.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { importPartitions(); } } );
        wCancel.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { cancel(); } } );
		
        SelectionAdapter selAdapter=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wName.addSelectionListener(selAdapter);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
	
		getData();

		BaseStepDialog.setSize(shell);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return ok;
	}
	

    public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
    
    public void getData()
	{
		wName.setText( Const.NVL(partitionSchema.getName(), "") );

        refreshPartitions();
        
		wName.setFocus();
	}
    
	private void refreshPartitions()
    {
        wPartitions.clearAll(false);
        String[] partitionIDs = partitionSchema.getPartitionIDs();
        for (int i=0;i<partitionIDs.length;i++)
        {
            TableItem item = new TableItem(wPartitions.table, SWT.NONE);
            if (partitionIDs[i]!=null) item.setText(1, partitionIDs[i]);
        }
        wPartitions.removeEmptyRows();
        wPartitions.setRowNums();
        wPartitions.optWidth(true);
    }

    private void cancel()
	{
		originalSchema = null;
		dispose();
	}
	
	public void ok()
	{
        getInfo();
        originalSchema.setName(partitionSchema.getName());
        originalSchema.setPartitionIDs(partitionSchema.getPartitionIDs());
        originalSchema.setChanged();

        ok=true;
        
        dispose();
	}
    
    // Get dialog info in securityService
	private void getInfo()
    {
        partitionSchema.setName(wName.getText());
        
        String parts[] = new String[wPartitions.nrNonEmpty()];
        for (int i=0;i<parts.length;i++)
        {
            parts[i] = wPartitions.getNonEmpty(i).getText(1);
        }
        partitionSchema.setPartitionIDs(parts);
    }
    
    protected void importPartitions()
    {
        ArrayList partitionedDatabaseNames = new ArrayList();
        
        for (int i=0;i<databases.size();i++)
        {
            DatabaseMeta databaseMeta = (DatabaseMeta) databases.get(i); 
            if (databaseMeta.isPartitioned())
            {
                partitionedDatabaseNames.add(databaseMeta.getName());
            }
        }
        String dbNames[] = (String[]) partitionedDatabaseNames.toArray(new String[partitionedDatabaseNames.size()]);
        
        if (dbNames.length>0)
        {
            EnterSelectionDialog dialog = new EnterSelectionDialog(shell, dbNames, Messages.getString("PartitionSchema.SelectDatabase"), 
						Messages.getString("PartitionSchema.SelectPartitionnedDatabase"));
            String dbName = dialog.open();
            if (dbName!=null)
            {
                DatabaseMeta databaseMeta = DatabaseMeta.findDatabase(databases, dbName);
                PartitionDatabaseMeta[] partitioningInformation = databaseMeta.getPartitioningInformation();
                if (partitioningInformation!=null)
                {
                    // Here we are...
                    wPartitions.clearAll(false);
                    
                    for (int i = 0; i < partitioningInformation.length; i++)
                    {
                        PartitionDatabaseMeta meta = partitioningInformation[i];
                        wPartitions.add(new String[] { meta.getPartitionId() } );
                    }
                    
                    wPartitions.removeEmptyRows();
                    wPartitions.setRowNums();
                    wPartitions.optWidth(true);
                }
            }
        }   
    }    
}