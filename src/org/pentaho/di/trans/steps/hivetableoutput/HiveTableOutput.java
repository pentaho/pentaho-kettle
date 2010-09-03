package org.pentaho.di.trans.steps.hivetableoutput;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.HiveDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutput;

public class HiveTableOutput extends TextFileOutput {

	private static Class<?> PKG = HiveTableOutput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	/**
	 * 
	 * @param stepMeta
	 * @param stepDataInterface
	 * @param copyNr
	 * @param transMeta
	 * @param trans
	 */
	public HiveTableOutput(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
				
		meta=(HiveTableOutputMeta)smi;
		data=(HiveTableOutputData)sdi;

		return super.init(smi, sdi);
	}
	
	/**
	 * 
	 */
	@Override
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		return super.processRow(smi, sdi);
		
	}
	
	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		super.dispose(smi, sdi);
		
		loadData(smi, sdi);
	}
	
	/**
	 * 
	 */
	private void loadData(StepMetaInterface smi, StepDataInterface sdi) {
				
		String fileLocation = "LOCAL";
		
		// if the database is set in the data object
		  
	   HiveTableOutputMeta meta = (HiveTableOutputMeta)smi;
	   
		//  if the database is not set in data, connect to it
		if (((HiveTableOutputData)data).db == null) {
		   
		    DatabaseMeta databaseMeta = new DatabaseMeta();
		    HiveDatabaseMeta hiveDatabaseMeta = new HiveDatabaseMeta();
		    
		    hiveDatabaseMeta.setName("HIVE");
		    
		    hiveDatabaseMeta.setHostname(meta.getHiveHostName());
		    hiveDatabaseMeta.setDatabasePortNumberString(meta.getHivePort());
		    hiveDatabaseMeta.setServername(meta.getHiveHostName());
		    hiveDatabaseMeta.setDatabaseName(meta.getHiveDatabase());
		    hiveDatabaseMeta.setUsername(meta.getHiveUsername());
		    hiveDatabaseMeta.setPassword(meta.getHivePassword());
		    hiveDatabaseMeta.setPluginName("Hadoop Hive");
		    hiveDatabaseMeta.setPluginId("HIVE");
		    databaseMeta.setDatabaseInterface(hiveDatabaseMeta);
		      
		    ((HiveTableOutputData)data).db = new Database(this, databaseMeta);			   
		}
		
		//  connect 
		
		try {
			((HiveTableOutputData)data).db.connect();
			StringBuilder sqlStringBuilder = new StringBuilder();
			sqlStringBuilder.append("LOAD DATA ");
			sqlStringBuilder.append(fileLocation);
			sqlStringBuilder.append((meta.getTruncateTable()?" OVERWRITE ":""));
			sqlStringBuilder.append(" INPATH '");		
			sqlStringBuilder.append(meta.getFileName());
			sqlStringBuilder.append(".");
			sqlStringBuilder.append(meta.getExtension());
			sqlStringBuilder.append("' INTO TABLE ");
			sqlStringBuilder.append(meta.getTargetTableName());
			
			String sqlString = sqlStringBuilder.toString();
			
			System.out.println(getClass().getName()+".loadData(): sql = "+sqlString);

			//  execute the statement
			Result result = ((HiveTableOutputData)data).db.execStatement(sqlString);
			System.out.println(getClass().getName()+".loadData(): statement executed.  result.getExitStatus =  "+result.getExitStatus());
		}
		catch (Exception e) {
		   e.printStackTrace();
		}
		/*catch (KettleException kettleException) {

			System.out.println(getClass().getName()+".loadData: databaseMeta is null.");
		}*/
	}
}
