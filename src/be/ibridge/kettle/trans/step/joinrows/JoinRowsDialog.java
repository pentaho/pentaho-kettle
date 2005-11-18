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

package be.ibridge.kettle.trans.step.joinrows;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Condition;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.widget.ConditionEditor;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;


public class JoinRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlSortDir;
	private Button       wbSortDir;
    private Button       wbcSortDir;
	private Text         wSortDir;
	private FormData     fdlSortDir, fdbSortDir, fdbcSortDir, fdSortDir;

	private Label        wlPrefix;
	private Text         wPrefix;
	private FormData     fdlPrefix, fdPrefix;

	private Label        wlCache;
	private Text         wCache;
	private FormData     fdlCache, fdCache;

	private Label        wlMainStep;
	private CCombo       wMainStep;
	private FormData     fdlMainStep, fdMainStep;
	
	private Label           wlCondition;
	private ConditionEditor wCondition;
	private FormData        fdlCondition, fdCondition;

	private JoinRowsMeta input;
	private Condition      condition;
	
	private Condition      backupCondition;
	
	public JoinRowsDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(JoinRowsMeta)in;
		condition = input.getCondition();
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();
		backupCondition = (Condition)condition.clone(); 

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Join rows");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText("Step name ");
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

		// Connection line
		wlSortDir=new Label(shell, SWT.RIGHT);
		wlSortDir.setText("Temp directory ");
 		props.setLook(wlSortDir);
		fdlSortDir=new FormData();
		fdlSortDir.left = new FormAttachment(0, 0);
		fdlSortDir.right= new FormAttachment(middle, -margin);
		fdlSortDir.top  = new FormAttachment(wStepname, margin);
		wlSortDir.setLayoutData(fdlSortDir);

		wbSortDir=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSortDir);
		wbSortDir.setText("&Browse...");
		fdbSortDir=new FormData();
		fdbSortDir.right= new FormAttachment(100, 0);
		fdbSortDir.top  = new FormAttachment(wStepname, margin);
        wbSortDir.setLayoutData(fdbSortDir);

        wbcSortDir=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(        wbcSortDir);
        wbcSortDir.setText("&Variable...");
        fdbcSortDir=new FormData();
        fdbcSortDir.right= new FormAttachment(wbSortDir, -margin);
        fdbcSortDir.top  = new FormAttachment(wStepname, margin);
        wbcSortDir.setLayoutData(fdbcSortDir);

    	wSortDir=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSortDir.setText("temp");
 		props.setLook(wSortDir);
		wSortDir.addModifyListener(lsMod);
		fdSortDir=new FormData();
		fdSortDir.left = new FormAttachment(middle, 0);
		fdSortDir.top  = new FormAttachment(wStepname, margin);
		fdSortDir.right= new FormAttachment(wbcSortDir, -margin);
		wSortDir.setLayoutData(fdSortDir);

        wbSortDir.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    DirectoryDialog dd = new DirectoryDialog(shell, SWT.NONE);
                    dd.setFilterPath(wSortDir.getText());
                    String dir = dd.open();
                    if (dir!=null)
                    {
                        wSortDir.setText(dir);
                    }
                }
            }
        );

        // Whenever something changes, set the tooltip to the expanded version:
        wSortDir.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent e)
                {
                    wSortDir.setToolTipText(Const.replEnv( wSortDir.getText() ) );
                }
            }
        );

        // Listen to the Variable... button
        wbcSortDir.addSelectionListener
        (
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    Properties sp = System.getProperties();
                    Enumeration keys = sp.keys();
                    int size = sp.values().size();
                    String key[] = new String[size];
                    String val[] = new String[size];
                    String str[] = new String[size];
                    int i=0;
                    while (keys.hasMoreElements())
                    {
                        key[i] = (String)keys.nextElement();
                        val[i] = sp.getProperty(key[i]);
                        str[i] = key[i]+"  ["+val[i]+"]";
                        i++;
                    }
                    
                    EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, str, "Select an Environment Variable", "Select an Environment Variable");
                    if (esd.open()!=null)
                    {
                        int nr = esd.getSelectionNr();
                        wSortDir.insert("%%"+key[nr]+"%%");
                    }
                }
                
            }
        );
        
        
		// Table line...
		wlPrefix=new Label(shell, SWT.RIGHT);
		wlPrefix.setText("TMP-file prefix ");
 		props.setLook(wlPrefix);
		fdlPrefix=new FormData();
		fdlPrefix.left = new FormAttachment(0, 0);
		fdlPrefix.right= new FormAttachment(middle, -margin);
		fdlPrefix.top  = new FormAttachment(wbSortDir, margin*2);
		wlPrefix.setLayoutData(fdlPrefix);
		wPrefix=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wPrefix);
		wPrefix.addModifyListener(lsMod);
		fdPrefix=new FormData();
		fdPrefix.left  = new FormAttachment(middle, 0);
		fdPrefix.top   = new FormAttachment(wbSortDir, margin*2);
		fdPrefix.right = new FormAttachment(100, 0);
		wPrefix.setLayoutData(fdPrefix);
		wPrefix.setText("srt");

		// Cache size...
		wlCache=new Label(shell, SWT.RIGHT);
		wlCache.setText("Max. cache size (in rows)");
 		props.setLook(wlCache);
		fdlCache=new FormData();
		fdlCache.left = new FormAttachment(0, 0);
		fdlCache.right= new FormAttachment(middle, -margin);
		fdlCache.top  = new FormAttachment(wPrefix, margin*2);
		wlCache.setLayoutData(fdlCache);
		wCache=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wCache);
		wCache.addModifyListener(lsMod);
		fdCache=new FormData();
		fdCache.left  = new FormAttachment(middle, 0);
		fdCache.top   = new FormAttachment(wPrefix, margin*2);
		fdCache.right = new FormAttachment(100, 0);
		wCache.setLayoutData(fdCache);

		// Read date from...
		wlMainStep=new Label(shell, SWT.RIGHT);
		wlMainStep.setText("Main step to read from");
 		props.setLook(wlMainStep);
		fdlMainStep=new FormData();
		fdlMainStep.left = new FormAttachment(0, 0);
		fdlMainStep.right= new FormAttachment(middle, -margin);
		fdlMainStep.top  = new FormAttachment(wCache, margin);
		wlMainStep.setLayoutData(fdlMainStep);
		wMainStep=new CCombo(shell, SWT.BORDER );
 		props.setLook(wMainStep);

		for (int i=0;i<transMeta.findNrPrevSteps(stepname);i++)
		{
			StepMeta stepMeta = transMeta.findPrevStep(stepname, i);
			wMainStep.add(stepMeta.getName());
		}
		
		wMainStep.addModifyListener(lsMod);
		fdMainStep=new FormData();
		fdMainStep.left = new FormAttachment(middle, 0);
		fdMainStep.top  = new FormAttachment(wCache, margin);
		fdMainStep.right= new FormAttachment(100, 0);
		wMainStep.setLayoutData(fdMainStep);


		// Condition widget...
		wlCondition=new Label(shell, SWT.NONE);
		wlCondition.setText("The condition: ");
 		props.setLook(wlCondition);
		fdlCondition=new FormData();
		fdlCondition.left  = new FormAttachment(0, 0);
		fdlCondition.top   = new FormAttachment(wMainStep, margin);
		wlCondition.setLayoutData(fdlCondition);
		
		Row inputfields = null;
		try
		{
			inputfields = transMeta.getPrevStepFields(stepname);
		}
		catch(KettleException ke)
		{
			inputfields = new Row();
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
		}

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		wCondition = new ConditionEditor(shell, SWT.BORDER, condition, props, inputfields);
		
		fdCondition=new FormData();
		fdCondition.left  = new FormAttachment(0, 0);
		fdCondition.top   = new FormAttachment(wlCondition, margin);
		fdCondition.right = new FormAttachment(100, 0);
		fdCondition.bottom= new FormAttachment(wOK, -2*margin);
		wCondition.setLayoutData(fdCondition);
		wCondition.addModifyListener(lsMod);



		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wSortDir.addSelectionListener( lsDef );
		wPrefix.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
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
		if (input.getPrefix() != null) wPrefix.setText(input.getPrefix());
		if (input.getDirectory() != null) wSortDir.setText(input.getDirectory());
		wCache.setText(""+input.getCacheSize());
		if (input.getLookupStepname() != null) wMainStep.setText(input.getLookupStepname());
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		input.setCondition(backupCondition);
		dispose();
	}
	
	private void ok()
	{
		if (wCondition.getLevel()>0) 
		{
			wCondition.goUp();
		}
		else
		{
		    stepname = wStepname.getText(); // return value
		    
			input.setPrefix( wPrefix.getText() );
			input.setDirectory( wSortDir.getText() );
			input.setCacheSize( Const.toInt(wCache.getText(), -1) );
			input.setMainStep( transMeta.findStep( wMainStep.getText() ) );
			
			dispose();
		}
	}
	
}
