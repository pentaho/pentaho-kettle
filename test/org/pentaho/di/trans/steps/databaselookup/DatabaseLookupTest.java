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

package org.pentaho.di.trans.steps.databaselookup;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.injector.InjectorMeta;


/**
 * Test class for database lookup. H2 is used
 * as database in memory to get an easy playground for database
 * tests. H2 does not support all SQL features but it should
 * proof enough for most of our tests.
 * 
 * Still to do:
 *  - cache testing.
 *  - Do not pass rows functionality/eat rows on failed lookup
 *  - Fail on multiple rows
 *  - Order by
 *  - Different comparators
 *
 * @author Sven Boden
 */
public class DatabaseLookupTest 
{
    static Database database;

	public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject("Database Lookup test", LoggingObjectType.GENERAL, null);
    
    public static final String[] databasesXML = {
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<connection>" +
            "<name>db</name>" +
            "<server>127.0.0.1</server>" +
            "<type>H2</type>" +
            "<access>Native</access>" +
            "<database>mem:db</database>" +
            "<port></port>" +
            "<username>sa</username>" +
            "<password></password>" +
          "</connection>",
    };

    private static String lookup_table = "lookup_table1";

    private static String insertStatement[] = 
    {
    	// New rows for the source
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (1, 100, '1')",
        
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (2, 100, '2')",
        
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (3, 100, '3')",

        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (4, 100, '4')",

        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (5, 101, '5')",
        
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (6, 101, '6')",
        
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (7, 101, '7')",

        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (8, 101, '8')",
        
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (9, 102, '9')",
        
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (10, 102, '10')",
        
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (11, 102, '11')",

        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (12, 102, '12')",
        
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (13, 103, '13')",

        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (14, 103, '14')",
        
        "INSERT INTO " + lookup_table + "(ID, CODE, STRING) " +
        "VALUES (15, 103, '15')"        
    };
    
	public static RowMetaInterface createSourceRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("ID",     ValueMeta.TYPE_INTEGER,  8, 0),
			    new ValueMeta("CODE",   ValueMeta.TYPE_INTEGER,  8, 0),
			    new ValueMeta("STRING", ValueMeta.TYPE_STRING,  30, 0)
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}       	
	
	/**
	 * Create source table.
	 */
	public static void createTables(Database db) throws Exception
	{		
		String source = db.getCreateTableStatement(lookup_table, createSourceRowMetaInterface(), null, false, null, true);
		try  {
		    db.execStatement(source);
		}
		catch ( KettleException ex ) 
		{
			fail("failure while creating table " + lookup_table + ": " + ex.getMessage());	
		}						
	}

	/**
	 * Insert data in the source table.
	 * 
	 * @param db database to use. 
	 */
	private static void createData(Database db) throws Exception
	{		
		for ( int idx = 0; idx < insertStatement.length; idx++ )
		{
		    db.execStatement(insertStatement[idx]);
		}
	}
	
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("int_field", ValueMeta.TYPE_INTEGER)
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}

	/**
	 * Create the input rows used for a unit test.
	 */
	public List<RowMetaAndData> createDataRows()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { new Long( 5L) };
		Object[] r2 = new Object[] { new Long( 9L) };
		Object[] r3 = new Object[] { new Long(20L) };  // non-existing one.
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		
		return list;
	}	

	public RowMetaInterface createResultRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("int_gield",  ValueMeta.TYPE_INTEGER,  8, 0),
			    new ValueMeta("RET_CODE",   ValueMeta.TYPE_INTEGER,  8, 0),
			    new ValueMeta("RET_STRING", ValueMeta.TYPE_STRING,  30, 0)
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}       	
	
	
	/**
	 * Create the result rows for a test.
	 */
	public List<RowMetaAndData> createResultDataRows()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createResultRowMetaInterface();
		
		Object[] r1  = new Object[] { new Long(5L),   new Long(101L), "5" };
		Object[] r2  = new Object[] { new Long(9L),   new Long(102L), "9" };
		Object[] r3  = new Object[] { new Long(20L),  new Long(-1L),  "UNDEF" };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		
		return list;
	}	
	
	/**
	 *  Check the 2 lists comparing the rows in order.
	 *  If they are not the same fail the test. 
	 */
    public void checkRows(List<RowMetaAndData> rows1, List<RowMetaAndData> rows2)
    {
    	int idx = 1;
        if ( rows1.size() != rows2.size() )
        {
        	fail("Number of rows is not the same: " + 
          		 rows1.size() + " and " + rows2.size());
        }
        Iterator<RowMetaAndData> it1 = rows1.iterator();
        Iterator<RowMetaAndData> it2 = rows2.iterator();
        
        while ( it1.hasNext() && it2.hasNext() )
        {
        	RowMetaAndData rm1 = it1.next();
        	RowMetaAndData rm2 = it2.next();
        	
        	Object[] r1 = rm1.getData();
        	Object[] r2 = rm2.getData();
        	
        	if ( rm1.size() != rm2.size() )
        	{
        		fail("row nr " + idx + " is not equal");
        	}
        	int fields[] = new int[r1.length];
        	for ( int ydx = 0; ydx < r1.length; ydx++ )
        	{
        		fields[ydx] = ydx;
        	}
            try {
				if ( rm1.getRowMeta().compare(r1, r2, fields) != 0 )
				{
					fail("row nr " + idx + " is not equal");
				}
			} catch (KettleValueException e) {
				fail("row nr " + idx + " is not equal");
			}
            	
            idx++;
        }
    }
    
    @BeforeClass
    public static void createDatabase() throws Exception
    {
        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("transname");

        // Add the database connections
        for (int i=0;i<databasesXML.length;i++)
        {
            DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
            transMeta.addDatabase(databaseMeta);
        }
        DatabaseMeta dbInfo = transMeta.findDatabase("db");

        // Execute our setup SQLs in the database.
        database = new Database(loggingObject, dbInfo);
        database.connect();
        createTables(database);
        createData(database);
    }
    
	/**
	 * Basic Test case for database lookup.
	 */
    @Test
    public void BasicDatabaseLookup() throws Exception
    {
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
            TransMeta transMeta = new TransMeta();
            transMeta.setName("transname");

            // Add the database connections
            for (int i=0;i<databasesXML.length;i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
                transMeta.addDatabase(databaseMeta);
            }

            DatabaseMeta dbInfo = transMeta.findDatabase("db");
            PluginRegistry registry = PluginRegistry.getInstance();            

            // 
            // create an injector step...
            //
            String injectorStepname = "injector step";
            InjectorMeta im = new InjectorMeta();
            
            // Set the information of the injector.
                    
            String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
            StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
            transMeta.addStep(injectorStep);            
            
            // 
            // create the lookup step...
            //
            String lookupName = "look up from [" + lookup_table + "]";
            DatabaseLookupMeta dbl = new DatabaseLookupMeta();
            dbl.setDatabaseMeta(transMeta.findDatabase("db"));
            dbl.setTablename(lookup_table);
            dbl.setCached(false);
            dbl.setEatingRowOnLookupFailure(false);
            dbl.setFailingOnMultipleResults(false);
            dbl.setOrderByClause("");
            
            dbl.setTableKeyField(new String[] {"ID"});
            dbl.setKeyCondition(new String[] {"="});
            dbl.setStreamKeyField1(new String[] {"int_field"});
            dbl.setStreamKeyField2(new String[] {""});
            
            dbl.setReturnValueField(new String[] {"CODE", "STRING"});
            dbl.setReturnValueDefaultType( new int[] {ValueMeta.TYPE_INTEGER, ValueMeta.TYPE_STRING});      
            dbl.setReturnValueDefault(new String[] {"-1", "UNDEF"});
            dbl.setReturnValueNewName(new String[] {"RET_CODE", "RET_STRING"});
            
            String lookupId = registry.getPluginId(StepPluginType.getInstance(), dbl);
            StepMeta lookupStep = new StepMeta(lookupId, lookupName, (StepMetaInterface) dbl);
            lookupStep.setDescription("Reads information from table [" + lookup_table + "] on database [" + dbInfo + "]");
            transMeta.addStep(lookupStep);
            
            TransHopMeta hi = new TransHopMeta(injectorStep, lookupStep);
            transMeta.addTransHop(hi);

            // Now execute the transformation...
            Trans trans = new Trans(transMeta);

            trans.prepareExecution(null);
                    
            StepInterface si = trans.getStepInterface(lookupName, 0);
            RowStepCollector rc = new RowStepCollector();
            si.addRowListener(rc);
            
            RowProducer rp = trans.addRowProducer(injectorStepname, 0);
            trans.startThreads();
            
            // add rows
            List<RowMetaAndData> inputList = createDataRows();
            for (RowMetaAndData rm : inputList )
            {
            	rp.putRow(rm.getRowMeta(), rm.getData());
            }   
            rp.finished();

            trans.waitUntilFinished();   

            List<RowMetaAndData> resultRows = rc.getRowsWritten();
            List<RowMetaAndData> goldRows = createResultDataRows();
            checkRows(goldRows, resultRows);
        }    	
        finally {}    
    }
    
	/**
	 * Test "Load All Rows" version of BasicDatabaseLookup test.
	 */
    @Test
    public void CacheAndLoadAllRowsDatabaseLookup() throws Exception
    {
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
            TransMeta transMeta = new TransMeta();
            transMeta.setName("transname");

            // Add the database connections
            for (int i=0;i<databasesXML.length;i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
                transMeta.addDatabase(databaseMeta);
            }

            DatabaseMeta dbInfo = transMeta.findDatabase("db");

            PluginRegistry registry = PluginRegistry.getInstance();            

            // 
            // create an injector step...
            //
            String injectorStepname = "injector step";
            InjectorMeta im = new InjectorMeta();
            
            // Set the information of the injector.
                    
            String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
            StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
            transMeta.addStep(injectorStep);            
            
            // 
            // create the lookup step...
            //
            String lookupName = "look up from [" + lookup_table + "]";
            DatabaseLookupMeta dbl = new DatabaseLookupMeta();
            dbl.setDatabaseMeta(transMeta.findDatabase("db"));
            dbl.setTablename(lookup_table);
            dbl.setCached(true);
            dbl.setLoadingAllDataInCache(true);
            dbl.setEatingRowOnLookupFailure(false);
            dbl.setFailingOnMultipleResults(false);
            dbl.setOrderByClause("");
            
            dbl.setTableKeyField(new String[] {"ID"});
            dbl.setKeyCondition(new String[] {"="});
            dbl.setStreamKeyField1(new String[] {"int_field"});
            dbl.setStreamKeyField2(new String[] {""});
            
            dbl.setReturnValueField(new String[] {"CODE", "STRING"});
            dbl.setReturnValueDefaultType( new int[] {ValueMeta.TYPE_INTEGER, ValueMeta.TYPE_STRING});      
            dbl.setReturnValueDefault(new String[] {"-1", "UNDEF"});
            dbl.setReturnValueNewName(new String[] {"RET_CODE", "RET_STRING"});
            
            String lookupId = registry.getPluginId(StepPluginType.getInstance(), dbl);
            StepMeta lookupStep = new StepMeta(lookupId, lookupName, (StepMetaInterface) dbl);
            lookupStep.setDescription("Reads information from table [" + lookup_table + "] on database [" + dbInfo + "]");
            transMeta.addStep(lookupStep);
            
            TransHopMeta hi = new TransHopMeta(injectorStep, lookupStep);
            transMeta.addTransHop(hi);

            // Now execute the transformation...
            Trans trans = new Trans(transMeta);

            trans.prepareExecution(null);
                    
            StepInterface si = trans.getStepInterface(lookupName, 0);
            RowStepCollector rc = new RowStepCollector();
            si.addRowListener(rc);
            
            RowProducer rp = trans.addRowProducer(injectorStepname, 0);
            trans.startThreads();
            
            // add rows
            List<RowMetaAndData> inputList = createDataRows();
            for (RowMetaAndData rm : inputList )
            {
            	rp.putRow(rm.getRowMeta(), rm.getData());
            }   
            rp.finished();

            trans.waitUntilFinished();   

            List<RowMetaAndData> resultRows = rc.getRowsWritten();
            List<RowMetaAndData> goldRows = createResultDataRows();
            checkRows(goldRows, resultRows);
        }    	
        finally {}    
    }    

	/**
	 * Test with cache turned off but "Load All Rows" enabled (Load all rows should have no effect)
	 * See JIRA PDI-1910
	 */
    @Test
    public void NOTCachedAndLoadAllRowsDatabaseLookup() throws Exception
    {
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
            TransMeta transMeta = new TransMeta();
            transMeta.setName("transname");

            // Add the database connections
            for (int i=0;i<databasesXML.length;i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
                transMeta.addDatabase(databaseMeta);
            }

            DatabaseMeta dbInfo = transMeta.findDatabase("db");

            PluginRegistry registry = PluginRegistry.getInstance();            

            // 
            // create an injector step...
            //
            String injectorStepname = "injector step";
            InjectorMeta im = new InjectorMeta();
            
            // Set the information of the injector.
                    
            String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
            StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
            transMeta.addStep(injectorStep);            
            
            // 
            // create the lookup step...
            //
            String lookupName = "look up from [" + lookup_table + "]";
            DatabaseLookupMeta dbl = new DatabaseLookupMeta();
            dbl.setDatabaseMeta(transMeta.findDatabase("db"));
            dbl.setTablename(lookup_table);
            dbl.setCached(false);
            dbl.setLoadingAllDataInCache(true);
            dbl.setEatingRowOnLookupFailure(false);
            dbl.setFailingOnMultipleResults(false);
            dbl.setOrderByClause("");
            
            dbl.setTableKeyField(new String[] {"ID"});
            dbl.setKeyCondition(new String[] {"="});
            dbl.setStreamKeyField1(new String[] {"int_field"});
            dbl.setStreamKeyField2(new String[] {""});
            
            dbl.setReturnValueField(new String[] {"CODE", "STRING"});
            dbl.setReturnValueDefaultType( new int[] {ValueMeta.TYPE_INTEGER, ValueMeta.TYPE_STRING});      
            dbl.setReturnValueDefault(new String[] {"-1", "UNDEF"});
            dbl.setReturnValueNewName(new String[] {"RET_CODE", "RET_STRING"});
            
            String lookupId = registry.getPluginId(StepPluginType.getInstance(), dbl);
            StepMeta lookupStep = new StepMeta(lookupId, lookupName, (StepMetaInterface) dbl);
            lookupStep.setDescription("Reads information from table [" + lookup_table + "] on database [" + dbInfo + "]");
            transMeta.addStep(lookupStep);
            
            TransHopMeta hi = new TransHopMeta(injectorStep, lookupStep);
            transMeta.addTransHop(hi);

            // Now execute the transformation...
            Trans trans = new Trans(transMeta);

            trans.prepareExecution(null);
                    
            StepInterface si = trans.getStepInterface(lookupName, 0);
            RowStepCollector rc = new RowStepCollector();
            si.addRowListener(rc);
            
            RowProducer rp = trans.addRowProducer(injectorStepname, 0);
            trans.startThreads();
            
            // add rows
            List<RowMetaAndData> inputList = createDataRows();
            for (RowMetaAndData rm : inputList )
            {
            	rp.putRow(rm.getRowMeta(), rm.getData());
            }   
            rp.finished();

            trans.waitUntilFinished();   

            List<RowMetaAndData> resultRows = rc.getRowsWritten();
            List<RowMetaAndData> goldRows = createResultDataRows();
            checkRows(goldRows, resultRows);
        }    	
        finally {}    
    }    
    
}