package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;

// import plugin.palo.core.PaloHelper;

public class PaloDatabaseFactory implements DatabaseFactoryInterface {

	public String getConnectionTestReport(DatabaseMeta databaseMeta) throws KettleDatabaseException {

		StringBuffer report = new StringBuffer();
		
		PaloHelper helper = new PaloHelper(databaseMeta);
		try {
			helper.connect();
			
			// If the connection was successful
			//
			report.append("Connecting to PALO server [").append(databaseMeta.getName()).append("] went without a problem.").append(Const.CR);
			
		} catch (KettleException e) {
			report.append("Unable to connect to the PALO server: ").append(e.getMessage()).append(Const.CR);
			report.append(Const.getStackTracker(e));
		}
		finally
		{
			helper.disconnect();	
		}
		
		return report.toString();
	}

}
