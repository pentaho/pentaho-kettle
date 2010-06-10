/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.spoon.delegates;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Partitioner;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.StepErrorMetaDialog;

public class SpoonStepsDelegate extends SpoonDelegate
{
	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public SpoonStepsDelegate(Spoon spoon)
	{
		super(spoon);
	}

	public void editStepErrorHandling(TransMeta transMeta, StepMeta stepMeta)
	{
		if (stepMeta != null && stepMeta.supportsErrorHandling())
		{
			StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();
			if (stepErrorMeta == null)
			{
				stepErrorMeta = new StepErrorMeta(transMeta, stepMeta);
			}
			List<StepMeta> targetSteps = transMeta.findNextSteps(stepMeta);

			// now edit this stepErrorMeta object:
			StepErrorMetaDialog dialog = new StepErrorMetaDialog(spoon.getShell(), stepErrorMeta, transMeta,
					targetSteps);
			if (dialog.open())
			{
				stepMeta.setStepErrorMeta(stepErrorMeta);
				stepMeta.setChanged();
				spoon.refreshGraph();
			}
		}
	}

	public void dupeStep(TransMeta transMeta, StepMeta stepMeta)
	{
		spoon.getLog().logDebug(toString(),
				BaseMessages.getString(PKG, "Spoon.Log.DuplicateStep") + stepMeta.getName());// Duplicate
		// step:

		StepMeta stMeta = (StepMeta) stepMeta.clone();
		if (stMeta != null)
		{
			String newname = transMeta.getAlternativeStepname(stepMeta.getName());
			int nr = 2;
			while (transMeta.findStep(newname) != null)
			{
				newname = stepMeta.getName() + " (copy " + nr + ")";
				nr++;
			}
			stMeta.setName(newname);
			// Don't select this new step!
			stMeta.setSelected(false);
			Point loc = stMeta.getLocation();
			stMeta.setLocation(loc.x + 20, loc.y + 20);
			transMeta.addStep(stMeta);
			spoon.addUndoNew(transMeta, new StepMeta[] { (StepMeta) stMeta.clone() }, new int[] { transMeta
					.indexOfStep(stMeta) });
			spoon.refreshTree();
			spoon.refreshGraph();
		}
	}

	public void clipStep(StepMeta stepMeta)
	{
		try {
			String xml = stepMeta.getXML();
			GUIResource.getInstance().toClipboard(xml);
		} catch(Exception ex) {
			new ErrorDialog(spoon.getShell(), "Error", "Error encoding to XML", ex);
		}
	}

	public String editStep(TransMeta transMeta, StepMeta stepMeta)
	{
		boolean refresh = false;
        String stepname = null;
		try
		{
			String name = stepMeta.getName();

			// Before we do anything, let's store the situation the way it
			// was...
			//
			StepMeta before = (StepMeta) stepMeta.clone();
			StepDialogInterface dialog = spoon.getStepEntryDialog(stepMeta.getStepMetaInterface(), transMeta, name);
			if (dialog != null)
			{
				dialog.setRepository(spoon.getRepository());
				stepname = dialog.open();
			}

			if (!Const.isEmpty(stepname))
			{
				// Force the recreation of the step IO metadata object. (cached by default)
				//
				stepMeta.getStepMetaInterface().resetStepIoMeta();
				
				// 
				// See if the new name the user enter, doesn't collide with
				// another step.
				// If so, change the stepname and warn the user!
				//
				String newname = stepname;
				StepMeta smeta = transMeta.findStep(newname, stepMeta);
				int nr = 2;
				while (smeta != null)
				{
					newname = stepname + " " + nr;
					smeta = transMeta.findStep(newname);
					nr++;
				}
				if (nr > 2)
				{
					stepname = newname;
					MessageBox mb = new MessageBox(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.StepnameExists.Message", stepname)); // $NON-NLS-1$
					mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.StepnameExists.Title")); // $NON-NLS-1$
					mb.open();
				}

				if (!stepname.equals(name))
					refresh = true;

				stepMeta.setName(stepname);

				// 
				// OK, so the step has changed...
				// Backup the situation for undo/redo
				//
				StepMeta after = (StepMeta) stepMeta.clone();
				spoon.addUndoChange(transMeta, new StepMeta[] { before }, new StepMeta[] { after },
						new int[] { transMeta.indexOfStep(stepMeta) });
			} 
			else
			{
				// Scenario: change connections and click cancel...
				// Perhaps new connections were created in the step dialog?
				if (transMeta.haveConnectionsChanged())
				{
					refresh = true;
				}
			}
			spoon.refreshGraph(); // name is displayed on the graph too.

			// TODO: verify "double pathway" steps for bug #4365
			// After the step was edited we can complain about the possible
			// deadlock here.
			//
		} catch (Throwable e)
		{
			if (spoon.getShell().isDisposed())
				return null;
			new ErrorDialog(spoon.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.UnableOpenDialog.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.UnableOpenDialog.Message"), e);
		}

		if (refresh)
			spoon.refreshTree(); // Perhaps new connections were created in
		// the step
		// dialog or the step name changed.

		return stepname;
	}

	public void delStep(TransMeta transMeta, StepMeta stepMeta)
	{
		spoon.getLog().logDebug(toString(), BaseMessages.getString(PKG, "Spoon.Log.DeleteStep") + stepMeta.getName());// "Delete
		// step:
		// "

		for (int i = transMeta.nrTransHops() - 1; i >= 0; i--)
		{
			TransHopMeta hi = transMeta.getTransHop(i);
			if (hi.getFromStep().equals(stepMeta) || hi.getToStep().equals(stepMeta)) {
				int idx = transMeta.indexOfTransHop(hi);
			  spoon.addUndoDelete(transMeta, 
                				    new TransHopMeta[] { (TransHopMeta)hi.clone() }, 
                				    new int[] { idx }
			                      //,true            // the true flag was causing the hops to not get restored on Undo delete step with hop(s)
				);
				transMeta.removeTransHop(idx);
				spoon.refreshTree();
			}
		}

		int pos = transMeta.indexOfStep(stepMeta);
		transMeta.removeStep(pos);
		spoon.addUndoDelete(transMeta, new StepMeta[] { stepMeta }, new int[] { pos });

		spoon.refreshTree();
		spoon.refreshGraph();
	}

	public StepDialogInterface getStepEntryDialog(StepMetaInterface stepMeta, TransMeta transMeta,
			String stepName) throws KettleException
	{
		String dialogClassName = stepMeta.getDialogClassName();

		Class<?> dialogClass;
		Class<?>[] paramClasses = new Class[] { Shell.class, Object.class, TransMeta.class, String.class };
		Object[] paramArgs = new Object[] { spoon.getShell(), stepMeta, transMeta, stepName };
		Constructor<?> dialogConstructor;
		try
		{
			dialogClass = stepMeta.getClass().getClassLoader().loadClass(dialogClassName);
			dialogConstructor = dialogClass.getConstructor(paramClasses);
			return (StepDialogInterface) dialogConstructor.newInstance(paramArgs);
		} catch (Exception e)
		{
			// try the old way for compatibility 
			Method method = null;
				try {
					Class<?> sig[] = new Class[] {Shell.class, StepMetaInterface.class, TransMeta.class, String.class};
					method = stepMeta.getClass().getDeclaredMethod( "getDialog", sig );
					if( method != null ) {
						return (StepDialogInterface) method.invoke( stepMeta, new Object[] { spoon.getShell(), stepMeta, transMeta, stepName } );
					}
				} catch (Throwable t) {
				}

			throw new KettleException(e);
		}

	}

	public StepDialogInterface getPartitionerDialog(StepMeta stepMeta, StepPartitioningMeta partitioningMeta, TransMeta transMeta) throws KettleException
	{
		Partitioner partitioner = partitioningMeta.getPartitioner();
		String dialogClassName = partitioner.getDialogClassName();

		Class<?> dialogClass;
		Class<?>[] paramClasses = new Class[] { Shell.class, StepMeta.class, StepPartitioningMeta.class, TransMeta.class };
		Object[] paramArgs = new Object[] { spoon.getShell(), stepMeta, partitioningMeta, transMeta };
		Constructor<?> dialogConstructor;
		try
		{
			dialogClass = partitioner.getClass().getClassLoader().loadClass(dialogClassName);
			dialogConstructor = dialogClass.getConstructor(paramClasses);
			return (StepDialogInterface) dialogConstructor.newInstance(paramArgs);
		} catch (Exception e)
		{
			// try the old way for compatibility 
			Method method = null;
				try {
					Class<?> sig[] = new Class[] {Shell.class, StepMetaInterface.class, TransMeta.class};
					method = stepMeta.getClass().getDeclaredMethod( "getDialog", sig );
					if( method != null ) {
						return (StepDialogInterface) method.invoke( stepMeta, new Object[] { spoon.getShell(), stepMeta, transMeta } );
					}
				} catch (Throwable t) {
				}

			throw new KettleException(e);
		}

	}

}