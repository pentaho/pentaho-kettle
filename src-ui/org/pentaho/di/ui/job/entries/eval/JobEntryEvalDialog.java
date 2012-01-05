/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.job.entries.eval;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.eval.JobEntryEval;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit a JobEntryEval object.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryEvalDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryEval.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlScript;

    private StyledTextComp wScript;

    private FormData fdlScript, fdScript;

    private Label wlPosition;

    private FormData fdlPosition;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryEval jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;

    public JobEntryEvalDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryEval) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobEval.Name.Default"));
    }

    public JobEntryInterface open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

        ModifyListener lsMod = new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                jobEntry.setChanged();
            }
        };
        changed = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "JobEval.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        // at the bottom
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

        // Filename line
        wlName = new Label(shell, SWT.NONE);
        wlName.setText(BaseMessages.getString(PKG, "JobEval.Jobname.Label"));
        props.setLook(wlName);
        fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
        fdlName.top = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.left = new FormAttachment(middle, 0);
        fdName.top = new FormAttachment(0, margin);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

        wlPosition = new Label(shell, SWT.NONE);
        wlPosition.setText(BaseMessages.getString(PKG, "JobEval.LineNr.Label", "0"));
        props.setLook(wlPosition);
        fdlPosition = new FormData();
        fdlPosition.left = new FormAttachment(0, 0);
        fdlPosition.bottom = new FormAttachment(wOK, -margin);
        wlPosition.setLayoutData(fdlPosition);

        // Script line
        wlScript = new Label(shell, SWT.NONE);
        wlScript.setText(BaseMessages.getString(PKG, "JobEval.Script.Label"));
        props.setLook(wlScript);
        fdlScript = new FormData();
        fdlScript.left = new FormAttachment(0, 0);
        fdlScript.top = new FormAttachment(wName, margin);
        wlScript.setLayoutData(fdlScript);
        wScript=new StyledTextComp(jobEntry, shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
        wScript.setText(BaseMessages.getString(PKG, "JobEval.Script.Default"));
        props.setLook(wScript, Props.WIDGET_STYLE_FIXED);
        wScript.addModifyListener(lsMod);
        fdScript = new FormData();
        fdScript.left = new FormAttachment(0, 0);
        fdScript.top = new FormAttachment(wlScript, margin);
        fdScript.right = new FormAttachment(100, -10);
        fdScript.bottom = new FormAttachment(wlPosition, -margin);
        wScript.setLayoutData(fdScript);
        wScript.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent arg0)
            {
                setPosition();
            }
	
	        }
	    );
		
	        wScript.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) { setPosition(); }
			public void keyReleased(KeyEvent e) { setPosition(); }
			} 
		);
	        wScript.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent e) { setPosition(); }
			public void focusLost(FocusEvent e) { setPosition(); }
			}
		);
	        wScript.addMouseListener(new MouseAdapter(){
			public void mouseDoubleClick(MouseEvent e) { setPosition(); }
			public void mouseDown(MouseEvent e) { setPosition(); }
			public void mouseUp(MouseEvent e) { setPosition(); }
			}
		);
        wScript.addModifyListener(lsMod);
        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };
        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });



        getData();

        BaseStepDialog.setSize(shell, 250, 250, false);

        shell.open();
        props.setDialogSize(shell, "JobEvalDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
	public void setPosition(){
		
		String scr = wScript.getText();
		int linenr = wScript.getLineAtOffset(wScript.getCaretOffset())+1;
		int posnr  = wScript.getCaretOffset();
				
		// Go back from position to last CR: how many positions?
		int colnr=0;
		while (posnr>0 && scr.charAt(posnr-1)!='\n' && scr.charAt(posnr-1)!='\r')
		{
			posnr--;
			colnr++;
		}
		wlPosition.setText(BaseMessages.getString(PKG, "JobEval.Position.Label",""+linenr,""+colnr));

	}
    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData()
    {
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        wName.selectAll();
        if (jobEntry.getScript() != null)
            wScript.setText(jobEntry.getScript());
    }

    private void cancel()
    {
        jobEntry.setChanged(changed);
        jobEntry = null;
        dispose();
    }

    private void ok()
    {
 	   if(Const.isEmpty(wName.getText())) 
       {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title"));
			mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
       }
        jobEntry.setName(wName.getText());
        jobEntry.setScript(wScript.getText());
        dispose();
    }
}
