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

package org.pentaho.di.ui.spoon.wizards;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;



/**
 * This wizard page let's you select the tables that need to be ripped.
 * 
 * @author Matt
 * @since 17-apr-04
 *
 */
public class RipDatabaseWizardPage2 extends WizardPage
{
	private PropsUI props;
	 
	private String    input[];
	
	private Hashtable<Integer,String> selection;
	
	private Shell     shell;
	private List      wListSource, wListDest;
	private Label     wlListSource, wlListDest;
	
	private Button    wAddOne, wAddAll, wRemoveAll, wRemoveOne;
    
	public RipDatabaseWizardPage2(String arg)
	{
		super(arg);
		this.props=PropsUI.getInstance();

		selection = new Hashtable<Integer,String>();

		setTitle("Select the tables");
		setDescription("Select the tables to rip from the source database");
	}
	
	public void createControl(Composite parent)
	{
		shell   = parent.getShell();

		int margin  = Const.MARGIN;		
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
        props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		// Put it all on the composite!
		////////////////////////////////////////////////////
		// Top & Bottom regions.
		////////////////////////////////////////////////////
		Composite top    = new Composite(composite, SWT.NONE);
		FormLayout topLayout = new FormLayout();
		topLayout.marginHeight = margin;
		topLayout.marginWidth  = margin;
		top.setLayout(topLayout);
		
		FormData fdTop  = new FormData(); 
		fdTop.left   = new FormAttachment(0, 0);
		fdTop.top    = new FormAttachment(0, 0);
		fdTop.right  = new FormAttachment(100, 0);
		fdTop.bottom = new FormAttachment(100, -50);
		top.setLayoutData(fdTop);
        props.setLook(top);
		
		Composite bottom = new Composite(composite, SWT.NONE);
		bottom.setLayout(new FormLayout());
		FormData fdBottom = new FormData(); 
		fdBottom.left   = new FormAttachment(0, 0); 
		fdBottom.top    = new FormAttachment(top, 0);
		fdBottom.right  = new FormAttachment(100, 0);
		fdBottom.bottom = new FormAttachment(100, 0);
		bottom.setLayoutData(fdBottom);
		props.setLook(bottom);

		
		////////////////////////////////////////////////////
		// Sashform
		////////////////////////////////////////////////////
		
		SashForm sashform = new SashForm(top, SWT.HORIZONTAL); 
		sashform.setLayout(new FormLayout());
		FormData fdSashform = new FormData(); 
		fdSashform.left   = new FormAttachment(0, 0); 
		fdSashform.top    = new FormAttachment(0, 0);
		fdSashform.right  = new FormAttachment(100, 0);
		fdSashform.bottom = new FormAttachment(100, 0);
		sashform.setLayoutData(fdSashform);

		//////////////////////////
		/// LEFT
		//////////////////////////
		Composite leftsplit = new Composite(sashform, SWT.NONE);
		leftsplit.setLayout(new FormLayout());
		FormData fdLeftsplit = new FormData(); 
		fdLeftsplit.left   = new FormAttachment(0, 0); 
		fdLeftsplit.top    = new FormAttachment(0, 0);
		fdLeftsplit.right  = new FormAttachment(100, 0);
		fdLeftsplit.bottom = new FormAttachment(100, 0);
		leftsplit.setLayoutData(fdLeftsplit);
        props.setLook(leftsplit);

 		// Source list to the left...
		wlListSource  = new Label(leftsplit, SWT.NONE);
		wlListSource.setText("Available items:");
        props.setLook(wlListSource);

 		FormData fdlListSource = new FormData();
		fdlListSource.left   = new FormAttachment(0, 0); 
		fdlListSource.top    = new FormAttachment(0, 0);
		wlListSource.setLayoutData(fdlListSource);
		
 		wListSource = new List(leftsplit, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wListSource);

 		FormData fdListSource = new FormData();
		fdListSource.left   = new FormAttachment(0, 0); 
		fdListSource.top    = new FormAttachment(wlListSource, 0);
		fdListSource.right  = new FormAttachment(100, 0);
		fdListSource.bottom = new FormAttachment(100, 0);
		wListSource.setLayoutData(fdListSource);
 		
 		///////////////////////////
 		// MIDDLE
 		///////////////////////////
 		
		Composite compmiddle = new Composite(sashform, SWT.NONE);
		compmiddle.setLayout (new FormLayout());
 		FormData fdCompMiddle = new FormData();
		fdCompMiddle.left   = new FormAttachment(0, 0); 
		fdCompMiddle.top    = new FormAttachment(0, 0);
		fdCompMiddle.right  = new FormAttachment(100, 0);
		fdCompMiddle.bottom = new FormAttachment(100, 0);
		compmiddle.setLayoutData(fdCompMiddle);
        props.setLook(compmiddle);

		wAddOne    = new Button(compmiddle, SWT.PUSH); wAddOne   .setText(" > ");  wAddOne   .setToolTipText("Add the selected items on the left.");
		wAddAll    = new Button(compmiddle, SWT.PUSH); wAddAll   .setText(" >> "); wAddAll   .setToolTipText("Add all items on the left.");
		wRemoveOne = new Button(compmiddle, SWT.PUSH); wRemoveOne.setText(" < ");  wRemoveOne.setToolTipText("Remove the selected items on the right.");
		wRemoveAll = new Button(compmiddle, SWT.PUSH); wRemoveAll.setText(" << "); wRemoveAll.setToolTipText("Add all items on the right.");

		FormData fdAddOne = new FormData();
		fdAddOne.left   = new FormAttachment(compmiddle, 0, SWT.CENTER); 
		fdAddOne.top    = new FormAttachment(30, 0);
		wAddOne.setLayoutData(fdAddOne);

		FormData fdAddAll = new FormData();
		fdAddAll.left   = new FormAttachment(compmiddle, 0, SWT.CENTER);
		fdAddAll.top    = new FormAttachment(wAddOne, margin);
		wAddAll.setLayoutData(fdAddAll);

		FormData fdRemoveAll = new FormData();
		fdRemoveAll.left   = new FormAttachment(compmiddle, 0, SWT.CENTER);
		fdRemoveAll.top    = new FormAttachment(wAddAll, margin);
		wRemoveAll.setLayoutData(fdRemoveAll);

		FormData fdRemoveOne = new FormData();
		fdRemoveOne.left   = new FormAttachment(compmiddle, 0, SWT.CENTER);
		fdRemoveOne.top    = new FormAttachment(wRemoveAll, margin);
		wRemoveOne.setLayoutData(fdRemoveOne);


		/////////////////////////////////
		// RIGHT
		/////////////////////////////////
		
		Composite rightsplit = new Composite(sashform, SWT.NONE);
		rightsplit.setLayout(new FormLayout());
		FormData fdRightsplit = new FormData(); 
		fdRightsplit .left   = new FormAttachment(0, 0); 
		fdRightsplit .top    = new FormAttachment(0, 0);
		fdRightsplit .right  = new FormAttachment(100, 0);
		fdRightsplit .bottom = new FormAttachment(100, 0);
		rightsplit.setLayoutData(fdRightsplit );
        props.setLook(rightsplit);

		wlListDest = new Label(rightsplit, SWT.NONE);
		wlListDest.setText("Your selection:");
        props.setLook(wlListDest);

 		FormData fdlListDest = new FormData();
		fdlListDest.left   = new FormAttachment(0, 0); 
		fdlListDest.top    = new FormAttachment(0, 0);
		wlListDest.setLayoutData(fdlListDest);

		wListDest = new List(rightsplit, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wListDest);

		FormData fdListDest = new FormData(); 
		fdListDest .left   = new FormAttachment(0, 0); 
		fdListDest .top    = new FormAttachment(wlListDest, 0);
		fdListDest .right  = new FormAttachment(100, 0);
		fdListDest .bottom = new FormAttachment(100, 0);
		wListDest.setLayoutData(fdListDest );

		sashform.setWeights(new int[] { 45, 10, 45 });

		// Drag & Drop for steps
		Transfer[] ttypes = new Transfer[] {TextTransfer.getInstance() };
		
		DragSource ddSource = new DragSource(wListSource, DND.DROP_MOVE | DND.DROP_COPY);
		ddSource.setTransfer(ttypes);
		ddSource.addDragListener(new DragSourceListener() 
			{
				public void dragStart(DragSourceEvent event){ }
	
				public void dragSetData(DragSourceEvent event) 
				{
					String ti[] = wListSource.getSelection();
					String data = new String();
					for (int i=0;i<ti.length;i++) data+=ti[i]+Const.CR;
					event.data = data;
				}
	
				public void dragFinished(DragSourceEvent event) {}
			}
		);
		DropTarget ddTarget = new DropTarget(wListDest, DND.DROP_MOVE | DND.DROP_COPY);
		ddTarget.setTransfer(ttypes);
		ddTarget.addDropListener(new DropTargetListener() 
		{
			public void dragEnter(DropTargetEvent event) { }
			public void dragLeave(DropTargetEvent event) { }
			public void dragOperationChanged(DropTargetEvent event) { }
			public void dragOver(DropTargetEvent event) { }
			public void drop(DropTargetEvent event) 
			{
				if (event.data == null) { // no data to copy, indicate failure in event.detail
					event.detail = DND.DROP_NONE;
					return;
				}
				StringTokenizer strtok = new StringTokenizer((String)event.data, Const.CR);
				while (strtok.hasMoreTokens())
				{
					String   source = strtok.nextToken();
					addToDestination(source);
				}
			}

			public void dropAccept(DropTargetEvent event) 
			{
			}
		});

		wListSource.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.character==SWT.CR)
				{
					addToSelection(wListSource.getSelection());
				}
			}
		});
		wListDest.addKeyListener(new KeyAdapter()
			{
				public void keyPressed(KeyEvent e)
				{
					if (e.character==SWT.CR)
					{
						delFromSelection(wListDest.getSelection());
					}
				}
			});

		// Double click adds to destination.
		wListSource.addSelectionListener(new SelectionAdapter()
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					addToSelection(wListSource.getSelection());
				}
			}
		);
		// Double click adds to source
		wListDest.addSelectionListener(new SelectionAdapter()
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					String sel[] = wListDest.getSelection();
					delFromSelection(sel);
				}
			}
		);

		wAddOne.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					addToSelection(wListSource.getSelection());
				}
			}
		);

		wRemoveOne.addSelectionListener(new SelectionAdapter()
										{
											public void widgetSelected(SelectionEvent e)
											{
												delFromSelection(wListDest.getSelection());
											}
										}
									);

		wAddAll.addSelectionListener(new SelectionAdapter()
										{
											public void widgetSelected(SelectionEvent e)
											{
												addAllToSelection();
											}
										}
									);

		wRemoveAll.addSelectionListener(new SelectionAdapter()
										{
											public void widgetSelected(SelectionEvent e)
											{
												removeAllFromSelection();
											}
										}
									);
		
		// set the composite as the control for this page
		setControl(composite);
	}	
	
	public boolean getInputData()
	{
		// Get some data...
		RipDatabaseWizardPage1 page1 = (RipDatabaseWizardPage1)getPreviousPage();
		
		Database sourceDb = new Database(RipDatabaseWizard.loggingObject, page1.getSourceDatabase());
		try
		{
			sourceDb.connect();
			input = sourceDb.getTablenames(false); // Don't include the schema since it can cause invalid syntax
		}
		catch(KettleDatabaseException dbe)
		{
			new ErrorDialog(shell, "Error getting tables", "Error obtaining table list from database!", dbe);
			input = null;
			return false;
		}
		finally
		{
			sourceDb.disconnect();
		}
		return true;
	}
	
	public void getData()
	{
		wListSource.removeAll();
		wListDest.removeAll();
		
		if (input!=null)
		{
			for (int i=0;i<input.length;i++)
			{
				Integer idx = Integer.valueOf(i);
				String str = selection.get(idx);
				if (str==null) // Not selected: show in source!
				{
					wListSource.add(input[i]);
				}
				else // Selected, show in destination!
				{
					wListDest.add(input[i]);
				}
			}
		}
		setPageComplete(canFlipToNextPage());
	}
	
	public void addAllToSelection()
	{
		// Just remove it all from both lists
		// Then add input[] to the destination list...
		// This is much faster.
		wListSource.removeAll();
		wListDest.removeAll();
		selection.clear();
		for (int i=0;i<input.length;i++) 
		{
			wListDest.add(input[i]);
			selection.put(Integer.valueOf(i), input[i]);
		}
		
		setPageComplete(canFlipToNextPage());
	}
	
	public void removeAllFromSelection()
	{
		// Just remove it all from both lists
		// Then add input[] to the source list...
		// This is much faster.
		wListSource.removeAll();
		wListDest.removeAll();
		selection.clear();
		for (int i=0;i<input.length;i++) wListSource.add(input[i]);
		
		setPageComplete(canFlipToNextPage());
	}
	
	
	
	public void addToSelection(String string[])
	{
		for (int i=0;i<string.length;i++) addToDestination(string[i]);
		setPageComplete(canFlipToNextPage());
	}

	public void delFromSelection(String string[])
	{
		for (int i=0;i<string.length;i++) delFromDestination(string[i]);
		setPageComplete(canFlipToNextPage());
	}

	public void addToDestination(String string)
	{
		int idxInput = Const.indexOfString(string, input);
		selection.put(Integer.valueOf(idxInput), string);
		
		getData();
	}

	public void delFromDestination(String string)
	{
		int idxInput = Const.indexOfString(string, input);
		selection.remove(Integer.valueOf(idxInput));
		
		getData();
	}
	
	public boolean canFlipToNextPage()
	{
		boolean canFlip = wListDest.getItemCount()>0;
		// System.out.println("canflip = "+canFlip);
		return canFlip;
	}	
	
	public String[] getSelection()
	{
		return wListDest.getItems();
	}
}
