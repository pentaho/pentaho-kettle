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
package org.pentaho.di.trans;

import org.pentaho.di.core.DomainObjectCreationException;
import org.pentaho.di.core.DomainObjectRegistry;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.groupby.GroupByMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.w3c.dom.Node;

/**
 * Helper class to generate profiling transformations...
 * 
 * @author Matt Casters (mcasters@pentaho.org)
 *
 */
public class TransProfileFactory
{
	public static final String RESULT_STEP_NAME = "calc stats";
	
	private DatabaseMeta databaseMeta;
	private String schemaTable;

	private RowMetaInterface tableLayout;

	/**
	 * 
	 * @param parent
	 * @param databaseMeta
	 * @param schemaTable the properly quoted schema-table combination
	 * @param degreeParallelism The degree of parallelism or the number of copies that the calculation module is running with.
	 */
	public TransProfileFactory(DatabaseMeta databaseMeta, String schemaTable) {
		this.databaseMeta = databaseMeta;
		this.schemaTable = schemaTable;
	}
	
    public TransMeta generateTransformation(LoggingObjectInterface parentLoggingInterface) throws KettleException
    {
        PluginRegistry registry = PluginRegistry.getInstance();
        
        // Get the list of fields from the table...
        //
        tableLayout = getTableFields(parentLoggingInterface);

        // Now start building the transformation...
        //
        TransMeta transMeta = null;
        
        try {
          transMeta = DomainObjectRegistry.getInstance().constructTransMeta(new Class[] {DatabaseMeta.class}, new Object[]{databaseMeta}); 
        } catch(DomainObjectCreationException doce) {
          transMeta = new TransMeta(databaseMeta);
        } 
        transMeta.addDatabase(databaseMeta);
        
        // Create a step to read the content of the table
        // Read the data from the database table...
        // For now we read it all, later we add options to only read the first X rows
        //
        TableInputMeta readMeta = new TableInputMeta();
        readMeta.setSQL("SELECT * FROM "+schemaTable);
        readMeta.setDatabaseMeta(databaseMeta);
        StepMeta read = new StepMeta(registry.getPluginId(StepPluginType.class, readMeta), "Read data", readMeta);
        read.setLocation(50,50);
        read.setDraw(true);
        transMeta.addStep(read);
        
        // Grab the data types too
        //
        
        // Now calculate the requested statistics for all fields...
        // TODO: create configuration possibility
        // For now, just do : min, max, sum, count, avg, std dev. (7)
        //
        int numericCalculations[] = new int[] { 
        		GroupByMeta.TYPE_GROUP_MIN, 	  GroupByMeta.TYPE_GROUP_MAX, 	  GroupByMeta.TYPE_GROUP_SUM, 
        		GroupByMeta.TYPE_GROUP_COUNT_ALL, GroupByMeta.TYPE_GROUP_AVERAGE, GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION,  
        	};

        int stringCalculations[] = new int[] { 
        		GroupByMeta.TYPE_GROUP_MIN, 	  GroupByMeta.TYPE_GROUP_MAX, 	  
        		GroupByMeta.TYPE_GROUP_COUNT_ALL,  
        	};

        int dateCalculations[] = new int[] { 
        		GroupByMeta.TYPE_GROUP_MIN, 	  GroupByMeta.TYPE_GROUP_MAX, 	  
        		GroupByMeta.TYPE_GROUP_COUNT_ALL,  
        	};

        int booleanCalculations[] = new int[] { 
        		GroupByMeta.TYPE_GROUP_MIN, 	  GroupByMeta.TYPE_GROUP_MAX, 	  
        		GroupByMeta.TYPE_GROUP_COUNT_ALL,  
        	};

        // Run it through the "group by" step without a grouping.
        // Later, we can use the UnivariateStats plugin/step perhaps.
        //
        GroupByMeta statsMeta = new GroupByMeta();
        int nrNumeric = 0;
        int nrDates = 0;
        int nrStrings = 0;
        int nrBooleans = 0;
        for (ValueMetaInterface valueMeta : tableLayout.getValueMetaList()) {
        	if (valueMeta.isNumeric()) nrNumeric++;
        	if (valueMeta.isDate()) nrDates++;
        	if (valueMeta.isString()) nrStrings++;
        	if (valueMeta.isBoolean()) nrBooleans++;
        }
        int nrCalculations = nrNumeric*numericCalculations.length+
        					 nrDates*dateCalculations.length+
        					 nrStrings*stringCalculations.length+
        					 nrBooleans*booleanCalculations.length;
        
        statsMeta.allocate(0,nrCalculations);
        int calcIndex=0;
        for (int i=0;i<tableLayout.size();i++) {
        	ValueMetaInterface valueMeta = tableLayout.getValueMeta(i);
        	// Numeric data...
        	//
        	if (valueMeta.isNumeric()) {
	        	for (int c=0;c<numericCalculations.length;c++) {
		        	statsMeta.getAggregateField()[calcIndex] = valueMeta.getName()+"("+GroupByMeta.getTypeDesc(numericCalculations[c])+")"; // $NON-NLS-1$ $NON-NLS-2$
		        	statsMeta.getSubjectField()[calcIndex]   = valueMeta.getName();
		        	statsMeta.getAggregateType()[calcIndex]  = numericCalculations[c];
		        	calcIndex++;
	        	}
        	}

	        // String data
	        //
        	if (valueMeta.isString()) {
	        	for (int c=0;c<stringCalculations.length;c++) {
		        	statsMeta.getAggregateField()[calcIndex] = valueMeta.getName()+"("+GroupByMeta.getTypeDesc(stringCalculations[c])+")"; // $NON-NLS-1$ $NON-NLS-2$
		        	statsMeta.getSubjectField()[calcIndex]   = valueMeta.getName();
		        	statsMeta.getAggregateType()[calcIndex]  = stringCalculations[c];
		        	calcIndex++;
	        	}
        	}

	        // Date data
	        //
        	if (valueMeta.isDate()) {
	        	for (int c=0;c<dateCalculations.length;c++) {
		        	statsMeta.getAggregateField()[calcIndex] = valueMeta.getName()+"("+GroupByMeta.getTypeDesc(dateCalculations[c])+")"; // $NON-NLS-1$ $NON-NLS-2$
		        	statsMeta.getSubjectField()[calcIndex]   = valueMeta.getName();
		        	statsMeta.getAggregateType()[calcIndex]  = dateCalculations[c];
		        	calcIndex++;
	        	}
        	}

	        // Boolean data
	        //
        	if (valueMeta.isBoolean()) {
	        	for (int c=0;c<booleanCalculations.length;c++) {
		        	statsMeta.getAggregateField()[calcIndex] = valueMeta.getName()+"("+GroupByMeta.getTypeDesc(booleanCalculations[c])+")"; // $NON-NLS-1$ $NON-NLS-2$
		        	statsMeta.getSubjectField()[calcIndex]   = valueMeta.getName();
		        	statsMeta.getAggregateType()[calcIndex]  = booleanCalculations[c];
		        	calcIndex++;	        
	        	}
        	}
        }
        StepMeta calc = new StepMeta(registry.getPluginId(StepPluginType.class, statsMeta), "Calc", statsMeta); //$NON-NLS-1$
        calc.setLocation(250,50);
        calc.setDraw(true);
        transMeta.addStep(calc);
        
        TransHopMeta hop = new TransHopMeta(read, calc);
        transMeta.addTransHop(hop);
        
        DummyTransMeta dummyMeta = new DummyTransMeta();
        StepMeta result = new StepMeta(registry.getPluginId(StepPluginType.class, dummyMeta), RESULT_STEP_NAME, dummyMeta); //$NON-NLS-1$
        result.setLocation(450,50);
        result.setDraw(true);
        transMeta.addStep(result);
        
        TransHopMeta hop2 = new TransHopMeta(calc, result);
        transMeta.addTransHop(hop2);
        
        return transMeta;
    }

	private RowMetaInterface getTableFields(LoggingObjectInterface parentLoggingObject) throws KettleDatabaseException {
		Database database = new Database(parentLoggingObject, databaseMeta);
		try {
			database.connect();
			return database.getTableFields(schemaTable);
		}
		finally {
			database.disconnect();
		}
		 
	}

	/**
	 * @return the tableLayout
	 */
	public RowMetaInterface getTableLayout() {
		return tableLayout;
	}
}
