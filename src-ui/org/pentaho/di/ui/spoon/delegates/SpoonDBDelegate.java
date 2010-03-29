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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.database.dialog.XulDatabaseDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.SQLStatementsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.dialog.GetJobSQLProgressDialog;
import org.pentaho.di.ui.spoon.dialog.GetSQLProgressDialog;

public class SpoonDBDelegate extends SpoonDelegate
{
	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	private DatabaseDialog databaseDialog;
	public SpoonDBDelegate(Spoon spoon)
	{
		super(spoon);
	}

	public void sqlConnection(DatabaseMeta databaseMeta)
	{
		SQLEditor sql = new SQLEditor(spoon.getShell(), SWT.NONE, databaseMeta, DBCache.getInstance(), "");
		sql.open();
	}

	public void editConnection(DatabaseMeta databaseMeta) {
		HasDatabasesInterface hasDatabasesInterface = spoon.getActiveHasDatabasesInterface();
		if (hasDatabasesInterface == null) {
			return; // program error, exit just to make sure.
		}

		getDatabaseDialog().setDatabaseMeta(databaseMeta);
		getDatabaseDialog().setDatabases(hasDatabasesInterface.getDatabases());
		String newname = getDatabaseDialog().open();
		if (!Const.isEmpty(newname)) // null: CANCEL
		{
			databaseMeta = getDatabaseDialog().getDatabaseMeta();

			saveConnection(databaseMeta, Const.VERSION_COMMENT_EDIT_VERSION);

			spoon.refreshTree();
		}
		spoon.setShellText();
	}
	
	private DatabaseDialog getDatabaseDialog(){
	  if(databaseDialog != null){
	    return databaseDialog;
	  }
	  databaseDialog = new DatabaseDialog(spoon.getShell());
	  return databaseDialog;
	}

	public void dupeConnection(HasDatabasesInterface hasDatabasesInterface, DatabaseMeta databaseMeta)
	{
		String name = databaseMeta.getName();
		int pos = hasDatabasesInterface.indexOfDatabase(databaseMeta);
		if (databaseMeta != null)
		{
			DatabaseMeta databaseMetaCopy = (DatabaseMeta) databaseMeta.clone();
			String dupename = BaseMessages.getString(PKG, "Spoon.Various.DupeName") + name;
			databaseMetaCopy.setName(dupename);

			getDatabaseDialog().setDatabaseMeta(databaseMetaCopy);
			
			String newname = getDatabaseDialog().open();
			if (newname != null) // null: CANCEL
			{
				databaseMetaCopy.verifyAndModifyDatabaseName(hasDatabasesInterface.getDatabases(), name);
				hasDatabasesInterface.addDatabase(pos + 1, databaseMetaCopy);
				spoon
						.addUndoNew((UndoInterface) hasDatabasesInterface,
								new DatabaseMeta[] { (DatabaseMeta) databaseMetaCopy.clone() },
								new int[] { pos + 1 });
				saveConnection(databaseMetaCopy, Const.VERSION_COMMENT_EDIT_VERSION);
				spoon.refreshTree();
			}
		}
	}

	public void clipConnection(DatabaseMeta databaseMeta)
	{
		String xml = XMLHandler.getXMLHeader() + databaseMeta.getXML();
		GUIResource.getInstance().toClipboard(xml);
	}

	/**
	 * Delete a database connection
	 * 
	 * @param name
	 *            The name of the database connection.
	 */
	public void delConnection(HasDatabasesInterface hasDatabasesInterface, DatabaseMeta db)
	{
		int pos = hasDatabasesInterface.indexOfDatabase(db);
		boolean worked = false;

		// delete from repository?
		Repository rep = spoon.getRepository();
		if (rep != null)
		{
			if (!rep.getSecurityProvider().isReadOnly())
			{
				try {
					rep.deleteDatabaseMeta(db.getName());
					worked = true;
				} catch (KettleException dbe) {
					new ErrorDialog(spoon.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingConnection.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingConnection.Message", db.getName()), dbe);
				}
			} 
			else
			{
				new ErrorDialog(spoon.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingConnection.Title"), 
						BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingConnection.Message", db.getName()), 
						new KettleException(BaseMessages.getString(PKG, "Spoon.Dialog.Exception.ReadOnlyUser")));
			}
		}

		if (spoon.getRepository() == null || worked)
		{
			spoon.addUndoDelete((UndoInterface) hasDatabasesInterface, new DatabaseMeta[] { (DatabaseMeta) db
					.clone() }, new int[] { pos });
			hasDatabasesInterface.removeDatabase(pos);
			DBCache.getInstance().clear(db.getName());  // remove this from the cache as well.
		}

		spoon.refreshTree();
		spoon.setShellText();
	}

	public String exploreDB(DatabaseMeta databaseMeta, boolean aLook)
	{
		List<DatabaseMeta> databases = null;
		HasDatabasesInterface activeHasDatabasesInterface = spoon.getActiveHasDatabasesInterface();
		if (activeHasDatabasesInterface != null)
			databases = activeHasDatabasesInterface.getDatabases();

		DatabaseExplorerDialog std = new DatabaseExplorerDialog(spoon.getShell(), SWT.NONE, databaseMeta,
				databases, aLook);
		return (String) std.open();
	}

	public void clearDBCache(DatabaseMeta databaseMeta)
	{
		if (databaseMeta != null)
		{
			DBCache.getInstance().clear(databaseMeta.getName());
		} else
		{
			DBCache.getInstance().clear(null);
		}
	}

	public void getSQL()
	{
		TransMeta transMeta = spoon.getActiveTransformation();
		if (transMeta != null)
			getTransSQL(transMeta);
		JobMeta jobMeta = spoon.getActiveJob();
		if (jobMeta != null)
			getJobSQL(jobMeta);
	}

	/**
	 * Get & show the SQL required to run the loaded transformation...
	 * 
	 */
	public void getTransSQL(TransMeta transMeta)
	{
		GetSQLProgressDialog pspd = new GetSQLProgressDialog(spoon.getShell(), transMeta);
		List<SQLStatement> stats = pspd.open();
		if (stats != null) // null means error, but we already displayed the
		// error
		{
			if (stats.size() > 0)
			{
				SQLStatementsDialog ssd = new SQLStatementsDialog(spoon.getShell(), Variables
						.getADefaultVariableSpace(), SWT.NONE, stats);
				String sn = ssd.open();

	            if (sn != null)
	            {
	                StepMeta esi = transMeta.findStep(sn);
	                if (esi != null)
	                {
	                    spoon.delegates.steps.editStep(transMeta,esi);
	                }
	            }
			} else
			{
				MessageBox mb = new MessageBox(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION);
				mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.NoSQLNeedEexecuted.Message"));
				mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.NoSQLNeedEexecuted.Title"));// "SQL"
				mb.open();
			}
		}
	}

	/**
	 * Get & show the SQL required to run the loaded job entry...
	 * 
	 */
	public void getJobSQL(JobMeta jobMeta)
	{
		GetJobSQLProgressDialog pspd = new GetJobSQLProgressDialog(spoon.getShell(), jobMeta, spoon
				.getRepository());
		List<SQLStatement> stats = pspd.open();
		if (stats != null) // null means error, but we already displayed the
		// error
		{
			if (stats.size() > 0)
			{
				SQLStatementsDialog ssd = new SQLStatementsDialog(spoon.getShell(), (VariableSpace) jobMeta,
						SWT.NONE, stats);
				ssd.open();
			} else
			{
				MessageBox mb = new MessageBox(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION);
				mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.JobNoSQLNeedEexecuted.Message")); //$NON-NLS-1$
				mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.JobNoSQLNeedEexecuted.Title")); //$NON-NLS-1$
				mb.open();
			}
		}
	}

  public boolean copyTable(DatabaseMeta sourceDBInfo, DatabaseMeta targetDBInfo, String tablename)
  {
    try
    {
      //
      // Create a new transformation...
      //
      TransMeta meta = new TransMeta();
      meta.addDatabase(sourceDBInfo);
      meta.addDatabase(targetDBInfo);

      //
      // Add a note
      //
      String note = BaseMessages.getString(PKG, "Spoon.Message.Note.ReadInformationFromTableOnDB", tablename,
          sourceDBInfo.getDatabaseName())
          + Const.CR;// "Reads information from table ["+tablename+"]
      // on database ["+sourceDBInfo+"]"
      note += BaseMessages.getString(PKG, "Spoon.Message.Note.WriteInformationToTableOnDB", tablename,
          targetDBInfo.getDatabaseName());// "After that, it writes
      // the information to table
      // ["+tablename+"] on
      // database
      // ["+targetDBInfo+"]"
      NotePadMeta ni = new NotePadMeta(note, 150, 10, -1, -1);
      meta.addNote(ni);

      // 
      // create the source step...
      //
      String fromstepname = BaseMessages.getString(PKG, "Spoon.Message.Note.ReadFromTable", tablename); // "read
      // from
      // ["+tablename+"]";
      TableInputMeta tii = new TableInputMeta();
      tii.setDatabaseMeta(sourceDBInfo);
      tii.setSQL("SELECT * FROM " + tablename);

      PluginRegistry registry = PluginRegistry.getInstance();

      String fromstepid = registry.getPluginId(StepPluginType.class, tii);
      StepMeta fromstep = new StepMeta(fromstepid, fromstepname, tii);
      fromstep.setLocation(150, 100);
      fromstep.setDraw(true);
      fromstep.setDescription(BaseMessages.getString(PKG, "Spoon.Message.Note.ReadInformationFromTableOnDB",
          tablename, sourceDBInfo.getDatabaseName()));
      meta.addStep(fromstep);

      //
      // add logic to rename fields in case any of the field names contain
      // reserved words...
      // Use metadata logic in SelectValues, use SelectValueInfo...
      //
      Database sourceDB = new Database(loggingObject, sourceDBInfo);
      sourceDB.shareVariablesWith(meta);
      sourceDB.connect();

      // Get the fields for the input table...
      RowMetaInterface fields = sourceDB.getTableFields(tablename);

      // See if we need to deal with reserved words...
      int nrReserved = targetDBInfo.getNrReservedWords(fields);
      if (nrReserved > 0)
      {
        SelectValuesMeta svi = new SelectValuesMeta();
        svi.allocate(0, 0, nrReserved);
        int nr = 0;
        for (int i = 0; i < fields.size(); i++)
        {
          ValueMetaInterface v = fields.getValueMeta(i);
          if (targetDBInfo.isReservedWord(v.getName()))
          {
            svi.getMeta()[nr].setName(v.getName());
            svi.getMeta()[nr].setRename(targetDBInfo.quoteField(v.getName()));
            nr++;
          }
        }

		String selstepname = BaseMessages.getString(PKG, "Spoon.Message.Note.HandleReservedWords"); // "Handle reserved words";
		String selstepid = registry.getPluginId(StepPluginType.class, svi);
		StepMeta selstep = new StepMeta(selstepid, selstepname, svi);
		selstep.setLocation(350, 100);
		selstep.setDraw(true);
		selstep.setDescription(BaseMessages.getString(PKG, "Spoon.Message.Note.RenamesReservedWords", targetDBInfo.getPluginId()));// 
		meta.addStep(selstep);

        TransHopMeta shi = new TransHopMeta(fromstep, selstep);
        meta.addTransHop(shi);
        fromstep = selstep;
      }

      // 
      // Create the target step...
      //
      //
      // Add the TableOutputMeta step...
      //
      String tostepname = BaseMessages.getString(PKG, "Spoon.Message.Note.WriteToTable", tablename);
      TableOutputMeta toi = new TableOutputMeta();
      toi.setDatabaseMeta(targetDBInfo);
      toi.setTablename(tablename);
      toi.setCommitSize(200);
      toi.setTruncateTable(true);

      String tostepid = registry.getPluginId(StepPluginType.class, toi);
      StepMeta tostep = new StepMeta(tostepid, tostepname, toi);
      tostep.setLocation(550, 100);
      tostep.setDraw(true);
      tostep.setDescription(BaseMessages.getString(PKG, "Spoon.Message.Note.WriteInformationToTableOnDB2",
          tablename, targetDBInfo.getDatabaseName()));
      meta.addStep(tostep);

      //
      // Add a hop between the two steps...
      //
      TransHopMeta hi = new TransHopMeta(fromstep, tostep);
      meta.addTransHop(hi);

      // OK, if we're still here: overwrite the current transformation...
      // Set a name on this generated transformation
      // 
      String name = "Copy table from [" + sourceDBInfo.getName() + "] to [" + targetDBInfo.getName()
          + "]";
      String transName = name;
      int nr = 1;
      if (spoon.delegates.trans.getTransformation(transName) != null)
      {
        nr++;
        transName = name + " " + nr;
      }
      meta.setName(transName);
      spoon.delegates.trans.addTransGraph(meta);

      spoon.refreshGraph();
      spoon.refreshTree();
    } catch (Exception e)
    {
      new ErrorDialog(spoon.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.UnexpectedError.Title"),
          BaseMessages.getString(PKG, "Spoon.Dialog.UnexpectedError.Message"), new KettleException(e
              .getMessage(), e));
      return false;
    }
    return true;
  }

	public void saveConnection(DatabaseMeta db, String versionComment)
	{
		// Also add to repository?
		Repository rep = spoon.getRepository();

		if (rep != null)
		{
			if (!rep.getSecurityProvider().isReadOnly())
			{
				try
				{
					
					if (Const.isEmpty(versionComment)) {
						rep.insertLogEntry("Saving database '" + db.getName() + "'");
					} else {
						rep.insertLogEntry("Save database : "+versionComment);
					}
					rep.save(db, versionComment, null);
					spoon.getLog().logDetailed(toString(), BaseMessages.getString(PKG, "Spoon.Log.SavedDatabaseConnection", db.getDatabaseName()));

					db.setChanged(false);
				} catch (KettleException ke)
				{
					new ErrorDialog(spoon.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingConnection.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingConnection.Message", db.getDatabaseName()), ke);
				} 
			} else
			{
				// This repository user is read-only!
				//
				new ErrorDialog(
						spoon.getShell(),
						BaseMessages.getString(PKG, "Spoon.Dialog.UnableSave.Title"),
						BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingConnection.Message", db.getDatabaseName()),
						new KettleException(BaseMessages.getString(PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser")));
			}
		}
	}

	public void newConnection() {
	  HasDatabasesInterface hasDatabasesInterface = spoon.getActiveHasDatabasesInterface();
    if (hasDatabasesInterface == null && spoon.rep == null) {
      return;
    }
    newConnection(hasDatabasesInterface);
	}
	
	
	public void newConnection(HasDatabasesInterface hasDatabasesInterface) {

		DatabaseMeta databaseMeta = new DatabaseMeta();
		if (hasDatabasesInterface instanceof VariableSpace) {
			databaseMeta.shareVariablesWith((VariableSpace) hasDatabasesInterface);
		} else {
			databaseMeta.initializeVariablesFrom(null);
		}


		getDatabaseDialog().setDatabaseMeta(databaseMeta);
		String con_name = getDatabaseDialog().open();
		if (!Const.isEmpty(con_name)) {
			databaseMeta = getDatabaseDialog().getDatabaseMeta();

			databaseMeta.verifyAndModifyDatabaseName(hasDatabasesInterface.getDatabases(), null);
			hasDatabasesInterface.addDatabase(databaseMeta);
			spoon.addUndoNew((UndoInterface) hasDatabasesInterface, new DatabaseMeta[] { (DatabaseMeta) databaseMeta.clone() }, new int[] { hasDatabasesInterface.indexOfDatabase(databaseMeta) });
			if (spoon.rep!=null) {
				try {
					if (!spoon.rep.getSecurityProvider().isReadOnly()) {
						spoon.rep.save(databaseMeta, Const.VERSION_COMMENT_INITIAL_VERSION, null);
					} else {
						throw new KettleException(BaseMessages.getString(PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser"));
					}
				} catch (KettleException e) {
					new ErrorDialog(spoon.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingConnection.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingConnection.Message", databaseMeta.getName()), e);
				}
			}
			spoon.refreshTree();
		}			
	}

}
