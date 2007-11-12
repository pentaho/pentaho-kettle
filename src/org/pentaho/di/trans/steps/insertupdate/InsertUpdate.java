 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.insertupdate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Performs a lookup in a database table.  If the key doesn't exist it inserts values 
 * into the table, otherwise it performs an update of the changed values.
 * If nothing changed, do nothing.
 *  
 * @author Matt
 * @since 26-apr-2003
 */
public class InsertUpdate extends BaseStep implements StepInterface
{
	private InsertUpdateMeta meta;
	private InsertUpdateData data;
	
	public InsertUpdate(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
    private synchronized void lookupValues(RowMetaInterface rowMeta, Object[] row) throws KettleException
    {
        // OK, now do the lookup.
        // We need the lookupvalues for that.
        Object[] lookupRow = new Object[data.lookupParameterRowMeta.size()];
        int lookupIndex = 0;
        
        for (int i=0;i<meta.getKeyStream().length;i++)
        {
            if (data.keynrs[i]>=0)
            {
                lookupRow[lookupIndex] = row[ data.keynrs[i] ];
                lookupIndex++;
            }
            if (data.keynrs2[i]>=0)
            {
                lookupRow[lookupIndex] = row[ data.keynrs2[i] ];
                lookupIndex++;
            }
        }
        
        data.db.setValues(data.lookupParameterRowMeta, lookupRow, data.prepStatementLookup);
		
		if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.ValuesSetForLookup")+data.lookupParameterRowMeta.getString(lookupRow)); //$NON-NLS-1$
        Object[] add = data.db.getLookup(data.prepStatementLookup);  // Got back the complete row!
        linesInput++;
		
		if (add==null) 
		{
			/* nothing was found:
			 *  
			 * INSERT ROW
			 *
			 */
			if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.InsertRow")+rowMeta.getString(row)); //$NON-NLS-1$

			// The values to insert are those in the update section (all fields should be specified)
            // For the others, we have no definite mapping!
            //
            Object[] insertRow = new Object[data.valuenrs.length];
            for (int i=0;i<data.valuenrs.length;i++)
            {
                insertRow[i] = row[ data.valuenrs[i] ];
            }
            
            // Set the values on the prepared statement...
			data.db.setValuesInsert(data.insertRowMeta, insertRow);
            
			// Insert the row
            data.db.insertRow();
            
			linesOutput++;
		}
		else
		{
			if (!meta.isUpdateBypassed())
			{
				if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.Log.FoundRowForUpdate")+rowMeta.getString(row)); //$NON-NLS-1$
				
                /* Row was found:
                 *  
                 * UPDATE row or do nothing?
                 *
                 */
                boolean update = false;
                for (int i=0;i<data.valuenrs.length;i++)
                {
            		if ( meta.getUpdate()[i].booleanValue() ) 
            		{
                        ValueMetaInterface valueMeta = rowMeta.getValueMeta( data.valuenrs[i] );
                        Object rowvalue = row[ data.valuenrs[i] ];
                        Object retvalue = add[ i ];
                    
                        if ( valueMeta.compare(rowvalue, retvalue)!=0 )
                        {
                            update=true;
                        }
            		}
                }
                if (update)
                {
                    // Create the update row...
                    Object[] updateRow = new Object[data.updateParameterRowMeta.size()];
                    int j = 0;
                    for (int i=0;i<data.valuenrs.length;i++)
                    {
                		if( meta.getUpdate()[i].booleanValue() ) 
                		{
                            updateRow[j] = row[ data.valuenrs[i] ]; // the setters
                            j++;
                		}
                    }
                    // add the where clause parameters, they are exactly the same for lookup and update
                    for (int i=0;i<lookupRow.length;i++)
                    {
                        updateRow[j+i] = lookupRow[i];
                    }
                    
                    if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.Log.UpdateRow")+data.lookupParameterRowMeta.getString(lookupRow)); //$NON-NLS-1$
                    data.db.setValues(data.updateParameterRowMeta, updateRow, data.prepStatementUpdate);
                    data.db.insertRow(data.prepStatementUpdate);
                    linesUpdated++;
                }
                else
                {
                    linesSkipped++;
                }
			}
			else
			{
				if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.Log.UpdateBypassed")+rowMeta.getString(row)); //$NON-NLS-1$
				linesSkipped++;
			}
		}
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(InsertUpdateMeta)smi;
		data=(InsertUpdateData)sdi;
		
		boolean sendToErrorRow=false;
		String errorMessage = null;

		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)          // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first=false;
            
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
            data.schemaTable = meta.getDatabaseMeta().getQuotedSchemaTableCombination(environmentSubstitute(meta.getSchemaName()), 
            		                                                                  environmentSubstitute(meta.getTableName()));
            
            // lookup the values!
            if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.CheckingRow")+getInputRowMeta().getString(r)); //$NON-NLS-1$
            
            data.keynrs  = new int[meta.getKeyStream().length];
            data.keynrs2 = new int[meta.getKeyStream().length];
            for (int i=0;i<meta.getKeyStream().length;i++)
            {
                data.keynrs[i]=getInputRowMeta().indexOfValue(meta.getKeyStream()[i]);
                if (data.keynrs[i]<0 &&  // couldn't find field!
                    !"IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) &&   // No field needed! //$NON-NLS-1$
                    !"IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i])  // No field needed! //$NON-NLS-1$
                   )
                {
                    throw new KettleStepException(Messages.getString("InsertUpdate.Exception.FieldRequired",meta.getKeyStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
                }
                data.keynrs2[i]=getInputRowMeta().indexOfValue(meta.getKeyStream2()[i]);
                if (data.keynrs2[i]<0 &&  // couldn't find field!
                    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed! //$NON-NLS-1$
                   )
                {
                    throw new KettleStepException(Messages.getString("InsertUpdate.Exception.FieldRequired",meta.getKeyStream2()[i])); //$NON-NLS-1$ //$NON-NLS-2$
                }
                
                if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.FieldHasDataNumbers",meta.getKeyStream()[i])+data.keynrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            // Cache the position of the compare fields in Row row
            //
            data.valuenrs = new int[meta.getUpdateLookup().length];
            for (int i=0;i<meta.getUpdateLookup().length;i++)
            {
                data.valuenrs[i]=getInputRowMeta().indexOfValue(meta.getUpdateStream()[i]);
                if (data.valuenrs[i]<0)  // couldn't find field!
                {
                    throw new KettleStepException(Messages.getString("InsertUpdate.Exception.FieldRequired",meta.getUpdateStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.FieldHasDataNumbers",meta.getUpdateStream()[i])+data.valuenrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            setLookup(getInputRowMeta());
            
            data.insertRowMeta = new RowMeta();
            
            // Insert the update fields: just names.  Type doesn't matter!
            for (int i=0;i<meta.getUpdateLookup().length;i++) 
            {
                ValueMetaInterface insValue = data.insertRowMeta.searchValueMeta( meta.getUpdateLookup()[i]); 
                if (insValue==null) // Don't add twice!
                {
                    // we already checked that this value exists so it's probably safe to ignore lookup failure...
                    ValueMetaInterface insertValue = getInputRowMeta().searchValueMeta( meta.getUpdateStream()[i] ).clone();
                    insertValue.setName(meta.getUpdateLookup()[i]);
                    data.insertRowMeta.addValueMeta( insertValue );
                }
                else
                {
                    throw new KettleStepException("The same column can't be inserted into the target row twice: "+insValue.getName()); // TODO i18n
                }
            }
            data.db.prepareInsert(data.insertRowMeta, environmentSubstitute(meta.getSchemaName()), 
            		                                  environmentSubstitute(meta.getTableName()));
            
            if (!meta.isUpdateBypassed())
            {
                List<String> updateColumns = new ArrayList<String>();
                for(int i=0;i<meta.getUpdate().length;i++) {
                    if(meta.getUpdate()[i].booleanValue()) {
                        updateColumns.add(meta.getUpdateLookup()[i]);
                    }
                }
                prepareUpdate(getInputRowMeta());
            }
        }

		    
		try
		{
			lookupValues(getInputRowMeta(), r); // add new values to the row in rowset[0].
            putRow(data.outputRowMeta, r);      // Nothing changed to the input, return the same row, pass a "cloned" metadata row.
			
			if (checkFeedback(linesRead)) logBasic(Messages.getString("InsertUpdate.Log.LineNumber")+linesRead); //$NON-NLS-1$
		}
		catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
	        {
                sendToErrorRow = true;
                errorMessage = e.toString();
	        }
	        else
	        {
			
				throw new KettleStepException(Messages.getString("InsertUpdate.Log.ErrorInStep"), e); //$NON-NLS-1$
	        }
			 
			 if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
	             putError(getInputRowMeta(), r, 1, errorMessage, null, "ISU001");
	         }
		}
	
		return true;
	}
    
    public void setLookup(RowMetaInterface rowMeta) throws KettleDatabaseException
    {
        data.lookupParameterRowMeta = new RowMeta();
        data.lookupReturnRowMeta = new RowMeta();
        
        DatabaseMeta databaseMeta = meta.getDatabaseMeta();
        
        String sql = "SELECT ";

        for (int i = 0; i < meta.getUpdateLookup().length; i++)
        {
            if (i != 0) sql += ", ";
            sql += databaseMeta.quoteField(meta.getUpdateLookup()[i]);
            data.lookupReturnRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getUpdateStream()[i]) );
        }

        sql += " FROM " + data.schemaTable + " WHERE ";

        for (int i = 0; i < meta.getKeyLookup().length; i++)
        {
            if (i != 0) sql += " AND ";
            sql += databaseMeta.quoteField(meta.getKeyLookup()[i]);
            if ("BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i]))
            {
                sql += " BETWEEN ? AND ? ";
                data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream()[i]) );
                data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream2()[i]) );
            }
            else
            {
                if ("IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) || "IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i]))
                {
                    sql += " " + meta.getKeyCondition()[i] + " ";
                }
                else
                {
                    sql += " " + meta.getKeyCondition()[i] + " ? ";
                    data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream()[i]) );
                }
            }
        }
        
        try
        {
            log.logDetailed(toString(), "Setting preparedStatement to [" + sql + "]");
            data.prepStatementLookup = data.db.getConnection().prepareStatement(databaseMeta.stripCR(sql));
        }
        catch (SQLException ex)
        {
            throw new KettleDatabaseException("Unable to prepare statement for SQL statement [" + sql + "]", ex);
        }
    }
    
    // Lookup certain fields in a table
    public void prepareUpdate(RowMetaInterface rowMeta) throws KettleDatabaseException
    {
        DatabaseMeta databaseMeta = meta.getDatabaseMeta();
        data.updateParameterRowMeta = new RowMeta();
        
        String sql = "UPDATE " + data.schemaTable + Const.CR;
        sql += "SET ";

        for (int i=0;i<meta.getUpdateLookup().length;i++)
        {
    		if ( meta.getUpdate()[i].booleanValue() ) {
                if (i!=0) sql+= ",   ";
                sql += databaseMeta.quoteField(meta.getUpdateLookup()[i]);
                sql += " = ?" + Const.CR;
                data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getUpdateStream()[i]) );
    		}
        }

        sql += "WHERE ";

        for (int i=0;i<meta.getKeyLookup().length;i++)
        {
            if (i!=0) sql += "AND   ";
            sql += databaseMeta.quoteField(meta.getKeyLookup()[i]);
            if ("BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i]))
            {
                sql += " BETWEEN ? AND ? ";
                data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream()[i]) );
                data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream2()[i]) );
            }
            else
            if ("IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) || "IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i]))
            {
                sql += " "+meta.getKeyCondition()[i]+" ";
            }
            else
            {
                sql += " "+meta.getKeyCondition()[i]+" ? ";
                data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream()[i]) );
            }
        }

        try
        {
            log.logDetailed(toString(), "Setting update preparedStatement to ["+sql+"]");
            data.prepStatementUpdate=data.db.getConnection().prepareStatement(databaseMeta.stripCR(sql));
        }
        catch(SQLException ex) 
        {
            throw new KettleDatabaseException("Unable to prepare statement for SQL statement [" + sql + "]", ex);
        }
    }

	
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(InsertUpdateMeta)smi;
		data=(InsertUpdateData)sdi;
		
		if (super.init(smi, sdi))
		{
		    try
		    {
				data.db=new Database(meta.getDatabaseMeta());
				data.db.shareVariablesWith(this);
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }
				data.db.setCommit(meta.getCommitSize());

				return true;
			}
			catch(KettleException ke)
			{
				logError(Messages.getString("InsertUpdate.Log.ErrorOccurredDuringStepInitialize")+ke.getMessage()); //$NON-NLS-1$
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (InsertUpdateMeta)smi;
	    data = (InsertUpdateData)sdi;
	
        try
        {
            if (!data.db.isAutoCommit())
            {
                if (getErrors()==0)
                {
                    data.db.commit();
                }
                else
                {
                    data.db.rollback();
                }
            }
            data.db.closeUpdate();
            data.db.closeInsert();
        }
        catch(KettleDatabaseException e)
        {
            log.logError(toString(), Messages.getString("InsertUpdate.Log.UnableToCommitConnection")+e.toString()); //$NON-NLS-1$
            setErrors(1);
        }
        finally 
        {
  		    data.db.disconnect();
        }

	    super.dispose(smi, sdi);
	}

	public String toString()
	{
		return this.getClass().getName();
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
            setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}