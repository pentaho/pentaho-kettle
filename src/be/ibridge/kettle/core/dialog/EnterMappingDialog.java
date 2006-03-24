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

 

package be.ibridge.kettle.core.dialog;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.SourceToTargetMapping;
import be.ibridge.kettle.core.WindowProperty;

/**
 * Shows a user 2 lists of strings and allows the linkage of values between values in the 2 lists
 * 
 * @author Matt
 * @since 23-03-2006
 */
public class EnterMappingDialog extends Dialog
{
	private Label        wlSource;
	private List         wSource;
    private FormData     fdlSource, fdSource;

    private Label        wlTarget;
    private List         wTarget;
    private FormData     fdlTarget, fdTarget;
    
    private Label        wlResult;
    private List         wResult;
    private FormData     fdlResult, fdResult;
    
    private Button       wAdd;
    private FormData     fdAdd; 

    private Button       wDelete;
    private FormData     fdDelete;

	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	
	private String sourceList[];
	private String targetList[];
    
    private Props props;
	
    private ArrayList mappings;
	
    /**
     *   Create a new dialog allowing the user to enter a mapping
     *   @param parent the parent shell
     *   @param source the source values
     *   @param target the target values
     */
	public EnterMappingDialog(Shell parent, String source[], String target[])
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		this.sourceList = source;
        this.targetList = target;
        
        mappings = new ArrayList();
	}
    
	public ArrayList open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Enter the mapping");
		
		int margin = Const.MARGIN;

		// Source table
		wlSource=new Label(shell, SWT.NONE);
		wlSource.setText("Source fields: ");
 		props.setLook(wlSource);
		fdlSource=new FormData();
		fdlSource.left = new FormAttachment(0, 0);
		fdlSource.top  = new FormAttachment(0, margin);
		wlSource.setLayoutData(fdlSource);
		wSource=new List(shell, SWT.SINGLE | SWT.RIGHT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		for (int i=0;i<sourceList.length;i++) wSource.add(sourceList[i]);
 		props.setLook(wSource);
		fdSource=new FormData();
		fdSource.left = new FormAttachment(0, 0);
		fdSource.right= new FormAttachment(25, 0);
		fdSource.top  = new FormAttachment(wlSource, margin);
		fdSource.bottom= new FormAttachment(100, -30);
		wSource.setLayoutData(fdSource);

        // Target table
        wlTarget=new Label(shell, SWT.NONE);
        wlTarget.setText("Target fields: ");
        props.setLook(wlTarget);
        fdlTarget=new FormData();
        fdlTarget.left = new FormAttachment(wSource, margin*2);
        fdlTarget.top  = new FormAttachment(0, margin);
        wlTarget.setLayoutData(fdlTarget);
        wTarget=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        for (int i=0;i<targetList.length;i++) wTarget.add(targetList[i]);
        props.setLook(wTarget);
        fdTarget=new FormData();
        fdTarget.left = new FormAttachment(wSource, margin*2);
        fdTarget.right= new FormAttachment(50, 0);
        fdTarget.top  = new FormAttachment(wlTarget, margin);
        fdTarget.bottom= new FormAttachment(100, -30);
        wTarget.setLayoutData(fdTarget);

        // Add a couple of buttons:
        wAdd=new Button(shell, SWT.PUSH);
        fdAdd=new FormData();
        wAdd.setText("  &Add  ");
        fdAdd.left       = new FormAttachment(wTarget, margin*2);
        fdAdd.top        = new FormAttachment(wTarget, 0, SWT.CENTER);
        wAdd.setLayoutData(fdAdd);
        Listener lsAdd       = new Listener() { public void handleEvent(Event e) { add();     } };
        wAdd.addListener    (SWT.Selection, lsAdd     );

        // Delete a couple of buttons:
        wDelete=new Button(shell, SWT.PUSH);
        fdDelete=new FormData();
        wDelete.setText("  &Delete  ");
        fdDelete.left       = new FormAttachment(wTarget, margin*2);
        fdDelete.top        = new FormAttachment(wAdd,    margin*2);
        wDelete.setLayoutData(fdDelete);
        Listener lsDelete       = new Listener() { public void handleEvent(Event e) { delete();     } };
        wDelete.addListener    (SWT.Selection, lsDelete     );

        
        // Result table
        wlResult=new Label(shell, SWT.NONE);
        wlResult.setText("Result mappings: ");
        props.setLook(wlResult);
        fdlResult=new FormData();
        fdlResult.left = new FormAttachment(wDelete, margin*2);
        fdlResult.top  = new FormAttachment(0, margin);
        wlResult.setLayoutData(fdlResult);
        wResult=new List(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        for (int i=0;i<targetList.length;i++) wResult.add(targetList[i]);
        props.setLook(wResult);
        fdResult=new FormData();
        fdResult.left = new FormAttachment(wDelete, margin*2);
        fdResult.right= new FormAttachment(100, 0);
        fdResult.top  = new FormAttachment(wlResult, margin);
        fdResult.bottom= new FormAttachment(100, -30);
        wResult.setLayoutData(fdResult);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		fdOK=new FormData();
		wOK.setText("  &OK  ");
		fdOK.left       = new FormAttachment(33, 0);
		fdOK.bottom     = new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		wOK.addListener    (SWT.Selection, lsOK     );
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		fdCancel=new FormData();
		fdCancel.left   = new FormAttachment(66, 0);
		fdCancel.bottom = new FormAttachment(100, 0);
		wCancel.setLayoutData(fdCancel);
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		wCancel.addListener(SWT.Selection, lsCancel );

        wSource.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    findTarget();
                }
                
                public void widgetDefaultSelected(SelectionEvent e)
                {
                    add();
                }
            }
        );
        
        wTarget.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    findSource();
                }
                
                public void widgetDefaultSelected(SelectionEvent e)
                {
                    add();
                }
            }
        );

		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return mappings;
	}

    private void findTarget()
    {
        // OK, user selects an entry in the list on the left.
        // Find a comparable entry in the target list...
        boolean found = false;
        
        int sourceIndex = wSource.getSelectionIndex();
        // Skip eventhing after the bracket...
        String sourceStr = wSource.getItem(sourceIndex).toUpperCase();

        int indexOfBracket = sourceStr.indexOf(" (");
        String sourceString = sourceStr;
        if (indexOfBracket>=0)
        {
            sourceString = sourceStr.substring(0, indexOfBracket);
        }

        int length = sourceString.length();
        boolean first = true;
        
        while (!found && ( length >=2 || first ))
        {
            first=false;
            
            for (int i=0;i<wTarget.getItemCount() && !found;i++)
            {
                if (wTarget.getItem(i).toUpperCase().indexOf(sourceString.substring(0,length))>=0)
                {
                    wTarget.setSelection(i);
                    found = true;
                }
            }
            length--;
        }
    }
    
    private void findSource()
    {
        // OK, user selects an entry in the list on the right.
        // Find a comparable entry in the source list...
        boolean found = false;
        
        int targetIndex = wTarget.getSelectionIndex();
        // Skip eventhing after the bracket...
        String targetString = wTarget.getItem(targetIndex).toUpperCase();

        int length = targetString.length();
        boolean first = true;
        
        while (!found && ( length >=2 || first ))
        {
            first=false;
            
            for (int i=0;i<wSource.getItemCount() && !found;i++)
            {
                if (wSource.getItem(i).toUpperCase().indexOf(targetString.substring(0,length))>=0)
                {
                    wSource.setSelection(i);
                    found = true;
                }
            }
            length--;
        }
    }


    private void add()
    {
        int srcIndex = wSource.getSelectionIndex();
        int tgtIndex = wTarget.getSelectionIndex();
        
        if (srcIndex>=0 && tgtIndex>=0)
        {
            // New mapping: add it to the list...
            SourceToTargetMapping mapping = new SourceToTargetMapping(srcIndex, tgtIndex);
            mappings.add(mapping);

            refreshMappings();
        }
    }

	private void refreshMappings()
    {
        wResult.removeAll();
        for (int i=0;i<mappings.size();i++)
        {
            SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
            String mappingString = sourceList[mapping.getSourcePosition()]+" --> "+targetList[mapping.getTargetPosition()];
            wResult.add(mappingString);
        }
    }

    private void delete()
    {
        int resultIndex[] = wResult.getSelectionIndices();
        for (int i=resultIndex.length-1;i>=0;i--)
        {
            mappings.remove(resultIndex[i]);
        }
        refreshMappings();
    }

    public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
        refreshMappings();
	}
	
	private void cancel()
	{
		mappings=null;
		dispose();
	}
	
	private void ok()
	{
		dispose();
	}
}
