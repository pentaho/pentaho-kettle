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

package be.ibridge.kettle.trans.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.dialog.SelectDirectoryDialog;
import be.ibridge.kettle.trans.TransDependency;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.databaselookup.DatabaseLookupMeta;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;


public class TransDialog extends Dialog
{
	private LogWriter    log;
	
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wTransTab, wLogTab, wDateTab, wDepTab, wPerfTab;

	private FormData     fdTransComp, fdLogComp, fdDateComp, fdDepComp, fdPerfComp;
	
	private Label        wlTransname;
	private Text         wTransname;
    private FormData     fdlTransname, fdTransname;
    
    private Label        wlDirectory;
	private Text         wDirectory;
	private Button       wbDirectory;
    private FormData     fdlDirectory, fdbDirectory, fdDirectory;    

	private Label        wlModUser;
	private Text         wModUser;
	private FormData     fdlModUser, fdModUser;

	private Label        wlModDate;
	private Text         wModDate;
	private FormData     fdlModDate, fdModDate;

	private Label        wlReadStep;
	private CCombo       wReadStep;
	private FormData     fdlReadStep, fdReadStep;

	private Label        wlInputStep;
	private CCombo       wInputStep;
	private FormData     fdlInputStep, fdInputStep;

	private Label        wlWriteStep;
	private CCombo       wWriteStep;
	private FormData     fdlWriteStep, fdWriteStep;

	private Label        wlOutputStep;
	private CCombo       wOutputStep;
	private FormData     fdlOutputStep, fdOutputStep;

	private Label        wlUpdateStep;
	private CCombo       wUpdateStep;
	private FormData     fdlUpdateStep, fdUpdateStep;

	private Label        wlLogconnection;
	private Button       wbLogconnection;
	private CCombo       wLogconnection;
	private FormData     fdlLogconnection, fdbLogconnection, fdLogconnection;

	private Label        wlLogtable;
	private Text         wLogtable;
	private FormData     fdlLogtable, fdLogtable;
	
	private Label        wlBatch;
	private Button       wBatch;
	private FormData     fdlBatch, fdBatch;

	private Label        wlLogfield;
	private Button       wLogfield;
	private FormData     fdlLogfield, fdLogfield;

	private Label        wlMaxdateconnection;
	private CCombo       wMaxdateconnection;
	private FormData     fdlMaxdateconnection, fdMaxdateconnection;

	private Label        wlMaxdatetable;
	private Text         wMaxdatetable;
	private FormData     fdlMaxdatetable, fdMaxdatetable;

	private Label        wlMaxdatefield;
	private Text         wMaxdatefield;
	private FormData     fdlMaxdatefield, fdMaxdatefield;

	private Label        wlMaxdateoffset;
	private Text         wMaxdateoffset;
	private FormData     fdlMaxdateoffset, fdMaxdateoffset;

	private Label        wlMaxdatediff;
	private Text         wMaxdatediff;
	private FormData     fdlMaxdatediff, fdMaxdatediff;
	
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Label        wlSizeRowset;
	private Text         wSizeRowset;
	private FormData     fdlSizeRowset, fdSizeRowset;

	private Button wOK, wGet, wSQL, wCancel;
	private FormData fdGet;
	private Listener lsOK, lsGet, lsSQL, lsCancel;

	private TransMeta transMeta;
	private Shell  shell;
	
	private SelectionAdapter lsDef;
	
	private ModifyListener lsMod;
	private boolean changed;
	private Repository rep;
	private Props props;
	private RepositoryDirectory newDirectory;
	
    /** @deprecated */
	public TransDialog(Shell parent, int style, LogWriter log, Props props, TransMeta transMeta, Repository rep)
	{
		super(parent, style);
		this.log      = log;
		this.props    = props;
		this.transMeta    = transMeta;
		this.rep      = rep;
		
		this.newDirectory = null;
	}

 
    public TransDialog(Shell parent, int style, TransMeta transMeta, Repository rep)
    {
        super(parent, style);
        this.log      = LogWriter.getInstance();
        this.props    = Props.getInstance();
        this.transMeta    = transMeta;
        this.rep      = rep;
        
        this.newDirectory = null;
    }


	public TransMeta open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				transMeta.setChanged();
			}
		};
		changed = transMeta.hasChanged();
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Transformation properties");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
        
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		//////////////////////////
		// START OF TRANS TAB///
		///
		wTransTab=new CTabItem(wTabFolder, SWT.NONE);
		wTransTab.setText("Transformation");
		
		Composite wTransComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wTransComp);

		FormLayout transLayout = new FormLayout();
		transLayout.marginWidth  = Const.MARGIN;
		transLayout.marginHeight = Const.MARGIN;
		wTransComp.setLayout(transLayout);


		// Transformation name:
		wlTransname=new Label(wTransComp, SWT.RIGHT);
		wlTransname.setText("Transformation name :");
 		props.setLook(wlTransname);
		fdlTransname=new FormData();
		fdlTransname.left = new FormAttachment(0, 0);
		fdlTransname.right= new FormAttachment(middle, -margin);
		fdlTransname.top  = new FormAttachment(0, margin);
		wlTransname.setLayoutData(fdlTransname);
		wTransname=new Text(wTransComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTransname);
		wTransname.addModifyListener(lsMod);
		fdTransname=new FormData();
		fdTransname.left = new FormAttachment(middle, 0);
		fdTransname.top  = new FormAttachment(0, margin);
		fdTransname.right= new FormAttachment(100, 0);
		wTransname.setLayoutData(fdTransname);
		
		// Directory:
		wlDirectory=new Label(wTransComp, SWT.RIGHT);
		wlDirectory.setText("Directory :");
 		props.setLook(wlDirectory);
		fdlDirectory=new FormData();
		fdlDirectory.left = new FormAttachment(0, 0);
		fdlDirectory.right= new FormAttachment(middle, -margin);
		fdlDirectory.top  = new FormAttachment(wTransname, margin);
		wlDirectory.setLayoutData(fdlDirectory);

		wbDirectory=new Button(wTransComp, SWT.PUSH);
		wbDirectory.setText("...");
 		props.setLook(wbDirectory);
		fdbDirectory=new FormData();
		fdbDirectory.right= new FormAttachment(100, 0);
		fdbDirectory.top  = new FormAttachment(wTransname, margin);
		wbDirectory.setLayoutData(fdbDirectory);
		wbDirectory.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				RepositoryDirectory directoryFrom = transMeta.getDirectory();
				long idDirectoryFrom  = directoryFrom.getID();
				
				SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, rep);
				RepositoryDirectory rd = sdd.open();
				if (rd!=null)
				{
					if (idDirectoryFrom!=rd.getID())
					{
						// We need to change this in the repository as well!!
					    // We do this when the user pressed OK
					    newDirectory = rd;
						wDirectory.setText(rd.getPath());
					}
					else
					{
						// Same directory!
					}
				}
			}
		});

		wDirectory=new Text(wTransComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDirectory);
		wDirectory.setEditable(false);
		fdDirectory=new FormData();
		fdDirectory.left = new FormAttachment(middle, 0);
		fdDirectory.top  = new FormAttachment(wTransname, margin);
		fdDirectory.right= new FormAttachment(wbDirectory, 0);
		wDirectory.setLayoutData(fdDirectory);

		// Modified User:
		wlModUser=new Label(wTransComp, SWT.RIGHT);
		wlModUser.setText("Last modified by ");
 		props.setLook(wlModUser);
		fdlModUser=new FormData();
		fdlModUser.left = new FormAttachment(0, 0);
		fdlModUser.right= new FormAttachment(middle, -margin);
		fdlModUser.top  = new FormAttachment(wDirectory, margin*3);
		wlModUser.setLayoutData(fdlModUser);
		wModUser=new Text(wTransComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wModUser);
		wModUser.setEditable(false);
		wModUser.addModifyListener(lsMod);
		fdModUser=new FormData();
		fdModUser.left = new FormAttachment(middle, 0);
		fdModUser.top  = new FormAttachment(wDirectory, margin*3);
		fdModUser.right= new FormAttachment(100, 0);
		wModUser.setLayoutData(fdModUser);

		// Modified Date:
		wlModDate=new Label(wTransComp, SWT.RIGHT);
		wlModDate.setText("Last modified at ");
 		props.setLook(wlModDate);
		fdlModDate=new FormData();
		fdlModDate.left = new FormAttachment(0, 0);
		fdlModDate.right= new FormAttachment(middle, -margin);
		fdlModDate.top  = new FormAttachment(wModUser, margin);
		wlModDate.setLayoutData(fdlModDate);
		wModDate=new Text(wTransComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wModDate);
		wModDate.setEditable(false);
		wModDate.addModifyListener(lsMod);
		fdModDate=new FormData();
		fdModDate.left = new FormAttachment(middle, 0);
		fdModDate.top  = new FormAttachment(wModUser, margin);
		fdModDate.right= new FormAttachment(100, 0);
		wModDate.setLayoutData(fdModDate);

		fdTransComp=new FormData();
		fdTransComp.left  = new FormAttachment(0, 0);
		fdTransComp.top   = new FormAttachment(0, 0);
		fdTransComp.right = new FormAttachment(100, 0);
		fdTransComp.bottom= new FormAttachment(100, 0);
		wTransComp.setLayoutData(fdTransComp);
	
		wTransComp.layout();
		wTransTab.setControl(wTransComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF TRANS TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF LOG TAB///
		///
		wLogTab=new CTabItem(wTabFolder, SWT.NONE);
		wLogTab.setText("Logging");

		FormLayout LogLayout = new FormLayout ();
		LogLayout.marginWidth  = Const.MARGIN;
		LogLayout.marginHeight = Const.MARGIN;
		
		Composite wLogComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wLogComp);
		wLogComp.setLayout(LogLayout);


		// Log step: lines read...
		wlReadStep=new Label(wLogComp, SWT.RIGHT);
		wlReadStep.setText("READ log step: ");
 		props.setLook(wlReadStep);
		fdlReadStep=new FormData();
		fdlReadStep.left = new FormAttachment(0, 0);
		fdlReadStep.right= new FormAttachment(middle, -margin);
		fdlReadStep.top  = new FormAttachment(0, 0);
		wlReadStep.setLayoutData(fdlReadStep);
		wReadStep=new CCombo(wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wReadStep);
		wReadStep.addModifyListener(lsMod);
		fdReadStep=new FormData();
		fdReadStep.left = new FormAttachment(middle, 0);
		fdReadStep.top  = new FormAttachment(0, 0);
		fdReadStep.right= new FormAttachment(100, 0);
		wReadStep.setLayoutData(fdReadStep);

		// Log step: lines input...
		wlInputStep=new Label(wLogComp, SWT.RIGHT);
		wlInputStep.setText("INPUT log step: ");
 		props.setLook(wlInputStep);
		fdlInputStep=new FormData();
		fdlInputStep.left = new FormAttachment(0, 0);
		fdlInputStep.right= new FormAttachment(middle, -margin);
		fdlInputStep.top  = new FormAttachment(wReadStep, margin*2);
		wlInputStep.setLayoutData(fdlInputStep);
		wInputStep=new CCombo(wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInputStep);
		wInputStep.addModifyListener(lsMod);
		fdInputStep=new FormData();
		fdInputStep.left = new FormAttachment(middle, 0);
		fdInputStep.top  = new FormAttachment(wReadStep, margin*2);
		fdInputStep.right= new FormAttachment(100, 0);
		wInputStep.setLayoutData(fdInputStep);

		// Log step: lines written...
		wlWriteStep=new Label(wLogComp, SWT.RIGHT);
		wlWriteStep.setText("WRITE log step: ");
 		props.setLook(wlWriteStep);
		fdlWriteStep=new FormData();
		fdlWriteStep.left = new FormAttachment(0, 0);
		fdlWriteStep.right= new FormAttachment(middle, -margin);
		fdlWriteStep.top  = new FormAttachment(wInputStep, margin*2);
		wlWriteStep.setLayoutData(fdlWriteStep);
		wWriteStep=new CCombo(wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wWriteStep);
		wWriteStep.addModifyListener(lsMod);
		fdWriteStep=new FormData();
		fdWriteStep.left = new FormAttachment(middle, 0);
		fdWriteStep.top  = new FormAttachment(wInputStep, margin*2);
		fdWriteStep.right= new FormAttachment(100, 0);
		wWriteStep.setLayoutData(fdWriteStep);

		// Log step: lines to output...
		wlOutputStep=new Label(wLogComp, SWT.RIGHT);
		wlOutputStep.setText("OUTPUT log step: ");
 		props.setLook(wlOutputStep);
		fdlOutputStep=new FormData();
		fdlOutputStep.left = new FormAttachment(0, 0);
		fdlOutputStep.right= new FormAttachment(middle, -margin);
		fdlOutputStep.top  = new FormAttachment(wWriteStep, margin*2);
		wlOutputStep.setLayoutData(fdlOutputStep);
		wOutputStep=new CCombo(wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wOutputStep);
		wOutputStep.addModifyListener(lsMod);
		fdOutputStep=new FormData();
		fdOutputStep.left = new FormAttachment(middle, 0);
		fdOutputStep.top  = new FormAttachment(wWriteStep, margin*2);
		fdOutputStep.right= new FormAttachment(100, 0);
		wOutputStep.setLayoutData(fdOutputStep);

		// Log step: update...
		wlUpdateStep=new Label(wLogComp, SWT.RIGHT);
		wlUpdateStep.setText("UPDATE log step: ");
 		props.setLook(wlUpdateStep);
		fdlUpdateStep=new FormData();
		fdlUpdateStep.left = new FormAttachment(0, 0);
		fdlUpdateStep.right= new FormAttachment(middle, -margin);
		fdlUpdateStep.top  = new FormAttachment(wOutputStep, margin*2);
		wlUpdateStep.setLayoutData(fdlUpdateStep);
		wUpdateStep=new CCombo(wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUpdateStep);
		wUpdateStep.addModifyListener(lsMod);
		fdUpdateStep=new FormData();
		fdUpdateStep.left = new FormAttachment(middle, 0);
		fdUpdateStep.top  = new FormAttachment(wOutputStep, margin*2);
		fdUpdateStep.right= new FormAttachment(100, 0);
		wUpdateStep.setLayoutData(fdUpdateStep);

		for (int i=0;i<transMeta.nrSteps();i++)
		{
			StepMeta stepMeta = transMeta.getStep(i);
			wReadStep.add(stepMeta.getName());
			wWriteStep.add(stepMeta.getName());
			wInputStep.add(stepMeta.getName());
			wOutputStep.add(stepMeta.getName());
			wUpdateStep.add(stepMeta.getName());
		}

		// Log table connection...
		wlLogconnection=new Label(wLogComp, SWT.RIGHT);
		wlLogconnection.setText("Log Connection: ");
 		props.setLook(wlLogconnection);
		fdlLogconnection=new FormData();
		fdlLogconnection.left = new FormAttachment(0, 0);
		fdlLogconnection.right= new FormAttachment(middle, -margin);
		fdlLogconnection.top  = new FormAttachment(wUpdateStep, margin*4);
		wlLogconnection.setLayoutData(fdlLogconnection);

		wbLogconnection=new Button(wLogComp, SWT.PUSH);
		wbLogconnection.setText("&New...");
		wbLogconnection.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				DatabaseMeta ci = new DatabaseMeta();
				DatabaseDialog cid = new DatabaseDialog(shell, SWT.NONE, log, ci, props);
				if (cid.open()!=null)
				{
					transMeta.addDatabase(ci);
					wLogconnection.add(ci.getName());
					wLogconnection.select(wLogconnection.getItemCount()-1);
				}
			}
		});
		fdbLogconnection=new FormData();
		fdbLogconnection.right= new FormAttachment(100, 0);
		fdbLogconnection.top  = new FormAttachment(wUpdateStep, margin*4);
		wbLogconnection.setLayoutData(fdbLogconnection);

		wLogconnection=new CCombo(wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLogconnection);
		wLogconnection.addModifyListener(lsMod);
		fdLogconnection=new FormData();
		fdLogconnection.left = new FormAttachment(middle, 0);
		fdLogconnection.top  = new FormAttachment(wUpdateStep, margin*4);
		fdLogconnection.right= new FormAttachment(wbLogconnection, -margin);
		wLogconnection.setLayoutData(fdLogconnection);


		// Log table...:
		wlLogtable=new Label(wLogComp, SWT.RIGHT);
		wlLogtable.setText("Log table:");
 		props.setLook(wlLogtable);
		fdlLogtable=new FormData();
		fdlLogtable.left = new FormAttachment(0, 0);
		fdlLogtable.right= new FormAttachment(middle, -margin);
		fdlLogtable.top  = new FormAttachment(wLogconnection, margin);
		wlLogtable.setLayoutData(fdlLogtable);
		wLogtable=new Text(wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLogtable);
		wLogtable.addModifyListener(lsMod);
		fdLogtable=new FormData();
		fdLogtable.left = new FormAttachment(middle, 0);
		fdLogtable.top  = new FormAttachment(wLogconnection, margin);
		fdLogtable.right= new FormAttachment(100, 0);
		wLogtable.setLayoutData(fdLogtable);


		wlBatch=new Label(wLogComp, SWT.RIGHT);
		wlBatch.setText("Use Batch-ID? ");
 		props.setLook(wlBatch);
		fdlBatch=new FormData();
		fdlBatch.left = new FormAttachment(0, 0);
		fdlBatch.top  = new FormAttachment(wLogtable, margin);
		fdlBatch.right= new FormAttachment(middle, -margin);
		wlBatch.setLayoutData(fdlBatch);
		wBatch=new Button(wLogComp, SWT.CHECK);
 		props.setLook(wBatch);
		fdBatch=new FormData();
		fdBatch.left = new FormAttachment(middle, 0);
		fdBatch.top  = new FormAttachment(wLogtable, margin);
		fdBatch.right= new FormAttachment(100, 0);
		wBatch.setLayoutData(fdBatch);

		wlLogfield=new Label(wLogComp, SWT.RIGHT);
		wlLogfield.setText("Use logfield to store logging in? ");
 		props.setLook(wlLogfield);
		fdlLogfield=new FormData();
		fdlLogfield.left = new FormAttachment(0, 0);
		fdlLogfield.top  = new FormAttachment(wBatch, margin);
		fdlLogfield.right= new FormAttachment(middle, -margin);
		wlLogfield.setLayoutData(fdlLogfield);
		wLogfield=new Button(wLogComp, SWT.CHECK);
 		props.setLook(wLogfield);
		fdLogfield=new FormData();
		fdLogfield.left = new FormAttachment(middle, 0);
		fdLogfield.top  = new FormAttachment(wBatch, margin);
		fdLogfield.right= new FormAttachment(100, 0);
		wLogfield.setLayoutData(fdLogfield);

		fdLogComp=new FormData();
		fdLogComp.left  = new FormAttachment(0, 0);
		fdLogComp.top   = new FormAttachment(0, 0);
		fdLogComp.right = new FormAttachment(100, 0);
		fdLogComp.bottom= new FormAttachment(100, 0);
		wLogComp.setLayoutData(fdLogComp);
	
		wLogComp.layout();
		wLogTab.setControl(wLogComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF LOG TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF DATE TAB///
		///
		wDateTab=new CTabItem(wTabFolder, SWT.NONE);
		wDateTab.setText("Dates");

		FormLayout DateLayout = new FormLayout ();
		DateLayout.marginWidth  = Const.MARGIN;
		DateLayout.marginHeight = Const.MARGIN;
		
		Composite wDateComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wDateComp);
		wDateComp.setLayout(DateLayout);

		// Max date table connection...
		wlMaxdateconnection=new Label(wDateComp, SWT.RIGHT);
		wlMaxdateconnection.setText("Maxdate Connection: ");
 		props.setLook(wlMaxdateconnection);
		fdlMaxdateconnection=new FormData();
		fdlMaxdateconnection.left = new FormAttachment(0, 0);
		fdlMaxdateconnection.right= new FormAttachment(middle, -margin);
		fdlMaxdateconnection.top  = new FormAttachment(0, 0);
		wlMaxdateconnection.setLayoutData(fdlMaxdateconnection);
		wMaxdateconnection=new CCombo(wDateComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxdateconnection);
		wMaxdateconnection.addModifyListener(lsMod);
		fdMaxdateconnection=new FormData();
		fdMaxdateconnection.left = new FormAttachment(middle, 0);
		fdMaxdateconnection.top  = new FormAttachment(0, 0);
		fdMaxdateconnection.right= new FormAttachment(100, 0);
		wMaxdateconnection.setLayoutData(fdMaxdateconnection);

		// Maxdate table...:
		wlMaxdatetable=new Label(wDateComp, SWT.RIGHT);
		wlMaxdatetable.setText("Maxdate table:");
 		props.setLook(wlMaxdatetable);
		fdlMaxdatetable=new FormData();
		fdlMaxdatetable.left = new FormAttachment(0, 0);
		fdlMaxdatetable.right= new FormAttachment(middle, -margin);
		fdlMaxdatetable.top  = new FormAttachment(wMaxdateconnection, margin);
		wlMaxdatetable.setLayoutData(fdlMaxdatetable);
		wMaxdatetable=new Text(wDateComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxdatetable);
		wMaxdatetable.addModifyListener(lsMod);
		fdMaxdatetable=new FormData();
		fdMaxdatetable.left = new FormAttachment(middle, 0);
		fdMaxdatetable.top  = new FormAttachment(wMaxdateconnection, margin);
		fdMaxdatetable.right= new FormAttachment(100, 0);
		wMaxdatetable.setLayoutData(fdMaxdatetable);

		// Maxdate field...:
		wlMaxdatefield=new Label(wDateComp, SWT.RIGHT);
		wlMaxdatefield.setText("Maxdate field:");
 		props.setLook(wlMaxdatefield);
		fdlMaxdatefield=new FormData();
		fdlMaxdatefield.left = new FormAttachment(0, 0);
		fdlMaxdatefield.right= new FormAttachment(middle, -margin);
		fdlMaxdatefield.top  = new FormAttachment(wMaxdatetable, margin);
		wlMaxdatefield.setLayoutData(fdlMaxdatefield);
		wMaxdatefield=new Text(wDateComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxdatefield);
		wMaxdatefield.addModifyListener(lsMod);
		fdMaxdatefield=new FormData();
		fdMaxdatefield.left = new FormAttachment(middle, 0);
		fdMaxdatefield.top  = new FormAttachment(wMaxdatetable, margin);
		fdMaxdatefield.right= new FormAttachment(100, 0);
		wMaxdatefield.setLayoutData(fdMaxdatefield);

		// Maxdate offset...:
		wlMaxdateoffset=new Label(wDateComp, SWT.RIGHT);
		wlMaxdateoffset.setText("Maxdate offset (seconds):");
 		props.setLook(wlMaxdateoffset);
		fdlMaxdateoffset=new FormData();
		fdlMaxdateoffset.left = new FormAttachment(0, 0);
		fdlMaxdateoffset.right= new FormAttachment(middle, -margin);
		fdlMaxdateoffset.top  = new FormAttachment(wMaxdatefield, margin);
		wlMaxdateoffset.setLayoutData(fdlMaxdateoffset);
		wMaxdateoffset=new Text(wDateComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxdateoffset);
		wMaxdateoffset.addModifyListener(lsMod);
		fdMaxdateoffset=new FormData();
		fdMaxdateoffset.left = new FormAttachment(middle, 0);
		fdMaxdateoffset.top  = new FormAttachment(wMaxdatefield, margin);
		fdMaxdateoffset.right= new FormAttachment(100, 0);
		wMaxdateoffset.setLayoutData(fdMaxdateoffset);

		// Maxdate diff...:
		wlMaxdatediff=new Label(wDateComp, SWT.RIGHT);
		wlMaxdatediff.setText("Max. date difference (seconds):");
 		props.setLook(wlMaxdatediff);
		fdlMaxdatediff=new FormData();
		fdlMaxdatediff.left = new FormAttachment(0, 0);
		fdlMaxdatediff.right= new FormAttachment(middle, -margin);
		fdlMaxdatediff.top  = new FormAttachment(wMaxdateoffset, margin);
		wlMaxdatediff.setLayoutData(fdlMaxdatediff);
		wMaxdatediff=new Text(wDateComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxdatediff);
		wMaxdatediff.addModifyListener(lsMod);
		fdMaxdatediff=new FormData();
		fdMaxdatediff.left = new FormAttachment(middle, 0);
		fdMaxdatediff.top  = new FormAttachment(wMaxdateoffset, margin);
		fdMaxdatediff.right= new FormAttachment(100, 0);
		wMaxdatediff.setLayoutData(fdMaxdatediff);


		String conns[] = new String[transMeta.nrDatabases()]; 
		for (int i=0;i<transMeta.nrDatabases();i++)
		{
			DatabaseMeta ci = transMeta.getDatabase(i);
			wLogconnection.add(ci.getName());
			wMaxdateconnection.add(ci.getName());
			conns[i] = ci.getName();
		}
		
		fdDateComp=new FormData();
		fdDateComp.left  = new FormAttachment(0, 0);
		fdDateComp.top   = new FormAttachment(0, 0);
		fdDateComp.right = new FormAttachment(100, 0);
		fdDateComp.bottom= new FormAttachment(100, 0);
		wDateComp.setLayoutData(fdDateComp);
	
		wDateComp.layout();
		wDateTab.setControl(wDateComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF DATE TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF Dep TAB///
		///
		wDepTab=new CTabItem(wTabFolder, SWT.NONE);
		wDepTab.setText("Dependencies");

		FormLayout DepLayout = new FormLayout ();
		DepLayout.marginWidth  = Const.MARGIN;
		DepLayout.marginHeight = Const.MARGIN;
		
		Composite wDepComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wDepComp);
		wDepComp.setLayout(DepLayout);

		wlFields=new Label(wDepComp, SWT.RIGHT);
		wlFields.setText("Dependencies : ");
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(0, 0);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=3;
		final int FieldsRows=transMeta.nrDependencies();
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo("Connection", ColumnInfo.COLUMN_TYPE_CCOMBO, conns);
		colinf[1]=new ColumnInfo("Table",      ColumnInfo.COLUMN_TYPE_TEXT,   false);
		colinf[2]=new ColumnInfo("Field",      ColumnInfo.COLUMN_TYPE_TEXT,   false);
		
		wFields=new TableView(wDepComp, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );

		wGet=new Button(wDepComp, SWT.PUSH);
		wGet.setText(" &Get dependencies ");

		fdGet = new FormData();
		fdGet.bottom = new FormAttachment(100, 0);
		fdGet.left   = new FormAttachment(50, 0);
		wGet.setLayoutData(fdGet);
		
		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wGet, 0);
		wFields.setLayoutData(fdFields);

		fdDepComp=new FormData();
		fdDepComp.left  = new FormAttachment(0, 0);
		fdDepComp.top   = new FormAttachment(0, 0);
		fdDepComp.right = new FormAttachment(100, 0);
		fdDepComp.bottom= new FormAttachment(100, 0);
		wDepComp.setLayoutData(fdDepComp);
		
		wDepComp.layout();
		wDepTab.setControl(wDepComp);

		/////////////////////////////////////////////////////////////
		/// END OF DEP TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF PERFORMANCE TAB///
		///
		wPerfTab=new CTabItem(wTabFolder, SWT.NONE);
		wPerfTab.setText("Performance");
		
		Composite wPerfComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wPerfComp);

		FormLayout perfLayout = new FormLayout();
		perfLayout.marginWidth  = Const.MARGIN;
		perfLayout.marginHeight = Const.MARGIN;
		wPerfComp.setLayout(perfLayout);


		// Rows in Rowset:
		wlSizeRowset=new Label(wPerfComp, SWT.RIGHT);
		wlSizeRowset.setText("Nr of rows in rowset :");
 		props.setLook(wlSizeRowset);
		fdlSizeRowset=new FormData();
		fdlSizeRowset.left = new FormAttachment(0, 0);
		fdlSizeRowset.right= new FormAttachment(middle, -margin);
		fdlSizeRowset.top  = new FormAttachment(0, margin);
		wlSizeRowset.setLayoutData(fdlSizeRowset);
		wSizeRowset=new Text(wPerfComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSizeRowset);
		wSizeRowset.addModifyListener(lsMod);
		fdSizeRowset=new FormData();
		fdSizeRowset.left = new FormAttachment(middle, 0);
		fdSizeRowset.top  = new FormAttachment(0, margin);
		fdSizeRowset.right= new FormAttachment(100, 0);
		wSizeRowset.setLayoutData(fdSizeRowset);


		fdPerfComp=new FormData();
		fdPerfComp.left  = new FormAttachment(0, 0);
		fdPerfComp.top   = new FormAttachment(0, 0);
		fdPerfComp.right = new FormAttachment(100, 0);
		fdPerfComp.bottom= new FormAttachment(100, 0);
		wPerfComp.setLayoutData(fdPerfComp);
	
		wPerfComp.layout();
		wPerfTab.setControl(wPerfComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF PERF TAB
		/////////////////////////////////////////////////////////////

		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(0, 0);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wSQL=new Button(shell, SWT.PUSH);
		wSQL.setText(" &SQL ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");
		
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wSQL, wCancel }, Const.MARGIN, null);
		
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsSQL      = new Listener() { public void handleEvent(Event e) { sql();    } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wSQL.addListener   (SWT.Selection, lsSQL   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wTransname.addSelectionListener( lsDef );
		wMaxdatetable.addSelectionListener( lsDef );
		wMaxdatefield.addSelectionListener( lsDef );
		wMaxdateoffset.addSelectionListener( lsDef );
		wMaxdatediff.addSelectionListener( lsDef );
		wLogtable.addSelectionListener( lsDef );
		wSizeRowset.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);
		
		getData();

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return transMeta;
	}

	public void dispose()
	{
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		log.logDebug(toString(), "getting transformation info...");

		if (transMeta.getName()!=null)         wTransname.setText        ( transMeta.getName());
		if (transMeta.getModifiedUser()!=null)     wModUser.setText          ( transMeta.getModifiedUser() );
		if (transMeta.getModifiedDate()!=null && 
			transMeta.getModifiedDate().getString()!=null
			)     						   wModDate.setText          ( transMeta.getModifiedDate().getString() );
	
		if (transMeta.getReadStep()!=null)      wReadStep.setText         ( transMeta.getReadStep().getName() );
		if (transMeta.getWriteStep()!=null)     wWriteStep.setText        ( transMeta.getWriteStep().getName() );
		if (transMeta.getInputStep()!=null)     wInputStep.setText        ( transMeta.getInputStep().getName() );
		if (transMeta.getOutputStep()!=null)    wOutputStep.setText       ( transMeta.getOutputStep().getName() );
		if (transMeta.getUpdateStep()!=null)    wUpdateStep.setText       ( transMeta.getUpdateStep().getName() );
		if (transMeta.getLogConnection()!=null) wLogconnection.setText    ( transMeta.getLogConnection().getName());
		if (transMeta.getLogTable()!=null)      wLogtable.setText         ( transMeta.getLogTable());
		wBatch.setSelection(transMeta.isBatchIdUsed());
		wLogfield.setSelection(transMeta.isLogfieldUsed());
		
		if (transMeta.getMaxDateConnection()!=null) wMaxdateconnection.setText( transMeta.getMaxDateConnection().getName());
		if (transMeta.getMaxDateTable()!=null)      wMaxdatetable.setText     ( transMeta.getMaxDateTable());
		if (transMeta.getMaxDateField()!=null)      wMaxdatefield.setText     ( transMeta.getMaxDateField());
		wMaxdateoffset.setText(""+transMeta.getMaxDateOffset());
		wMaxdatediff.setText(""+transMeta.getMaxDateDifference());
		
		for (i=0;i<transMeta.nrDependencies();i++)
		{
			TableItem item = wFields.table.getItem(i);
			TransDependency td = transMeta.getDependency(i);
			
			DatabaseMeta conn = td.getDatabase();
			String table   = td.getTablename();
			String field   = td.getFieldname();
			if (conn !=null) item.setText(1, conn.getName() );
			if (table!=null) item.setText(2, table);
			if (field!=null) item.setText(3, field);
		}
		
		wSizeRowset.setText(""+transMeta.getSizeRowset());
		
		wFields.setRowNums();
		wFields.optWidth(true);
		
		// Directory:
		if (transMeta.getDirectory()!=null && transMeta.getDirectory().getPath()!=null) 
			wDirectory.setText(transMeta.getDirectory().getPath());
		
		wTransname.selectAll();
		wTransname.setFocus();
	}
	
	private void cancel()
	{
		props.setScreen(new WindowProperty(shell));
		transMeta.setChanged(changed);
		transMeta=null;
		dispose();
	}
	
	private void ok()
	{
		int i;
		boolean OK = true;
	
		transMeta.setReadStep(          transMeta.findStep( wReadStep.getText() )            );
		transMeta.setWriteStep(         transMeta.findStep( wWriteStep.getText() )           );
		transMeta.setInputStep(         transMeta.findStep( wInputStep.getText() )           );
		transMeta.setOutputStep(        transMeta.findStep( wOutputStep.getText() )          );
		transMeta.setUpdateStep(        transMeta.findStep( wUpdateStep.getText() )          );
		transMeta.setLogConnection(     transMeta.findDatabase(wLogconnection.getText())     );
		transMeta.setLogTable(          wLogtable.getText()                              );
		transMeta.setMaxDateConnection( transMeta.findDatabase(wMaxdateconnection.getText()) );
		transMeta.setMaxDateTable(      wMaxdatetable.getText()                          );
		transMeta.setMaxDateField(      wMaxdatefield.getText()                          );
		transMeta.setBatchIdUsed(       wBatch.getSelection()                            );
		transMeta.setLogfieldUsed(      wLogfield.getSelection()                         );
		transMeta.setName(              wTransname.getText()                             );
		
		try
		{
			transMeta.setMaxDateOffset( Double.parseDouble(wMaxdateoffset.getText()) ) ;
		}
		catch(Exception e)
		{
			MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			mb.setText("Not a number!");
			mb.setMessage("The offset number could not be parsed!");
			mb.open();
			wMaxdateoffset.setFocus();
			wMaxdateoffset.selectAll();
			OK=false;
		}

		try
		{
			transMeta.setMaxDateDifference( Double.parseDouble(wMaxdatediff.getText()) );
		}
		catch(Exception e)
		{
			MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			mb.setText("Not a number!");
			mb.setMessage("The date difference number could not be parsed!");
			mb.open();
			wMaxdatediff.setFocus();
			wMaxdatediff.selectAll();
			OK=false;
		}
		
		transMeta.removeAllDependencies();
		
		for (i=0;i<wFields.nrNonEmpty();i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			
			DatabaseMeta db  = transMeta.findDatabase(item.getText(1));
			String tablename = item.getText(2);
			String fieldname = item.getText(3);
			TransDependency td = new TransDependency(db, tablename, fieldname);
			transMeta.addDependency(td);
		}
		
		transMeta.setSizeRowset( Const.toInt( wSizeRowset.getText(), Const.ROWS_IN_ROWSET) );
		
		if (newDirectory!=null)
		{
		    RepositoryDirectory dirFrom = transMeta.getDirectory();
		    long idDirFrom = dirFrom==null?-1L:dirFrom.getID();
		    
			try
			{
				rep.moveTransformation(transMeta.getName(), idDirFrom, newDirectory.getID() );
		 		log.logDetailed(getClass().getName(), "Moved directory to ["+newDirectory.getPath()+"]");
				transMeta.setDirectory( newDirectory );
			}
			catch(KettleException ke)
			{
		 		transMeta.setDirectory( dirFrom );
		 		OK=false;
		 		new ErrorDialog(shell, props, "Error moving transformation", "There was an error moving the transformation to another directory!", ke);
			}
		}

		
		if (OK) dispose();
	}
	
	// Get the dependencies
	private void get()
	{
		Table table = wFields.table;
		for (int i=0;i<transMeta.nrSteps();i++)
		{
			StepMeta stepMeta = transMeta.getStep(i);
			String con=null;
			String tab=null;
			TableItem item=null;
			StepMetaInterface sii = stepMeta.getStepMetaInterface();
			if (sii instanceof TableInputMeta)
			{
				TableInputMeta tii = (TableInputMeta)stepMeta.getStepMetaInterface();
				con  = tii.getDatabaseMeta().getName();
				tab  = getTableFromSQL(tii.getSQL());
				if (tab==null) tab=stepMeta.getName();
			}
			if (sii instanceof DatabaseLookupMeta)
			{
				DatabaseLookupMeta dvli = (DatabaseLookupMeta)stepMeta.getStepMetaInterface();
				con  = dvli.getDatabaseMeta().getName();
				tab  = dvli.getTablename();
				if (tab==null) tab=stepMeta.getName();
				break;	
			}

			if (tab!=null || con!=null)
			{
				item = new TableItem(table, SWT.NONE);
				if (con!=null) item.setText(1, con);
				if (tab!=null) item.setText(2, tab);
			}
		}
		wFields.setRowNums();
	}
	
	private String getTableFromSQL(String sql)
	{
		if (sql==null) return null;
		
		int idxfrom = sql.toUpperCase().indexOf("FROM");
		int idxto   = sql.toUpperCase().indexOf("WHERE");
		if (idxfrom==-1) return null;
		if (idxto==-1) idxto=sql.length();
		return sql.substring(idxfrom+5, idxto);
	}

	// Generate code for create table...
	// Conversions done by Database
	private void sql()
	{
		DatabaseMeta ci = transMeta.findDatabase(wLogconnection.getText());
		if (ci!=null)
		{
			Row r = Database.getTransLogrecordFields(wBatch.getSelection(), wLogfield.getSelection());
			if (r!=null && r.size()>0)
			{
				String tablename = wLogtable.getText();
				if (tablename!=null && tablename.length()>0)
				{
					Database db = new Database(ci);
					try
					{
						db.connect();

						String createTable = db.getDDL(tablename, r);
						if (createTable!=null && createTable.length()>0)
						{
							log.logBasic(toString(), createTable);
		
							SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, ci, transMeta.getDbCache(), createTable);
							sqledit.open();
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
							mb.setText("OK!");
							mb.setMessage("No sql needs to be executed.  The log table looks great!");
							mb.open(); 
						}
					}
					catch(KettleException e)
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setText("ERROR!");
						mb.setMessage("An error occurred: "+Const.CR+e.getMessage());
						mb.open(); 
					}
					finally
					{
						db.disconnect();
					}
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setText("ERROR");
					mb.setMessage("Please enter a logtable-name!");
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setText("ERROR");
				mb.setMessage("I couldn't find any fields to create the logtable!");
				mb.open(); 
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText("ERROR");
			mb.setMessage("Please select a valid logtable connection!");
			mb.open(); 
		}
	}



	public String toString()
	{
		return this.getClass().getName();
	}
}
