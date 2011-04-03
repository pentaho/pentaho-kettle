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

package org.pentaho.di.ui.trans.steps.javafilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.javafilter.JavaFilterMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class JavaFilterDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = JavaFilterMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Text         wStepname;
	private CCombo       wTrueTo;
	private CCombo       wFalseTo;
    private StyledTextComp      wCondition;
    
	private JavaFilterMeta input;
    
    private Map<String, Integer> inputFields;
    private ColumnInfo[] colinf;
    
	private Group wSettingsGroup;
	private FormData fdSettingsGroup;

	public JavaFilterDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		
		// The order here is important... currentMeta is looked at for changes
		input=(JavaFilterMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
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
		shell.setText(BaseMessages.getString(PKG, "JavaFilterDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
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
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);
		
		
		// /////////////////////////////////
		// START OF Settings GROUP
		// /////////////////////////////////

		wSettingsGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wSettingsGroup);
		wSettingsGroup.setText(BaseMessages.getString(PKG, "JavaFIlterDialog.Settings.Label"));
		FormLayout settingsLayout = new FormLayout();
		settingsLayout.marginWidth = 10;
		settingsLayout.marginHeight = 10;
		wSettingsGroup.setLayout(settingsLayout);
		
		// Send 'True' data to...
		Label wlTrueTo = new Label(wSettingsGroup, SWT.RIGHT);
		wlTrueTo.setText(BaseMessages.getString(PKG, "JavaFilterDialog.SendTrueTo.Label")); //$NON-NLS-1$
 		props.setLook(wlTrueTo);
		FormData fdlTrueTo = new FormData();
		fdlTrueTo.left = new FormAttachment(0, 0);
		fdlTrueTo.right= new FormAttachment(middle, -margin);
		fdlTrueTo.top  = new FormAttachment(wStepname, margin);
		wlTrueTo.setLayoutData(fdlTrueTo);
		wTrueTo=new CCombo(wSettingsGroup, SWT.BORDER );
 		props.setLook(wTrueTo);

		StepMeta stepinfo = transMeta.findStep(stepname);
		if (stepinfo!=null)
		{
			List<StepMeta> nextSteps = transMeta.findNextSteps(stepinfo);
			for (int i=0;i<nextSteps.size();i++)
			{
				StepMeta stepMeta = nextSteps.get(i);
				wTrueTo.add(stepMeta.getName());
			}
		}
		
		wTrueTo.addModifyListener(lsMod);
		FormData fdTrueTo = new FormData();
		fdTrueTo.left = new FormAttachment(middle, 0);
		fdTrueTo.top  = new FormAttachment(wStepname, margin);
		fdTrueTo.right= new FormAttachment(100, 0);
		wTrueTo.setLayoutData(fdTrueTo);

		// Send 'False' data to...
		Label wlFalseTo = new Label(wSettingsGroup, SWT.RIGHT);
		wlFalseTo.setText(BaseMessages.getString(PKG, "JavaFilterDialog.SendFalseTo.Label")); //$NON-NLS-1$
 		props.setLook(wlFalseTo);
		FormData fdlFalseTo = new FormData();
		fdlFalseTo.left = new FormAttachment(0, 0);
		fdlFalseTo.right= new FormAttachment(middle, -margin);
		fdlFalseTo.top  = new FormAttachment(wTrueTo, margin);
		wlFalseTo.setLayoutData(fdlFalseTo);
		wFalseTo=new CCombo(wSettingsGroup, SWT.BORDER );
 		props.setLook(wFalseTo);

		stepinfo = transMeta.findStep(stepname);
		if (stepinfo!=null)
		{
			List<StepMeta> nextSteps = transMeta.findNextSteps(stepinfo);
			for (int i=0;i<nextSteps.size();i++)
			{
				StepMeta stepMeta = nextSteps.get(i);
				wFalseTo.add(stepMeta.getName());
			}
		}
		
		wFalseTo.addModifyListener(lsMod);
		FormData fdFalseFrom = new FormData();
		fdFalseFrom.left = new FormAttachment(middle, 0);
		fdFalseFrom.top  = new FormAttachment(wTrueTo, margin);
		fdFalseFrom.right= new FormAttachment(100, 0);
		wFalseTo.setLayoutData(fdFalseFrom);
		
		// bufferSize
		//
		Label wlCondition = new Label(wSettingsGroup, SWT.RIGHT);
		wlCondition.setText(BaseMessages.getString(PKG, "JavaFIlterDialog.Condition.Label")); //$NON-NLS-1$
 		props.setLook(wlCondition);
		FormData fdlCondition = new FormData();
		fdlCondition.top  = new FormAttachment(wFalseTo, margin);
		fdlCondition.left = new FormAttachment(0, 0);
		fdlCondition.right= new FormAttachment(middle, -margin);
		wlCondition.setLayoutData(fdlCondition);
		wCondition=new StyledTextComp(transMeta, wSettingsGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
		props.setLook(wCondition);
		wCondition.addModifyListener(lsMod);
		FormData fdCondition = new FormData();
		fdCondition.top  = new FormAttachment(wFalseTo, margin);
		fdCondition.left = new FormAttachment(middle, 0);
		fdCondition.right= new FormAttachment(100, 0);
		fdCondition.bottom = new FormAttachment(100, -margin);
		wCondition.setLayoutData(fdCondition);
        
		fdSettingsGroup = new FormData();
		fdSettingsGroup.left = new FormAttachment(0, margin);
		fdSettingsGroup.top = new FormAttachment(wStepname, margin);
		fdSettingsGroup.right = new FormAttachment(100, -margin);
		fdSettingsGroup.bottom = new FormAttachment(wOK, -margin);
		wSettingsGroup.setLayoutData(fdSettingsGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Settings GROUP
		// ///////////////////////////////////////////////////////////
		

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
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        shell.getDisplay().syncExec(new Runnable()
            {
                public void run()
                {
                    // Add the newly create fields.
                    //
                	/*
                    int nrNonEmptyFields = wFields.nrNonEmpty();
                    for (int i=0;i<nrNonEmptyFields;i++)
                    {
                        TableItem item = wFields.getNonEmpty(i);
                        fields.put(item.getText(1), new Integer(1000000+i));  // The number is just to debug the origin of the fieldname
                    }
                    */
                    
                    Set<String> keySet = fields.keySet();
                    List<String> entries = new ArrayList<String>(keySet);
                    
                    String fieldNames[] = entries.toArray(new String[entries.size()]);

                    Const.sortStrings(fieldNames);
                    
                    colinf[5].setComboValues(fieldNames);
                }
            }
        );
        
    }

    /**
	 * Copy information from the meta-data currentMeta to the dialog fields.
	 */ 
	public void getData()
	{
    	List<StreamInterface> targetStreams = input.getStepIOMeta().getTargetStreams();

		wTrueTo.setText(Const.NVL(targetStreams.get(0).getStepname(), ""));
		wFalseTo.setText(Const.NVL(targetStreams.get(1).getStepname(), ""));
		wCondition.setText(Const.NVL(input.getCondition(), ""));

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

		stepname = wStepname.getText(); // return value

		String trueStepname = Const.NVL(wTrueTo.getText(), null);
        String falseStepname = Const.NVL(wFalseTo.getText(), null);

        List<StreamInterface> targetStreams = input.getStepIOMeta().getTargetStreams();

        targetStreams.get(0).setStepMeta(transMeta.findStep( trueStepname ) );
        targetStreams.get(1).setStepMeta(transMeta.findStep( falseStepname ) );
        
        input.setCondition(wCondition.getText());
		
		dispose();
	}
}
